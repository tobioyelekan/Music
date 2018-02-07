package com.tobioyelekan.music;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 03/01/2018.
 */

public class MusicService extends IntentService implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    private final IBinder iBinder = new MusicService.LocalBinder();
    RemoteViews views;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private MediaPlayer mediaPlayer;
    private int resumePosition;
    private AudioManager audioManager;
    private String mediaFile;
    private ArrayList<ArrayList<String>> audioList;
    private int audioIndex = -1;
    private ArrayList<String> activeAudio;

    public MusicService() {
        super("MusicService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
//        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            audioIndex = new Storage(getApplicationContext()).loadAudioIndex();
            audioList = new Storage(getApplicationContext()).getSongs();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
                Log.e("DATA", activeAudio.get(5));
            } else {
                stopSelf();
                stopForeground(true);
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();

            showNotification(activeAudio.get(0), activeAudio.get(1));
            handleIncomingActions(intent);
        } catch (NullPointerException e) {
            stopSelf();
            stopForeground(true);
        }

        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
            stopForeground(true);
        }
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(activeAudio.get(5)));
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    public void handleIncomingActions(Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        switch (intent.getAction()) {
            case Constants.ACTION.PREV_ACTION:
                Toast.makeText(getApplicationContext(), "prev", Toast.LENGTH_SHORT).show();
                break;
            case Constants.ACTION.PLAY_ACTION:
                Toast.makeText(getApplicationContext(), "play", Toast.LENGTH_SHORT).show();
                break;
            case Constants.ACTION.NEXT_ACTION:
                Toast.makeText(getApplicationContext(), "next", Toast.LENGTH_SHORT).show();
                break;
            case Constants.ACTION.STOPFOREGROUND_ACTION:
                Toast.makeText(getApplicationContext(), "closeup", Toast.LENGTH_SHORT).show();
                stopForeground(true);
                stopSelf();
                break;
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "done!", Toast.LENGTH_LONG).show();
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
//            buildNotification(com.tobioyelekan.music.PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //regsiter after gettting audio_focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

//    private void callStateListener() {
//        telephonyManager = (TelephonyManager) Context.getSystemService(Context.TELECOM_SERVICE);
//        phoneStateListener = new PhoneStateListener() {
//            @Override
//            public void onCallStateChanged(int state, String incomingNumber) {
//                switch (state) {
//                    case TelephonyManager.CALL_STATE_OFFHOOK:
//                    case TelephonyManager.CALL_STATE_RINGING:
//                        if (mediaPlayer != null) {
//                            pauseMedia();
//                            ongoingCall = true;
//                        }
//                        break;
//                    case TelephonyManager.CALL_STATE_IDLE:
//                        if (mediaPlayer != null) {
//                            if (ongoingCall) {
//                                ongoingCall = false;
//                                resumeMedia();
//                            }
//                        }
//                        break;
//                }
//                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
//                super.onCallStateChanged(state, incomingNumber);
//            }
//        };
//    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new Storage(getApplicationContext()).loadAudioIndex();
            audioList = new Storage(getApplicationContext()).getSongs();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
                Log.e("DATA", activeAudio.get(5));
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            //            updateMetaData();
//            buildNotification(com.tobioyelekan.music.PlaybackStatus.PLAYING)
        }
    };

    private void register_playNewAudio() {
        IntentFilter intentFilter = new IntentFilter(Home.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        //Disable the Phonelistener
//        if (phoneStateListener != null) {
//            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
//        }

        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        new Storage(getApplicationContext()).clearCache();
    }

    private void showNotification(String title, String artist) {
        views = new RemoteViews(getPackageName(), R.layout.bottom2);

        views.setTextViewText(R.id.title, title);
        views.setTextViewText(R.id.artist, artist);

        NotificationCompat.Builder notification;
        Intent notificationIntent = new Intent(getApplicationContext(), Home.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(getApplicationContext(), MusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent previntent = PendingIntent.getService(getApplicationContext(), 0, previousIntent, 0);

        Intent playpauseIntent = new Intent(getApplicationContext(), MusicService.class);
        playpauseIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent playpauseintent = PendingIntent.getService(getApplicationContext(), 0, playpauseIntent, 0);

        Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent nextintent = PendingIntent.getService(getApplicationContext(), 0, nextIntent, 0);

        Intent closeIntent = new Intent(getApplicationContext(), MusicService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent closeintent = PendingIntent.getService(getApplicationContext(), 0, closeIntent, 0);

        views.setOnClickPendingIntent(R.id.prev, previntent);
        views.setOnClickPendingIntent(R.id.playpause, playpauseintent);
        views.setOnClickPendingIntent(R.id.next, nextintent);
        views.setOnClickPendingIntent(R.id.close, closeintent);

        notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.logo)
                .setTicker("Music is playing")
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(views)
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build());
    }

}

