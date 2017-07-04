package com.pbluedotsoft.atapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.databinding.ActivityPatientsBinding;

import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;

public class PatientsActivity extends AppCompatActivity implements
        AddPatientDialogFragment.AddPatientDialogListener,
        EditPatientDialogFragment.EditPatientDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = PatientsActivity.class.getSimpleName();
    private static final int PATIENT_LOADER = 0;

    // Context menu constants
    private static final int PATIENT_TESTS = 0;
    private static final int PATIENT_RESULTS = 1;
    private static final int PATIENT_EDIT = 2;
    private static final int PATIENT_DELETE = 3;

    private int mUserID;
    private String mUserName;
    private int mPatientID;
    private String mPatientName, mPatientEntry;

    private ActivityPatientsBinding bind;
    private PatientCursorAdapter mCursorAdapter;
    private AlertDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients);

        // Extract user id from bundle
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRAS.KEY_USER_ID) &&
                intent.hasExtra(EXTRAS.KEY_USER_NAME)) {
            mUserID = intent.getExtras().getInt(EXTRAS.KEY_USER_ID);
            mUserName = intent.getExtras().getString(EXTRAS.KEY_USER_NAME);
        } else {
            mUserID = -1;   // should never happen
            mUserName = "anonymous";
        }

        setTitle("ATTests (" + mUserName + ")");

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_patients);

        // Fab button
        bind.fabAddPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPatientDialogFragment dialogFragment = AddPatientDialogFragment.newInstance();
                dialogFragment.show(getSupportFragmentManager(), "add_patient_dialog");
            }
        });

        // There is no data yet (until the loader finishes) so cursor is null for now.
        mCursorAdapter = new PatientCursorAdapter(this, null);

        // List view
        bind.listviewPatients.setAdapter(mCursorAdapter);
        bind.listviewPatients.setLongClickable(false);
        bind.listviewPatients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(LOG_TAG, "list item clicked");
                openContextMenu(view);
            }
        });

        // Context menu
        registerForContextMenu(bind.listviewPatients);

        // Set empty view on ListView, so it only shows image when list has 0 items
        bind.listviewPatients.setEmptyView(bind.layoutRelativeEmptyList);

        // Kick off loader
        getSupportLoaderManager().initLoader(PATIENT_LOADER, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // closing dialog avoids leakage
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            Log.d(LOG_TAG, "onDestroy: dismissing loading dialog");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // Extract patient's data
        mPatientID = Integer.parseInt(((TextView) info.targetView.findViewById(R.id.tv_patient_id))
                .getText().toString());
        mPatientName = ((TextView) info.targetView.findViewById(R.id.tv_patient_name))
                .getText().toString();
        mPatientEntry = ((TextView) info.targetView.findViewById(R.id.tv_patient_entry))
                .getText().toString();
        menu.setHeaderTitle(mPatientName + " - " + mPatientEntry);
        // Menu options
        menu.add(Menu.NONE, PATIENT_TESTS, 0, "Tests");
        menu.add(Menu.NONE, PATIENT_RESULTS, 1, "Mätresultat");
        menu.add(Menu.NONE, PATIENT_EDIT, 2, "Edit patient details");
        menu.add(Menu.NONE, PATIENT_DELETE, 3, "Delete patient");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Action following menu option chosen
        switch (item.getItemId()) {
            case PATIENT_TESTS: {
                // Open new activity with list of tests for given patient id
                Intent intent = new Intent(PatientsActivity.this, TestListActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(EXTRAS.KEY_USER_ID, mUserID);
                extras.putString(EXTRAS.KEY_USER_NAME, mUserName);
                extras.putInt(EXTRAS.KEY_PATIENT_ID, mPatientID);
                String headerStr = mPatientName + " - " + mPatientEntry;
                extras.putString(EXTRAS.KEY_HEADER, headerStr);
                intent.putExtras(extras);
                startActivity(intent);
                break;
            }
            case PATIENT_RESULTS: {
                // Loading dialog
                mLoadingDialog = new AlertDialog.Builder(this).create();
                mLoadingDialog.setMessage("Loading...");
                mLoadingDialog.show();
                // Get patient's info from view
                String headerStr = mPatientName + " - " + mPatientEntry;
                Bundle extras = new Bundle();
                extras.putInt(EXTRAS.KEY_USER_ID, mUserID);
                extras.putString(EXTRAS.KEY_USER_NAME, mUserName);
                extras.putInt(EXTRAS.KEY_PATIENT_ID, mPatientID);
                extras.putString(EXTRAS.KEY_HEADER, headerStr);
                Intent intent = new Intent(PatientsActivity.this, ResultTableActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
                break;
            }
            case PATIENT_EDIT: {
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRAS.KEY_PATIENT_ID, mPatientID);
                bundle.putString(EXTRAS.KEY_PATIENT_NAME, mPatientName);
                bundle.putString(EXTRAS.KEY_PATIENT_ENTRY, mPatientEntry);
                EditPatientDialogFragment dialogFragment = new EditPatientDialogFragment();
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getSupportFragmentManager(), "edit_patient_dialog");
                break;
            }
            case PATIENT_DELETE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("All tests and results for this patient will be destroyed.")
                        .setTitle(mPatientName + " - " + mPatientEntry)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletePatient(mPatientID);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .create().show();
                break;
            }
        }

        return super.onContextItemSelected(item);
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
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
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

    /**
     * Interface method implementation (AddPatientDialogFragment.AddPatientDialogListener)
     *
     */
    @Override
    public void onCreateAddPatientDialog(String name, int entry) {
        insertPatient(name, entry, null);
    }

    /**
     * Interface method implementation (EditPatientDialogFragment.EditPatientDialogListener)
     *
     */
    @Override
    public void onUpdateEditPatientDialog(int id, String name, int entry) {
        updatePatient(id, name, entry);
    }

    /**
     * Insert new patient in 'patient' table
     */
    private Uri insertPatient(String name, int entry, String notes) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_USER_ID_FK, mUserID);
        values.put(PatientEntry.COLUMN_NAME, name);
        values.put(PatientEntry.COLUMN_ENTRY_NUMBER, entry);
        values.put(PatientEntry.COLUMN_NOTES, notes);

        Uri uri = null;
        try {
            uri = getContentResolver().insert(PatientEntry.CONTENT_URI, values);
            Log.d(LOG_TAG, "Inserted patient returned uri: " + uri.toString());
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        // Create test placeholders for patient in 'test' database
        if (uri != null) {
            int newPatientID = (int) ContentUris.parseId(uri);
            addTestForPatient(newPatientID, "JAMAR", "JAMAR", "- Standard Grip Strength Evaluation");
            addTestForPatient(newPatientID, "VIGO", "VIGORIMETER", "");
            addTestForPatient(newPatientID, "VAS", "VAS", "- Visuell Analog Skala");
            addTestForPatient(newPatientID, "NINE", "9-Hole Peg Test", "");
            addTestForPatient(newPatientID, "BOX", "Box and Block", "");
            addTestForPatient(newPatientID, "SOFI", "SOFI funktionstest", "");
            addTestForPatient(newPatientID, "GAT", "GAT", "- Grip Ability Test");
            addTestForPatient(newPatientID, "HAQ", "HAQ ADL-index", "");
            addTestForPatient(newPatientID, "STATUS", "Handstatus", "");
        }

        return uri;
    }

    /**
     * Change status of patient to inactive. Only admin can see inactive patients
     */
    private int deletePatient(int id) {
        // Form uri
        Uri uri = ContentUris.withAppendedId(PatientEntry.CONTENT_URI, id);

        // Delete does not remove the item from table, only change status to inactive
        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_ACTIVE, 0);
        int rowsUpdated = getContentResolver().update(uri, values, null, null);
        Log.d(LOG_TAG, "Rows 'deleted': " + rowsUpdated);
        return rowsUpdated;
    }

    /**
     * Update patient entry on database
     */
    private int updatePatient(int id, String name, int entry) {
        Uri uri = ContentUris.withAppendedId(PatientEntry.CONTENT_URI, id);

        // Values to update
        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_NAME, name);
        values.put(PatientEntry.COLUMN_ENTRY_NUMBER, entry);

        int rowsUpdated = getContentResolver().update(uri, values, null, null);
        Log.d(LOG_TAG, "Rows updated: " + rowsUpdated);
        return rowsUpdated;
    }

    /**
     * Add test to database for given patient id
     */
    private Uri addTestForPatient(int patientId, String code, String name, String title) {
        // Insert a new test for given patient id
        ContentValues values = new ContentValues();
        values.put(TestEntry.COLUMN_PATIENT_ID_FK, patientId);
        values.put(TestEntry.COLUMN_CODE, code);
        values.put(TestEntry.COLUMN_NAME, name);
        values.put(TestEntry.COLUMN_TITLE_NAME, title);

        Uri uri = getContentResolver().insert(TestEntry.CONTENT_URI, values);
        Log.d(LOG_TAG, "Insert patient returned uri: " + uri);

        return uri;
    }

    /**
     * Cursor Loader method implementations
     *
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This loader returns only active patients
        if (id == PATIENT_LOADER) {
            String selection = PatientEntry.COLUMN_ACTIVE + "=? AND " +
                    PatientEntry.COLUMN_USER_ID_FK + "=?";
            String[] selectionArgs = {String.valueOf(1), String.valueOf(mUserID)};
            return new CursorLoader(this,
                    PatientEntry.CONTENT_URI, null, selection, selectionArgs, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PATIENT_LOADER) {
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PATIENT_LOADER) {
            mCursorAdapter.swapCursor(null);
        }
    }


}
