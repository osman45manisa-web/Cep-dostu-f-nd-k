package com.cep.dostu.findik;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class PetView extends View {

    private static final int FRAME_COUNT = 6;

    private final Bitmap sprite;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect[] frameBounds = new Rect[FRAME_COUNT];

    private int currentFrame = 0;
    private int direction = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private float touchStartX;
    private float touchStartY;

    private int windowStartX;
    private int windowStartY;

    private boolean dragging = false;

    public PetView(Context context) {
        super(context);

        setBackgroundColor(android.graphics.Color.TRANSPARENT);

        sprite = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_walk
        );

        if (sprite != null) {
            prepareFrameBounds();
        }

        handler.post(animationRunnable);
    }

    public void setWindowManager(
            WindowManager manager,
            WindowManager.LayoutParams params
    ) {
        windowManager = manager;
        windowParams = params;
    }

    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {

            currentFrame++;

            if (currentFrame >= FRAME_COUNT) {
                currentFrame = 0;
            }

            movePet();

            invalidate();

            handler.postDelayed(this, 130);
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

        int petWidth = windowParams.width;

        windowParams.x += 10 * direction;

        if (windowParams.x + petWidth >= screenWidth) {
            windowParams.x = screenWidth - petWidth;
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

    private void prepareFrameBounds() {

        int cellWidth = sprite.getWidth() / FRAME_COUNT;
        int imageHeight = sprite.getHeight();

        for (int frame = 0; frame < FRAME_COUNT; frame++) {

            int startX = frame * cellWidth;

            int minX = cellWidth;
            int minY = imageHeight;
            int maxX = 0;
            int maxY = 0;

            for (int y = 0; y < imageHeight; y += 2) {

                for (int x = 0; x < cellWidth; x += 2) {

                    int pixel = sprite.getPixel(
                            startX + x,
                            y
                    );

                    int alpha =
                            android.graphics.Color.alpha(pixel);

                    if (alpha > 20) {

                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }

            if (maxX <= minX || maxY <= minY) {

                frameBounds[frame] =
                        new Rect(
                                startX,
                                0,
                                startX + cellWidth,
                                imageHeight
                        );

            } else {

                frameBounds[frame] =
                        new Rect(
                                startX + minX,
                                minY,
                                startX + maxX,
                                maxY
                        );
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sprite == null) {
            return;
        }

        Rect source = frameBounds[currentFrame];

        if (source == null) {
            return;
        }

        float sourceWidth = source.width();
        float sourceHeight = source.height();

        float availableWidth = getWidth() - 30f;
        float availableHeight = getHeight() - 30f;

        float scale = Math.min(
                availableWidth / sourceWidth,
                availableHeight / sourceHeight
        );

        int drawWidth =
                (int) (sourceWidth * scale);

        int drawHeight =
                (int) (sourceHeight * scale);

        int left =
                (getWidth() - drawWidth) / 2;

        int top =
                getHeight() - drawHeight - 10;

        Rect destination =
                new Rect(
                        left,
                        top,
                        left + drawWidth,
                        top + drawHeight
                );

        canvas.save();

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (windowManager == null ||
                windowParams == null) {
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                dragging = true;

                touchStartX = event.getRawX();
                touchStartY = event.getRawY();

                windowStartX = windowParams.x;
                windowStartY = windowParams.y;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx =
                        event.getRawX() - touchStartX;

                float dy =
                        event.getRawY() - touchStartY;

                windowParams.x =
                        windowStartX + (int) dx;

                windowParams.y =
                        windowStartY + (int) dy;

                try {
                    windowManager.updateViewLayout(
                            this,
                            windowParams
                    );
                } catch (Exception ignored) {
                }

                return true;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:

                dragging = false;

                performClick();

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
                animationRunnable
        );
    }
}
