package com.pbluedotsoft.atapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.Test;

/**
 * Created by Daniel Ibanez on 2016-10-24.
 */

public class TestCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = TestCursorAdapter.class.getSimpleName();

    public TestCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.test_list_item,
                parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Lookup view for data population
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        ImageView imgTickIn = (ImageView) view.findViewById(R.id.tick_in);
        ImageView imgTickOut = (ImageView) view.findViewById(R.id.tick_out);

        // Extract data from cursor
        String name = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_NAME));
        int statusIn = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_IN));
        int statusOut = cursor.getInt(cursor.getColumnIndex(TestEntry.COLUMN_STATUS_OUT));

        // Populate layout components
        tvName.setText(name);

        switch (statusIn) {
            case Test.INCOMPLETED:
                imgTickIn.setVisibility(ImageView.VISIBLE);
                imgTickIn.setColorFilter(ContextCompat.getColor(context, R.color.incomplete));
                break;
            case Test.COMPLETED:
                imgTickIn.setVisibility(ImageView.VISIBLE);
                imgTickIn.setColorFilter(ContextCompat.getColor(context, R.color.complete));
                break;
            case Test.BLANK:
                imgTickIn.setVisibility(ImageView.INVISIBLE);
        }

        switch (statusOut) {
            case Test.INCOMPLETED:
                imgTickOut.setVisibility(ImageView.VISIBLE);
                imgTickOut.setColorFilter(ContextCompat.getColor(context, R.color.incomplete));
                break;
            case Test.COMPLETED:
                imgTickOut.setVisibility(ImageView.VISIBLE);
                imgTickOut.setColorFilter(ContextCompat.getColor(context, R.color.complete));
                break;
            case Test.BLANK:
                imgTickOut.setVisibility(ImageView.INVISIBLE);
        }

    }
}
