package com.cep.dostu.findik;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class PetView extends View {

    private enum State {
        WALK,
        SIT,
        PAW,
        BARK,
        LICK,
        SPIN
    }

    private static final int FRAME_COUNT = 6;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SharedPreferences prefs;

    private Bitmap walk;
    private Bitmap sit;
    private Bitmap paw;
    private Bitmap bark;
    private Bitmap lick;
    private Bitmap spin;

    private State state = State.WALK;

    private int currentFrame = 0;
    private int direction = 1;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private float downX;
    private float downY;
    private int startX;
    private int startY;

    private long downTime;
    private long lastTapTime = 0;

    private boolean dragging = false;
    private boolean reacting = false;

    private MediaPlayer player;

    public PetView(Context context) {
        super(context);

        prefs = context.getSharedPreferences(
                "findik_settings",
                Context.MODE_PRIVATE
        );

        walk = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_walk
        );

        sit = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_sit
        );

        paw = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_paw
        );

        bark = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_bark
        );

        lick = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_lick
        );

        spin = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_spin
        );

        handler.post(animationLoop);
    }

    public void setWindowManager(
            WindowManager manager,
            WindowManager.LayoutParams params
    ) {
        windowManager = manager;
        windowParams = params;
    }

    private final Runnable animationLoop = new Runnable() {
        @Override
        public void run() {

            currentFrame++;

            if (currentFrame >= FRAME_COUNT) {
                currentFrame = 0;

                if (state != State.WALK) {
                    state = State.WALK;
                    reacting = false;
                }
            }

            if (state == State.WALK && !dragging) {
                movePet();
            }

            invalidate();

            long delay;

            if (state == State.WALK) {
                delay = getWalkFrameDelay();
            } else {
                delay = 320;
            }

            handler.postDelayed(this, delay);
        }
    };

    private long getWalkFrameDelay() {

        int speed = prefs.getInt("walk_speed", 50);

        return 270 - (speed * 170L / 100L);
    }

    private int getMoveStep() {

        int speed = prefs.getInt("walk_speed", 50);

        return 2 + (speed * 7 / 100);
    }

    private void movePet() {

        if (windowManager == null || windowParams == null) {
            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        windowParams.x += getMoveStep() * direction;

        if (windowParams.x + windowParams.width >= screenWidth) {

            windowParams.x =
                    screenWidth - windowParams.width;

            direction = -1;
        }

        if (windowParams.x <= 0) {

            windowParams.x = 0;
            direction = 1;
        }

        try {
            windowManager.updateViewLayout(
                    this,
                    windowParams
            );
        } catch (Exception ignored) {
        }
    }

    private Bitmap currentBitmap() {

        switch (state) {

            case SIT:
                return sit;

            case PAW:
                return paw;

            case BARK:
                return bark;

            case LICK:
                return lick;

            case SPIN:
                return spin;

            case WALK:
            default:
                return walk;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap sprite = currentBitmap();

        if (sprite == null) {
            return;
        }

        int frameWidth =
                sprite.getWidth() / FRAME_COUNT;

        int frameHeight =
                sprite.getHeight();

        int sourceLeft =
                currentFrame * frameWidth;

        Rect source =
                new Rect(
                        sourceLeft,
                        0,
                        sourceLeft + frameWidth,
                        frameHeight
                );

        float scale =
                Math.min(
                        getWidth() / (float) frameWidth,
                        getHeight() / (float) frameHeight
                );

        int drawWidth =
                (int) (frameWidth * scale);

        int drawHeight =
                (int) (frameHeight * scale);

        int drawLeft =
                (getWidth() - drawWidth) / 2;

        int drawTop =
                getHeight() - drawHeight;

        Rect destination =
                new Rect(
                        drawLeft,
                        drawTop,
                        drawLeft + drawWidth,
                        drawTop + drawHeight
                );

        canvas.save();

        /*
         * Yön düzeltmesi.
         * Sprite sağa bakıyorsa sola giderken aynala.
         */
        if (direction < 0) {

            canvas.scale(
                    -1f,
                    1f,
                    getWidth() / 2f,
                    getHeight() / 2f
            );
        }

        canvas.drawBitmap(
                sprite,
                source,
                destination,
                paint
        );

        canvas.restore();
    }

    private void startReaction(State newState) {

        if (reacting) {
            return;
        }

        reacting = true;
        state = newState;
        currentFrame = 0;
    }

    private void barkReaction() {

        if (reacting) {
            return;
        }

        reacting = true;
        state = State.BARK;
        currentFrame = 0;

        playBark();
    }

    private void playBark() {

        boolean enabled =
                prefs.getBoolean(
                        "sound_enabled",
                        true
                );

        if (!enabled) {
            return;
        }

        int volumePercent =
                prefs.getInt(
                        "sound_volume",
                        80
                );

        float volume =
                volumePercent / 100f;

        try {

            if (player != null) {
                player.release();
                player = null;
            }

            player = MediaPlayer.create(
                    getContext(),
                    R.raw.findik_bark
            );

            if (player != null) {

                player.setVolume(
                        volume,
                        volume
                );

                player.start();
            }

        } catch (Exception ignored) {
        }
    }

    private void handleTap(float x, float y) {

        if (reacting) {
            return;
        }

        float nx = x / getWidth();
        float ny = y / getHeight();

        /*
         * BAŞ BÖLGESİ
         * üst orta
         */
        if (ny < 0.42f &&
                nx > 0.25f &&
                nx < 0.75f) {

            startReaction(State.LICK);
            return;
        }

        /*
         * ÖN PATİ BÖLGESİ
         * alt ön taraf
         */
        if (ny > 0.62f &&
                nx > 0.48f) {

            startReaction(State.PAW);
            return;
        }

        /*
         * GÖVDE
         */
        startReaction(State.SIT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (windowManager == null ||
                windowParams == null) {

            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                downX = event.getRawX();
                downY = event.getRawY();

                startX = windowParams.x;
                startY = windowParams.y;

                downTime =
                        System.currentTimeMillis();

                dragging = false;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getRawX() - downX;

                float dy =
                        event.getRawY() - downY;

                if (Math.abs(dx) > 20 ||
                        Math.abs(dy) > 20) {

                    dragging = true;

                    windowParams.x =
                            startX + (int) dx;

                    windowParams.y =
                            startY + (int) dy;

                    try {
                        windowManager.updateViewLayout(
                                this,
                                windowParams
                        );
                    } catch (Exception ignored) {
                    }
                }

                return true;

            case MotionEvent.ACTION_UP:

                long now =
                        System.currentTimeMillis();

                long duration =
                        now - downTime;

                if (!dragging) {

                    if (duration > 700) {

                        barkReaction();

                    } else if (now - lastTapTime < 350) {

                        startReaction(State.SPIN);

                        lastTapTime = 0;

                    } else {

                        lastTapTime = now;

                        final float tapX =
                                event.getX();

                        final float tapY =
                                event.getY();

                        handler.postDelayed(
                                () -> {

                                    if (lastTapTime != 0 &&
                                            System.currentTimeMillis()
                                                    - lastTapTime >= 300) {

                                        handleTap(
                                                tapX,
                                                tapY
                                        );

                                        lastTapTime = 0;
                                    }
                                },
                                320
                        );
                    }
                }

                dragging = false;
                performClick();

                return true;

            case MotionEvent.ACTION_CANCEL:

                dragging = false;

                return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        handler.removeCallbacksAndMessages(null);

        if (player != null) {

            try {
                player.release();
            } catch (Exception ignored) {
            }

            player = null;
        }
    }
}
