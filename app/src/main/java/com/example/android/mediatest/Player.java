package com.example.android.mediatest;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.android.mediatest.data.MediosContract.MediosEntry;

public class Player extends AppCompatActivity implements View.OnClickListener {

    private Uri mCurrentSongUri, uri;
    MediaPlayer mp;
    Button btPlay, btNxt, btPv;
    Cursor cursor;
    int position, songPathColumnIndex, songTitleColumnIndex;
    String songPath, songTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btPlay = (Button)findViewById(R.id.btPlay);
        btNxt = (Button) findViewById(R.id.btNxt);
        btPv = (Button) findViewById(R.id.btPv);

        btPlay.setOnClickListener(this);
        btNxt.setOnClickListener(this);
        btPv.setOnClickListener(this);


        Intent intent = getIntent();
//        mCurrentSongUri = intent.getData();
        position = intent.getIntExtra("pos", 0);

        String[] projection = {
                MediosEntry._ID,
                MediosEntry.COLUMN_MUSIC_TITLE,
                MediosEntry.COLUMN_MUSIC_PATH
        };

        //cursor = getContentResolver().query(mCurrentSongUri, projection, null, null, null);
        cursor = getContentResolver().query(MediosEntry.CONTENT_URI,projection,null,null,null);
        cursor.moveToPosition(position);
        songPathColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_PATH);
        songPath = cursor.getString(songPathColumnIndex);
        songTitleColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
        songTitle = cursor.getString(songTitleColumnIndex);
        uri = Uri.parse(songPath);
        setTitle(songTitle);

        if (mp != null) {
            mp.stop();
            mp.release();
        }

        mp = MediaPlayer.create(getApplicationContext(), uri);
        mp.start();

//        cursor.close();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btPlay:
                if (mp.isPlaying()) {
                    btPlay.setText(">");
                    mp.pause();
                } else {
                    btPlay.setText("||");
                    mp.start();
                }
                break;
            case R.id.btNxt:
                mp.stop();
                mp.reset();
                mp.release();

                position = (position + 1) % cursor.getCount();
                cursor.moveToPosition(position);
                songPathColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_PATH);
                songPath = cursor.getString(songPathColumnIndex);
                uri = Uri.parse(songPath);

                mp = MediaPlayer.create(getApplicationContext(),uri);
                mp.start();
                btPlay.setText("||");

                songTitleColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
                songTitle = cursor.getString(songTitleColumnIndex);
                setTitle(songTitle);
                break;
            case R.id.btPv:
                mp.stop();
                mp.reset();
                mp.release();

                position = (position - 1 < 0) ? cursor.getCount() - 1 : position - 1;
                cursor.moveToPosition(position);
                songPathColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_PATH);
                songPath = cursor.getString(songPathColumnIndex);
                uri = Uri.parse(songPath);

                mp = MediaPlayer.create(getApplicationContext(),uri);
                mp.start();
                btPlay.setText("||");

                songTitleColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
                songTitle = cursor.getString(songTitleColumnIndex);
                setTitle(songTitle);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mp.stop();
        mp.release();
        finish();
    }
}
