package com.tobioyelekan.music;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class CurrentlyPlaying extends AppCompatActivity implements View.OnClickListener {

    TextView title, artist, time1, time2;
    ImageView albumart, prev, playpause, next, shuffle, repeat, fav, list, more;
    SeekBar seek;
    Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.playing);

        storage = new Storage(this);
        seek = (SeekBar) findViewById(R.id.seek);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);
        time1 = (TextView) findViewById(R.id.time1);
        time2 = (TextView) findViewById(R.id.time2);

        albumart = (ImageView) findViewById(R.id.albumart);
        prev = (ImageView) findViewById(R.id.prev);
        playpause = (ImageView) findViewById(R.id.playpause);
        next = (ImageView) findViewById(R.id.next);
        shuffle = (ImageView) findViewById(R.id.shuffle);
        repeat = (ImageView) findViewById(R.id.repeat);
        fav = (ImageView) findViewById(R.id.fav);
        list = (ImageView) findViewById(R.id.list);
        more = (ImageView) findViewById(R.id.more);

        prev.setOnClickListener(this);
        playpause.setOnClickListener(this);
        next.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        repeat.setOnClickListener(this);
        fav.setOnClickListener(this);
        list.setOnClickListener(this);
        more.setOnClickListener(this);

        setUp();
    }

    public void setUp() {
        title.setText(storage.getLastTitle());
        artist.setText(storage.getLastArtist());
        if (storage.getStatus().equals("play")) playpause.setImageResource(R.drawable.pause2);
        else playpause.setImageResource(R.drawable.play2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.prev:
                break;
            case R.id.playpause:
                break;
            case R.id.next:
                break;
            case R.id.shuffle:
                break;
            case R.id.repeat:
                break;
            case R.id.fav:
                break;
            case R.id.list:
                break;
            case R.id.more:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        title.setText(storage.getLastTitle());
        artist.setText(storage.getLastArtist());
        if (storage.getStatus().equals("play")) playpause.setImageResource(R.drawable.pause);
        else playpause.setImageResource(R.drawable.play);

    }
}
