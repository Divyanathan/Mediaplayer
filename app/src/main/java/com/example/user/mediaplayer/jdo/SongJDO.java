package com.example.user.mediaplayer.jdo;

import java.io.Serializable;

/**
 * Created by user on 03/05/17.
 */

public class SongJDO implements Serializable {

    int mColumnId;
    String mSongId;
    String mSongTitel;
    String mSongArtist;
    String mSongDuration;
    String mSongAlbum;
    String mSongImage;
    int mFavourite;

    public SongJDO(int mColumnId,String mSongId, String mSongTitel, String mSongArtist, String mSongDuration, String mSongAlbum, String mSongImage,int pFavourite) {
        this.mColumnId=mColumnId;
        this.mSongId = mSongId;
        this.mSongTitel = mSongTitel;
        this.mSongArtist = mSongArtist;
        this.mSongDuration = mSongDuration;
        this.mSongAlbum = mSongAlbum;
        this.mSongImage = mSongImage;
        this.mFavourite=pFavourite;
    }

    public int getmColumnId() {
        return mColumnId;
    }

    public void setmColumnId(int mColumnId) {
        this.mColumnId = mColumnId;
    }

    public String getmSongAlbum() {
        return mSongAlbum;
    }

    public void setmSongAlbum(String mSongAlbum) {
        this.mSongAlbum = mSongAlbum;
    }

    public String getmSongImage() {
        return mSongImage;
    }

    public void setmSongImage(String mSongImage) {
        this.mSongImage = mSongImage;
    }

    public String getmSongDuration() {
        return mSongDuration;
    }

    public void setmSongDuration(String mSongDuration) {
        this.mSongDuration = mSongDuration;
    }


    public String getmSongId() {
        return mSongId;
    }

    public void setmSongId(String mSongId) {
        this.mSongId = mSongId;
    }

    public String getmSongTitel() {
        return mSongTitel;
    }

    public void setmSongTitel(String mSongTitel) {
        this.mSongTitel = mSongTitel;
    }

    public String getmSongArtist() {
        return mSongArtist;
    }

    public void setmSongArtist(String mSongArtist) {
        this.mSongArtist = mSongArtist;
    }

    public int getmFavourite() {
        return mFavourite;
    }

    public void setmFavourite(int mFavourite) {
        this.mFavourite = mFavourite;
    }
}
