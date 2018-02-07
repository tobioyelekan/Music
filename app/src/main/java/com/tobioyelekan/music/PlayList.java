package com.tobioyelekan.music;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayList extends Fragment {
    ContentResolver musicResolver;
    RecyclerView recycler;
    Dialog dialog;
    Button create, cancel;
    EditText name;
    TextView dialogtitle;
    String modes;
    long idrename;
    PlayListAdapter playlistAdapter;
    RecyclerView.LayoutManager layoutManager;
    public ArrayList<ArrayList<String>> musics = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public PlayList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        playlistAdapter = new PlayListAdapter(musics, getActivity());
        layoutManager = new LinearLayoutManager(getActivity());
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        recycler.setLayoutManager(layoutManager);

        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.createplaylist);
        create = (Button) dialog.findViewById(R.id.create);
        cancel = (Button) dialog.findViewById(R.id.cancel);
        name = (EditText) dialog.findViewById(R.id.name);
        name = (EditText) dialog.findViewById(R.id.name);
        dialogtitle = (TextView) dialog.findViewById(R.id.dialogtitle);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    if (s.toString().equalsIgnoreCase("Recently added") ||
                            s.toString().equalsIgnoreCase("Last added") ||
                            s.toString().equalsIgnoreCase("Favorites")) {
                        create.setEnabled(false);
                        create.setBackgroundResource(R.drawable.disable);
                    } else if (getPlaylist(s.toString()) == -1) {
                        create.setEnabled(true);
                        create.setBackgroundResource(R.drawable.btnback);
                        if (modes.equals("rename")) create.setText("rename");
                        else create.setText("create");
                    } else {
                        create.setEnabled(true);
                        create.setBackgroundResource(R.drawable.btnback);
                        create.setEnabled(true);
                        create.setText("overwrite");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (create.getText().equals("create")) {
                    createPlayList("create", name.getText().toString(), -1);
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "created!", Toast.LENGTH_SHORT).show();
                    populate();
                } else if (create.getText().equals("overwrite")) {
                    if (modes.equals("create"))
                        createPlayList("overwrite", name.getText().toString(), getPlaylist(name.getText().toString()));
                    else
                        renamePlayList("overwrite", name.getText().toString(), getPlaylist(name.getText().toString()));
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "overwritten!", Toast.LENGTH_SHORT).show();
                    populate();
                } else if (create.getText().equals("rename")) {
                    renamePlayList("rename", name.getText().toString(), idrename);
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "renamed!", Toast.LENGTH_SHORT).show();
                    populate();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        playlistAdapter.setOnClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onClick(ArrayList<String> data, int position) {
                if (position > 0) {
                    callListSong("playlist", data.get(0), data.get(2));
                } else {
                    modes = "create";
                    create.setText(modes);
                    dialogtitle.setText("Create Playlist");
                    name.setText("");
                    dialog.show();
                }
            }
        });

        playlistAdapter.setOnOptionsClickListener(new PlayListAdapter.OnOptionsClickListener() {
            @Override
            public void onOptionClick(String mode, int position, ArrayList<String> data) {
                modes = "rename";
                idrename = Long.parseLong(data.get(2));
                dialogtitle.setText("Rename '" + data.get(0) + "' to");
                name.setText("");
                create.setText(modes);
                dialog.show();
            }
        });

        populate();

        recycler.setAdapter(playlistAdapter);

        return view;
    }

    public void callListSong(String mode, String displayName, String playlist_id) {
        Intent intent = new Intent(getActivity(), ListSong.class);
        intent.putExtra("data", new String[]{mode, displayName, playlist_id});
        startActivity(intent);
    }

    public void populate() {
//        Toast.makeText(getActivity(), "i was here", Toast.LENGTH_SHORT).show();
        musics.clear();
        ArrayList<String> dat = new ArrayList<>();
        dat.add("New Playlist");
        dat.add("");
        dat.add("");

        musics.add(dat);
        musicResolver = getActivity().getContentResolver();
        Uri musicuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicuri, null, null, null, MediaStore.Audio.Playlists.DATE_ADDED);
        if (musicCursor != null) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int id = musicCursor.getColumnIndex(MediaStore.Audio.Playlists._ID);

            while (musicCursor.moveToNext()) {
                ArrayList<String> tempmusic = new ArrayList<>();
                tempmusic.add(0, musicCursor.getString(titleColumn));
                tempmusic.add(1, getNoSongs(Long.parseLong(musicCursor.getString(id))));
                tempmusic.add(2, musicCursor.getString(id));

                musics.add(tempmusic);
            }
            musicCursor.close();
        }
        playlistAdapter.notifyDataSetChanged();
    }

    public String getNoSongs(long id) {
        String no = "No songs";
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToNext())
                no = Integer.toString(cursor.getCount());
            cursor.close();
        }

        return no;
    }

    public long createPlayList(String mode, String name, long id) {
        long listid = -1;
        ContentResolver contentResolver = getActivity().getContentResolver();
        switch (mode) {
            case "create":
                //create new playlist
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);
                Uri uri = contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                listid = Long.parseLong(uri.getLastPathSegment());
                break;
            case "overwrite":
                Uri uri2 = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
                contentResolver.delete(uri2, null, null);
                break;
        }
        return listid;
    }

    public void renamePlayList(String mode, String name, long id) {

        if (mode.equals("overwrite")) {
            deletePlaylist(id);
            createPlayList("create", name, -1);
        } else {
            ContentResolver contentResolver = getActivity().getContentResolver();
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Playlists.NAME, name);
            contentResolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
        }

    }

    public void deletePlaylist(long id) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
        contentResolver.delete(uri, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playlistAdapter != null) {
            populate();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    public long getPlaylist(String name) {
        long id = -1;
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?", new String[]{name}, null);

        if (cursor != null) {
            if (cursor.moveToNext())
                id = cursor.getLong(0);
            cursor.close();
        }

        return id;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
