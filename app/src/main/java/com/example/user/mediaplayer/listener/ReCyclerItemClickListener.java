package com.example.user.mediaplayer.listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by user on 04/05/17.
 */

public class ReCyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    //InterFace
    OnItemClickLisenter mListener;

    GestureDetector mGestureDetector;

    View mChildView;
    int mItemPosotion;


    public ReCyclerItemClickListener(Context context, final OnItemClickLisenter mListener) {
        this.mListener = mListener;

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent pMotionEvent) {
                mListener.onSingleTab(mChildView, mItemPosotion);
                return true;
            }


            @Override
            public void onLongPress(MotionEvent pMotionEvent) {
                mListener.onLongPress(mChildView, mItemPosotion);
                super.onLongPress(pMotionEvent);
            }

            @Override
            public boolean onDoubleTap(MotionEvent pMotionEvent) {
                mListener.onDoubleTab(mChildView, mItemPosotion);
                return true;
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView pRecyclerView, MotionEvent pMotionEvent) {

        mChildView = pRecyclerView.findChildViewUnder(pMotionEvent.getX(), pMotionEvent.getY());
        mItemPosotion = pRecyclerView.getChildAdapterPosition(mChildView);
        mGestureDetector.onTouchEvent(pMotionEvent);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
