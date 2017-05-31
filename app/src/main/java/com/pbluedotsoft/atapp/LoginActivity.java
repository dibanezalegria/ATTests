package com.pbluedotsoft.atapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.pbluedotsoft.atapp.data.DbUtils;
import com.pbluedotsoft.atapp.databinding.ActivityLoginBinding;
import com.pbluedotsoft.atapp.data.DbContract.UserEntry;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    public static final int REQUEST_CODE = 1;

    private ActivityLoginBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Layout background listener closes soft keyboard, so keyboard does not pop up
        // automatically when launching activity
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_login);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide soft keyboard
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Login button
        bind.btnLoginOrCreate.setTransformationMethod(null);    // button text non capitalized
        bind.btnLoginOrCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = bind.etUsername.getText().toString().toLowerCase();
                String pass = bind.etPassword.getText().toString().toLowerCase();
                if (user.equals("admin") && pass.equals("admin")) {
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                } else if(loginAuthentication(user, pass)) {
                    Intent intent = new Intent(LoginActivity.this, PatientsActivity.class);
                    startActivity(intent);
                } else {
                    // Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    // Login failed
                    bind.tvMessageOutput.setTextColor(ContextCompat.getColor(LoginActivity.this,
                            R.color.red_400));
                    bind.tvMessageOutput.setText(R.string.failed_login);
                }

            }
        });

        // Registration button
        bind.tvRegisterClickableText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
        // Check which request it is that we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("USERNAME");
                bind.tvMessageOutput.setTextColor(ContextCompat.getColor(this, R.color.green_500));
                bind.tvMessageOutput.setText(String.format(Locale.ENGLISH,
                        "Account successfully created: %s", username));
            } else if (resultCode == RESULT_CANCELED) {
                bind.tvMessageOutput.setTextColor(ContextCompat.getColor(this, R.color.red_400));
                bind.tvMessageOutput.setText(R.string.error_user_registration);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.users_log:
                DbUtils.logUserDb(this);
                return true;
            case R.id.patients_log:
                DbUtils.logPatientDb(this);
                return true;
            case R.id.tests_log:
                DbUtils.logTestDb(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Login authentication
     *
     * @param user  username
     * @param pass  password
     * @return true if user and pass match entry in 'user' database
     */
    private boolean loginAuthentication(String user, String pass) {
        String selection = UserEntry.COLUMN_NAME + "=?" + " AND " + UserEntry.COLUMN_PASS + "=?";
        String[] selectionArgs = {user, pass};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(UserEntry.CONTENT_URI, null,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() == 1) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }
}
