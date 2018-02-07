package com.tobioyelekan.music;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MyMusic extends Fragment {
    private OnFragmentInteractionListener mListener;
    Storage storage;
    ContentResolver musicResolver;
    RecyclerView recycler;
    RVAdapter rvAdapter;
    RecyclerView.LayoutManager layoutManager;
    public ArrayList<ArrayList<String>> musics = new ArrayList<>();

    public MyMusic() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_my_music, container, false);
        storage = new Storage(getActivity());
        rvAdapter = new RVAdapter(musics, getActivity(), "all");
        layoutManager = new LinearLayoutManager(getActivity());
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        recycler.setLayoutManager(layoutManager);

        rvAdapter.setOnItemClickListener(new RVAdapter.OnRvItemClickListener() {
            @Override
            public void onRvItemClickListener(ArrayList<String> data, int position) {
                mListener.onFragmentInteraction(position);
//                Snackbar.make(getActivity().findViewById(R.id.main_content), "music id: " + data.get(4), Snackbar.LENGTH_LONG).show();
            }
        });

        rvAdapter.setOnOptionClickListener(new RVAdapter.OnOptionClickListener() {
            @Override
            public void onOptionClick(String mode, ArrayList<String> data) {
                processOption(mode, data);
            }
        });

        populate();

        recycler.setAdapter(rvAdapter);

        return view;
    }

    public void processOption(String mode, ArrayList<String> data) {
        switch (mode) {
            case "add":
                Intent addto = new Intent(getActivity(), AddTo.class);
                addto.putStringArrayListExtra("musicData", data);
                startActivity(addto);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case "set":
                break;
            case "share":
                break;
            case "info":
                Intent intent = new Intent(getActivity(), Info.class);
                intent.putStringArrayListExtra("data", data);
                getActivity().startActivity(intent);
                break;
        }
    }

    public void populate() {
        musics.clear();
        musicResolver = getActivity().getContentResolver();
        Uri musicuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicuri, null, null, null, "LOWER(" + MediaStore.Audio.Media.TITLE + ")ASC");
        if (musicCursor != null) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int id = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int duration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int size = musicCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int data = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int year = musicCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
            int album = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            while (musicCursor.moveToNext()) {
                ArrayList<String> tempmusic = new ArrayList<>();
                tempmusic.add(0, musicCursor.getString(titleColumn));
                tempmusic.add(1, musicCursor.getString(artist));
                long time = Integer.parseInt(musicCursor.getString(duration));
                tempmusic.add(2, (new SimpleDateFormat("mm:ss", Locale.getDefault())).format(new Date(time)));
                tempmusic.add(3, musicCursor.getString(size));
                tempmusic.add(4, musicCursor.getString(id));
                tempmusic.add(5, musicCursor.getString(data));
                tempmusic.add(6, musicCursor.getString(albumid));
                tempmusic.add(7, musicCursor.getString(year));
                tempmusic.add(8, musicCursor.getString(album));

                musics.add(tempmusic);
            }
            musicCursor.close();
            storage.storeSongs(musics);
        }

        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rvAdapter != null) {
            populate();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int position);
    }
}
