package com.tobioyelekan.music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ListSong extends AppCompatActivity implements View.OnClickListener {

    String mode, mode2;
    RecyclerView recycler;
    Storage storage;
    private MyMusicPlayerService player;
    RVAdapter rvAdapter;
    TextView empty;
    boolean bound = false;
    String[] data;
    CardView playinfo;
    ImageView prev, playpause, fwd, img;
    TextView title, artist;
    RecyclerView.LayoutManager layoutManager;
    public ArrayList<ArrayList<String>> musics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(Constants.ACTION.PLAY_INFO));

        storage = new Storage(this);
        playinfo = (CardView) findViewById(R.id.playinfo);
        prev = (ImageView) findViewById(R.id.prev);
        playpause = (ImageView) findViewById(R.id.playpause);
        fwd = (ImageView) findViewById(R.id.fwd);

        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);

        playinfo.setOnClickListener(this);
        prev.setOnClickListener(this);
        playpause.setOnClickListener(this);
        fwd.setOnClickListener(this);

        layoutManager = new LinearLayoutManager(this);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(layoutManager);
        empty = (TextView) findViewById(R.id.empty);

        if (getIntent() != null) {
            data = getIntent().getStringArrayExtra("data");
            rvAdapter = new RVAdapter(musics, this, data[1]);

            mode = data[0];

            if (data[1].equals("<unknown>")) getSupportActionBar().setTitle("Unknown artist");
            else getSupportActionBar().setTitle(data[1]);

            mode2 = data[1];

            if (data[0].equals("playlist")) getPlaylistMembers(Long.parseLong(data[2]));
            else listSong(data[0], data[2]);
        }

        rvAdapter.setOnItemClickListener(new RVAdapter.OnRvItemClickListener() {
            @Override
            public void onRvItemClickListener(ArrayList<String> data, int position) {
//                Toast.makeText(ListSong.this, "music id: " + data.get(4), Toast.LENGTH_SHORT).show();

                storage.storeSongs(musics);
                Intent intent = new Intent(Constants.ACTION.PLAY_INFO);
                intent.putExtra("title", data.get(0));
                intent.putExtra("artist", data.get(1));
                intent.putExtra("playStatus", "play");

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                storage.storeAudioIndex(position);
                Intent intent2 = new Intent(getBaseContext(), MyMusicPlayerService.class);
                intent2.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startService(intent2);

                updateBottom();
            }
        });

        rvAdapter.setOnOptionClickListener(new RVAdapter.OnOptionClickListener() {
            @Override
            public void onOptionClick(String mode, ArrayList<String> data) {
                processOption(mode, data);
            }
        });

        rvAdapter.setOnItemChangeListener(new RVAdapter.OnItemChangeListener() {
            @Override
            public void onItemChangeListener(int size) {
                if (size < 1) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            }
        });

        recycler.setAdapter(rvAdapter);

        Intent service = new Intent(this, MyMusicPlayerService.class);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playinfo:
                Intent current = new Intent(ListSong.this, CurrentlyPlaying.class);
                startActivity(current);
                break;
            case R.id.prev:
                player.processPrev();
                break;
            case R.id.playpause:
                player.processPlayPause();
                break;
            case R.id.fwd:
                player.processNext();
                break;
        }
    }

    public void updateBottom() {
        title.setText(storage.getSongs().get(storage.loadAudioIndex()).get(0));
        artist.setText(storage.getSongs().get(storage.loadAudioIndex()).get(1));
        playpause.setImageResource(R.drawable.pause);
        playinfo.setVisibility(View.VISIBLE);
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            title.setText(intent.getStringExtra("title"));
            artist.setText(intent.getStringExtra("artist"));
            String status = intent.getStringExtra("playStatus");
            if (status.equals("play")) playpause.setImageResource(R.drawable.pause);
            else playpause.setImageResource(R.drawable.play);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        if (mode2.equals("Recently added") || mode2.equals("Last Added") || mode2.equals("Most PLayed") || mode.equals("artist") || mode.equals("album")) {
            menu.findItem(R.id.add).setVisible(false);
        } else {
            menu.findItem(R.id.add).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void processOption(String mode, ArrayList<String> data) {
        switch (mode) {
            case "add":
                Intent addto = new Intent(ListSong.this, AddTo.class);
                addto.putStringArrayListExtra("musicData", data);
                startActivity(addto);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case "set":
                break;
            case "share":
                break;
            case "info":
                Intent intent = new Intent(ListSong.this, Info.class);
                intent.putStringArrayListExtra("data", data);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
//                super.onBackPressed();@}

                return true;
            case R.id.add:
                Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void listSong(String mode, String id) {
        musics.clear();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = null;
        if (mode.equals("album"))
            selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
        else if (mode.equals("artist"))
            selection = MediaStore.Audio.Media.ARTIST_ID + "=?";

        Cursor musicCursor = contentResolver.query(uri, null, selection,
                new String[]{id}, "LOWER(" + MediaStore.Audio.Media.TITLE + ")ASC");

        if (musicCursor != null) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int music_id = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
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
                tempmusic.add(4, musicCursor.getString(music_id));
                tempmusic.add(5, musicCursor.getString(data));
                tempmusic.add(6, musicCursor.getString(albumid));
                tempmusic.add(7, musicCursor.getString(year));
                tempmusic.add(8, musicCursor.getString(album));

                musics.add(tempmusic);
            }
            musicCursor.close();
        }
        rvAdapter.notifyDataSetChanged();
    }

    public void getPlaylistMembers(long playlistId) {
        musics.clear();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        ContentResolver contentResolver = getContentResolver();
        Cursor musicCursor = contentResolver.query(uri, null, null, null, null);

        if (musicCursor != null) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
            int artist = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
            int music_id = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            int duration = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION);
            int size = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.SIZE);
            int data = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA);
            int albumid = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);
            int year = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.YEAR);
            int album = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM);

            while (musicCursor.moveToNext()) {
                ArrayList<String> tempmusic = new ArrayList<>();
                tempmusic.add(0, musicCursor.getString(titleColumn));
                tempmusic.add(1, musicCursor.getString(artist));
                long time = Integer.parseInt(musicCursor.getString(duration));
                tempmusic.add(2, (new SimpleDateFormat("mm:ss", Locale.getDefault())).format(new Date(time)));
                tempmusic.add(3, musicCursor.getString(size));
                tempmusic.add(4, musicCursor.getString(music_id));
                tempmusic.add(5, musicCursor.getString(data));
                tempmusic.add(6, musicCursor.getString(albumid));
                tempmusic.add(7, musicCursor.getString(year));
                tempmusic.add(8, musicCursor.getString(album));

                musics.add(tempmusic);
            }
            musicCursor.close();
        } else {
            Toast.makeText(this, "empty nigga", Toast.LENGTH_SHORT).show();
        }

        if (musics.size() < 1) {
            empty.setVisibility(View.VISIBLE);
        }
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (storage.getStatus().equals("play")) playpause.setImageResource(R.drawable.pause);
        else playpause.setImageResource(R.drawable.play);

        if (data[0].equals("playlist")) getPlaylistMembers(Long.parseLong(data[2]));
        else listSong(data[0], data[2]);

        if (TextUtils.isEmpty(new Storage(this).getLastArtist()) || TextUtils.isEmpty(new Storage(this).getLastTitle())) {
            playinfo.setVisibility(View.GONE);
        } else {
            playinfo.setVisibility(View.VISIBLE);
            title.setText(new Storage(this).getLastTitle());
            artist.setText(new Storage(this).getLastArtist());
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicPlayerService.LocalBinder binder = (MyMusicPlayerService.LocalBinder) service;
            player = binder.getService();
            bound = true;

            Toast.makeText(ListSong.this, "Service Bound2", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", bound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        if (bound) {
            unbindService(serviceConnection);
        }
    }
}
