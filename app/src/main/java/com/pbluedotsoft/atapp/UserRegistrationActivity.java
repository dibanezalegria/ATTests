package com.pbluedotsoft.atapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.pbluedotsoft.atapp.data.DbContract.UserEntry;
import com.pbluedotsoft.atapp.databinding.ActivityUserRegistrationBinding;

public class UserRegistrationActivity extends AppCompatActivity {

    private static final String LOG_TAG = UserRegistrationActivity.class.getSimpleName();

    private ActivityUserRegistrationBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bind = DataBindingUtil.setContentView(this, R.layout.activity_user_registration);

        // Layout background listener closes soft keyboard, so keyboard does not pop up
        // automatically when launching activity
        bind.userRegistrationLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide soft keyboard
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = bind.etUsername.getText().toString().trim().toLowerCase();
                String pass = bind.etPassword.getText().toString().trim().toLowerCase();
                String passR = bind.etPasswordRepeat.getText().toString().trim().toLowerCase();

                // Name validation
                if (user.isEmpty()) {
                    bind.tvMessageOutput.setText(R.string.enter_username);
                    return;
                }

                // Password validation
                if (pass.isEmpty() || passR.isEmpty() ) {
                    bind.tvMessageOutput.setText(R.string.enter_password);
                    return;
                }

                if (!pass.equals(passR)) {
                    bind.tvMessageOutput.setText(R.string.password_no_match);
                    return;
                }

                // Name availability in db
                String selection = UserEntry.COLUMN_NAME + "=?";
                String[] selectionArgs= {user};
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(UserEntry.CONTENT_URI, null, selection, selectionArgs, null);
                    if (cursor == null || cursor.getCount() > 0) {
                        bind.tvMessageOutput.setText(R.string.user_taken);
                        return;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // Result intent
                Intent intent = new Intent(UserRegistrationActivity.this, LoginActivity.class);
                intent.putExtra("USERNAME", bind.etUsername.getText().toString());

                // Insert user in db
                ContentValues values = new ContentValues();
                values.put(UserEntry.COLUMN_NAME, user);
                values.put(UserEntry.COLUMN_PASS, pass);
                Uri uri = getContentResolver().insert(UserEntry.CONTENT_URI, values);
                if (uri != null)
                    setResult(RESULT_OK, intent);
                else
                    setResult(RESULT_CANCELED, intent);

                // Return to LoginActivity
                finish();
            }
        });

        // Cancel button
        bind.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(UserRegistrationActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        // Overriding method disables back press button
        // I do not want user to leave registration form with a back press (button). The resultCode
        // returned by the back press conflicts with the cancel button.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d(LOG_TAG, "onDestroy()");
    }
}
