package com.example.user.mediaplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.mediaplayer.R;
import com.example.user.mediaplayer.jdo.SongJDO;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by user on 03/05/17.
 */

public class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.MyViewHolder> {


    ArrayList<SongJDO> mSongJDOArrayList;
    Context mContext;
    SongJDO mSongJDOobj;

    public SongRecyclerAdapter(Context mContext, ArrayList<SongJDO> mSongJDOArrayList) {
        this.mSongJDOArrayList = mSongJDOArrayList;
        this.mContext = mContext;
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
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View lView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_list_item, parent, false);

        return new SongRecyclerAdapter.MyViewHolder(lView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        mSongJDOobj = mSongJDOArrayList.get(position);
        holder.sSongIdTextView.setText(mSongJDOobj.getmSongId());
        holder.sTitleTextView.setText(mSongJDOobj.getmSongTitel());
        holder.sArtistTextView.setText(mSongJDOobj.getmSongAlbum());

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
            holder.sFavourImageView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mSongJDOArrayList.size();
    }


}
