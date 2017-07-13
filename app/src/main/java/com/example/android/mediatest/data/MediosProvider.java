package com.example.android.mediatest.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.example.android.mediatest.data.MediosContract.*;
import android.util.Log;


/**
 * Created by Arijit on 12-07-2017.
 */

public class MediosProvider extends ContentProvider {

    /** URI matcher code for the content URI for the music table */
    private static final int MUSIC = 100;

    /** URI matcher code for the content URI for a single row in the music table */
    private static final int MUSIC_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.mediatest/music" will map to the
        // integer code {@link #MUSIC}. This URI is used to provide access to MULTIPLE rows
        // of the music table.
        sUriMatcher.addURI(MediosContract.CONTENT_AUTHORITY, MediosContract.PATH_MUSIC, MUSIC);

        // The content URI of the form "content://com.example.android.mediatest/music/#" will map to the
        // integer code {@link #MUSIC_ID}. This URI is used to provide access to ONE single row of the music table.
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        sUriMatcher.addURI(MediosContract.CONTENT_AUTHORITY, MediosContract.PATH_MUSIC + "/#", MUSIC_ID);
    }


    /** Database helper object */
    private MediosDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new MediosDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match){
            case MUSIC:
                //Query the table directly with the given projection, selection, selection arguments, and sort order.
                // The cursor could contain multiple rows of the music table.
                cursor = database.query(
                        MediosEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MUSIC_ID:
                //To query a specific row of the table. "?" represent the value of where clause.
                selection = MediosEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the music table where the _id equals #(any integer value)
                // to return a Cursor containing that row of the table.
                cursor = database.query(
                        MediosEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor, so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MUSIC:
                return insertSong(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertSong(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(MediosEntry.COLUMN_MUSIC_TITLE);
        if (name == null) {
            throw new IllegalArgumentException("Song requires a name");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new song with the given values
        long id = database.insert(MediosEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(MediosProvider.class.getSimpleName(), "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the medios content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MUSIC:
                return MediosEntry.CONTENT_LIST_TYPE;
            case MUSIC_ID:
                return MediosEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
