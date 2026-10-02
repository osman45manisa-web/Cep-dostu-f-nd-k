package com.cep.dostu.findik;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SharedPreferences prefs;

    private final int background = Color.rgb(250, 246, 240);
    private final int brown = Color.rgb(86, 58, 43);
    private final int lightBrown = Color.rgb(146, 112, 87);
    private final int green = Color.rgb(75, 145, 84);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "findik_settings",
                MODE_PRIVATE
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(24),
                dp(45),
                dp(24),
                dp(30)
        );
        root.setBackgroundColor(background);

        TextView title = new TextView(this);
        title.setText("Cep Dostu Fındık");
        title.setTextSize(30);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setTextColor(brown);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Fındık telefon ekranında seninle dolaşsın."
        );
        subtitle.setTextSize(15);
        subtitle.setTextColor(lightBrown);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(
                0,
                dp(8),
                0,
                dp(30)
        );

        LinearLayout statusCard =
                new LinearLayout(this);

        statusCard.setOrientation(
                LinearLayout.VERTICAL
        );

        statusCard.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(18)
        );

        GradientDrawable cardBackground =
                new GradientDrawable();

        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(dp(22));

        statusCard.setBackground(cardBackground);

        TextView statusTitle =
                new TextView(this);

        statusTitle.setText("Fındık");
        statusTitle.setTextSize(17);
        statusTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        statusTitle.setTextColor(brown);

        TextView statusText =
                new TextView(this);

        statusText.setText("● Dinleniyor");
        statusText.setTextSize(15);
        statusText.setTextColor(lightBrown);
        statusText.setPadding(
                0,
                dp(6),
                0,
                0
        );

        statusCard.addView(statusTitle);
        statusCard.addView(statusText);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(25)
        );

        statusCard.setLayoutParams(cardParams);

        TextView sizeTitle =
                new TextView(this);

        sizeTitle.setText("Fındık'ın boyutu");
        sizeTitle.setTextSize(17);
        sizeTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        sizeTitle.setTextColor(brown);

        TextView sizeValue =
                new TextView(this);

        int savedSize =
                prefs.getInt(
                        "pet_size",
                        520
                );

        sizeValue.setText(
                savedSize + " px"
        );

        sizeValue.setTextSize(14);
        sizeValue.setTextColor(lightBrown);
        sizeValue.setPadding(
                0,
                dp(5),
                0,
                dp(8)
        );

        SeekBar sizeSeek =
                new SeekBar(this);

        /*
         * 350 - 750 px arası
         */
        sizeSeek.setMax(400);

        int initialProgress =
                savedSize - 350;

        if (initialProgress < 0) {
            initialProgress = 0;
        }

        if (initialProgress > 400) {
            initialProgress = 400;
        }

        sizeSeek.setProgress(
                initialProgress
        );

        sizeSeek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int newSize =
                                350 + progress;

                        sizeValue.setText(
                                newSize + " px"
                        );

                        prefs.edit()
                                .putInt(
                                        "pet_size",
                                        newSize
                                )
                                .apply();
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {

                        /*
                         * Fındık açıksa yeni boyutu
                         * uygulamak için servisi
                         * yeniden başlatıyoruz.
                         */

                        stopService(
                                new Intent(
                                        MainActivity.this,
                                        PetService.class
                                )
                        );

                        if (Settings.canDrawOverlays(
                                MainActivity.this
                        )) {

                            startForegroundService(
                                    new Intent(
                                            MainActivity.this,
                                            PetService.class
                                    )
                            );

                            statusText.setText(
                                    "● Ekranda seninle"
                            );

                            statusText.setTextColor(
                                    green
                            );
                        }
                    }
                }
        );

        TextView smallLarge =
                new TextView(this);

        smallLarge.setText(
                "Küçük                              Büyük"
        );

        smallLarge.setTextSize(12);
        smallLarge.setTextColor(lightBrown);
        smallLarge.setGravity(Gravity.CENTER);
        smallLarge.setPadding(
                0,
                0,
                0,
                dp(25)
        );

        Button startButton =
                new Button(this);

        startButton.setText(
                "Fındık'ı Ekrana Getir"
        );

        startButton.setTextSize(17);
        startButton.setTextColor(Color.WHITE);
        startButton.setAllCaps(false);

        GradientDrawable startBackground =
                new GradientDrawable();

        startBackground.setColor(brown);
        startBackground.setCornerRadius(dp(18));

        startButton.setBackground(
                startBackground
        );

        LinearLayout.LayoutParams startParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        startParams.setMargins(
                0,
                0,
                0,
                dp(12)
        );

        startButton.setLayoutParams(
                startParams
        );

        Button stopButton =
                new Button(this);

        stopButton.setText(
                "Fındık'ı Dinlendir"
        );

        stopButton.setTextSize(16);
        stopButton.setTextColor(brown);
        stopButton.setAllCaps(false);

        GradientDrawable stopBackground =
                new GradientDrawable();

        stopBackground.setColor(
                Color.TRANSPARENT
        );

        stopBackground.setStroke(
                dp(2),
                Color.rgb(
                        210,
                        190,
                        175
                )
        );

        stopBackground.setCornerRadius(
                dp(18)
        );

        stopButton.setBackground(
                stopBackground
        );

        stopButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                )
        );

        TextView help =
                new TextView(this);

        help.setText(
                "Dokun: tepki verir\n" +
                "Uzun bas: havlar\n" +
                "Sürükle: istediğin yere taşı"
        );

        help.setGravity(Gravity.CENTER);
        help.setTextSize(14);
        help.setTextColor(lightBrown);

        help.setPadding(
                dp(10),
                dp(28),
                dp(10),
                0
        );

        startButton.setOnClickListener(v -> {

            if (!Settings.canDrawOverlays(this)) {

                Intent permission =
                        new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse(
                                        "package:" +
                                                getPackageName()
                                )
                        );

                startActivity(permission);
                return;
            }

            stopService(
                    new Intent(
                            this,
                            PetService.class
                    )
            );

            startForegroundService(
                    new Intent(
                            this,
                            PetService.class
                    )
            );

            statusText.setText(
                    "● Ekranda seninle"
            );

            statusText.setTextColor(green);
        });

        stopButton.setOnClickListener(v -> {

            stopService(
                    new Intent(
                            this,
                            PetService.class
                    )
            );

            statusText.setText(
                    "● Dinleniyor"
            );

            statusText.setTextColor(
                    lightBrown
            );
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(statusCard);

        root.addView(sizeTitle);
        root.addView(sizeValue);
        root.addView(sizeSeek);
        root.addView(smallLarge);

        root.addView(startButton);
        root.addView(stopButton);
        root.addView(help);

        setContentView(root);
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}
