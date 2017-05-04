package com.example.user.mediaplayer.jdo;

/**
 * Created by user on 03/05/17.
 */

public class SongJDO {

    String mSongId,mSongTitel,mSongArtist,mSongDuration;

    public SongJDO(String mSongId, String mSongTitel, String mSongArtist,String mSongDuration) {
        this.mSongId = mSongId;
        this.mSongTitel = mSongTitel;
        this.mSongArtist = mSongArtist;
        this.mSongDuration=mSongDuration;
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
}
