package com.example.android.mediatest;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.mediatest.data.MediosContract;

import java.io.File;
import java.util.ArrayList;

public class Player extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_SONG_LOADER = 0;
    private Uri mCurrentSongUri;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        mCurrentSongUri = intent.getData();

        if (mCurrentSongUri != null){
            getLoaderManager().initLoader(EXISTING_SONG_LOADER, null, this);
        }

        mp = MediaPlayer.create(getApplicationContext(),mCurrentSongUri);
        mp.start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MediosContract.MediosEntry._ID,
                MediosContract.MediosEntry.COLUMN_MUSIC_TITLE,
                MediosContract.MediosEntry.COLUMN_MUSIC_PATH,
        };

        return new CursorLoader(this,
                MediosContract.MediosEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
