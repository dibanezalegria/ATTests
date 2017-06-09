package com.pbluedotsoft.atapp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pbluedotsoft.atapp.data.DbContract.UserEntry;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;
/**
 * Created by Daniel Ibanez on 2016-10-02.
 */

public class UserCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = UserCursorAdapter.class.getSimpleName();

    private Context mContext;

    public UserCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate
        final TextView tvId = (TextView) view.findViewById(R.id.user_list_item_id_tv);
        final TextView tvName = (TextView) view.findViewById(R.id.user_list_item_name_tv);
        final TextView tvPass = (TextView) view.findViewById(R.id.user_list_item_pass_tv);

        final Button deleteBtn = (Button) view.findViewById(R.id.user_list_item_delete_btn);
        deleteBtn.setTransformationMethod(null);    // button text non capitalize
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirmation dialog
                final View clickedView = v;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Warning! All patients and tests for this user will be removed.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUser(clickedView);
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

        final ToggleButton showPassBtn = (ToggleButton) view.findViewById(R.id.user_list_item_show_pass_btn);
        showPassBtn.setTransformationMethod(null);   // button text non capitalize
        showPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPassBtn.isChecked()) {
                    tvPass.setVisibility(TextView.VISIBLE);
                } else
                    tvPass.setVisibility(TextView.INVISIBLE);
            }
        });

        // Extract properties from cursor
        int id = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_NAME));
        String pass = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_PASS));

        // Populate text views
        tvId.setText(String.valueOf(id));
        tvName.setText(name);
        tvPass.setText(pass);
    }

    private void deleteUser(View v) {
        Log.d(LOG_TAG, "deleteUser");
        // Foreign key forces us to follow order of deletion (tests, patients and finally user)
        View itemView = (View) v.getParent();
        TextView tvId = (TextView) itemView.findViewById(R.id.user_list_item_id_tv);
        int userId = -1;
        try {
            userId = Integer.parseInt(tvId.getText().toString());
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Exception: Error deleting user");
            return;
        }

        Cursor cursor = null;
        try {
            // Find patients with user_foreign_key = userId
            String selection = PatientEntry.COLUMN_USER_ID_FK + "=?";
            String[] selectionArgs = {String.valueOf(userId)};
            cursor = mContext.getContentResolver().query(PatientEntry.CONTENT_URI, null,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.getCount() > 0) {
                // Loop patients and delete all tests for each patient
                while (cursor.moveToNext()) {
                    String patientId = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_ID));
                    String sel = TestEntry.COLUMN_PATIENT_ID_FK + "=?";
                    String[] args = {patientId};
                    int nTests = mContext.getContentResolver().delete(TestEntry.CONTENT_URI, sel, args);
                    Log.d(LOG_TAG, "Tests deleted for patient " + patientId + ": " + nTests);
                }

                // Delete patients now
                String sel = PatientEntry.COLUMN_USER_ID_FK + "=?";
                String[] args = {String.valueOf(userId)};
                int nPat = mContext.getContentResolver().delete(PatientEntry.CONTENT_URI, sel, args);
                Log.d(LOG_TAG, "Delete patients: " + nPat);
            }

            // Delete user finally
            String s = PatientEntry.COLUMN_ID + "=?";
            String[] a = {String.valueOf(userId)};
            int nUsers = mContext.getContentResolver().delete(UserEntry.CONTENT_URI, s, a);
            Log.d(LOG_TAG, "Delete user: " + nUsers);

        } catch(Exception ex) {
            Log.d(LOG_TAG, "Exception at delete user");

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
