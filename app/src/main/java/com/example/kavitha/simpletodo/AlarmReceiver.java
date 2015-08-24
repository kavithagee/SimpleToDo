package com.example.kavitha.simpletodo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

/**
 * Created by kavitha on 8/23/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
        boolean isItSunday = (day == Calendar.SUNDAY);
        boolean isItEndOfTheMonth = (dayInMonth == Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        String notifText = "You have items to finish today!";
        if (isItEndOfTheMonth) {
            notifText = "You have items to finish this month!";
        } else if (isItSunday) {
            notifText = "You have items to finish this week!";
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_announcement_black_18dp)
                        .setContentTitle("SimpleToDo")
                        .setContentText(notifText);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        Notification noti = mBuilder.build();
        noti.flags  = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1000, noti);
    }
}
