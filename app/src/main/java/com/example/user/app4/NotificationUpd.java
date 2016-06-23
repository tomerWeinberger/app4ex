package com.example.user.app4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
