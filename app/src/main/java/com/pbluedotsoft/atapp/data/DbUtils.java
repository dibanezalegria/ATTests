package com.pbluedotsoft.atapp.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.pbluedotsoft.atapp.data.DbContract.UserEntry;
import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;

/**
 * Created by daniel on 31/05/17.
 */

public class DbUtils {
    private static final String LOG_TAG = DbUtils.class.getSimpleName();

    public static void logUserDb(Context context) {
        Cursor cursor = context.getContentResolver().query(UserEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            Log.d(LOG_TAG, "- User Table ---------------------------------------------------------");
            Log.d(LOG_TAG, "- id name pass -------------------------------------------------------");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_NAME));
                String pass = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_PASS));
                Log.d(LOG_TAG, id + " " + name + " " + pass);
            }
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            cursor.close();
        }
    }

    public static void logPatientDb(Context context) {
        Cursor cursor = context.getContentResolver().query(PatientEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            Log.d(LOG_TAG, "- Patient Table ------------------------------------------------------");
            Log.d(LOG_TAG, "- id name entry notes active -----------------------------------------");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(PatientEntry.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_NAME));
                String entry = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_ENTRY_NUMBER));
                String notes = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_NOTES));
                int active = cursor.getInt(cursor.getColumnIndex(PatientEntry.COLUMN_ACTIVE));
                Log.d(LOG_TAG, id + " " + name + " " + entry + " " + notes + " " + active);
            }
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            cursor.close();
        }
    }

    public static void logTestDb(Context context) {
        Cursor cursor = context.getContentResolver().query(TestEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            Log.d(LOG_TAG, "- Test Table ---------------------------------------------------------");
            Log.d(LOG_TAG, "- id pat_id code name title con_in con_out res_in res_out not_in" +
                    " not_out dat_in dat_out st_in st_out");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_ID));
                int patId = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_PATIENT_ID_FK));
                String code = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CODE));
                String name = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NAME));
                String title = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_TITLE_NAME));
                String contentIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
                String contentOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
                String resultIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_IN));
                String resultOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_RESULT_OUT));
                String notesIn = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_IN));
                String notesOut = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NOTES_OUT));
                int dateIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_DATE_IN));
                int dateOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_DATE_OUT));
                int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
                int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));
                Log.d(LOG_TAG, id + " " + patId + " " + code + " " + name + " " + title + " " +
                        contentIn + " " + contentOut + " " + resultIn + " " + resultOut + " " +
                        notesIn + " " + notesOut + " " + dateIn + " " + dateOut + " " + statusIn +
                        " " + statusOut + " ");
            }
            Log.d(LOG_TAG, "----------------------------------------------------------------------");
            cursor.close();
        }
    }
}
