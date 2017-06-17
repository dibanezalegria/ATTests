package com.pbluedotsoft.atapp;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentHaqBinding;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class HAQFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static String LOG_TAG = HAQFragment.class.getSimpleName();
    private static int N_QUESTIONS = 20;
    private static int N_SUMS = 8;
    // Save state constant
    private static final String STATE_CONTENT = "state_content";
    private static final String STATE_HIGH_ON = "state_high_on";

    private Uri mTestUri;
    private int mTab, mInOut;
    private boolean mHighlightsON;

    private Spinner[] mSpin;
    private TextView[] mTvSum;
    private TextView[] mTvQ;    // used for highlighting

    private FragmentHaqBinding bind;

    public HAQFragment() {
        mSpin = new Spinner[N_QUESTIONS];
        mTvSum = new TextView[N_SUMS];
        mTvQ = new TextView[N_QUESTIONS];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Binding instead of findViewById
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_haq, container, false);

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

        // TextViews
        mTvSum[0] = bind.tvSum0;
        mTvSum[1] = bind.tvSum1;
        mTvSum[2] = bind.tvSum2;
        mTvSum[3] = bind.tvSum3;
        mTvSum[4] = bind.tvSum4;
        mTvSum[5] = bind.tvSum5;
        mTvSum[6] = bind.tvSum6;
        mTvSum[7] = bind.tvSum7;


        // Spinners
        mSpin[0] = bind.spin0;
        mSpin[1] = bind.spin1;
        mSpin[2] = bind.spin2;
        mSpin[3] = bind.spin3;
        mSpin[4] = bind.spin4;
        mSpin[5] = bind.spin5;
        mSpin[6] = bind.spin6;
        mSpin[7] = bind.spin7;
        mSpin[8] = bind.spin8;
        mSpin[9] = bind.spin9;
        mSpin[10] = bind.spin10;
        mSpin[11] = bind.spin11;
        mSpin[12] = bind.spin12;
        mSpin[13] = bind.spin13;
        mSpin[14] = bind.spin14;
        mSpin[15] = bind.spin15;
        mSpin[16] = bind.spin16;
        mSpin[17] = bind.spin17;
        mSpin[18] = bind.spin18;
        mSpin[19] = bind.spin19;

        // Spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.haq_spinner_values,
                R.layout.spinner_list_item);

        for (Spinner spin : mSpin) {
            spin.setAdapter(adapter);
            spin.setOnItemSelectedListener(this);
        }

        // Done button
        bind.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save to database: return false if test incomplete
                if (!saveToDatabase()) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_incomplete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "SHOW ME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mHighlightsON = true;
                            highlightQuestions();
                        }
                    });
                    dialog.show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                    dialog.setMessage(getResources().getString(R.string.test_saved_complete));
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            highlightQuestions(); // clear  highlights
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
            mHighlightsON = savedInstanceState.getBoolean(STATE_HIGH_ON);
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
            for (int i = 0; i < N_QUESTIONS; i++) {
                try {
                    mSpin[i].setSelection(Integer.parseInt(content[i]));
                } catch (NumberFormatException ex) {
                    Log.d(LOG_TAG, "Exception converting spinner position from DB");
                }
            }
        }

        if (!checkMissingAnswers()) {
            bind.tvTotalSum.setText(calculateSums());
        }

        if (mHighlightsON) {
            highlightQuestions();
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
        outState.putBoolean(STATE_HIGH_ON, mHighlightsON);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    /**
     * Checks whether all spinners have been answered and fills the array mMissingAnswer
     *
     * @return false - test is complete
     */
    private boolean checkMissingAnswers() {
        boolean missing = false;
        // Check index of selected spinner
        for (int i = 0; i < N_QUESTIONS; i++) {
            if (mSpin[i].getSelectedItemPosition() == 0) {
                missing = true;
            }
        }

        return missing;
    }

    /**
     * Saves content, result and status
     *
     * @return true if test is complete, false if there are answers mMissingAnswer
     */
    public boolean saveToDatabase() {
        // Check whether all spinners have been answered
        boolean missingAnswers = checkMissingAnswers();

        // Result in database will be sumHand|sumArm|sumBen
        StringBuilder contentBuilder = new StringBuilder();
        for (Spinner spin : mSpin) {
            contentBuilder.append(spin.getSelectedItemPosition());
            contentBuilder.append("|");
        }

        int status;
        String result = "-1";
        if (missingAnswers) {
            status = Test.INCOMPLETED;
        } else {
            status = Test.COMPLETED;
            result = calculateSums();
        }

        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_RESULT_IN, result);
            values.put(TestEntry.COLUMN_STATUS_IN, status);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_RESULT_OUT, result);
            values.put(TestEntry.COLUMN_STATUS_OUT, status);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

        return !missingAnswers;
    }

    /**
     * Highlights unanswered question
     */
    private void highlightQuestions() {
        // Find text view questions here to avoid slowing down fragment inflate
        if (mTvQ[0] == null) {
            mTvQ[0] = bind.tvQ0;
            mTvQ[1] = bind.tvQ1;
            mTvQ[2] = bind.tvQ2;
            mTvQ[3] = bind.tvQ3;
            mTvQ[4] = bind.tvQ4;
            mTvQ[5] = bind.tvQ5;
            mTvQ[6] = bind.tvQ6;
            mTvQ[7] = bind.tvQ7;
            mTvQ[8] = bind.tvQ8;
            mTvQ[9] = bind.tvQ9;
            mTvQ[10] = bind.tvQ10;
            mTvQ[11] = bind.tvQ11;
            mTvQ[12] = bind.tvQ12;
            mTvQ[13] = bind.tvQ13;
            mTvQ[14] = bind.tvQ14;
            mTvQ[15] = bind.tvQ15;
            mTvQ[16] = bind.tvQ16;
            mTvQ[17] = bind.tvQ17;
            mTvQ[18] = bind.tvQ18;
            mTvQ[19] = bind.tvQ19;
        }

        for (int i = 0; i < N_QUESTIONS; i++) {
            if (mSpin[i].getSelectedItemPosition() == 0 && mHighlightsON) {
                mTvQ[i].setTextColor(ContextCompat.getColor(getContext(), R.color.highlight));
            } else {
                mTvQ[i].setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
            }
        }
    }


    /**
     * Calculates partial and total sums for spinners AND displays partial and total sums
     *
     * @return total sum OR -1 when one or more spinners missing
     */
    private String calculateSums() {
        int sum[] = new int[N_SUMS];
        if (mSpin[0].getSelectedItemPosition() != 0 && mSpin[1].getSelectedItemPosition() != 0) {
            sum[0] = getValue(mSpin[0]) + getValue(mSpin[1]);
            bind.tvSum0.setText(String.valueOf(sum[0]));
        } else {
            sum[0] = -1;
            bind.tvSum0.setText("");
        }

        if (mSpin[2].getSelectedItemPosition() != 0 && mSpin[3].getSelectedItemPosition() != 0) {
            sum[1] = getValue(mSpin[2]) + getValue(mSpin[3]);
            bind.tvSum1.setText(String.valueOf(sum[1]));
        } else {
            sum[1] = -1;
            bind.tvSum1.setText("");
        }

        if (mSpin[4].getSelectedItemPosition() != 0 && mSpin[5].getSelectedItemPosition() != 0 &&
                mSpin[6].getSelectedItemPosition() != 0) {
            sum[2] = getValue(mSpin[4]) + getValue(mSpin[5]) + getValue(mSpin[6]);
            bind.tvSum2.setText(String.valueOf(sum[2]));
        } else {
            sum[2] = -1;
            bind.tvSum2.setText("");
        }

        if (mSpin[7].getSelectedItemPosition() != 0 && mSpin[8].getSelectedItemPosition() != 0) {
            sum[3] = getValue(mSpin[7]) + getValue(mSpin[8]);
            bind.tvSum3.setText(String.valueOf(sum[3]));
        } else {
            sum[3] = -1;
            bind.tvSum3.setText("");
        }

        if (mSpin[9].getSelectedItemPosition() != 0 && mSpin[10].getSelectedItemPosition() != 0 &&
                mSpin[11].getSelectedItemPosition() != 0) {
            sum[4] = getValue(mSpin[9]) + getValue(mSpin[10]) + getValue(mSpin[11]);
            bind.tvSum4.setText(String.valueOf(sum[4]));
        } else {
            sum[4] = -1;
            bind.tvSum4.setText("");
        }

        if (mSpin[12].getSelectedItemPosition() != 0 && mSpin[13].getSelectedItemPosition() != 0) {
            sum[5] = getValue(mSpin[12]) + getValue(mSpin[13]);
            bind.tvSum5.setText(String.valueOf(sum[5]));
        } else {
            sum[5] = -1;
            bind.tvSum5.setText("");
        }

        if (mSpin[14].getSelectedItemPosition() != 0 && mSpin[15].getSelectedItemPosition() != 0 &&
                mSpin[16].getSelectedItemPosition() != 0) {
            sum[6] = getValue(mSpin[14]) + getValue(mSpin[15]) + getValue(mSpin[16]);
            bind.tvSum6.setText(String.valueOf(sum[6]));
        } else {
            sum[6] = -1;
            bind.tvSum6.setText("");
        }

        if (mSpin[17].getSelectedItemPosition() != 0 && mSpin[18].getSelectedItemPosition() != 0 &&
                mSpin[19].getSelectedItemPosition() != 0) {
            sum[7] = getValue(mSpin[17]) + getValue(mSpin[18]) + getValue(mSpin[19]);
            bind.tvSum7.setText(String.valueOf(sum[7]));
        } else {
            sum[7] = -1;
            bind.tvSum7.setText("");
        }

        float total = 0;
        for (float value : sum) {
            if (value != -1) {
                total += value;
            } else {
                bind.tvTotalSum.setText("");
                return "-1";
            }
        }

        String totalStr = String.format(Locale.ENGLISH, "%.2f", (total / 20.0f));
        bind.tvTotalSum.setText(totalStr);

        return totalStr;
    }

    /**
     * Returns the value of selected item for given spinner
     */
    private int getValue(Spinner spin) {
        int pos = spin.getSelectedItemPosition();
        if (pos == 2) {
            return 1;
        } else if (pos == 3 || pos == 4 || pos == 5) {
            return 2;
        } else if (pos == 6) {
            return 3;
        }

        return 0;
    }

    /**
     * Save index of selected item for all spinners
     *
     * @return String representing state for radio groups in layout
     */
    private String generateContent() {
        StringBuilder contentBuilder = new StringBuilder();
        for (Spinner spin : mSpin) {
            contentBuilder.append(spin.getSelectedItemPosition());
            contentBuilder.append("|");
        }

        return contentBuilder.toString();
    }

    /**
     * Disable all views in a given layout
     */
    private void disableTouchOnLayout(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
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
     * Interface AdapterView.OnItemSelectedListener implementation
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (((TestActivity) getActivity()).mUserInteracting) {
            // Inform parent activity
            ((TestActivity) getActivity()).setUserHasSaved(false);
//            Log.d(LOG_TAG, "onItemSelected");
            calculateSums();
            highlightQuestions();
        } else {
//            Log.d(LOG_TAG, "onItemSelected: user is NOT interacting");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
