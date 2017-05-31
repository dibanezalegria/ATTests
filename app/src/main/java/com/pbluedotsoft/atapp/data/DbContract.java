package com.pbluedotsoft.atapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daniel on 31/05/17.
 */

public class DbContract {
    //  Content Provider constants
    public static final String CONTENT_AUTHORITY = "com.pbluedotsoft.atapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PATIENT = "patient";
    public static final String PATH_TEST = "test";
    public static final String PATH_USER = "user";

    /**
     * Inner class that defines the table contents for 'user' table
     */
    public static final class UserEntry implements BaseColumns {
        // The content URI to access the user data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USER);

        public static final String TABLE_NAME = "user";

        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PASS = "pass";

        // MIME types used by the getType method ContentProvider
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
    }

    /**
     * Inner class that defines the table contents for 'patient' table
     */
    public static final class PatientEntry implements BaseColumns {
        //  The content URI to access the patient data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PATIENT);

        public static final String TABLE_NAME = "patient";

        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_USER_ID_FK = "user_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ENTRY_NUMBER = "entry_number";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_ACTIVE = "active";

        // MIME types used by the getType method ContentProvider
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PATIENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PATIENT;
    }

    /**
     * Inner class that defines the table contents for 'test' table
     */
    public static final class TestEntry implements BaseColumns {
        //  The content URI to access the patient data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TEST);

        public static final String TABLE_NAME = "test";

        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_PATIENT_ID_FK = "patient_id";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TITLE_NAME = "title_name";
        public static final String COLUMN_CONTENT_IN = "content_in";
        public static final String COLUMN_CONTENT_OUT = "content_out";
        public static final String COLUMN_RESULT_IN = "result_in";
        public static final String COLUMN_RESULT_OUT = "result_out";
        public static final String COLUMN_NOTES_IN = "notes_in";
        public static final String COLUMN_NOTES_OUT = "notes_out";
        public static final String COLUMN_DATE_IN = "date_in";
        public static final String COLUMN_DATE_OUT = "date_out";
        public static final String COLUMN_STATUS_IN = "status_in";
        public static final String COLUMN_STATUS_OUT = "status_out";

        // MIME types used by the getType method ContentProvider
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEST;

    }
}
