package com.tobioyelekan.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by TOBI OYELEKAN on 16/07/2017.
 */

public class Database extends SQLiteOpenHelper {

    public static String DBNAME = "MyMusic";
    private String tblfavs = "MyMusicFavs";
    private String tblplaylists = "MyMusicPlayLists";
    private String musicid = "musicid";

    public Database(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + tblfavs + "( id INTEGER PRIMARY KEY AUTOINCREMENT, " + musicid + " TEXT)");
        db.execSQL("CREATE TABLE " + tblplaylists + "( id INTEGER PRIMARY KEY AUTOINCREMENT, " + musicid + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tblfavs);
        db.execSQL("DROP TABLE IF EXISTS " + tblplaylists);
        onCreate(db);
    }

    public boolean insert(String tblname, String musicid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(this.musicid, musicid);

        long insert = db.insert(tblname, null, contentValues);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public int remove(String tblname, String musicid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tblname, this.musicid + "=?", new String[]{musicid});
    }

    public Cursor getMusicIds(String tblname) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tblname, null);
    }
}