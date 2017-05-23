package com.example.user.mediaplayer.listener;

import android.view.View;

/**
 * Created by user on 04/05/17.
 */

public interface OnItemClickLisenter {
    public void onSingleTab(View pChaildView, int pItemPosition);
    public  void onDoubleTab(View pChaildView, int pItemPosition);
    public void onLongPress(View pChaildView, int pItemPosition);
}
