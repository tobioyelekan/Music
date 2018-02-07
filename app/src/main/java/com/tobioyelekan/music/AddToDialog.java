package com.tobioyelekan.music;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 22/12/2017.
 */

public class AddToDialog {

    Dialog dialog;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    AddRecycler addRecycler;
    ContentResolver musicResolver;
    Context context;
    ArrayList<ArrayList<String>> playlist;
    ProgressBar progress;
    ArrayList<String> musicData = new ArrayList<>();

    public AddToDialog(){

    }

    public AddToDialog(final Context context, ArrayList<String> musicData) {

        playlist = new ArrayList<>();
        this.context = context;
        this.musicData = musicData;
        dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.addtolayout);
        layoutManager = new LinearLayoutManager(this.context);
        recyclerView = (RecyclerView) dialog.findViewById(R.id.recycler);
        progress = (ProgressBar) dialog.findViewById(R.id.progress);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        addRecycler = new AddRecycler(playlist);
        recyclerView.setAdapter(addRecycler);
        addRecycler.setOnItemClickListener(new AddRecycler.OnClickListener() {
            @Override
            public void onClick(ArrayList<String> data) {
                String name = data.get(0);
                if (name.equalsIgnoreCase("Last added") || name.equalsIgnoreCase("recently added") ||
                        name.equalsIgnoreCase("most played")) {
                    Toast.makeText(context, "cannot add to " + name, Toast.LENGTH_SHORT).show();
                } else {
                    performAdd(data.get(1), data.get(0));
                }
            }
        });

        getDatas();

    }

    public void showDialog() {
//        if (context instanceof FragmentActivity) {
//            if (!((FragmentActivity) context).isFinishing()) {
//                dialog.show();
//            }else{
//
//            }
//        } else {
//            dialog.show();
//        }

        dialog.show();

    }

    public void setContext(Context context){
        this.context = context;
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    private void performAdd(String playlist_id, String playlist_name) {
        if (exist(musicData.get(4), playlist_id)) {
            Toast.makeText(context, "music already exist in " + playlist_name, Toast.LENGTH_SHORT).show();
        } else {
            addToPlaylist(Integer.parseInt(musicData.get(4)), playlist_id, playlist_name);
        }
    }

    private void addToPlaylist(int audio_id, String playlist_id, String playlist_name) {
        ContentResolver resolver = context.getContentResolver();
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

            dismissDialog();
            Toast.makeText(context, "added to " + playlist_name, Toast.LENGTH_SHORT).show();

        }
    }

    private boolean exist(String audio_id, String playlist_id) {
        boolean exist = false;
        ContentResolver contentResolver = context.getContentResolver();
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
        musicResolver = context.getContentResolver();
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
