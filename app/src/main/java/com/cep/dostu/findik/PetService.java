package com.cep.dostu.findik;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

public class PetService extends Service {

    private static final String CHANNEL_ID = "findik_channel";
    private static final int NOTIFICATION_ID = 101;

    private WindowManager windowManager;
    private PetView petView;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Cep Dostu Fındık")
                        .setContentText("Fındık ekranında seninle.")
                        .setSmallIcon(android.R.drawable.ic_menu_myplaces)
                        .build();

        startForeground(NOTIFICATION_ID, notification);

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        petView = new PetView(this);

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        420,
                        420,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 80;
        params.y = 500;

        windowManager.addView(petView, params);
        petView.setWindowManager(windowManager, params);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Cep Dostu Fındık",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (windowManager != null && petView != null) {
            windowManager.removeView(petView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
