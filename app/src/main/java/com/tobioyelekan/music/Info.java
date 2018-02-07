package com.tobioyelekan.music;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Info extends AppCompatActivity {

    CircleImageView img;
    TextView title, artist, album, year, duration, size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);
        album = (TextView) findViewById(R.id.album);
        year = (TextView) findViewById(R.id.year);
        duration = (TextView) findViewById(R.id.duration);
        size = (TextView) findViewById(R.id.size);
        img = (CircleImageView) findViewById(R.id.img);


        setData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData() {

        ArrayList<String> data = getIntent().getStringArrayListExtra("data");
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?", new String[]{data.get(6)}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int albumart = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            String artpath = cursor.getString(albumart);
            Picasso.with(this).load("file://" + artpath).placeholder(R.mipmap.musiclogo).error(R.mipmap.musiclogo).noFade().into(img);

            cursor.close();
        }

        title.setText(data.get(0));
        if (data.get(1).equals("<unknown>"))
            artist.setText("Unknwon artist");
        else
            artist.setText(data.get(1));
        album.setText(data.get(8));
        year.setText(data.get(7));
        duration.setText(data.get(2));

        double res = Double.parseDouble(data.get(3)) * 0.00000095367432;
        String estsize = String.format(Locale.getDefault(), "%.1f", res);

        size.setText(estsize + "mb");

    }

}
