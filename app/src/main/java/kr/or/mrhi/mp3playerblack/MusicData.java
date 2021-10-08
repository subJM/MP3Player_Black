package kr.or.mrhi.mp3playerblack;

import java.io.Serializable;

public class MusicData implements Serializable {

    private String id;
    private String artist;
    private String title;
    private String albumArt;
    private String duration;
    private String favorite;

    public MusicData(String id, String artist, String title, String albumArt, String duration, String favorite) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.albumArt = albumArt;
        this.duration = duration;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFavorit() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "id='" + id +", artist='" + artist +", title='" + title + ", albumArt='" + albumArt +
                ", duration='" + duration + ", favorite='" + favorite;
    }
}
