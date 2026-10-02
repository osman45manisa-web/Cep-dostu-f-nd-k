package com.cep.dostu.findik;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int brown = Color.rgb(92, 60, 42);
    private int cream = Color.rgb(255, 248, 241);
    private int softCream = Color.rgb(250, 239, 227);
    private int green = Color.rgb(87, 146, 96);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(cream);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(42), dp(22), dp(30));

        TextView title = new TextView(this);
        title.setText("Cep Dostu Fındık");
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(brown);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Telefonunda yaşayan küçük dostun");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.rgb(120, 95, 80));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(24));

        PetPreviewView preview = new PetPreviewView(this);

        GradientDrawable previewBg = new GradientDrawable();
        previewBg.setColor(softCream);
        previewBg.setCornerRadius(dp(28));
        preview.setBackground(previewBg);

        LinearLayout.LayoutParams previewParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(290)
                );

        previewParams.setMargins(0, 0, 0, dp(18));
        preview.setLayoutParams(previewParams);

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(20), dp(18), dp(20), dp(18));

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(Color.WHITE);
        statusBg.setCornerRadius(dp(22));
        statusCard.setBackground(statusBg);

        TextView statusTitle = new TextView(this);
        statusTitle.setText("Fındık şu an");
        statusTitle.setTextSize(15);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusTitle.setTextColor(brown);

        TextView status = new TextView(this);
        status.setText("●  Dinleniyor");
        status.setTextSize(18);
        status.setTextColor(green);
        status.setPadding(0, dp(8), 0, dp(5));

        TextView statusInfo = new TextView(this);
        statusInfo.setText(
                "Ekrana getirince yürür, dokununca tepki verir ve seninle birlikte dolaşır."
        );
        statusInfo.setTextSize(14);
        statusInfo.setTextColor(Color.rgb(110, 100, 95));

        statusCard.addView(statusTitle);
        statusCard.addView(status);
        statusCard.addView(statusInfo);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(20));
        statusCard.setLayoutParams(cardParams);

        Button startButton = new Button(this);
        startButton.setText("Fındık'ı Ekrana Getir");
        startButton.setTextSize(17);
        startButton.setTextColor(Color.WHITE);
        startButton.setAllCaps(false);

        GradientDrawable startBg = new GradientDrawable();
        startBg.setColor(brown);
        startBg.setCornerRadius(dp(20));
        startButton.setBackground(startBg);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        buttonParams.setMargins(0, 0, 0, dp(12));
        startButton.setLayoutParams(buttonParams);

        Button stopButton = new Button(this);
        stopButton.setText("Fındık'ı Dinlendir");
        stopButton.setTextSize(16);
        stopButton.setTextColor(brown);
        stopButton.setAllCaps(false);

        GradientDrawable stopBg = new GradientDrawable();
        stopBg.setColor(Color.TRANSPARENT);
        stopBg.setStroke(dp(2), Color.rgb(210, 190, 175));
        stopBg.setCornerRadius(dp(20));
        stopButton.setBackground(stopBg);

        LinearLayout.LayoutParams stopParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        stopButton.setLayoutParams(stopParams);

        TextView hint = new TextView(this);
        hint.setText(
                "İpucu: Fındık'ı parmağınla sürükleyebilirsin. " +
                "Dokununca farklı hareketler yapar, uzun basınca havlar."
        );
        hint.setTextSize(13);
        hint.setTextColor(Color.rgb(130, 115, 105));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(8), dp(22), dp(8), 0);

        startButton.setOnClickListener(v -> {

            if (!Settings.canDrawOverlays(this)) {

                Intent permissionIntent =
                        new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse(
                                        "package:" + getPackageName()
                                )
                        );

                startActivity(permissionIntent);
                return;
            }

            Intent serviceIntent =
                    new Intent(
                            this,
                            PetService.class
                    );

            startForegroundService(serviceIntent);

            status.setText("●  Ekranda seninle");
            status.setTextColor(green);
        });

        stopButton.setOnClickListener(v -> {

            stopService(
                    new Intent(
                            this,
                            PetService.class
                    )
            );

            status.setText("●  Dinleniyor");
            status.setTextColor(
                    Color.rgb(150, 120, 100)
            );
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(preview);
        root.addView(statusCard);
        root.addView(startButton);
        root.addView(stopButton);
        root.addView(hint);

        scroll.addView(root);
        setContentView(scroll);
    }

    private int dp(int value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private static class PetPreviewView extends View {

        private Bitmap sprite;
        private Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        public PetPreviewView(Activity context) {
            super(context);

            sprite =
                    BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.findik_idle
                    );
        }

        @Override
        protected void onDraw(Canvas canvas) {
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

            Rect source =
                    new Rect(
                            0,
                            0,
                            frameWidth,
                            frameHeight
                    );

            float scale =
                    Math.min(
                            (getWidth() - 40f)
                                    / frameWidth,

                            (getHeight() - 40f)
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
