package com.tobioyelekan.music;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Artist extends Fragment implements FinishListener {
    ContentResolver musicResolver;
    RecyclerView recycler;
    ArtistAdapter artistAdapter;
    RecyclerView.LayoutManager layoutManager;
    public ArrayList<ArrayList<String>> musics = new ArrayList<>();
    ArrayList<String> artistData = new ArrayList<>();
    private OnFragmentInteractionListener mListener;

    public Artist() {
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

        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        artistAdapter = new ArtistAdapter(musics, getActivity());
        layoutManager = new LinearLayoutManager(getActivity());
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        recycler.setLayoutManager(layoutManager);
        artistAdapter.setOnItemClickListener(new ArtistAdapter.OnRvItemClickListener() {
            @Override
            public void onRvItemClickListener(ArrayList<String> data) {
                callListSong("artist", data.get(0), data.get(2));
            }
        });

        artistAdapter.setOnOptionsClickListener(new ArtistAdapter.OnOptionsClickListener() {
            @Override
            public void onOptionClick(String mode, ArrayList<String> data, int position) {
//                artistData = data;
                switch (mode) {
                    case "play":
                        break;
                    case "share":
                        break;
                    case "delete":
                        new Artist.MyTask(getActivity(), musicResolver, Artist.this).execute(data.get(2), Integer.toString(position));
                        break;
                }
            }
        });

        populate();

        recycler.setAdapter(artistAdapter);

        return view;
    }

    public void callListSong(String mode, String displayName, String artist_id) {
        Intent intent = new Intent(getActivity(), ListSong.class);
        intent.putExtra("data", new String[]{mode, displayName, artist_id});
        startActivity(intent);
    }

    public void populate() {
//        Toast.makeText(getActivity(), "i was here", Toast.LENGTH_SHORT).show();
        musics.clear();
        musicResolver = getActivity().getContentResolver();
        Uri musicuri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicuri, null, null, null, "LOWER(" + MediaStore.Audio.Artists.ARTIST + ")ASC");
        if (musicCursor != null) {
            int artist = musicCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            int nosong = musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            int id = musicCursor.getColumnIndex(MediaStore.Audio.Artists._ID);

            while (musicCursor.moveToNext()) {
                ArrayList<String> tempmusic = new ArrayList<>();
                tempmusic.add(0, musicCursor.getString(artist));
                tempmusic.add(1, musicCursor.getString(nosong));
                tempmusic.add(2, musicCursor.getString(id));
                tempmusic.add(3, getAlbumArt(artist));
                musics.add(tempmusic);
            }
            musicCursor.close();
        }
//        Toast.makeText(getActivity(), Integer.toString(musics.size()), Toast.LENGTH_SHORT).show();
        artistAdapter.notifyDataSetChanged();
    }

    public String getAlbumArt(int artist) {
        String art = "";
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums.ARTIST + "=?", new String[]{String.valueOf(artist)}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int albumart = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            art = cursor.getString(albumart);
            cursor.close();
        }
        return art;
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

    @Override
    public void onResume() {
        super.onResume();
        if (artistAdapter != null) {
            populate();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void processFinish(String status, int position) {
        artistAdapter.notifyItemRemoved(position);
        populate();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static class MyTask extends AsyncTask<String, Void, String> {
        private Context context;
        private FinishListener finish = null;
        private ContentResolver contentResolver;
        private ProgressDialog progressDialog;
        private ArrayList<String> ids = new ArrayList<>();
        private int position;

        public MyTask(Context context, ContentResolver contentResolver, FinishListener finish) {
            this.context = context;
            this.contentResolver = contentResolver;
            this.finish = finish;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Deleting...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {

            String artist_id = strings[0];
            position = Integer.parseInt(strings[1]);

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, MediaStore.Audio.Media.ARTIST_ID + "=?",
                    new String[]{artist_id}, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    ids.add(cursor.getString(id));
                }

                cursor.close();
                int total = 0;

                for (int i = 0; i < ids.size(); i++) {
                    contentResolver.delete(uri, MediaStore.Audio.Media._ID + "=?", new String[]{ids.get(i)});
                    total++;
                }

                if (total == ids.size()) return "success";
                else return "fail";
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (s.equals("success")) {
                finish.processFinish(s, position);
            } else {
                Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
