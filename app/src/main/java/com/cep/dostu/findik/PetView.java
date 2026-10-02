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

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Handler handler =
            new Handler(Looper.getMainLooper());

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

    private long reactionEndTime = 0;

    private MediaPlayer barkPlayer;

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

    private final Runnable animationLoop =
            new Runnable() {

        @Override
        public void run() {

            long now =
                    System.currentTimeMillis();

            /*
             * Tepki süresi bittiyse
             * tekrar yürüyüşe dön.
             */
            if (reacting &&
                    now >= reactionEndTime) {

                reacting = false;
                state = State.WALK;
                currentFrame = 0;
            }

            currentFrame++;

            if (currentFrame >= FRAME_COUNT) {
                currentFrame = 0;
            }

            if (state == State.WALK &&
                    !dragging) {

                movePet();
            }

            invalidate();

            handler.postDelayed(
                    this,
                    getFrameDelay()
            );
        }
    };

    private long getFrameDelay() {

        switch (state) {

            case PAW:
                return 300;

            case LICK:
                return 230;

            case SIT:
                return 330;

            case SPIN:
                return 220;

            case BARK:
                return 190;

            case WALK:
            default:
                return getWalkDelay();
        }
    }

    private long getWalkDelay() {

        int speed =
                prefs.getInt(
                        "walk_speed",
                        50
                );

        return 280 -
                (speed * 170L / 100L);
    }

    private int getMoveStep() {

        int speed =
                prefs.getInt(
                        "walk_speed",
                        50
                );

        return 2 +
                (speed * 7 / 100);
    }

    private void movePet() {

        if (windowManager == null ||
                windowParams == null) {

            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        windowParams.x +=
                getMoveStep() *
                        direction;

        if (windowParams.x +
                windowParams.width >=
                screenWidth) {

            windowParams.x =
                    screenWidth -
                            windowParams.width;

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

    private Bitmap getCurrentBitmap() {

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

        Bitmap sprite =
                getCurrentBitmap();

        if (sprite == null) {
            return;
        }

        int frameWidth =
                sprite.getWidth() /
                        FRAME_COUNT;

        int frameHeight =
                sprite.getHeight();

        int sourceLeft =
                currentFrame *
                        frameWidth;

        Rect source =
                new Rect(
                        sourceLeft,
                        0,
                        sourceLeft +
                                frameWidth,
                        frameHeight
                );

        float scale =
                Math.min(
                        getWidth() /
                                (float) frameWidth,
                        getHeight() /
                                (float) frameHeight
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

        int drawLeft =
                (getWidth() -
                        drawWidth) / 2;

        int drawTop =
                getHeight() -
                        drawHeight;

        Rect destination =
                new Rect(
                        drawLeft,
                        drawTop,
                        drawLeft +
                                drawWidth,
                        drawTop +
                                drawHeight
                );

        canvas.save();

        /*
         * Sola giderken karakteri çevir.
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

    /*
     * HAREKETLER
     */

    private void startReaction(
            State newState,
            long duration
    ) {

        if (reacting) {
            return;
        }

        state = newState;
        reacting = true;
        currentFrame = 0;

        reactionEndTime =
                System.currentTimeMillis()
                        + duration;

        invalidate();
    }

    private void doLick() {

        startReaction(
                State.LICK,
                1400
        );
    }

    private void doPaw() {

        startReaction(
                State.PAW,
                1800
        );
    }

    private void doSit() {

        startReaction(
                State.SIT,
                2000
        );
    }

    private void doSpin() {

        startReaction(
                State.SPIN,
                1600
        );
    }

    private void doBark() {

        if (reacting) {
            return;
        }

        startReaction(
                State.BARK,
                1200
        );

        playBarkSound();
    }

    /*
     * HAVLAMA SESİ
     */

    private void playBarkSound() {

        boolean soundEnabled =
                prefs.getBoolean(
                        "sound_enabled",
                        true
                );

        if (!soundEnabled) {
            return;
        }

        int volumePercent =
                prefs.getInt(
                        "sound_volume",
                        80
                );

        float volume =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                volumePercent / 100f
                        )
                );

        try {

            if (barkPlayer != null) {

                barkPlayer.release();
                barkPlayer = null;
            }

            barkPlayer =
                    MediaPlayer.create(
                            getContext(),
                            R.raw.findik_bark
                    );

            if (barkPlayer != null) {

                barkPlayer.setVolume(
                        volume,
                        volume
                );

                barkPlayer.setOnCompletionListener(
                        mp -> {

                            mp.release();

                            if (barkPlayer == mp) {
                                barkPlayer = null;
                            }
                        }
                );

                barkPlayer.start();
            }

        } catch (Exception ignored) {
        }
    }

    /*
     * DOKUNMA BÖLGELERİ
     */

    private void handleTap(
            float x,
            float y
    ) {

        if (reacting) {
            return;
        }

        float nx =
                x / getWidth();

        float ny =
                y / getHeight();

        /*
         * BAŞ:
         * üst-orta bölge
         */
        if (ny < 0.45f &&
                nx > 0.22f &&
                nx < 0.78f) {

            doLick();
            return;
        }

        /*
         * PATİ:
         * alt-ön bölüm
         */
        if (ny > 0.60f &&
                nx > 0.52f) {

            doPaw();
            return;
        }

        /*
         * GÖVDE:
         * diğer bölgeler
         */
        doSit();
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        if (windowManager == null ||
                windowParams == null) {

            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                downX =
                        event.getRawX();

                downY =
                        event.getRawY();

                startX =
                        windowParams.x;

                startY =
                        windowParams.y;

                downTime =
                        System.currentTimeMillis();

                dragging = false;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getRawX() -
                                downX;

                float dy =
                        event.getRawY() -
                                downY;

                if (Math.abs(dx) > 22 ||
                        Math.abs(dy) > 22) {

                    dragging = true;

                    windowParams.x =
                            startX +
                                    (int) dx;

                    windowParams.y =
                            startY +
                                    (int) dy;

                    try {

                        windowManager
                                .updateViewLayout(
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

                long pressDuration =
                        now - downTime;

                if (!dragging &&
                        !reacting) {

                    /*
                     * UZUN BAS
                     */
                    if (pressDuration >= 700) {

                        lastTapTime = 0;
                        doBark();

                    }

                    /*
                     * ÇİFT DOKUNMA
                     */
                    else if (
                            lastTapTime != 0 &&
                            now - lastTapTime <= 350
                    ) {

                        lastTapTime = 0;
                        doSpin();

                    }

                    /*
                     * TEK DOKUNMA
                     */
                    else {

                        lastTapTime = now;

                        final long thisTap =
                                lastTapTime;

                        final float tapX =
                                event.getX();

                        final float tapY =
                                event.getY();

                        handler.postDelayed(
                                () -> {

                                    if (
                                            lastTapTime ==
                                                    thisTap &&
                                            !reacting
                                    ) {

                                        lastTapTime = 0;

                                        handleTap(
                                                tapX,
                                                tapY
                                        );
                                    }
                                },
                                380
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

        if (barkPlayer != null) {

            try {
                barkPlayer.release();
            } catch (Exception ignored) {
            }

            barkPlayer = null;
        }
    }
}
