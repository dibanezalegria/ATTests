package com.pbluedotsoft.atapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.databinding.ActivityTestBinding;

import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity implements NotesDialogFragment.NotesDialogListener {

    private static final String LOG_TAG = TestActivity.class.getSimpleName();

    // Save state constant
    private static final String STATE_USER_SAVED = "state_user_saved";

    // From bundle
    private String mHeaderString;
    private int mUserID;
    private String mUserName;
    private Uri mTestURI;
    private int mPatientID, mInOut;

    private ActivityTestBinding bind;

    private boolean mUserHasSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        // Long task ________________________________________________________________________________________ DEBUG
//        LoadingActivity load = new LoadingActivity();
//        load.execute();

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Bundle test URI
        mTestURI = getIntent().getData();
        Log.d(LOG_TAG, "testURI: " + mTestURI.toString());
        Cursor cursor = getContentResolver().query(mTestURI, null, null, null, null);
        // Early exit should never happen
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();
        final String testCode = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CODE));
        String testName = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NAME));
        String testTitle = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_TITLE_NAME));
        cursor.close();

        // This bundle will be returned when navigating up to parent activity (TestListActivity)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserID = extras.getInt(EXTRAS.KEY_USER_ID);
            mUserName = extras.getString(EXTRAS.KEY_USER_NAME);
            mPatientID = extras.getInt(EXTRAS.KEY_PATIENT_ID);
            mHeaderString = extras.getString(EXTRAS.KEY_HEADER);
            mInOut = extras.getInt(EXTRAS.KEY_INOUT);
            // Title for activity -> patient info
            setTitle(mHeaderString);
        }

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_test);

//        bind.progressLoader.setVisibility(View.VISIBLE);
//
//        try {
//            for (int i = 0; i < 5; i++) {
//                Log.d(LOG_TAG, "Emulating some task.. Step " + i);
//                TimeUnit.SECONDS.sleep(1);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // Test title
        bind.tvTestTitle.setText(testName + " " + testTitle);

        // Help dialog
        bind.btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog(testCode);
            }
        });

        // Notes dialog
        bind.btnNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesDialog();
            }
        });

        // Bundle
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS.KEY_URI, mTestURI.toString());
        bundle.putString(EXTRAS.KEY_TEST_CODE, testCode);
        bundle.putInt(EXTRAS.KEY_INOUT, mInOut);

        // Create a page adapter
        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(this, getSupportFragmentManager(),
                bundle);

        // Setup view pager
        bind.viewpager.setAdapter(pagerAdapter);
        bind.viewpager.setOffscreenPageLimit(1);    // one page off screen is retained (default)

        // Connect tabs to view pager
        bind.tabs.setupWithViewPager(bind.viewpager);

        TabLayout.Tab tabIn = bind.tabs.getTabAt(0);
        if (tabIn != null) {
            tabIn.setCustomView(R.layout.tab_in);
            if (mInOut == 0)
                tabIn.getCustomView().findViewById(R.id.tv_read_only).setVisibility(View.GONE);
        }

        TabLayout.Tab tabOut = bind.tabs.getTabAt(1);
        if (tabOut != null) {
            tabOut.setCustomView(R.layout.tab_out);
            if (mInOut == 1)
                tabOut.getCustomView().findViewById(R.id.tv_read_only).setVisibility(View.GONE);
        }

        // Select IN or OUT page following user's selection
        if (mInOut == 0) {
            bind.viewpager.setCurrentItem(0);
        } else
            bind.viewpager.setCurrentItem(1);

//        bind.progressLoader.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_USER_SAVED, mUserHasSaved);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mUserHasSaved = savedInstanceState.getBoolean(STATE_USER_SAVED);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                warnUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed()");
        warnUser();
    }

    /**
     * Navigate up
     */
    private void goBackToTestListActivity() {
        Intent upIntent = NavUtils.getParentActivityIntent(TestActivity.this);
        upIntent.putExtra(EXTRAS.KEY_USER_ID, mUserID);
        upIntent.putExtra(EXTRAS.KEY_USER_NAME, mUserName);
        upIntent.putExtra(EXTRAS.KEY_HEADER, mHeaderString);
        upIntent.putExtra(EXTRAS.KEY_PATIENT_ID, mPatientID);
        NavUtils.navigateUpTo(TestActivity.this, upIntent);
    }

    public void helpDialog(String testCode) {
        // Find manual for given test
        int ref = R.string.empty_string;
        switch (testCode) {
            case "VAS":
                ref = R.string.vas_manual;
                break;
            case "SOFI":
                ref = R.string.sofi_manual;
                break;
        }
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        // fromHtml deprecated for Android N and higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            dialog.setMessage(Html.fromHtml(getString(ref),
                    Html.FROM_HTML_MODE_LEGACY));
        } else {
            dialog.setMessage(Html.fromHtml(getString(ref)));
        }

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

        // Change text size
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        if (msg != null)
            msg.setTextSize(18);
    }

    public void notesDialog() {
        // Recover notes from database
        Cursor cursor = getContentResolver().query(mTestURI, null, null, null, null, null);
        String oldNotesIn = null;
        String oldNotesOut = null;
        if (cursor != null) {
            cursor.moveToFirst();
            oldNotesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
            oldNotesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));
            cursor.close();
        }

        // Call dialog
        FragmentManager fm = getSupportFragmentManager();
        NotesDialogFragment dialogFragment = NotesDialogFragment.newInstance(oldNotesIn, oldNotesOut);
        dialogFragment.show(fm, "notes_fragment_dialog");
    }

    /**
     * Interface NotesDialogFragment.NotesDialogListener method implementation
     */
    @Override
    public void onSaveNotesDialogFragment(String notesIn, String notesOut) {
        // Save to database
        ContentValues values = new ContentValues();
        values.put(TestEntry.COLUMN_NOTES_IN, notesIn);
        values.put(TestEntry.COLUMN_NOTES_OUT, notesOut);

        int rows = getContentResolver().update(mTestURI, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

//        // Inform TestActivity that notes have been updated
//        ((TestActivity) getActivity()).notesHaveBeenUpdated(notesIn, notesOut);
    }

    private void warnUser() {
        // Alert dialog if user has not saved
        if (!mUserHasSaved) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setMessage("Changes are NOT saved. Do you really want to exit?");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBackToTestListActivity();
                        }
                    });
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            dialog.show();
        } else {
            goBackToTestListActivity();
        }
    }

    /**
     * Test fragments update this value so we can warn user before exiting
     *
     * @param value boolean to set
     */
    public void setUserHasSaved(boolean value) {
        mUserHasSaved = value;
    }

}
