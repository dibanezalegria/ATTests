package com.pbluedotsoft.atapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.pbluedotsoft.atapp.data.DbContract.UserEntry;
import com.pbluedotsoft.atapp.data.DbContract.PatientEntry;
import com.pbluedotsoft.atapp.data.DbContract.TestEntry;

/**
 * Created by daniel on 31/05/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper sInstance;

    private static final String DATABASE_NAME = "atapp.db";
    private static final int DATABASE_VERSION = 1;

    private final String SQL_CREATE_USER_TABLE = "CREATE TABLE " +
            UserEntry.TABLE_NAME + " (" +
            UserEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            UserEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            UserEntry.COLUMN_PASS + " TEXT)";

    private final String SQL_CREATE_PATIENT_TABLE = "CREATE TABLE " +
            PatientEntry.TABLE_NAME + " (" +
            PatientEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PatientEntry.COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
            PatientEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            PatientEntry.COLUMN_ENTRY_NUMBER + " INTEGER, " +
            PatientEntry.COLUMN_NOTES + " TEXT, " +
            PatientEntry.COLUMN_ACTIVE + " INTEGER DEFAULT 1, " +

            // Set up the user_id_fk as a foreign key to user table
            "FOREIGN KEY (" + PatientEntry.COLUMN_USER_ID_FK + ") REFERENCES " +
            UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_ID + "));";

    private final String SQL_CREATE_TEST_TABLE = "CREATE TABLE " +
            TestEntry.TABLE_NAME + " (" +
            TestEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TestEntry.COLUMN_PATIENT_ID_FK + " INTEGER NOT NULL, " +
            TestEntry.COLUMN_CODE + " TEXT NOT NULL, " +
            TestEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            TestEntry.COLUMN_TITLE_NAME + " TEXT, " +
            TestEntry.COLUMN_CONTENT_IN + " TEXT, " +
            TestEntry.COLUMN_CONTENT_OUT + " TEXT, " +
            TestEntry.COLUMN_RESULT_IN + " TEXT DEFAULT -1, " +
            TestEntry.COLUMN_RESULT_OUT + " TEXT DEFAULT -1, " +
            TestEntry.COLUMN_NOTES_IN + " TEXT, " +
            TestEntry.COLUMN_NOTES_OUT + " TEXT, " +
            TestEntry.COLUMN_DATE_IN + " INTEGER, " +
            TestEntry.COLUMN_DATE_OUT + " INTEGER, " +
            TestEntry.COLUMN_STATUS_IN + " INTEGER, " +
            TestEntry.COLUMN_STATUS_OUT + " INTEGER, " +

            // Set up the patient_id_fk as a foreign key to patient table
            "FOREIGN KEY (" + TestEntry.COLUMN_PATIENT_ID_FK + ") REFERENCES " +
            PatientEntry.TABLE_NAME + " (" + PatientEntry.COLUMN_ID + "));";

//                // To assure the application have just one test type(code) per patient,
//                // it's created a UNIQUE constraint with REPLACE strategy
//                +
//                "UNIQUE (" + TestEntry.COLUMN_CODE + ", " +
//                TestEntry.COLUMN_PATIENT_ID_FK + ") ON CONFLICT REPLACE);";


    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DbHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_PATIENT_TABLE);
        db.execSQL(SQL_CREATE_TEST_TABLE);
        // Insert super user
//        db.execSQL("INSERT INTO " + UserEntry.TABLE_NAME + "(NAME, PASS) VALUES ('super', 'super')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Do not use break between cases -> allows multiple updates from old versions
//        switch (oldVersion) {
//            case 1:
//                // Create User table
//                db.execSQL(SQL_CREATE_USER_TABLE);
//                // Insert super user
////                db.execSQL("INSERT INTO " + UserEntry.TABLE_NAME +
////                        "(NAME, PASS) VALUES ('super', 'super')");
//
//                // Use ALTER to add column user_id_fk to Patient table
//                // When upgrading from v1, create 1st the account for the old user right after
//                // upgrading. This way old patients will get transferred to this user since we are
//                // creating the new column with id = 1
//                db.execSQL("ALTER TABLE " + PatientEntry.TABLE_NAME + " ADD COLUMN " +
//                        PatientEntry.COLUMN_USER_ID_FK + " INTEGER DEFAULT 1");
//
//        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constrains
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
