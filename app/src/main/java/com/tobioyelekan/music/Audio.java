package com.tobioyelekan.music;

import java.io.Serializable;

/**
 * Created by TOBI OYELEKAN on 29/12/2017.
 */

public class Audio implements Serializable {

    private String data;
    private String title;
    private String artist;
    private String id;
    private String duration;
    private String size;
    private String albumid;
    private String year;
    private String album;

    public Audio(String data, String title, String artist, String id, String duration, String size, String albumid, String year, String album) {
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.id = id;
        this.duration = duration;
        this.size = size;
        this.albumid = albumid;
        this.year = year;
        this.album = album;
    }

    public String getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getId() {
        return id;
    }

    public String getDuration() {
        return duration;
    }

    public String getSize() {
        return size;
    }

    public String getAlbumid() {
        return albumid;
    }

    public String getYear() {
        return year;
    }

    public String getAlbum() {
        return album;
    }

}
