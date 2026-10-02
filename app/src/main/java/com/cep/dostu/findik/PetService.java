package com.cep.dostu.findik;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

public class PetService extends Service
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String CHANNEL_ID = "findik_channel";
    private static final int NOTIFICATION_ID = 101;

    private WindowManager windowManager;
    private PetView petView;
    private WindowManager.LayoutParams params;
    private SharedPreferences prefs;

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

        startForeground(
                NOTIFICATION_ID,
                notification
        );

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        prefs = getSharedPreferences(
                "findik_settings",
                MODE_PRIVATE
        );

        prefs.registerOnSharedPreferenceChangeListener(this);

        int petSize =
                prefs.getInt(
                        "pet_size",
                        480
                );

        /*
         * Önceki sürümden 340 gibi
         * çok küçük değer kaldıysa düzelt.
         */
        if (petSize < 400) {
            petSize = 480;

            prefs.edit()
                    .putInt(
                            "pet_size",
                            petSize
                    )
                    .apply();
        }

        windowManager =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        petView =
                new PetView(this);

        int width = petSize;

        int height =
                (int) (
                        petSize * 0.80f
                );

        params =
                new WindowManager.LayoutParams(
                        width,
                        height,
                        Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.O
                                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity =
                Gravity.TOP |
                        Gravity.START;

        params.x = 20;
        params.y = 500;

        windowManager.addView(
                petView,
                params
        );

        petView.setWindowManager(
                windowManager,
                params
        );
    }

    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences,
            String key
    ) {

        if ("pet_size".equals(key)) {

            int newSize =
                    sharedPreferences.getInt(
                            "pet_size",
                            480
                    );

            /*
             * Çok küçük olmasına izin verme.
             */
            if (newSize < 400) {
                newSize = 400;
            }

            if (params != null &&
                    windowManager != null &&
                    petView != null) {

                /*
                 * Hareket yapmıyorsa
                 * normal boyuta geçir.
                 */
                if (!petView.isReacting()) {

                    params.width =
                            newSize;

                    params.height =
                            (int) (
                                    newSize *
                                            0.80f
                            );

                    try {

                        windowManager.updateViewLayout(
                                petView,
                                params
                        );

                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Cep Dostu Fındık",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (prefs != null) {

            prefs.unregisterOnSharedPreferenceChangeListener(
                    this
            );
        }

        if (windowManager != null &&
                petView != null) {

            try {

                windowManager.removeView(
                        petView
                );

            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public IBinder onBind(
            Intent intent
    ) {
        return null;
    }
}
