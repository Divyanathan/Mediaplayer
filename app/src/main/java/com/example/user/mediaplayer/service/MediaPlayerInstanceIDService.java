package com.example.user.mediaplayer.service;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by user on 22/05/17.
 */

public class MediaPlayerInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MediaPlayerInstanceIDSe";
    @Override
    public void onTokenRefresh() {
//        super.onTokenRefresh();

       String lToken= FirebaseInstanceId.getInstance().getToken();

        FirebaseDatabase lFireBaseDataBase= FirebaseDatabase.getInstance();

        DatabaseReference lRootRefrence=lFireBaseDataBase.getReference();
        DatabaseReference lChildRefrence=lRootRefrence.child("token");
        lChildRefrence.setValue(lToken);

        Log.d(TAG, "onTokenRefresh: ");

    }
}
