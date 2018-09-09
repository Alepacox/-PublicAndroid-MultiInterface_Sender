package it.unicam.project.multiinterfacesender;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class PushNotification extends FirebaseMessagingService{

    public PushNotification(){
        Log.e("Push","Received");
    }
    @Override
    public void onNewToken(String token) {
        //sendRegistrationToServer(token);
    }
}
