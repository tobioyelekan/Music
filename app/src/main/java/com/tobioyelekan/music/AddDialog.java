package com.tobioyelekan.music;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 23/12/2017.
 */

public class AddDialog extends DialogFragment {
    ArrayList<String> musicData = new ArrayList<>();
    ArrayList<ArrayList<String>> playlist = new ArrayList<>();
    LinearLayoutManager layoutManager;
    ProgressBar progress;
    ContentResolver musicResolver;
    RecyclerView recycler;
    AddRecycler addRecycler;

    static AddDialog newInstance(ArrayList<String> musicData) {
        AddDialog dialog = new AddDialog();

        Bundle args = new Bundle();
        args.putStringArrayList("musicData", musicData);
        dialog.setArguments(args);

        return dialog;
    }

    public AddDialog(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicData = getArguments().getStringArrayList("musicData");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.addtolayout, container, false);

        layoutManager = new LinearLayoutManager(getActivity());
        recycler = (RecyclerView) v.findViewById(R.id.recycler);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        addRecycler = new AddRecycler(playlist);
        recycler.setAdapter(addRecycler);

        addRecycler.setOnItemClickListener(new AddRecycler.OnClickListener() {
            @Override
            public void onClick(ArrayList<String> data) {
                String name = data.get(0);
                if (name.equalsIgnoreCase("Last added") || name.equalsIgnoreCase("recently added") ||
                        name.equalsIgnoreCase("most played")) {
                    Toast.makeText(getActivity(), "cannot add to " + name, Toast.LENGTH_SHORT).show();
                } else {
                    performAdd(data.get(1), data.get(0));
                }
            }
        });

        getDatas();

        return v;
    }

    private void performAdd(String playlist_id, String playlist_name) {
        if (exist(musicData.get(4), playlist_id)) {
            Toast.makeText(getActivity(), "music already exist in " + playlist_name, Toast.LENGTH_SHORT).show();
        } else {
            addToPlaylist(Integer.parseInt(musicData.get(4)), playlist_id, playlist_name);
        }
    }

    private void addToPlaylist(int audio_id, String playlist_id, String playlist_name) {
        ContentResolver resolver = getActivity().getContentResolver();
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

            this.dismiss();
            Toast.makeText(getActivity(), "added to " + playlist_name, Toast.LENGTH_SHORT).show();

        }
    }

    private boolean exist(String audio_id, String playlist_id) {
        boolean exist = false;
        ContentResolver contentResolver = getActivity().getContentResolver();
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
        musicResolver = getActivity().getContentResolver();
        Uri musicuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicuri, null, null, null, "LOWER(" + MediaStore.Audio.Playlists.NAME + ")ASC");
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
