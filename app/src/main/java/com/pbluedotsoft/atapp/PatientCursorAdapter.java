package com.pbluedotsoft.atapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;

/**
 * Created by daniel on 5/06/17.
 */

public class PatientCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = PatientCursorAdapter.class.getSimpleName();

    public PatientCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.patient_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate
        TextView tvId  = (TextView) view.findViewById(R.id.tv_patient_id);
        TextView tvName = (TextView)  view.findViewById(R.id.tv_patient_name);
        TextView tvEntrada = (TextView) view.findViewById(R.id.tv_patient_entry);

        // Enable only for debugging
        tvId.setVisibility(TextView.GONE);

        // Extract properties from cursor
        int id = cursor.getInt(cursor.getColumnIndex(PatientEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_NAME));
        int entrada = cursor.getInt(cursor.getColumnIndex(PatientEntry.COLUMN_ENTRY_NUMBER));

        // Populate text views
        tvId.setText(String.valueOf(id));
        tvName.setText(name);
        tvEntrada.setText(String.valueOf(entrada));
    }
}
