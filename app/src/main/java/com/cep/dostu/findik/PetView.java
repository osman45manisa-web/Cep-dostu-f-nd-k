package com.cep.dostu.findik;

import android.content.Context;
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

import java.util.Random;

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

    private final Random random =
            new Random();

    private Bitmap walk;
    private Bitmap sit;
    private Bitmap paw;
    private Bitmap bark;
    private Bitmap lick;
    private Bitmap spin;

    private State state = State.WALK;

    private int currentFrame = 0;

    // 1 = sağa, -1 = sola
    private int direction = 1;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private float downRawX;
    private float downRawY;

    private int startWindowX;
    private int startWindowY;

    private long downTime;

    private boolean dragging = false;

    private MediaPlayer barkPlayer;

    public PetView(Context context) {
        super(context);

        setBackgroundColor(
                android.graphics.Color.TRANSPARENT
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

            currentFrame++;

            if (currentFrame >= FRAME_COUNT) {

                currentFrame = 0;

                if (state != State.WALK) {
                    state = State.WALK;
                }
            }

            if (state == State.WALK) {
                movePet();
            }

            invalidate();

            handler.postDelayed(this, 140);
        }
    };

    private void movePet() {

        if (dragging ||
                windowManager == null ||
                windowParams == null) {
            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        windowParams.x +=
                8 * direction;

        if (windowParams.x +
                windowParams.width >= screenWidth) {

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

    private Bitmap getCurrentSprite() {

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
                getCurrentSprite();

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

        float scale =
                Math.min(
                        (getWidth() - 20f)
                                / frameWidth,

                        (getHeight() - 20f)
                                / frameHeight
                );

        int drawWidth =
                (int) (frameWidth * scale);

        int drawHeight =
                (int) (frameHeight * scale);

        int left =
                (getWidth() - drawWidth)
                        / 2;

        int top =
                getHeight()
                        - drawHeight
                        - 5;

        Rect destination =
                new Rect(
                        left,
                        top,
                        left + drawWidth,
                        top + drawHeight
                );

        canvas.save();

        /*
         * Kaynak yürüyüş görseli sola bakıyor.
         * Sağa giderken aynalıyoruz.
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

    private void randomReaction() {

        int choice =
                random.nextInt(3);

        if (choice == 0) {
            state = State.PAW;
        }

        else if (choice == 1) {
            state = State.LICK;
        }

        else {
            state = State.SIT;
        }

        currentFrame = 0;
    }

    private void bark() {

        state = State.BARK;
        currentFrame = 0;

        try {

            if (barkPlayer != null) {
                barkPlayer.release();
            }

            barkPlayer =
                    MediaPlayer.create(
                            getContext(),
                            R.raw.findik_bark
                    );

            barkPlayer.start();

        } catch (Exception ignored) {
        }
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

                downRawX =
                        event.getRawX();

                downRawY =
                        event.getRawY();

                startWindowX =
                        windowParams.x;

                startWindowY =
                        windowParams.y;

                downTime =
                        System.currentTimeMillis();

                dragging = false;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getRawX()
                                - downRawX;

                float dy =
                        event.getRawY()
                                - downRawY;

                if (Math.abs(dx) > 15 ||
                        Math.abs(dy) > 15) {

                    dragging = true;

                    windowParams.x =
                            startWindowX +
                                    (int) dx;

                    windowParams.y =
                            startWindowY +
                                    (int) dy;

                    try {

                        windowManager
                                .updateViewLayout(
                                        this,
                                        windowParams
                                );

                    } catch (
                            Exception ignored
                    ) {
                    }
                }

                return true;

            case MotionEvent.ACTION_UP:

                long pressDuration =
                        System.currentTimeMillis()
                                - downTime;

                if (!dragging) {

                    if (pressDuration > 650) {

                        bark();

                    } else {

                        randomReaction();
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

        handler.removeCallbacks(
                animationLoop
        );

        if (barkPlayer != null) {

            barkPlayer.release();
            barkPlayer = null;
        }
    }
}
