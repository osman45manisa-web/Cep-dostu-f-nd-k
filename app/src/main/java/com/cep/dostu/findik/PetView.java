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

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

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

    private boolean dragging = false;
    private boolean reacting = false;

    private MediaPlayer player;

    public PetView(Context context) {
        super(context);

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

            long speed;

            if (state == State.WALK) {
                speed = 170;
            } else {
                speed = 260;
            }

            handler.postDelayed(this, speed);
        }
    };

    private void movePet() {

        if (windowManager == null || windowParams == null) {
            return;
        }

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        windowParams.x += 6 * direction;

        if (windowParams.x + windowParams.width >= screenWidth) {
            windowParams.x = screenWidth - windowParams.width;
            direction = -1;
        }

        if (windowParams.x <= 0) {
            windowParams.x = 0;
            direction = 1;
        }

        try {
            windowManager.updateViewLayout(this, windowParams);
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

        int left =
                currentFrame * frameWidth;

        Rect source = new Rect(
                left,
                0,
                left + frameWidth,
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

        Rect dest = new Rect(
                drawLeft,
                drawTop,
                drawLeft + drawWidth,
                drawTop + drawHeight
        );

        canvas.save();

        if (direction > 0) {
            canvas.scale(
                    -1,
                    1,
                    getWidth() / 2f,
                    getHeight() / 2f
            );
        }

        canvas.drawBitmap(
                sprite,
                source,
                dest,
                paint
        );

        canvas.restore();
    }

    private void playSound(int soundRes) {

        try {

            if (player != null) {
                player.release();
                player = null;
            }

            player =
                    MediaPlayer.create(
                            getContext(),
                            soundRes
                    );

            if (player != null) {
                player.start();
            }

        } catch (Exception ignored) {
        }
    }

    private void shortReaction() {

        if (reacting) {
            return;
        }

        reacting = true;
        currentFrame = 0;

        int choice = random.nextInt(3);

        if (choice == 0) {
            state = State.PAW;
        } else if (choice == 1) {
            state = State.LICK;
        } else {
            state = State.SIT;
        }

        playSound(R.raw.findik_happy);
    }

    private void barkReaction() {

        if (reacting) {
            return;
        }

        reacting = true;
        state = State.BARK;
        currentFrame = 0;

        playSound(R.raw.findik_bark);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (windowManager == null || windowParams == null) {
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                downX = event.getRawX();
                downY = event.getRawY();

                startX = windowParams.x;
                startY = windowParams.y;

                downTime = System.currentTimeMillis();

                dragging = false;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getRawX() - downX;

                float dy =
                        event.getRawY() - downY;

                if (Math.abs(dx) > 18 ||
                        Math.abs(dy) > 18) {

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

                long duration =
                        System.currentTimeMillis() - downTime;

                if (!dragging) {

                    if (duration > 650) {
                        barkReaction();
                    } else {
                        shortReaction();
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

        handler.removeCallbacks(animationLoop);

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
