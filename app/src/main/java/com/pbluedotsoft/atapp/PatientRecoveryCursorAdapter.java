package com.pbluedotsoft.atapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;

/**
 * Created by Daniel Ibanez on 2016-10-02.
 */

public class PatientRecoveryCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = PatientRecoveryCursorAdapter.class.getSimpleName();

    private Context mContext;

    public PatientRecoveryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.patient_recovery_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate
        TextView tvId  = (TextView) view.findViewById(R.id.patient_recovery_list_item_id_tv);
        TextView tvName = (TextView)  view.findViewById(R.id.patient_recovery_list_item_name_tv);
        TextView tvEntry = (TextView) view.findViewById(R.id.patient_recovery_list_item_entry_tv);

        // Enable only for debugging
        tvId.setVisibility(TextView.GONE);

        Button restoreBtn = (Button) view.findViewById(R.id.patient_recovery_list_item_restore_btn);
        restoreBtn.setTransformationMethod(null);   // button text non capitalize
        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View itemView = (View) v.getParent();
                TextView tvId = (TextView) itemView.findViewById(R.id.patient_recovery_list_item_id_tv);
                int id = Integer.parseInt(tvId.getText().toString().trim());
                // Form uri
                Uri uri = ContentUris.withAppendedId(PatientEntry.CONTENT_URI, id);
                // Restore only changes value in column active from 0 to 1
                ContentValues values = new ContentValues();
                values.put(PatientEntry.COLUMN_ACTIVE, 1);
                int rowsUpdated = mContext.getContentResolver().update(uri, values, null, null);
                Log.d(LOG_TAG, "Rows restored: " + rowsUpdated);
            }
        });

        // Extract properties from cursor
        int id = cursor.getInt(cursor.getColumnIndex(PatientEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_NAME));
        int entry = cursor.getInt(cursor.getColumnIndex(PatientEntry.COLUMN_ENTRY_NUMBER));

        // Populate text views
        tvId.setText(String.valueOf(id));
        tvName.setText(name);
        tvEntry.setText(String.valueOf(entry));
    }
}
