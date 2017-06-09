package com.pbluedotsoft.atapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.UserEntry;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;
import com.pbluedotsoft.atapp.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AdminActivity.class.getSimpleName();

    private static final int PATIENT_LOADER = 0;
    private static final int USER_LOADER = 1;

    private TextView mTvNumberPatients;
    private PatientRecoveryCursorAdapter mPatientCursorAdapter;
    private UserCursorAdapter mUserCursorAdapter;

    private ActivityAdminBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Title
        setTitle("ATTests - Administrator");

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_admin);

        mTvNumberPatients = bind.tvPatientsCounter;

        bind.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bind.btnResetApp.setTransformationMethod(null);   // button text non capitalize
        bind.btnResetApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setMessage("All patients, tests and results will be deleted.\n\n" +
                        "User accounts will NOT be deleted.")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetApp();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .create().show();
            }
        });

        // There is no data yet (until the loader finishes) so cursor is null for now.
        mUserCursorAdapter = new UserCursorAdapter(this, null);
        mPatientCursorAdapter = new PatientRecoveryCursorAdapter(this, null);

        // List view users
        bind.listviewUsers.setAdapter(mUserCursorAdapter);
        bind.listviewUsers.setEmptyView(bind.listviewUsersEmpty);

        // List view patients
        bind.listviewPatients.setAdapter(mPatientCursorAdapter);

        // Kick off loader
        getSupportLoaderManager().initLoader(PATIENT_LOADER, null, this);
        getSupportLoaderManager().initLoader(USER_LOADER, null, this);
    }

    private void resetApp() {
        // Delete tests first
        int testsDeleted = getContentResolver().delete(TestEntry.CONTENT_URI, null, null);
        Log.d(LOG_TAG, "testsDeleted: " + testsDeleted);
        // Now patients
        int patientsDeleted = getContentResolver().delete(PatientEntry.CONTENT_URI, null, null);
        Log.d(LOG_TAG, "patientsDeleted: " + patientsDeleted);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This loader returns only inactive patients
        if (id == PATIENT_LOADER) {
            String selection = PatientEntry.COLUMN_ACTIVE + "=?";
            String[] selectionArgs = {String.valueOf(0)};
            return new CursorLoader(this,
                    PatientEntry.CONTENT_URI, null, selection, selectionArgs,
                    "_ID DESC");
        }

        if (id == USER_LOADER) {
            return new CursorLoader(this,
                    UserEntry.CONTENT_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PATIENT_LOADER) {
            mPatientCursorAdapter.swapCursor(data);
            mTvNumberPatients.setText(String.valueOf(mPatientCursorAdapter.getCount()));
        }

        if (loader.getId() == USER_LOADER) {
            mUserCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PATIENT_LOADER) {
            mPatientCursorAdapter.swapCursor(null);
        }

        if (loader.getId() == USER_LOADER) {
            mUserCursorAdapter.swapCursor(null);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        Intent upIntent = NavUtils.getParentActivityIntent(AdminActivity.this);
        NavUtils.navigateUpTo(AdminActivity.this, upIntent);
    }
}
