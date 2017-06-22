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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentVigoBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class VigoFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener{

    private static final String LOG_TAG = VigoFragment.class.getSimpleName();

    private static final int N_QUESTIONS = 12;  // EditText 6 x 2 hands

    // Position in spinner (0 is not selected '-')
    private static final int MALE = 1;
    private static final int FEMALE = 2;
    private static final int TEEN = 3;
    private static final int KID = 4;
    // Grip
    private static final int HAND = 10;
    private static final int FINGER = 11;
    private static final int THUMB = 12;

    // Save state constant
    private static final String STATE_CONTENT = "state_content";

    private Uri mTestUri;
    private int mTab, mInOut;

    private EditText etValue[];

    private FragmentVigoBinding bind;

    public VigoFragment() {
        etValue = new EditText[N_QUESTIONS];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binding instead of findViewById
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_vigo, container, false);

        // Test URI
        mTestUri = Uri.parse(getArguments().getString(EXTRAS.KEY_URI));

        // Tab (IN or OUT)
        mTab = getArguments().getInt(EXTRAS.KEY_TAB);

        // IN or OUT selected at TestListActivity
        mInOut = getArguments().getInt(EXTRAS.KEY_INOUT);

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

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.vigo_gender_spinner,
                R.layout.spinner_list_item);

        bind.spinPatientType.setAdapter(adapter);
        bind.spinPatientType.setOnItemSelectedListener(this);

        // EditText
        etValue[0] = bind.etValue1h;
        etValue[1] = bind.etValue1v;
        etValue[2] = bind.etValue2h;
        etValue[3] = bind.etValue2v;
        etValue[4] = bind.etValue3h;
        etValue[5] = bind.etValue3v;
        etValue[6] = bind.etValue4h;
        etValue[7] = bind.etValue4v;
        etValue[8] = bind.etValue5h;
        etValue[9] = bind.etValue5v;
        etValue[10] = bind.etValue6h;
        etValue[11] = bind.etValue6v;

        // EditText listeners
        for (EditText et : etValue) {
            et.addTextChangedListener(this);
        }

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bind.spinPatientType.getSelectedItemPosition() == 0) {
                    showDialog("Please select PATIENT");
                    return;
                }

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
            String[] content = contentStr.split("\\|");
            //  Set edit text values using info from content
            for (int i = 0; i < N_QUESTIONS; i++) {
                if (!content[i].equals("-1")) {
                    etValue[i].setText(content[i]);
                }
            }
            // Spinner state (last position in the array)
            try {
                bind.spinPatientType.setSelection(Integer.parseInt(content[content.length - 1]));
                displayNormalValues();
            } catch (NumberFormatException ex) {
                Log.d(LOG_TAG, "NumberFormatException converting content[0]");
            }
        }

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
     * Saves content and status
     */
    private boolean saveToDatabase() {
        // Check whether all spinners have been answered
        boolean missingAnswers = checkMissingAnswers();

        int status;
        if (missingAnswers) {
            status = Test.INCOMPLETED;
        } else {
            status = Test.COMPLETED;
        }

        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_STATUS_IN, status);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_STATUS_OUT, status);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

        return !missingAnswers;
    }

    /**
     * Checks whether all edit texts have been answered
     *
     * @return false - test is complete
     */
    private boolean checkMissingAnswers() {
        // Check all edit text fields
        for (EditText et : etValue) {
            if (et.getText().toString().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Save state for spinner and editable text fields
     *
     * @return String representing state
     */
    private String generateContent() {
        StringBuilder builder = new StringBuilder();
        // EditText fields
        for (EditText et : etValue) {
            if (et.getText().toString().isEmpty()) {
                builder.append("-1");
            } else {
                builder.append(et.getText().toString());
            }
            builder.append("|");
        }

        // Normal values
        builder.append(bind.tvNormal1.getText().toString());
        builder.append("|");
        builder.append(bind.tvNormal2.getText().toString());
        builder.append("|");
        builder.append(bind.tvNormal6.getText().toString());
        builder.append("|");

        // Spinner state
        builder.append(bind.spinPatientType.getSelectedItemPosition());
        builder.append("|");

        return builder.toString();
    }

    /**
     * Updates TextViews for Normal values
     */
    private void displayNormalValues() {
        int patient = bind.spinPatientType.getSelectedItemPosition();
        if (patient != 0) {
            bind.tvNormal1.setText(getNormal(patient, HAND));
            bind.tvNormal2.setText(getNormal(patient, FINGER));
            bind.tvNormal3.setText(getNormal(patient, FINGER));
            bind.tvNormal4.setText(getNormal(patient, FINGER));
            bind.tvNormal5.setText(getNormal(patient, FINGER));
            bind.tvNormal6.setText(getNormal(patient, THUMB));
        } else {
            bind.tvNormal1.setText("-");
            bind.tvNormal2.setText("-");
            bind.tvNormal3.setText("-");
            bind.tvNormal4.setText("-");
            bind.tvNormal5.setText("-");
            bind.tvNormal6.setText("-");
        }
    }

    /**
     * Return mean value from table 'Standard Values for Healthy Patients'
     */
    private String getNormal(int patient, int grip) {
        String value = "-";
        switch (patient) {
            case MALE:
                switch (grip) {
                    case HAND:
                        value = "0.8-1.3";
                        break;
                    case FINGER:
                        value = "0.05-0.4";
                        break;
                    case THUMB:
                        value = "0.1-0.5";
                        break;
                }
                break;
            case FEMALE:
                switch (grip) {
                    case HAND:
                        value = "0.7-1.25";
                        break;
                    case FINGER:
                        value = "0.05-0.3";
                        break;
                    case THUMB:
                        value = "0.1-0.3";
                        break;
                }
                break;
            case TEEN:
                switch (grip) {
                    case HAND:
                        value = "0.8-1.1";
                        break;
                    case FINGER:
                        value = "0.05-0.3";
                        break;
                    case THUMB:
                        value = "0.1-0.2";
                        break;
                }
                break;
            case KID:
                switch (grip) {
                    case HAND:
                        value = "0.1-0.4";
                        break;
                    case FINGER:
                        value = "0.05-0.2";
                        break;
                    case THUMB:
                        value = "0.05-0.15";
                        break;
                }
                break;
        }

        return value;
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

    /**
     * Interface OnItemSelectedListener method implementation (Spinner)
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (((TestActivity) getActivity()).mUserInteracting) {
            // Inform parent activity
            ((TestActivity) getActivity()).setUserHasSaved(false);
            displayNormalValues();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Interface TextWatcher method implementation (EditText)
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
        }
    }
}
