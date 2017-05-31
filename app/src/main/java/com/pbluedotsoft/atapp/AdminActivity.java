package com.pbluedotsoft.atapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class AdminActivity extends AppCompatActivity {

    private static final String LOG_TAG = AdminActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        // I disable the back button functionality by overriding this method,
        // because when admin goes back to login using the back button the user and pass are still
        // showing.
    }
}
