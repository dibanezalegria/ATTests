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

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentJamarBinding;

import java.util.Arrays;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class JamarFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = JamarFragment.class.getSimpleName();
    // Hand
    private static final int RIGHT = 10;
    private static final int LEFT = 11;
    // Position in spinner (0 is not selected '-')
    private static final int MALE = 1;
    private static final int FEMALE = 2;

    // Save state constant
    private static final String STATE_CONTENT = "state_content";

    private Uri mTestUri;
    private int mTab, mInOut;

    private FragmentJamarBinding bind;

    public JamarFragment() {
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
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_jamar, container, false);

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

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.jamar_gender_spinner,
                R.layout.spinner_list_item);

        bind.spinGender.setAdapter(adapter);
        bind.spinGender.setOnItemSelectedListener(this);

        // Age
        bind.etAge.addTextChangedListener(new TextWatcher() {
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
                    displayNormalValues();
                }
            }
        });

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bind.spinGender.getSelectedItemPosition() == 0) {
                    showDialog("Please enter KÖN");
                    return;
                }

                if (bind.etAge.getText().toString().isEmpty()) {
                    showDialog("Please enter ÅLDER");
                    return;
                }

                if (bind.etValue1h.getText().toString().isEmpty() ||
                        bind.etValue1v.getText().toString().isEmpty()) {
                    showDialog("Please enter FÖRSÖK 1 (höger och vänster)");
                    return;
                }

                saveToDatabase();
                showDialog(getResources().getString(R.string.test_saved_complete));

                // Inform parent activity
                ((TestActivity) getActivity()).setUserHasSaved(true);
            }
        });

        bind.etValue1h.addTextChangedListener(this);
        bind.etValue2h.addTextChangedListener(this);
        bind.etValue3h.addTextChangedListener(this);
        bind.etValue1v.addTextChangedListener(this);
        bind.etValue2v.addTextChangedListener(this);
        bind.etValue3v.addTextChangedListener(this);

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
            // Set radio buttons and total sum using info from content
            String[] content = contentStr.split("\\|");
            Log.d(LOG_TAG, "content length: " + content.length + " content: " + Arrays.toString(content));
            try {
                bind.spinGender.setSelection(Integer.parseInt(content[0]));
            } catch (NumberFormatException ex) {
                Log.d(LOG_TAG, "NumberFormatException content[0] parsing");
            }

            bind.etAge.setText(content[1]);
            bind.etValue1h.setText(content[2]);
            bind.etValue1v.setText(content[3]);
            bind.etValue2h.setText(content[4]);
            bind.etValue2v.setText(content[5]);
            bind.etValue3h.setText(content[6]);
            bind.etValue3v.setText(content[7]);
        }

        calculateResults();
        displayNormalValues();

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
    private void saveToDatabase() {
        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_RESULT_IN, calculateResults());
            values.put(TestEntry.COLUMN_STATUS_IN, Test.COMPLETED);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_RESULT_OUT, calculateResults());
            values.put(TestEntry.COLUMN_STATUS_OUT, Test.COMPLETED);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);
    }

    /**
     * Save state for spinner and editable text fields
     *
     * @return String representing state
     */
    private String generateContent() {
        StringBuilder builder = new StringBuilder();
        builder.append(bind.spinGender.getSelectedItemPosition());
        builder.append("|");
        builder.append(bind.etAge.getText().toString());
        builder.append("|");
        builder.append(bind.etValue1h.getText().toString());
        builder.append("|");
        builder.append(bind.etValue1v.getText().toString());
        builder.append("|");
        builder.append(bind.etValue2h.getText().toString());
        builder.append("|");
        builder.append(bind.etValue2v.getText().toString());
        builder.append("|");
        builder.append(bind.etValue3h.getText().toString());
        builder.append("|");
        builder.append(bind.etValue3v.getText().toString());
        builder.append("|0|");

        return builder.toString();
    }

    /**
     * Calculate results for right and left hand and displays mean values.
     * Minimum one set of values needed.
     * <p>
     * r
     * Return string representing mean and normal values for right and left hand.
     * Format -1|-1| when no values given
     */
    private String calculateResults() {
        StringBuilder builder = new StringBuilder();
        // Right hand
        if (!bind.etValue1h.getText().toString().isEmpty()) {
            float total = 0;
            int counter = 0;    // number of measures (1, 2 or 3)
            if (!bind.etValue1h.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue1h.getText().toString());
                counter++;
            }

            if (!bind.etValue2h.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue2h.getText().toString());
                counter++;
            }

            if (!bind.etValue3h.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue3h.getText().toString());
                counter++;
            }

            builder.append(String.format(Locale.ENGLISH, "%.1f", total / counter));
            builder.append("|");
            bind.tvMeanValueH.setText(String.format(Locale.ENGLISH, "%.1f", total / counter));
        } else {
            bind.tvMeanValueH.setText("-");
            builder.append("-1|");
        }

        // Left hand
        if (!bind.etValue1v.getText().toString().isEmpty()) {
            float total = 0;
            int counter = 0;    // number of measures (1, 2 or 3)
            if (!bind.etValue1v.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue1v.getText().toString());
                counter++;
            }

            if (!bind.etValue2v.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue2v.getText().toString());
                counter++;
            }

            if (!bind.etValue3v.getText().toString().isEmpty()) {
                total += Float.parseFloat(bind.etValue3v.getText().toString());
                counter++;
            }

            builder.append(String.format(Locale.ENGLISH, "%.1f", total / counter));
            builder.append("|");
            bind.tvMeanValueV.setText(String.format(Locale.ENGLISH, "%.1f", total / counter));
        } else {
            bind.tvMeanValueV.setText("-");
            builder.append("-1|");
        }

        // Append normal values
        if (!bind.etAge.getText().toString().isEmpty() &&
                bind.spinGender.getSelectedItemPosition() != 0) {
            builder.append(getNormal(RIGHT, bind.spinGender.getSelectedItemPosition()));
            builder.append("|");
            builder.append(getNormal(LEFT, bind.spinGender.getSelectedItemPosition()));
            builder.append("|");
        } else {
            builder.append("-1|-1|");
        }

        return builder.toString();
    }

    /**
     * Updates TextViews for Normal values
     */
    private void displayNormalValues() {
        if (!bind.etAge.getText().toString().isEmpty() &&
                bind.spinGender.getSelectedItemPosition() != 0) {
            bind.tvNormalValueH.setText(getNormal(RIGHT, bind.spinGender.getSelectedItemPosition()));
            bind.tvNormalValueV.setText(getNormal(LEFT, bind.spinGender.getSelectedItemPosition()));
        } else {
            bind.tvNormalValueV.setText("-");
            bind.tvNormalValueH.setText("-");
        }
    }

    /**
     * Interface method implementation TextWatcher (EditText) for försök 1, 2 and 3.
     * Obs! Age has its own inline listener in onCreate
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
     * Interface method implementation OnItemSelectedListener (Spinner)
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
     * Return mean value from table 'Normative Grip Strength Data'
     */
    private String getNormal(int hand, int gender) {
        // age
        int age;
        try {
            age = Integer.parseInt(bind.etAge.getText().toString());
        } catch (NumberFormatException ex) {
            Log.d(LOG_TAG, "Age parse exception");
            return "n/a";
        }

        if (age < 8) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "14.7";
                else return "13.9";
            } else {
                if (hand == RIGHT)
                    return "13.0";
                else return "12.3";
            }
        } else if (age < 10) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "19.0";
                else return "17.7";
            } else {
                if (hand == RIGHT)
                    return "16.0";
                else return "15.0";
            }
        } else if (age < 12) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "24.4";
                else return "22.0";
            } else {
                if (hand == RIGHT)
                    return "25.5";
                else return "20.5";
            }
        } else if (age < 14) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "26.6";
                else return "25.1";
            } else {
                if (hand == RIGHT)
                    return "27.1";
                else return "23.1";
            }
        } else if (age < 16) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "35.1";
                else return "29.2";
            } else {
                if (hand == RIGHT)
                    return "26.4";
                else return "22.4";
            }
        } else if (age < 18) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "41.6";
                else return "35.6";
            } else {
                if (hand == RIGHT)
                    return "30.5";
                else return "25.8";
            }
        } else if (age < 20) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "49.0";
                else return "42.2";
            } else {
                if (hand == RIGHT)
                    return "32.5";
                else return "28.0";
            }
        } else if (age < 25) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "54.9";
                else return "47.4";
            } else {
                if (hand == RIGHT)
                    return "31.9";
                else return "27.7";
            }
        } else if (age < 30) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "54.7";
                else return "50.1";
            } else {
                if (hand == RIGHT)
                    return "33.8";
                else return "28.8";
            }
        } else if (age < 35) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "55.2";
                else return "50.0";
            } else {
                if (hand == RIGHT)
                    return "35.7";
                else return "30.8";
            }
        } else if (age < 40) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "54.3";
                else return "51.2";
            } else {
                if (hand == RIGHT)
                    return "33.6";
                else return "30.1";
            }
        } else if (age < 45) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "53.0";
                else return "51.2";
            } else {
                if (hand == RIGHT)
                    return "31.9";
                else return "28.3";
            }
        } else if (age < 50) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "49.8";
                else return "45.7";
            } else {
                if (hand == RIGHT)
                    return "28.2";
                else return "26.4";
            }
        } else if (age < 55) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "51.5";
                else return "46.2";
            } else {
                if (hand == RIGHT)
                    return "29.8";
                else return "26.0";
            }
        } else if (age < 60) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "45.9";
                else return "37.7";
            } else {
                if (hand == RIGHT)
                    return "26.0";
                else return "21.5";
            }
        } else if (age < 65) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "40.7";
                else return "34.8";
            } else {
                if (hand == RIGHT)
                    return "25.0";
                else return "20.7";
            }
        } else if (age < 70) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "41.3";
                else return "34.8";
            } else {
                if (hand == RIGHT)
                    return "22.5";
                else return "18.6";
            }
        } else if (age < 75) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "34.2";
                else return "29.3";
            } else {
                if (hand == RIGHT)
                    return "22.5";
                else return "18.8";
            }
        } else {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "29.8";
                else return "24.9";
            } else {
                if (hand == RIGHT)
                    return "19.3";
                else return "17.1";
            }
        }
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
