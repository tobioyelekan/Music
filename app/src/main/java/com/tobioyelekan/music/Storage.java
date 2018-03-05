package com.tobioyelekan.music;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 29/12/2017.
 */

public class Storage {

    Context context;
    private final String STORAGE = "com.tobioyelekan.music.STORAGE";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public static final String KEY_SONGS = "songs";
    public static final String KEY_STATUS = "status";
    public static final String KEY_INDEX = "index";
    public static final String KEY_SONG_ID = "id";
    public static final String KEY_LAST_TITLE = "title";
    public static final String KEY_LAST_ARTIST = "artist";
    public static final String KEY_LAST_POSITION = "position";

    public Storage(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
    }

    public void storeSongs(ArrayList<ArrayList<String>> songs) {
        editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(songs);
        editor.putString(KEY_SONGS, json);
        editor.apply();
    }

    public ArrayList<ArrayList<String>> getSongs() {
        Gson gson = new Gson();
        String json = pref.getString(KEY_SONGS, null);
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {

        }.getType();

        return gson.fromJson(json, type);
    }

    public void storeStatus(String status) {
        editor = pref.edit();
        editor.putString(KEY_STATUS, status);
        editor.apply();
    }

    public void storeAudioIndex(int index) {
        editor = pref.edit();
        editor.putInt(KEY_INDEX, index);
        editor.apply();
    }

    public void storeSongId(long id) {
        editor = pref.edit();
        editor.putLong(KEY_SONG_ID, id);
        editor.apply();
    }

    public void storeTitle(String title) {
        editor = pref.edit();
        editor.putString(KEY_LAST_TITLE, title);
        editor.apply();
    }

    public void storeArtist(String artist) {
        editor = pref.edit();
        editor.putString(KEY_LAST_ARTIST, artist);
        editor.apply();
    }

    public String getLastTitle() {
        return pref.getString(KEY_LAST_TITLE, null);
    }

    public String getLastArtist() {
        return pref.getString(KEY_LAST_ARTIST, null);
    }

    public String getStatus() {
        return pref.getString(KEY_STATUS, null);
    }

    public int loadAudioIndex() {
        return pref.getInt(KEY_INDEX, -1);
    }

    public void clearCache() {
        editor = pref.edit();
        editor.clear();
        editor.apply();
    }
}
