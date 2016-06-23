package com.example.user.app4;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotificationUpd extends BroadcastReceiver {
    public static  Chat chat = null;
    /*
    the func activate the post request for updates
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        chat.updateMessages("time");
    }
}
