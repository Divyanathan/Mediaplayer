package com.example.user.mediaplayer.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.example.user.mediaplayer.utility.UtilityClass;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by user on 03/05/17.
 */

public class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.MyViewHolder> {


    ArrayList<SongJDO> mSongJDOArrayList;
    Context mContext;
    SongJDO mSongJDOobj;
    MyViewHolder mMyViewHolder;

    private static final String TAG = "SongRecyclerAdapter";

    public SongRecyclerAdapter(Context mContext, ArrayList<SongJDO> mSongJDOArrayList) {
        this.mSongJDOArrayList = mSongJDOArrayList;
        this.mContext = mContext;
        Log.d(TAG, "SongRecyclerAdapter: constructor");
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView sSongIdTextView, sTitleTextView, sArtistTextView;
        ImageView sSongImageView,sFavourImageView;

        
//        Context sContext;

        public MyViewHolder(View itemView) {
            super(itemView);
//            sContext=itemView.getContext();
            sTitleTextView = (TextView) itemView.findViewById(R.id.songTitle);
            sArtistTextView = (TextView) itemView.findViewById(R.id.songArtist);
            sSongIdTextView = (TextView) itemView.findViewById(R.id.songId);

            sSongImageView = (ImageView) itemView.findViewById(R.id.songImage);
            sFavourImageView = (ImageView) itemView.findViewById(R.id.favourite);
            Log.d(TAG, "MyViewHolder:  constructor");
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View lView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_list_item, parent, false);

        Log.d(TAG, "onCreateViewHolder: ");
        mMyViewHolder= new SongRecyclerAdapter.MyViewHolder(lView);
        return mMyViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        mSongJDOobj = mSongJDOArrayList.get(position);
        holder.sSongIdTextView.setText(mSongJDOobj.getmSongId());
        holder.sTitleTextView.setText(mSongJDOobj.getmSongTitel());
        holder.sArtistTextView.setText(mSongJDOobj.getmSongAlbum());
        int lSongPosition=mContext.getSharedPreferences(UtilityClass.MY_SHARED_PREFRENCE,Context.MODE_PRIVATE).getInt(UtilityClass.SONG_POSITION_SET_COLOR,-1);

        if(lSongPosition==position){

            Log.d(TAG, "onBindViewHolder: song_is_playing"+" "+mSongJDOobj.getmColumnId()+" "+mSongJDOobj.getmSongTitel()+" "+holder.sArtistTextView.getText().toString());
            holder.sTitleTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            holder.sArtistTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
        } else {
            Log.d(TAG, "onBindViewHolder: song_is_playing"+" "+mSongJDOobj.getmColumnId()+" "+mSongJDOobj.getmSongTitel()+" "+holder.sArtistTextView.getText().toString());
            holder.sTitleTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorWhite));
            holder.sArtistTextView.setTextColor(ContextCompat.getColor(mContext,R.color.colorWhite));
        }

        Picasso.with(mContext).load(mSongJDOobj.getmSongImage())
                .centerCrop()
                .resize(800, 800)
                .placeholder(R.drawable.song)
                .into(holder.sSongImageView);

        if(mSongJDOobj.getmFavourite()==1){
            holder.sFavourImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext)
                    .load(R.drawable.favourite_song)
                    .centerCrop()
                    .resize(800, 800)
                    .into(holder.sFavourImageView);
        }else {
            holder.sFavourImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext)
                    .load(R.drawable.favourite)
                    .centerCrop()
                    .resize(800, 800)
                    .into(holder.sFavourImageView);
        }

        Log.d(TAG, "onBindViewHolder: ");
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        return mSongJDOArrayList.size();
    }


    public  SongJDO getSongDetails(int pPosition){
       return  mSongJDOArrayList.get(pPosition);
    }


}
