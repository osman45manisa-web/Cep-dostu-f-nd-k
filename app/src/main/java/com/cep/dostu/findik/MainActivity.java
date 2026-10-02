package com.cep.dostu.findik;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(background);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(24),
                dp(40),
                dp(24),
                dp(35)
        );

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
                "Telefonunda yaşayan küçük dostun"
        );
        subtitle.setTextSize(15);
        subtitle.setTextColor(lightBrown);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(
                0,
                dp(8),
                0,
                dp(20)
        );

        /*
         * FINDIK GÖRSELİ
         */

        PetPreviewView preview =
                new PetPreviewView(this);

        GradientDrawable previewBg =
                new GradientDrawable();

        previewBg.setColor(
                Color.rgb(255, 252, 248)
        );

        previewBg.setCornerRadius(
                dp(26)
        );

        preview.setBackground(previewBg);

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(250)
                );

        previewParams.setMargins(
                0,
                0,
                0,
                dp(18)
        );

        preview.setLayoutParams(previewParams);

        /*
         * DURUM KARTI
         */

        LinearLayout statusCard =
                createCard();

        TextView statusTitle =
                new TextView(this);

        statusTitle.setText(
                "Fındık'ın durumu"
        );

        statusTitle.setTextSize(17);

        statusTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        statusTitle.setTextColor(brown);

        TextView statusText =
                new TextView(this);

        statusText.setText(
                "● Dinleniyor"
        );

        statusText.setTextSize(15);
        statusText.setTextColor(lightBrown);

        statusText.setPadding(
                0,
                dp(7),
                0,
                0
        );

        statusCard.addView(statusTitle);
        statusCard.addView(statusText);

        root.addView(title);
        root.addView(subtitle);
        root.addView(preview);
        root.addView(statusCard);

        /*
         * BOYUT
         */

        TextView sizeTitle =
                sectionTitle(
                        "Fındık'ın boyutu"
                );

        root.addView(sizeTitle);

        int savedSize =
                prefs.getInt(
                        "pet_size",
                        520
                );

        TextView sizeValue =
                valueText(
                        savedSize + " px"
                );

        root.addView(sizeValue);

        SeekBar sizeSeek =
                new SeekBar(this);

        sizeSeek.setMax(400);

        sizeSeek.setProgress(
                Math.max(
                        0,
                        Math.min(
                                400,
                                savedSize - 350
                        )
                )
        );

        sizeSeek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int size =
                                350 + progress;

                        sizeValue.setText(
                                size + " px"
                        );

                        prefs.edit()
                                .putInt(
                                        "pet_size",
                                        size
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
                    }
                }
        );

        root.addView(sizeSeek);

        TextView sizeHint =
                hintText(
                        "Küçük                               Büyük"
                );

        root.addView(sizeHint);

        /*
         * HIZ
         */

        TextView speedTitle =
                sectionTitle(
                        "Yürüme hızı"
                );

        root.addView(speedTitle);

        int savedSpeed =
                prefs.getInt(
                        "walk_speed",
                        50
                );

        TextView speedValue =
                valueText(
                        speedName(savedSpeed)
                );

        root.addView(speedValue);

        SeekBar speedSeek =
                new SeekBar(this);

        speedSeek.setMax(100);
        speedSeek.setProgress(savedSpeed);

        speedSeek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        prefs.edit()
                                .putInt(
                                        "walk_speed",
                                        progress
                                )
                                .apply();

                        speedValue.setText(
                                speedName(progress)
                        );
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
                    }
                }
        );

        root.addView(speedSeek);

        TextView speedHint =
                hintText(
                        "Yavaş                                  Hızlı"
                );

        root.addView(speedHint);

        /*
         * SES
         */

        TextView soundTitle =
                sectionTitle("Ses");

        root.addView(soundTitle);

        CheckBox soundEnabled =
                new CheckBox(this);

        soundEnabled.setText(
                "Havlama sesi açık"
        );

        soundEnabled.setTextSize(16);
        soundEnabled.setTextColor(brown);

        boolean soundOn =
                prefs.getBoolean(
                        "sound_enabled",
                        true
                );

        soundEnabled.setChecked(soundOn);

        soundEnabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefs.edit()
                            .putBoolean(
                                    "sound_enabled",
                                    isChecked
                            )
                            .apply();
                }
        );

        root.addView(soundEnabled);

        int savedVolume =
                prefs.getInt(
                        "sound_volume",
                        80
                );

        TextView volumeValue =
                valueText(
                        "Ses seviyesi: %" +
                                savedVolume
                );

        root.addView(volumeValue);

        SeekBar volumeSeek =
                new SeekBar(this);

        volumeSeek.setMax(100);
        volumeSeek.setProgress(savedVolume);

        volumeSeek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        prefs.edit()
                                .putInt(
                                        "sound_volume",
                                        progress
                                )
                                .apply();

                        volumeValue.setText(
                                "Ses seviyesi: %" +
                                        progress
                        );
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
                    }
                }
        );

        root.addView(volumeSeek);

        /*
         * BUTONLAR
         */

        Button startButton =
                new Button(this);

        startButton.setText(
                "Fındık'ı Ekrana Getir"
        );

        startButton.setTextSize(17);
        startButton.setTextColor(Color.WHITE);
        startButton.setAllCaps(false);

        GradientDrawable startBg =
                new GradientDrawable();

        startBg.setColor(brown);
        startBg.setCornerRadius(dp(18));

        startButton.setBackground(startBg);

        LinearLayout.LayoutParams startParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        startParams.setMargins(
                0,
                dp(25),
                0,
                dp(12)
        );

        startButton.setLayoutParams(startParams);

        Button stopButton =
                new Button(this);

        stopButton.setText(
                "Fındık'ı Dinlendir"
        );

        stopButton.setTextSize(16);
        stopButton.setTextColor(brown);
        stopButton.setAllCaps(false);

        GradientDrawable stopBg =
                new GradientDrawable();

        stopBg.setColor(
                Color.TRANSPARENT
        );

        stopBg.setStroke(
                dp(2),
                Color.rgb(
                        210,
                        190,
                        175
                )
        );

        stopBg.setCornerRadius(
                dp(18)
        );

        stopButton.setBackground(stopBg);

        stopButton.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                )
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

        root.addView(startButton);
        root.addView(stopButton);

        TextView info =
                new TextView(this);

        info.setText(
                "Başına dokun → Yalama\n" +
                "Gövdede dokun → Oturma\n" +
                "Pati bölgesine dokun → Pati verme\n" +
                "Çift dokun → Dönme\n" +
                "Uzun bas → Havlama\n" +
                "Sürükle → Yerini değiştir"
        );

        info.setTextSize(14);
        info.setTextColor(lightBrown);
        info.setGravity(Gravity.CENTER);

        info.setPadding(
                0,
                dp(25),
                0,
                dp(20)
        );

        root.addView(info);

        scrollView.addView(root);

        setContentView(scrollView);
    }

    private LinearLayout createCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(18)
        );

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(20));

        card.setBackground(bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                dp(25)
        );

        card.setLayoutParams(params);

        return card;
    }

    private TextView sectionTitle(String text) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(17);

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        view.setTextColor(brown);

        view.setPadding(
                0,
                dp(18),
                0,
                dp(4)
        );

        return view;
    }

    private TextView valueText(String text) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(14);
        view.setTextColor(lightBrown);

        return view;
    }

    private TextView hintText(String text) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(12);
        view.setTextColor(lightBrown);
        view.setGravity(Gravity.CENTER);

        view.setPadding(
                0,
                0,
                0,
                dp(10)
        );

        return view;
    }

    private String speedName(int value) {

        if (value < 25) {
            return "Çok yavaş";
        }

        if (value < 45) {
            return "Yavaş";
        }

        if (value < 65) {
            return "Normal";
        }

        if (value < 85) {
            return "Hızlı";
        }

        return "Çok hızlı";
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    /*
     * ANA EKRANDA FINDIK ÖNİZLEMESİ
     */

    private static class PetPreviewView extends View {

        private Bitmap sprite;

        private final Paint paint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        public PetPreviewView(
                Activity context
        ) {
            super(context);

            sprite =
                    BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.findik_idle
                    );
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {
            super.onDraw(canvas);

            if (sprite == null) {
                return;
            }

            int frameCount = 6;

            int frameWidth =
                    sprite.getWidth() /
                            frameCount;

            int frameHeight =
                    sprite.getHeight();

            /*
             * Idle sprite'ın ilk karesini göster.
             */

            Rect source =
                    new Rect(
                            0,
                            0,
                            frameWidth,
                            frameHeight
                    );

            float scale =
                    Math.min(
                            (getWidth() - 50f)
                                    / frameWidth,

                            (getHeight() - 30f)
                                    / frameHeight
                    );

            int drawWidth =
                    (int) (
                            frameWidth *
                                    scale
                    );

            int drawHeight =
                    (int) (
                            frameHeight *
                                    scale
                    );

            int left =
                    (getWidth() -
                            drawWidth) / 2;

            int top =
                    (getHeight() -
                            drawHeight) / 2;

            Rect destination =
                    new Rect(
                            left,
                            top,
                            left + drawWidth,
                            top + drawHeight
                    );

            canvas.drawBitmap(
                    sprite,
                    source,
                    destination,
                    paint
            );
        }
    }
}
