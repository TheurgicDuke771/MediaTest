package com.example.android.mediatest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.android.mediatest.data.MediosContract.MediosEntry;

public class Player extends AppCompatActivity implements View.OnClickListener {
    private AudioManager mAudioManager;
    private Uri uri;
    MediaPlayer mp;
    Button btPlay, btNxt, btPv;
    SeekBar sb;
    Cursor cursor;
    int position, songPathColumnIndex, songTitleColumnIndex, result;
    String songPath, songTitle;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        /**
         * This method is called whenever the audio focus changes
         * (i.e., we gain or lose audio focus because of another app or device).
         */
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS: {
                    /**
                     * This occurs when another app has requested audio focus.
                     * When this happens, you should stop audio playback in your app.
                     */
                    if (mp.isPlaying()) {
                        mp.stop();
                        btPlay.setText(">");
                    }
                    break;
                }

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                    /**
                     * This state is entered when another app wants to play audio,
                     * but it only anticipates needing focus for a short time.
                     * You can use this state to pause your audio playback.
                     */
                    mp.pause();
                    btPlay.setText(">");
                    break;
                }

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    /**
                     * When audio focus is requested, but throws a 'can duck' state, it means that you can continue your playback,
                     * but should bring the volume down a bit.
                     * This can occur when a notification sound is played by the device.
                     */
                    if (mp != null) {
                        mp.setVolume(0.3f, 0.3f);
                    }
                    break;
                }

                case AudioManager.AUDIOFOCUS_GAIN: {
                    /**
                     * The final state we will discuss is AUDIOFOCUS_GAIN.
                     * This is the state when a audio playback that can be ducked has completed, and your app can resume at its previous levels.
                     */
                    if (mp != null) {
                        if (!mp.isPlaying()) {
                            mp.start();
                            btPlay.setText("||");
                        }
                        mp.setVolume(1.0f, 1.0f);
                    }
                    break;
                }
            }
        }
    };

    Thread updateSeekBar = new Thread() {
        @Override
        public void run() {
            int totalDuration = mp.getDuration();
            int currentPosition = 0;
            while (currentPosition < totalDuration)
                try {
                    sleep(500);
                    currentPosition = mp.getCurrentPosition();
                    sb.setProgress(currentPosition);
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
        position = intent.getIntExtra("pos", 0);

        String[] projection = {
                MediosEntry._ID,
                MediosEntry.COLUMN_MUSIC_TITLE,
                MediosEntry.COLUMN_MUSIC_PATH
        };

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
                    result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mp.start();
                    }
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
