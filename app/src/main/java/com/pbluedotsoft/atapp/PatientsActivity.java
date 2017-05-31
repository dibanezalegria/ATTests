package com.pbluedotsoft.atapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class PatientsActivity extends AppCompatActivity {

    private static final String LOG_TAG = PatientsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's 'back to parent' arrow
            case android.R.id.home:
                Log.d(LOG_TAG, "onOptionsItemSelected()");
                logoutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        logoutDialog();
    }

    /**
     * Warn user about to logout
     */
    private void logoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage("Are you sure to want to logout?");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Log Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backToLoginActivity();
            }
        });
        dialog.show();
    }

    /**
     * Navigate up
     */
    private void backToLoginActivity() {
        Intent upIntent = NavUtils.getParentActivityIntent(PatientsActivity.this);
        NavUtils.navigateUpTo(PatientsActivity.this, upIntent);
    }
}
