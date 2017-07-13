package com.example.android.mediatest;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.mediatest.data.MediosContract.MediosEntry;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MEDIOS_LOADER =0;
    MediosCursorAdapter mediosCursorAdapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    String[] songName, artist;
    MediaMetadataRetriever mediaMetadataRetriever;
    String[] absolutePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            //if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //} else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //}
        }


        final ArrayList<File> mySongs = findSongs(Environment.getExternalStorageDirectory());
        songName = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            songName[i] = mySongs.get(i).getName().replace(".mp3", "").replace(".m4a","").replace(".wav","");
        }

        absolutePath = new String[mySongs.size()];
        for(int i =0; i < mySongs.size(); i++)
        {
            absolutePath[i] = mySongs.get(i).getAbsolutePath();
        }


        artist = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(mySongs.get(i).getPath());
            // Get the artist name
            artist[i] = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }

        ListView mediosListView = (ListView) findViewById(R.id.list);

        mediosCursorAdapter = new MediosCursorAdapter(this, null);
        mediosListView.setAdapter(mediosCursorAdapter);

        mediosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Player.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the {@link MediosEntry#CONTENT_URI}.
                Uri currentSongUri = ContentUris.withAppendedId(MediosEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentSongUri);

                //Launch the Player activity
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(MEDIOS_LOADER, null, this);

        insertMedia();
    }


    public ArrayList<File> findSongs(File root) {
        ArrayList<File> al = new ArrayList<>();
        File[] files = root.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3")||singleFile.getName().endsWith(".m4a")||singleFile.getName().endsWith(".wav")) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }

    private void insertMedia() {
        // Create a ContentValues object where column names are the keys, and attributes are the values.
        for (int i = 0; i < songName.length; i++) {
            ContentValues values = new ContentValues();
            values.put(MediosEntry.COLUMN_MUSIC_TITLE, songName[i]);
            values.put(MediosEntry.COLUMN_MUSIC_ARTIST, artist[i]);
            values.put(MediosEntry.COLUMN_MUSIC_PATH, absolutePath[i]);

            // Insert a new row into the provider using the ContentResolver.
            Uri newUri = getContentResolver().insert(MediosEntry.CONTENT_URI, values);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MediosEntry._ID,
                MediosEntry.COLUMN_MUSIC_TITLE,
                MediosEntry.COLUMN_MUSIC_PATH,
                MediosEntry.COLUMN_MUSIC_ARTIST
        };

        return new CursorLoader(this,
                MediosEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mediosCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mediosCursorAdapter.swapCursor(null);
    }
}
