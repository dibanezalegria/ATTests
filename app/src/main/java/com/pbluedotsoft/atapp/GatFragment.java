package com.pbluedotsoft.atapp;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentGatBinding;

import java.util.Arrays;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class GatFragment extends Fragment implements TextWatcher {

    private static final String LOG_TAG = GatFragment.class.getSimpleName();

    // Save state constant
    private static final String STATE_CONTENT = "state_content";

    private Uri mTestUri;
    private int mTab, mInOut;

    private FragmentGatBinding bind;

    public GatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Test URI
        mTestUri = Uri.parse(getArguments().getString(EXTRAS.KEY_URI));

        // Tab (IN or OUT)
        mTab = getArguments().getInt(EXTRAS.KEY_TAB);

        // IN or OUT selected at TestListActivity
        mInOut = getArguments().getInt(EXTRAS.KEY_INOUT);

        // Binding instead of findViewById
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_gat, container, false);

        // Disable touch events in the 'other' tab
        if ((mTab == Test.IN && mInOut == 1) || (mTab == Test.OUT && mInOut == 0)) {
            disableTouchOnLayout(bind.scrollview);
        }

        // IN or OUT background color adjustments
        if (mTab == Test.IN) {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_in));

        } else {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_out));
        }

        // Layout background listener closes soft keyboard
        bind.scrollview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save to database: return false if test incomplete
                if (!saveToDatabase()) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_incomplete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_complete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }

                // Inform parent activity
                ((TestActivity) getActivity()).setUserHasSaved(true);
            }
        });

        bind.etValueQ1.addTextChangedListener(this);
        bind.etValueQ2.addTextChangedListener(this);
        bind.etValueQ3.addTextChangedListener(this);

        // Get content from either saved instance OR database
        String contentStr;
        if (savedInstanceState != null) {
            // onRestoreInstanceState
            contentStr = savedInstanceState.getString(STATE_CONTENT);
            Log.d(LOG_TAG, "Content from savedInstance: " + contentStr);
        } else {
            // Read test content from database
            Cursor cursor = getActivity().getContentResolver().query(mTestUri, null, null, null, null);
            // Early exit: should never happen
            if (cursor == null || cursor.getCount() == 0) {
                return bind.getRoot();
            }
            cursor.moveToFirst();
            if (mTab == Test.IN) {
                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_IN));
            } else {
                contentStr = cursor.getString(cursor.getColumnIndex(TestEntry.COLUMN_CONTENT_OUT));
            }

            cursor.close();
            Log.d(LOG_TAG, "Content from database: " + contentStr);
        }

        // Content can be null. Database 'content_in' and 'content_out' are null when first created
        if (contentStr != null) {
            // Fill edit text fields
            String[] content = contentStr.split("\\|");
            Log.d(LOG_TAG, "content length: " + content.length + " content: " + Arrays.toString(content));

            bind.etValueQ1.setText(content[0]);
            bind.etValueQ2.setText(content[1]);
            bind.etValueQ3.setText(content[2]);
        }

        calculateResults();

        // Inform parent activity that form is up to date
        ((TestActivity) getActivity()).setUserHasSaved(true);

        return bind.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state for radio groups and total sum
        String content = generateContent();
        outState.putString(STATE_CONTENT, content);

        Log.d(LOG_TAG, "onSaveInstanceState: " + content);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    /**
     * Saves content, result and status
     */
    private boolean saveToDatabase() {
        int status = Test.INCOMPLETED;
        if (!calculateResults().equals("-1")) {
            status = Test.COMPLETED;
        }
        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_RESULT_IN, calculateResults());
            values.put(TestEntry.COLUMN_STATUS_IN, status);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_RESULT_OUT, calculateResults());
            values.put(TestEntry.COLUMN_STATUS_OUT, status);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

        if (status == Test.COMPLETED) {
            return true;
        } else
            return false;
    }

    /**
     * Save state for three edit text fields
     *
     * @return String representing state
     */
    private String generateContent() {
        StringBuilder builder = new StringBuilder();
        builder.append(bind.etValueQ1.getText().toString());
        builder.append("|");
        builder.append(bind.etValueQ2.getText().toString());
        builder.append("|");
        builder.append(bind.etValueQ3.getText().toString());
        builder.append("|0|");
        return builder.toString();
    }

    /**
     * Calculate results for three question, total summa and display.
     * Return string representing total summa OR '-1' if one or more questions are missing.
     */
    private String calculateResults() {
        boolean missing = false;
        float total = 0;
        if (!bind.etValueQ1.getText().toString().isEmpty()) {
            try {
                int temp = Integer.parseInt(bind.etValueQ1.getText().toString());
                bind.tvResultQ1.setText(String.format(Locale.ENGLISH, "%.1f", temp * 1.8f));
                total += temp * 1.8f;
            } catch (NumberFormatException ex) {
                Log.d(LOG_TAG, "NumberFormatException Q1");
            }
        } else {
            bind.tvResultQ1.setText("");
            missing = true;
        }

        if (!bind.etValueQ2.getText().toString().isEmpty()) {
            try {
                int temp = Integer.parseInt(bind.etValueQ2.getText().toString());
                bind.tvResultQ2.setText(String.format(Locale.ENGLISH, "%.1f", temp * 1.8f));
                total += temp * 1.8f;
            } catch (NumberFormatException ex) {
                Log.d(LOG_TAG, "NumberFormatException Q2");
            }
        } else {
            bind.tvResultQ2.setText("");
            missing = true;
        }

        if (!bind.etValueQ3.getText().toString().isEmpty()) {
            try {
                int temp = Integer.parseInt(bind.etValueQ3.getText().toString());
                bind.tvResultQ3.setText(String.format(Locale.ENGLISH, "%.1f", temp * 1.8f));
                total += temp * 1.8f;
            } catch (NumberFormatException ex) {
                Log.d(LOG_TAG, "NumberFormatException Q2");
            }
        } else {
            bind.tvResultQ3.setText("");
            missing = true;
        }

        if (!missing) {
            bind.tvTotalSum.setText(String.format(Locale.ENGLISH, "%.1f", total));
            return String.format(Locale.ENGLISH, "%.1f", total);
        } else {
            bind.tvTotalSum.setText("");
            return "-1";
        }
    }

    /**
     * Interface method implementation TextWatcher (EditText)
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (((TestActivity) getActivity()).mUserInteracting) {
            // Inform parent activity
            ((TestActivity) getActivity()).setUserHasSaved(false);
            calculateResults();
        }
    }

    /**
     * Shows AlertDialog with given message
     */
    private void showDialog(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setMessage(msg);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    /**
     * Disable all views in a given layout
     */
    private void disableTouchOnLayout(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setFocusable(false);  // needed for EditText
            child.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            if (child instanceof ViewGroup) {
                disableTouchOnLayout((ViewGroup) child);
            }
        }
    }

}
