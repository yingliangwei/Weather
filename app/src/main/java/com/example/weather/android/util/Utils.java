package com.example.weather.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.weather.R;
import com.example.weather.android.WeatherActivity;

public class Utils {
    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;

    /**
     * 发送通知
     */
    public static void sendNotification(Context context, String title, String text) {
        //设置 channel_id
        final String CHANNAL_ID = "chat";

        //获取 PendingIntent 对象，NotificationActivity是另一个活动
        Intent intent = new Intent(context, WeatherActivity.class);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        //获取系统通知服务
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Android 8.0开始要设置通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNAL_ID,
                    "chat message", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        //创建通知
        mBuilder = new NotificationCompat.Builder(context, CHANNAL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setAutoCancel(true);

        //发送通知( id唯一,可用于更新通知时对应旧通知; 通过mBuilder.build()拿到notification对象 )
        mNotificationManager.notify(1, mBuilder.build());
    }

}