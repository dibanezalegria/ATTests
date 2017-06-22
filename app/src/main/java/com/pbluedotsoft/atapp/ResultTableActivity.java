package com.pbluedotsoft.atapp;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.ActivityResultTableBinding;

public class ResultTableActivity extends AppCompatActivity {

    private static final String LOG_TAG = ResultTableActivity.class.getSimpleName();

    private int mUserID, mPatientID;
    private String mUserName;
    private String mHeaderString;

    private ActivityResultTableBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_table);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_result_table);

        // Extract info from Bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserID = extras.getInt(EXTRAS.KEY_USER_ID, mUserID);
            mUserName = extras.getString(EXTRAS.KEY_USER_NAME);
            mPatientID = extras.getInt(EXTRAS.KEY_PATIENT_ID);
            mHeaderString = extras.getString(EXTRAS.KEY_HEADER);
            Log.d(LOG_TAG, "Getting extras from Bundle -> mPatientID: " + mPatientID +
                    " mHeader: " + mHeaderString);
        }

        // Activity's title
        setTitle(mHeaderString);

        // Get all tests for patient
        String selection = TestEntry.COLUMN_PATIENT_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(mPatientID)};
        Cursor cursor = getContentResolver().query(TestEntry.CONTENT_URI, null, selection,
                selectionArgs, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            Log.d(LOG_TAG, "test count: " + cursor.getCount());
            // Process each test, extract result and populate table
            while (cursor.moveToNext()) {
                String testCode = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CODE));
                switch (testCode) {
                    case "VAS":
                        handleVAS(cursor);
                        break;
                    case "SOFI":
                        handleSOFI(cursor);
                        break;
                    case "HAQ":
                        handleHAQ(cursor);
                        break;
                    case "JAMAR":
                        handleJAMAR(cursor);
                        break;
                    case "VIGO":
                        handleVIGO(cursor);
                        break;
                    case "NINE":
                        handleNINE(cursor);
                        break;
                    case "BOX":
                        handleBOX(cursor);
                        break;
                    case "GAT":
                        handleGAT(cursor);
                        break;
                }
            }
        } else {
            Log.d(LOG_TAG, "cursor error for patient id: " + mPatientID);
        }

        if (cursor != null)
            cursor.close();
    }

    private void handleVAS(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.vasHandInH.setText(contentArray[0]);
            bind.vasHandInV.setText(contentArray[1]);
            bind.vasSmartaInH.setText(contentArray[2]);
            bind.vasSmartaInV.setText(contentArray[3]);
            bind.vasStelhetInH.setText(contentArray[4]);
            bind.vasStelhetInV.setText(contentArray[5]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.vasHandUtH.setText(contentArray[0]);
            bind.vasHandUtV.setText(contentArray[1]);
            bind.vasSmartaUtH.setText(contentArray[2]);
            bind.vasSmartaUtV.setText(contentArray[3]);
            bind.vasStelhetUtH.setText(contentArray[4]);
            bind.vasStelhetUtV.setText(contentArray[5]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvVasNotes.setText("*");
        }
    }

    private void handleSOFI(Cursor cursor) {
        // In
        String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
        String[] contentArray = contentIn.split("\\|");
        // Format: hand|arm|
        // But only after user has saved once, before -1 as default
        if (contentArray.length == 2) {
            if (!contentArray[0].equals("-1")) {
                bind.sofiHandIn.setText(contentArray[0]);
            }
            if (!contentArray[1].equals("-1")) {
                bind.sofiArmIn.setText(contentArray[1]);
            }
        }

        // Out
        String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
        contentArray = contentOut.split("\\|");
        // Format: hand|arm|
        // But only after user has saved once, before -1 as default
        if (contentArray.length == 2) {
            if (!contentArray[0].equals("-1")) {
                bind.sofiHandUt.setText(contentArray[0]);
            }
            if (!contentArray[1].equals("-1")) {
                bind.sofiArmUt.setText(contentArray[1]);
            }
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvSofiNotes.setText("*");
        }
    }

    private void handleHAQ(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.haqIn.setText(contentArray[0]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.haqUt.setText(contentArray[0]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvHaqNotes.setText("*");
        }
    }

    private void handleJAMAR(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.jamarHandInH.setText(contentArray[0]);
            bind.jamarHandInV.setText(contentArray[1]);
            bind.jamarNormalInH.setText(contentArray[2]);
            bind.jamarNormalInV.setText(contentArray[3]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.jamarHandUtH.setText(contentArray[0]);
            bind.jamarHandUtV.setText(contentArray[1]);
            bind.jamarNormalUtH.setText(contentArray[2]);
            bind.jamarNormalUtV.setText(contentArray[3]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvJamarNotes.setText("*");
        }
    }

    private void handleVIGO(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED || statusIn == Test.INCOMPLETED) {
            String contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
            String[] content = contentStr.split("\\|");
            // Values
            if (!content[0].equals("-1")) {
                bind.vigoHandInH.setText(content[0]);
            }
            if (!content[1].equals("-1")) {
                bind.vigoHandInV.setText(content[1]);
            }
            if (!content[2].equals("-1")) {
                bind.vigoFingerDig2InH.setText(content[2]);
            }
            if (!content[3].equals("-1")) {
                bind.vigoFingerDig2InV.setText(content[3]);
            }
            if (!content[4].equals("-1")) {
                bind.vigoFingerDig3InH.setText(content[4]);
            }
            if (!content[5].equals("-1")) {
                bind.vigoFingerDig3InV.setText(content[5]);
            }
            if (!content[6].equals("-1")) {
                bind.vigoFingerDig4InH.setText(content[6]);
            }
            if (!content[7].equals("-1")) {
                bind.vigoFingerDig4InV.setText(content[7]);
            }
            if (!content[8].equals("-1")) {
                bind.vigoFingerDig5InH.setText(content[8]);
            }
            if (!content[9].equals("-1")) {
                bind.vigoFingerDig5InV.setText(content[9]);
            }
            if (!content[10].equals("-1")) {
                bind.vigoThumbInH.setText(content[10]);
            }
            if (!content[11].equals("-1")) {
                bind.vigoThumbInV.setText(content[11]);
            }
            // Normal values
            bind.vigoNormalHandInH.setText(content[12]);
            bind.vigoNormalHandInV.setText(content[12]);
            bind.vigoNormalFingerInH.setText(content[13]);
            bind.vigoNormalFingerInV.setText(content[13]);
            bind.vigoNormalThumbInH.setText(content[14]);
            bind.vigoNormalThumbInV.setText(content[14]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED || statusOut == Test.INCOMPLETED) {
            String contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
            String[] content = contentStr.split("\\|");
            // Values
            if (!content[0].equals("-1")) {
                bind.vigoHandUtH.setText(content[0]);
            }
            if (!content[1].equals("-1")) {
                bind.vigoHandUtV.setText(content[1]);
            }
            if (!content[2].equals("-1")) {
                bind.vigoFingerDig2UtH.setText(content[2]);
            }
            if (!content[3].equals("-1")) {
                bind.vigoFingerDig2UtV.setText(content[3]);
            }
            if (!content[4].equals("-1")) {
                bind.vigoFingerDig3UtH.setText(content[4]);
            }
            if (!content[5].equals("-1")) {
                bind.vigoFingerDig3UtV.setText(content[5]);
            }
            if (!content[6].equals("-1")) {
                bind.vigoFingerDig4UtH.setText(content[6]);
            }
            if (!content[7].equals("-1")) {
                bind.vigoFingerDig4UtV.setText(content[7]);
            }
            if (!content[8].equals("-1")) {
                bind.vigoFingerDig5UtH.setText(content[8]);
            }
            if (!content[9].equals("-1")) {
                bind.vigoFingerDig5UtV.setText(content[9]);
            }
            if (!content[10].equals("-1")) {
                bind.vigoThumbUtH.setText(content[10]);
            }
            if (!content[11].equals("-1")) {
                bind.vigoThumbUtV.setText(content[11]);
            }
            // Normal values
            bind.vigoNormalHandUtH.setText(content[12]);
            bind.vigoNormalHandUtV.setText(content[12]);
            bind.vigoNormalFingerUtH.setText(content[13]);
            bind.vigoNormalFingerUtV.setText(content[13]);
            bind.vigoNormalThumbUtH.setText(content[14]);
            bind.vigoNormalThumbUtV.setText(content[14]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvVigoNotes.setText("*");
        }
    }

    private void handleNINE(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.nineInH.setText(contentArray[0]);
            bind.nineInV.setText(contentArray[1]);
            bind.nineNormalInH.setText(contentArray[2]);
            bind.nineNormalInV.setText(contentArray[3]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.nineUtH.setText(contentArray[0]);
            bind.nineUtV.setText(contentArray[1]);
            bind.nineNormalUtH.setText(contentArray[2]);
            bind.nineNormalUtV.setText(contentArray[3]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvNineNotes.setText("*");
        }
    }

    private void handleBOX(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.boxInH.setText(contentArray[0]);
            bind.boxInV.setText(contentArray[1]);
            bind.boxNormalInH.setText(contentArray[2]);
            bind.boxNormalInV.setText(contentArray[3]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.boxUtH.setText(contentArray[0]);
            bind.boxUtV.setText(contentArray[1]);
            bind.boxNormalUtH.setText(contentArray[2]);
            bind.boxNormalUtV.setText(contentArray[3]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvBoxNotes.setText("*");
        }
    }

    private void handleGAT(Cursor cursor) {
        // In
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        if (statusIn == Test.COMPLETED) {
            String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
            String[] contentArray = contentIn.split("\\|");
            bind.gatIn.setText(contentArray[0]);
        }

        // Out
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
        if (statusOut == Test.COMPLETED) {
            String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
            String[] contentArray = contentOut.split("\\|");
            bind.gatUt.setText(contentArray[0]);
        }

        // Notes
        String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
        String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));

        if ((notesIn != null && !notesIn.isEmpty()) || (notesOut != null && !notesOut.isEmpty())) {
            bind.tvGatNotes.setText("*");
        }
    }


    /**
     * Navigate up
     */
    private void goBackToPatientListActivity() {
        Intent upIntent = NavUtils.getParentActivityIntent(ResultTableActivity.this);
        upIntent.putExtra(EXTRAS.KEY_USER_ID, mUserID);
        upIntent.putExtra(EXTRAS.KEY_USER_NAME, mUserName);
        NavUtils.navigateUpTo(ResultTableActivity.this, upIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(LOG_TAG, "Back from ResultTableActivity: arrow");
                goBackToPatientListActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "Back from ResultTableActivity: back pressed");
        goBackToPatientListActivity();
    }
}
