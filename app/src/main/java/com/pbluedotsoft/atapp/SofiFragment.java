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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;
import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.data.Test;
import com.pbluedotsoft.atapp.databinding.FragmentSofiBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class SofiFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String LOG_TAG = SofiFragment.class.getSimpleName();

    private static int N_QUESTIONS = 16;    // 8 x two hands per question
    // Save state constant
    private static final String STATE_CONTENT = "state_content";
    private static final String STATE_HIGH_ON = "state_high_on";

    private RadioGroup[] mRGroup;
    private TextView[] mTvSum, mTvRGroup;
    private boolean[] mMissingAnswers;
    private TextView mTotalSum;

    private Uri mTestUri;
    private int mTab, mInOut;
    private boolean mHighlightsON;

    private FragmentSofiBinding bind;

    public SofiFragment() {
        mRGroup = new RadioGroup[N_QUESTIONS];
        mMissingAnswers = new boolean[N_QUESTIONS];
        mTvRGroup = new TextView[N_QUESTIONS];
        mTvSum = new TextView[2];   // hand and arm
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
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_sofi, container, false);

        // Disable touch events in the 'other' tab
        if ((mTab == Test.IN && mInOut == 1) || (mTab == Test.OUT && mInOut == 0)) {
            disableTouchOnLayout(bind.scrollview);
        }

        // IN or OUT background color adjustments
        if (mTab == Test.IN) {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_in));

        } else {
            bind.scrollview.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_out));
//            bind.tvSum1.setBackgroundResource(R.drawable.sofi_tv_sum);
        }

        // Hook up radio groups from layout
        mRGroup[0] = bind.rg1h;
        mRGroup[1] = bind.rg1v;
        mRGroup[2] = bind.rg2h;
        mRGroup[3] = bind.rg2v;
        mRGroup[4] = bind.rg3h;
        mRGroup[5] = bind.rg3v;
        mRGroup[6] = bind.rg4h;
        mRGroup[7] = bind.rg4v;
        mRGroup[8] = bind.rg5h;
        mRGroup[9] = bind.rg5v;
        mRGroup[10] = bind.rg6h;
        mRGroup[11] = bind.rg6v;
        mRGroup[12] = bind.rg7h;
        mRGroup[13] = bind.rg7v;
        mRGroup[14] = bind.rg8h;
        mRGroup[15] = bind.rg8v;

        // Listeners
        for (RadioGroup rGroup : mRGroup) {
            rGroup.setOnCheckedChangeListener(this);
        }

        // Hook up sums text views
        mTvSum[0] = bind.tvSum1;
        mTvSum[1] = bind.tvSum2;
        mTotalSum = bind.tvTotalSum;

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
            if (mHighlightsON) {
                checkMissingAnswers(0, N_QUESTIONS); // updates mMissingAnswers[] -> needed for highlighting
                highlightQuestions();
            }
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
            RadioButton radioButton;
            for (int i = 0; i < N_QUESTIONS; i++) {
                if (!content[i].trim().equals("-1")) {
                    int childIndex = Integer.parseInt(content[i].trim());
                    radioButton = (RadioButton) mRGroup[i].getChildAt(childIndex);
                    radioButton.setChecked(true);
                }
            }

            bind.tvTotalSum.setText(String.valueOf(calculateSum(0, N_QUESTIONS)));
        }

        // Inform parent activity that form is up to date
        ((TestActivity) getActivity()).setUserHasSaved(true);

        // Inflate the layout for this fragment
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
     * Calculates the sum of points for selected radio buttons
     * between two radio groups [from, to), including 'from' and excluding 'to'
     *
     * @return total sum
     */
    private int calculateSum(int fromRg, int toRg) {
        View radioButton;
        int sum = 0;
        // Check all radio groups
        for (int i = fromRg; i < toRg; i++) {
            // Check index of selected radio button
            int radioButtonID = mRGroup[i].getCheckedRadioButtonId();
            if (radioButtonID != -1) {
                radioButton = mRGroup[i].findViewById(radioButtonID);
                int index = mRGroup[i].indexOfChild(radioButton);
                sum += index;
            }
        }

        return sum;
    }

    /**
     * Check whether all radio groups have one radio button checked and fills the array mMissingAnswers
     *
     * @return false - test is complete
     */
    private boolean checkMissingAnswers(int fromRg, int toRg) {
        boolean missing = false;
        // Check all radio groups
        for (int i = fromRg; i < toRg; i++) {
            // Check index of selected radio button
            int radioButtonID = mRGroup[i].getCheckedRadioButtonId();
            if (radioButtonID != -1) {
                mMissingAnswers[i] = false;
            } else {
                mMissingAnswers[i] = true;
                missing = true;
            }
        }

        return missing;
    }

    /**
     * Save index of selected radio button for each radio group
     *
     * @return String representing state for radio groups in layout
     */
    private String generateContent() {
        View radioButton;
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < N_QUESTIONS; i++) {
            int radioButtonID = mRGroup[i].getCheckedRadioButtonId();
            if (radioButtonID != -1) {
                radioButton = mRGroup[i].findViewById(radioButtonID);
                int index = mRGroup[i].indexOfChild(radioButton);
                contentBuilder.append(index);
            } else {
                contentBuilder.append("-1");
            }

            contentBuilder.append("|");
        }

        return contentBuilder.toString();
    }

    /**
     * Saves content, result and status
     *
     * @return true if test is complete, false if there are answers mMissingAnswers
     */
    public boolean saveToDatabase() {
        // Test status Hand and Arm radio groups
        boolean missingHand = checkMissingAnswers(0, 8);
        boolean missingArm = checkMissingAnswers(8, 16);

        // Result in database will be sumHand|sumArm|sumBen
        StringBuilder contentBuilder = new StringBuilder();
        if (missingHand) {
            contentBuilder.append("-1");
        } else {
            contentBuilder.append(calculateSum(0, 8));
        }

        contentBuilder.append("|");

        if (missingArm) {
            contentBuilder.append("-1");
        } else {
            contentBuilder.append(calculateSum(8, 16));
        }

        contentBuilder.append("|");

        int status;
        if (missingHand || missingArm) {
            status = Test.INCOMPLETED;
        } else {
            status = Test.COMPLETED;
        }

        ContentValues values = new ContentValues();
        if (mTab == Test.IN) {
            values.put(TestEntry.COLUMN_CONTENT_IN, generateContent());
            values.put(TestEntry.COLUMN_RESULT_IN, contentBuilder.toString());
            values.put(TestEntry.COLUMN_STATUS_IN, status);
        } else {
            values.put(TestEntry.COLUMN_CONTENT_OUT, generateContent());
            values.put(TestEntry.COLUMN_RESULT_OUT, contentBuilder.toString());
            values.put(TestEntry.COLUMN_STATUS_OUT, status);
        }

        int rows = getActivity().getContentResolver().update(mTestUri, values, null, null);
        Log.d(LOG_TAG, "rows updated: " + rows);

        return !missingHand && !missingArm;
    }

    /**
     * Highlights unanswered question
     */
    private void highlightQuestions() {
        // Find text view questions here to avoid slowing down fragment inflate
        if (mTvRGroup[0] == null) {
            mTvRGroup[0] = bind.tvRg1h;
            mTvRGroup[1] = bind.tvRg1v;
            mTvRGroup[2] = bind.tvRg2h;
            mTvRGroup[3] = bind.tvRg2v;
            mTvRGroup[4] = bind.tvRg3h;
            mTvRGroup[5] = bind.tvRg3v;
            mTvRGroup[6] = bind.tvRg4h;
            mTvRGroup[7] = bind.tvRg4v;
            mTvRGroup[8] = bind.tvRg5h;
            mTvRGroup[9] = bind.tvRg5v;
            mTvRGroup[10] = bind.tvRg6h;
            mTvRGroup[11] = bind.tvRg6v;
            mTvRGroup[12] = bind.tvRg7h;
            mTvRGroup[13] = bind.tvRg7v;
            mTvRGroup[14] = bind.tvRg8h;
            mTvRGroup[15] = bind.tvRg8v;
        }

        for (int i = 0; i < N_QUESTIONS; i++) {
            if (mMissingAnswers[i] && mHighlightsON) {
                mTvRGroup[i].setTextColor(ContextCompat.getColor(getContext(), R.color.highlight));
            } else {
                mTvRGroup[i].setTextColor(ContextCompat.getColor(getContext(), R.color.textColor));
            }
        }
    }

    /**
     * Listener on radio buttons help calculate partial sums
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.rg1h:
            case R.id.rg1v:
            case R.id.rg2h:
            case R.id.rg2v:
            case R.id.rg3h:
            case R.id.rg3v:
            case R.id.rg4h:
            case R.id.rg4v: {
                // Sum 1
                int sum = calculateSum(0, 8);
                mTvSum[0].setText(String.valueOf(sum));
                break;
            }

            case R.id.rg5h:
            case R.id.rg5v:
            case R.id.rg6h:
            case R.id.rg6v:
            case R.id.rg7h:
            case R.id.rg7v:
            case R.id.rg8h:
            case R.id.rg8v: {
                // Sum 1
                int sum = calculateSum(8, 16);
                mTvSum[1].setText(String.valueOf(sum));
                break;
            }
        }

        // Dynamic highlighting
        checkMissingAnswers(0, N_QUESTIONS);
        highlightQuestions();

        mTotalSum.setText(String.valueOf(calculateSum(0, N_QUESTIONS)));

        // Inform parent activity that changes have been made
        ((TestActivity) getActivity()).setUserHasSaved(false);
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

}
