package com.pbluedotsoft.atapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.pbluedotsoft.atapp.databinding.FragmentAddPatientDialogBinding;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddPatientDialogFragment extends DialogFragment {

    private static final String LOG_TAG = AddPatientDialogFragment.class.getSimpleName();

    // Return data back to the activity through the implemented listener
    private AddPatientDialogListener mListener;

    /**
     * Interface callback declaration
     */
    public interface AddPatientDialogListener {
        void onCreateAddPatientDialog(String name, int entry);
    }

    public AddPatientDialogFragment() {
        // Required empty public constructor
    }

    public static AddPatientDialogFragment newInstance() {
        AddPatientDialogFragment dialogFragment = new AddPatientDialogFragment();
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_add_patient_dialog, null, false);
        // Binding instead of findViewById
        final FragmentAddPatientDialogBinding bind = FragmentAddPatientDialogBinding.bind(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameStr = bind.etName.getText().toString().trim();
                        String entryStr = bind.etEntry.getText().toString().trim();
                        // Data validation: name is mandatory
                        if (!nameStr.isEmpty()) {
                            // Validate entry number
                            int entry = 0;
                            if (!entryStr.isEmpty()) {
                                entry = Integer.parseInt(bind.etEntry.getText().toString());
                            }
                            mListener.onCreateAddPatientDialog(nameStr, entry);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (AddPatientDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AddPatientDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
