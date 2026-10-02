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

    public boolean isReacting() {
        return reacting;
    }

    private final Runnable animationLoop =
            new Runnable() {

        @Override
        public void run() {

            long now =
                    System.currentTimeMillis();

            if (reacting &&
                    now >= reactionEndTime) {

                reacting = false;
                state = State.WALK;
                currentFrame = 0;

                restoreWalkWindow();
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
                return 430;

            case LICK:
                return 380;

            case SIT:
                return 480;

            case SPIN:
                return 360;

            case BARK:
                return 330;

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

        updateWindow();
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
                        sourceLeft + frameWidth,
                        frameHeight
                );

        /*
         * Köpek her zaman pencerenin
         * TAMAMININ İÇİNDE kalır.
         */
        float availableWidth =
                getWidth() * 0.94f;

        float availableHeight =
                getHeight() * 0.94f;

        float scale =
                Math.min(
                        availableWidth /
                                frameWidth,
                        availableHeight /
                                frameHeight
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
                        drawLeft + drawWidth,
                        drawTop + drawHeight
                );

        canvas.save();

        /*
         * Sprite sola bakıyor.
         * Sağa giderken çevir.
         */
        if (direction > 0) {

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

    private void startReaction(
            State newState,
            long duration
    ) {

        if (reacting) {
            return;
        }

        reacting = true;
        dragging = false;

        state = newState;
        currentFrame = 0;

        /*
         * Hareket başladığı anda
         * pencereyi büyüt.
         */
        enlargeReactionWindow();

        reactionEndTime =
                System.currentTimeMillis()
                        + duration;

        invalidate();
    }

    private void enlargeReactionWindow() {

        if (windowManager == null ||
                windowParams == null) {
            return;
        }

        int baseSize =
                prefs.getInt(
                        "pet_size",
                        480
                );

        if (baseSize < 400) {
            baseSize = 400;
        }

        /*
         * Hareket sırasında yaklaşık
         * %30 daha büyük.
         */
        int reactionWidth =
                (int) (
                        baseSize * 1.30f
                );

        int reactionHeight =
                (int) (
                        reactionWidth * 0.80f
                );

        int oldWidth =
                windowParams.width;

        int oldHeight =
                windowParams.height;

        /*
         * Merkezi mümkün olduğunca
         * aynı yerde tut.
         */
        windowParams.x -=
                (reactionWidth -
                        oldWidth) / 2;

        windowParams.y -=
                (reactionHeight -
                        oldHeight) / 2;

        windowParams.width =
                reactionWidth;

        windowParams.height =
                reactionHeight;

        keepInsideScreen();

        updateWindow();
    }

    private void restoreWalkWindow() {

        if (windowManager == null ||
                windowParams == null) {
            return;
        }

        int baseSize =
                prefs.getInt(
                        "pet_size",
                        480
                );

        if (baseSize < 400) {
            baseSize = 400;
        }

        int normalWidth =
                baseSize;

        int normalHeight =
                (int) (
                        baseSize * 0.80f
                );

        int oldWidth =
                windowParams.width;

        int oldHeight =
                windowParams.height;

        windowParams.x +=
                (oldWidth -
                        normalWidth) / 2;

        windowParams.y +=
                (oldHeight -
                        normalHeight) / 2;

        windowParams.width =
                normalWidth;

        windowParams.height =
                normalHeight;

        keepInsideScreen();

        updateWindow();
    }

    private void keepInsideScreen() {

        if (windowParams == null) {
            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        int screenHeight =
                getResources()
                        .getDisplayMetrics()
                        .heightPixels;

        if (windowParams.x < 0) {
            windowParams.x = 0;
        }

        if (windowParams.y < 0) {
            windowParams.y = 0;
        }

        if (windowParams.x +
                windowParams.width >
                screenWidth) {

            windowParams.x =
                    Math.max(
                            0,
                            screenWidth -
                                    windowParams.width
                    );
        }

        if (windowParams.y +
                windowParams.height >
                screenHeight) {

            windowParams.y =
                    Math.max(
                            0,
                            screenHeight -
                                    windowParams.height
                    );
        }
    }

    private void updateWindow() {

        if (windowManager == null ||
                windowParams == null) {
            return;
        }

        try {

            windowManager.updateViewLayout(
                    this,
                    windowParams
            );

        } catch (Exception ignored) {
        }
    }

    /*
     * Hareket süreleri öncekinin
     * yaklaşık iki katı.
     */

    private void doLick() {

        startReaction(
                State.LICK,
                2800
        );
    }

    private void doPaw() {

        startReaction(
                State.PAW,
                3600
        );
    }

    private void doSit() {

        startReaction(
                State.SIT,
                4000
        );
    }

    private void doSpin() {

        startReaction(
                State.SPIN,
                3200
        );
    }

    private void doBark() {

        if (reacting) {
            return;
        }

        startReaction(
                State.BARK,
                2400
        );

        playBarkSound();
    }

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
                                volumePercent /
                                        100f
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

                barkPlayer.start();
            }

        } catch (Exception ignored) {
        }
    }

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
         * BAŞ
         */
        if (ny < 0.45f &&
                nx > 0.22f &&
                nx < 0.78f) {

            doLick();
            return;
        }

        /*
         * PATİ
         */
        if (ny > 0.60f &&
                nx > 0.52f) {

            doPaw();
            return;
        }

        /*
         * GÖVDE
         */
        doSit();
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        /*
         * Hareket sürerken hiçbir
         * dokunma hareketi kesemez.
         */
        if (reacting) {
            return true;
        }

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

                    keepInsideScreen();
                    updateWindow();
                }

                return true;

            case MotionEvent.ACTION_UP:

                long now =
                        System.currentTimeMillis();

                long pressDuration =
                        now - downTime;

                if (!dragging) {

                    if (pressDuration >= 700) {

                        lastTapTime = 0;
                        doBark();

                    } else if (
                            lastTapTime != 0 &&
                            now - lastTapTime <= 350
                    ) {

                        lastTapTime = 0;
                        doSpin();

                    } else {

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
                                            !reacting &&
                                            lastTapTime ==
                                                    thisTap
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
