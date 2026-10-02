package com.cep.dostu.findik;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(36, 70, 36, 40);
        root.setBackgroundColor(Color.rgb(255, 248, 241));

        TextView title = new TextView(this);
        title.setText("Cep Dostu Fındık");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(45, 35, 30));
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Fındık telefonunda seninle yaşayan küçük dostun.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.rgb(100, 85, 75));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 20, 0, 50);

        Button startButton = new Button(this);
        startButton.setText("Fındık'ı Ekrana Getir");

        Button stopButton = new Button(this);
        stopButton.setText("Fındık'ı Dinlendir");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 12, 0, 12);

        startButton.setLayoutParams(buttonParams);
        stopButton.setLayoutParams(buttonParams);

        startButton.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );
                startActivity(intent);
                return;
            }

            Intent serviceIntent = new Intent(this, PetService.class);
            startForegroundService(serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            stopService(new Intent(this, PetService.class));
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(startButton);
        root.addView(stopButton);

        setContentView(root);
    }
}
