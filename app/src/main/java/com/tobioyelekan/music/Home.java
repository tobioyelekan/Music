package com.tobioyelekan.music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity implements MyMusic.OnFragmentInteractionListener, View.OnClickListener {
    ContentResolver musicResolver;
    private MyMusicPlayerService player;
    boolean bound = false;
    Storage storage;
    ViewPager mViewPager;
    CardView playinfo;
    ImageView prev, playpause, fwd, img;
    TextView title, artist;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.tobioyelekan.music.PlayNewAudio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, new IntentFilter(Constants.ACTION.PLAY_INFO));
        storage = new Storage(Home.this);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent service = new Intent(this, MyMusicPlayerService.class);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            moveTaskToBack(true);
        } else {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onFragmentInteraction(int position) {
        playAudio(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playinfo:
                Intent current = new Intent(Home.this, CurrentlyPlaying.class);
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    fragment = new MyMusic();
                    break;
                case 1:
                    fragment = new PlayList();
                    break;
                case 2:
                    fragment = new Albums();
                    break;
                case 3:
                    fragment = new Artist();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.title1);
                case 1:
                    return getResources().getString(R.string.title2);
                case 2:
                    return getResources().getString(R.string.title3);
                case 3:
                    return getResources().getString(R.string.title4);
            }
            return null;
        }
    }

    public void playAudio(int audioIndex) {
//        if (!bound) {
//            Toast.makeText(getApplicationContext(), "doing bound", Toast.LENGTH_LONG).show();
//            storage.storeAudioIndex(audioIndex);
//            Intent intent = new Intent(this, MyMusicPlayerService.class);
//            startService(intent);
//            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//        } else {
//            //        service is active, send media with broadcast receiver
//            storage.storeAudioIndex(audioIndex);
//
//            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
//            sendBroadcast(broadcastIntent);
//        }

        if (bound) {
            storage.storeAudioIndex(audioIndex);
            Intent intent = new Intent(getBaseContext(), MyMusicPlayerService.class);
            intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(intent);
//            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
//            sendBroadcast(broadcastIntent);
        }

        updateBottom();
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", bound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bound = savedInstanceState.getBoolean("ServiceState");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicPlayerService.LocalBinder binder = (MyMusicPlayerService.LocalBinder) service;
            player = binder.getService();
            bound = true;

            Toast.makeText(Home.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(new Storage(this).getLastArtist()) || TextUtils.isEmpty(new Storage(this).getLastTitle())) {
            playinfo.setVisibility(View.GONE);
        } else {
            playinfo.setVisibility(View.VISIBLE);
            title.setText(new Storage(this).getLastTitle());
            artist.setText(new Storage(this).getLastArtist());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
        if (bound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }
}
