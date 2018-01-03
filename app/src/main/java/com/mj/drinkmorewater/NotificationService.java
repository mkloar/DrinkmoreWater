package com.mj.drinkmorewater;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.mj.drinkmorewater.Activities.MainActivity;
import com.mj.drinkmorewater.db.DatabaseHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mihaa on 3. 01. 2018.
 */

public class NotificationService extends Service {
    public static final int notify = 7200000;  //interval between two services(Here Service run every 2 hours)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    private String lastWaterEntry="";

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else {
            mTimer = new Timer();   //recreate new
            DatabaseHandler databaseHandler = new DatabaseHandler(this);
            Cursor cursor = databaseHandler.getLastWaterEntry();
            if(cursor.moveToFirst()) {
                lastWaterEntry = cursor.getString(0);
            }

        }

        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast
                    Toast.makeText(NotificationService.this, "Service is running", Toast.LENGTH_SHORT).show();
                    if (checkLastEntryFor2Hours(lastWaterEntry)) {
                        sendNotification();

                    }
                }
            });
        }
    }

    public void sendNotification() {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        mNotificationManager.notify(001, mBuilder.build());
    }

    public boolean checkLastEntryFor2Hours(String lastWaterEntry) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date minustwoHours=new Date(System.currentTimeMillis() - 7200*1000);

        try {
            Date lastEntry=df.parse(lastWaterEntry);

            if(lastEntry.before(minustwoHours)) {
                return true;
            }



        } catch (ParseException e) {
            e.printStackTrace();
        }



        return false;
    }
}
