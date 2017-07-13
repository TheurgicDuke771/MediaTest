package com.example.android.mediatest.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arijit on 10-07-2017.
 */

public final class MediosContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private MediosContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.mediatest";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.mediatest/music/ is a valid path for
     * looking at music data. content://com.example.android.music/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_MUSIC = "music";


    public static final class MediosEntry implements BaseColumns {
        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MUSIC);

        /** The MIME type of the {@link #CONTENT_URI} for a list of pets.*/
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC;

        /** The MIME type of the {@link #CONTENT_URI} for a single pet.*/
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC;

        //Name of the database table
        public static final String TABLE_NAME = "music";

        //Name of the columns
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_MUSIC_TITLE = "title";
        public static final String COLUMN_MUSIC_ALBUM = "album";
        public static final String COLUMN_MUSIC_PATH = "path";
        public static final String COLUMN_MUSIC_ARTIST = "artist";
        public static final String COLUMN_MUSIC_GENRE = "genre";
        public static final String COLUMN_MUSIC_YEAR = "year";
        public static final String COLUMN_MUSIC_DURATION = "duration";
    }
}
