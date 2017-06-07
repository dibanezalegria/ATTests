package com.pbluedotsoft.atapp;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import com.pbluedotsoft.atapp.data.EXTRAS;
import com.pbluedotsoft.atapp.databinding.ActivityTestListBinding;

import com.pbluedotsoft.atapp.data.DbContract.TestEntry;

public class TestListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = TestListActivity.class.getSimpleName();

    // Loader constant
    private static final int TEST_LOADER = 1;

    // Menu constants
    private static final int MENU_IN = 0;
    private static final int MENU_OUT = 1;

    private int mUserID;
    private int mPatientID;
    private String mUserName;
    private String mHeaderString;

    private TestCursorAdapter mCursorAdapter;

    private ActivityTestListBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Extract info from Bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserID = extras.getInt(EXTRAS.KEY_USER_ID);
            mUserName = extras.getString(EXTRAS.KEY_USER_NAME);
            mPatientID = extras.getInt(EXTRAS.KEY_PATIENT_ID);
            mHeaderString = extras.getString(EXTRAS.KEY_HEADER);
        }

        // Activity's title
        setTitle(mHeaderString);

        // Binding instead of findViewById
        bind = DataBindingUtil.setContentView(this, R.layout.activity_test_list);

        bind.btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog();
            }
        });

        // There is no data yet (until the loader finishes) so cursor is null for now.
        mCursorAdapter = new TestCursorAdapter(this, null);

        bind.listview.setAdapter(mCursorAdapter);
        bind.listview.setLongClickable(false);
        bind.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openContextMenu(view);
            }
        });

        // Context menu
        registerForContextMenu(bind.listview);

        // Kick off loader
        getSupportLoaderManager().initLoader(TEST_LOADER, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // Set title for the context menu
        String name = ((TextView) info.targetView.findViewById(R.id.tv_name))
                .getText().toString();
        menu.setHeaderTitle(name);
        // Menu options
        menu.add(Menu.NONE, MENU_IN, 0, "Starta IN test");
        menu.add(Menu.NONE, MENU_OUT, 1, "Starta UT test");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Start activity with IN or OUT test
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Uri uri = ContentUris.withAppendedId(TestEntry.CONTENT_URI, info.id);
        Intent intent = new Intent(TestListActivity.this, TestActivity.class);
        intent.setData(uri);
        intent.putExtra(EXTRAS.KEY_USER_ID, mUserID);
        intent.putExtra(EXTRAS.KEY_USER_NAME, mUserName);
        intent.putExtra(EXTRAS.KEY_PATIENT_ID, mPatientID);
        intent.putExtra(EXTRAS.KEY_HEADER, mHeaderString);
        intent.putExtra(EXTRAS.KEY_INOUT, item.getItemId());
        startActivity(intent);

        return super.onContextItemSelected(item);
    }

    private void helpDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        // fromHtml deprecated for Android N and higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            dialog.setMessage(Html.fromHtml(getString(R.string.test_list_help_info),
                    Html.FROM_HTML_MODE_LEGACY));
        } else {
            dialog.setMessage(Html.fromHtml(getString(R.string.test_list_help_info)));
        }

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

        // Change text size
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        if (msg != null)
            msg.setTextSize(18);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                goBackToPatientListActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goBackToPatientListActivity();
    }

    /**
     * Navigate up
     */
    private void goBackToPatientListActivity() {
        Intent upIntent = NavUtils.getParentActivityIntent(TestListActivity.this);
        upIntent.putExtra(EXTRAS.KEY_USER_ID, mUserID);
        upIntent.putExtra(EXTRAS.KEY_USER_NAME, mUserName);
        NavUtils.navigateUpTo(TestListActivity.this, upIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = TestEntry.COLUMN_PATIENT_ID_FK + "=?";
        String[] selectionArgs = {String.valueOf(mPatientID)};

        if (id == TEST_LOADER) {
            return new CursorLoader(this, TestEntry.CONTENT_URI, null, selection,
                    selectionArgs, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == TEST_LOADER) {
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == TEST_LOADER) {
            mCursorAdapter.swapCursor(null);
        }
    }
}
