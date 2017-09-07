package com.turing.encripturing;

import java.io.Serializable;

/**
 * Created by dbf_6 on 02/09/2017.
 */

public class Audio implements Serializable {
    /**
     * Cargar archivos locales de audio
     * Es posible reproducir audio por streaming desde alguna página
     */

    private String data, title, album, artist;

    public Audio(String data, String title, String album, String artist){
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
