package com.example.android.mediatest;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.mediatest.data.MediosContract.MediosEntry;

public class Player extends AppCompatActivity {

    private Uri mCurrentSongUri;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        mCurrentSongUri = intent.getData();

        String[] projection = {
                MediosEntry._ID,
                MediosEntry.COLUMN_MUSIC_PATH
        };

        Cursor cursor = getContentResolver().query(mCurrentSongUri, projection, null, null, null);
        cursor.moveToNext();
        int songPathColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_PATH);

        String songPath = cursor.getString(songPathColumnIndex);

        Uri uri = Uri.parse(songPath);

        mp = MediaPlayer.create(getApplicationContext(),uri);
        mp.start();

        cursor.close();
    }
}
