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

import com.pbluedotsoft.atapp.data.DbContract;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentBoxBinding;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class BoxFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = BoxFragment.class.getSimpleName();
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

    private FragmentBoxBinding bind;

    public BoxFragment() {
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
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_box, container, false);

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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.box_gender_spinner,
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

                if (bind.etValueH.getText().toString().isEmpty() ||
                        bind.etValueV.getText().toString().isEmpty()) {
                    showDialog("Please enter RESULTAT (höger och vänster)");
                    return;
                }

                saveToDatabase();
                showDialog(getResources().getString(R.string.test_saved_complete));

                // Inform parent activity
                ((TestActivity) getActivity()).setUserHasSaved(true);
            }
        });

        bind.etValueH.addTextChangedListener(this);
        bind.etValueV.addTextChangedListener(this);

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
                contentStr = cursor.getString(cursor.getColumnIndex(DbContract.TestEntry.COLUMN_CONTENT_IN));
            } else {
                contentStr = cursor.getString(cursor.getColumnIndex(DbContract.TestEntry.COLUMN_CONTENT_OUT));
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
            bind.etValueH.setText(content[2]);
            bind.etValueV.setText(content[3]);
        }

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
        builder.append(bind.etValueH.getText().toString());
        builder.append("|");
        builder.append(bind.etValueV.getText().toString());
        builder.append("|");
        // Append normal values, used in Result Table
        builder.append(bind.tvNormalH.getText().toString());
        builder.append("|");
        builder.append(bind.tvNormalV.getText().toString());
        builder.append("|0|");

        return builder.toString();
    }

    /**
     * Generate string of results for values and normal values.
     * Used in ResultTableActivity.
     */
    private String calculateResults() {
        StringBuilder builder = new StringBuilder();
        builder.append(bind.etValueH.getText().toString());
        builder.append("|");
        builder.append(bind.etValueV.getText().toString());
        builder.append("|");
        builder.append(bind.tvNormalH.getText().toString());
        builder.append("|");
        builder.append(bind.tvNormalV.getText().toString());
        builder.append("|");
        return builder.toString();
    }

    /**
     * Saves content, result and status
     * In this particular test, this method gets called when all fields are filled.
     * No need to control empty fields here. It has already been taken care of.
     */
    private void saveToDatabase() {
        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(DbContract.TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(DbContract.TestEntry.COLUMN_RESULT_IN, calculateResults());
            values.put(DbContract.TestEntry.COLUMN_STATUS_IN, Test.COMPLETED);
        } else {
            values.put(DbContract.TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(DbContract.TestEntry.COLUMN_RESULT_OUT, calculateResults());
            values.put(DbContract.TestEntry.COLUMN_STATUS_OUT, Test.COMPLETED);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);
    }

    /**
     * Updates TextViews for Normal values
     */
    private void displayNormalValues() {
        if (!bind.etAge.getText().toString().isEmpty() &&
                bind.spinGender.getSelectedItemPosition() != 0) {
            bind.tvNormalH.setText(getNormal(RIGHT, bind.spinGender.getSelectedItemPosition()));
            bind.tvNormalV.setText(getNormal(LEFT, bind.spinGender.getSelectedItemPosition()));
        } else {
            bind.tvNormalV.setText("-");
            bind.tvNormalH.setText("-");
        }
    }

    /**
     * Return mean value from table 'Female and Male Norms'
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

        if (age < 45) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "83.0";
                else return "80.0";
            } else {
                if (hand == RIGHT)
                    return "81,1";
                else return "79.7";
            }
        } else if (age < 50) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "76.9";
                else return "75.8";
            } else {
                if (hand == RIGHT)
                    return "82.1";
                else return "78.3";
            }
        } else if (age < 55) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "79.0";
                else return "77.0";
            } else {
                if (hand == RIGHT)
                    return "77.7";
                else return "74.3";
            }
        } else if (age < 60) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "75.2";
                else return "73.8";
            } else {
                if (hand == RIGHT)
                    return "74.7";
                else return "73.6";
            }
        } else if (age < 65) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "71.3";
                else return "70.5";
            } else {
                if (hand == RIGHT)
                    return "76.1";
                else return "73.6";
            }
        } else if (age < 70) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "68.5";
                else return "67.4";
            } else {
                if (hand == RIGHT)
                    return "72.0";
                else return "71.3";
            }
        } else if (age < 75) {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "66.3";
                else return "64.3";
            } else {
                if (hand == RIGHT)
                    return "68.6";
                else return "68.3";
            }
        } else {
            if (gender == MALE) {
                if (hand == RIGHT)
                    return "63.0";
                else return "61.3";
            } else {
                if (hand == RIGHT)
                    return "65.0";
                else return "63.6";
            }
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

    /**
     * Interface method implementation TextWatcher (EditText).
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

}
