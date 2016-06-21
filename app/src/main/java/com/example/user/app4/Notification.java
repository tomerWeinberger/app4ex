package com.example.user.app4;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by user on 21/06/2016.
 */
public class Notification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder mbuild = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.livechat)
                .setContentTitle("new meseges")
                .setContentText("new one");
        int notificationId = 001;
        NotificationManager notMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notMgr.notify(notificationId,mbuild.build());

    }
}
