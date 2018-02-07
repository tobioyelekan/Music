package com.tobioyelekan.music;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class AddTo extends AppCompatActivity {

    ArrayList<String> musicData = new ArrayList<>();
    ArrayList<ArrayList<String>> playlist = new ArrayList<>();
    LinearLayoutManager layoutManager;
    ProgressBar progress;
    ContentResolver musicResolver;
    RecyclerView recycler;
    AddRecycler addRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutManager = new LinearLayoutManager(this);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        progress = (ProgressBar) findViewById(R.id.progress);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        addRecycler = new AddRecycler(playlist);
        recycler.setAdapter(addRecycler);

        if (getIntent() != null) {
            musicData = getIntent().getStringArrayListExtra("musicData");
        }

        addRecycler.setOnItemClickListener(new AddRecycler.OnClickListener() {
            @Override
            public void onClick(ArrayList<String> data) {
                String name = data.get(0);
                if (name.equalsIgnoreCase("Last added") || name.equalsIgnoreCase("recently added") ||
                        name.equalsIgnoreCase("most played")) {
                    Toast.makeText(AddTo.this, "cannot add to " + name, Toast.LENGTH_SHORT).show();
                } else {
                    performAdd(data.get(1), data.get(0));
                }
            }
        });

        getDatas();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void performAdd(String playlist_id, String playlist_name) {
        if (exist(musicData.get(4), playlist_id)) {
            Toast.makeText(this, "music already exist in " + playlist_name, Toast.LENGTH_SHORT).show();
        } else {
            addToPlaylist(Integer.parseInt(musicData.get(4)), playlist_id, playlist_name);
        }
    }

    private void addToPlaylist(int audio_id, String playlist_id, String playlist_name) {
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(playlist_id));
        Cursor cursor = resolver.query(uri, new String[]{MediaStore.Audio.Playlists.Members.PLAY_ORDER}, null, null, null);
        int base = 0;
        if (cursor != null) {
            if (cursor.moveToLast())
                base = cursor.getInt(0);
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + 1);
            contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio_id);
            resolver.insert(uri, contentValues);

            Toast.makeText(this, "added to " + playlist_name, Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        }
    }

    private boolean exist(String audio_id, String playlist_id) {
        boolean exist = false;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(playlist_id));
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Playlists.Members.DISPLAY_NAME},
                MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{audio_id}, null);

        if (cursor != null) {
            exist = cursor.getCount() > 0;
            cursor.close();
        }

        return exist;
    }

    private void getDatas() {
        musicResolver = getContentResolver();
        Uri musicuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicuri, null, null, null, MediaStore.Audio.Playlists.DATE_ADDED);
        if (musicCursor != null) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int id = musicCursor.getColumnIndex(MediaStore.Audio.Playlists._ID);

            while (musicCursor.moveToNext()) {
                ArrayList<String> tempmusic = new ArrayList<>();
                tempmusic.add(0, musicCursor.getString(titleColumn));
                tempmusic.add(1, musicCursor.getString(id));

                playlist.add(tempmusic);
            }
            musicCursor.close();
        }
        addRecycler.notifyDataSetChanged();
    }


}
