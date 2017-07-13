package com.example.android.mediatest;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.mediatest.data.MediosContract.*;

/**
 * Created by Arijit on 12-07-2017.
 */

public class MediosCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link MediosCursorAdapter}.
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public MediosCursorAdapter(Context context, Cursor c) {
       super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // Inflate a list item view using the layout specified in songs_layout.xml
        return LayoutInflater.from(context).inflate(R.layout.songs_layout, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView songTextView = (TextView)view.findViewById(R.id.songName);
        TextView artistTextView = (TextView)view.findViewById(R.id.artistName);

        int songNameColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_TITLE);
        int artistNameColumnIndex = cursor.getColumnIndex(MediosEntry.COLUMN_MUSIC_ARTIST);

        String songName = cursor.getString(songNameColumnIndex);
        String artistName = cursor.getString(artistNameColumnIndex);

        if (TextUtils.isEmpty(artistName)) {
            artistName = context.getString(R.string.unknown_artist);
        }

        songTextView.setText(songName);
        artistTextView.setText(artistName);
    }
}
