package com.example.android.mediatest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.android.mediatest.data.MediosContract.MediosEntry;

public class Player extends AppCompatActivity implements View.OnClickListener {
    private AudioManager mAudioManager;
    int result;

    private Uri mCurrentSongUri, uri;
    MediaPlayer mp;
    Button btPlay, btNxt, btPv;
    SeekBar sb;
    Cursor cursor;
    int position, songPathColumnIndex, songTitleColumnIndex;
    String songPath, songTitle;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mp.pause();
                mp.seekTo(mp.getCurrentPosition());
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mp.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mp.release();
                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            }
        }
    };

    Thread updateSeekBar = new Thread() {
        @Override
        public void run() {
            int totalDuration = mp.getDuration();
            int currentPostion = 0;
            while (currentPostion < totalDuration)
                try {
                    sleep(500);
                    currentPostion = mp.getCurrentPosition();
                    sb.setProgress(currentPostion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mp.stop();
            mp.release();
            position = (position + 1) % cursor.getCount();
            cursor.moveToPosition(position);
            songPathColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_PATH);
            songPath = cursor.getString(songPathColumnIndex);
            uri = Uri.parse(songPath);
            result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mp = MediaPlayer.create(getApplicationContext(), uri);
                mp.start();
                sb.setMax(mp.getDuration());
                sb.setProgress(0);
                mp.setOnCompletionListener(mCompletionListener);
            }
            songTitleColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
            songTitle = cursor.getString(songTitleColumnIndex);
            setTitle(songTitle);
        }
    };

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

        sb = (SeekBar) findViewById(R.id.seekBar);

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

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp = MediaPlayer.create(getApplicationContext(), uri);
            mp.start();
            sb.setMax(mp.getDuration());
            sb.setProgress(0);
            mp.setOnCompletionListener(mCompletionListener);
        }

        updateSeekBar.start();

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
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
                result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mp = MediaPlayer.create(getApplicationContext(), uri);
                    mp.start();
                    sb.setMax(mp.getDuration());
                    sb.setProgress(0);
                }
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
                result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mp = MediaPlayer.create(getApplicationContext(), uri);
                    mp.start();
                    sb.setMax(mp.getDuration());
                    sb.setProgress(0);
                }
                btPlay.setText("||");

                songTitleColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
                songTitle = cursor.getString(songTitleColumnIndex);
                setTitle(songTitle);
                break;
        }
        mp.setOnCompletionListener(mCompletionListener);
    }
}
