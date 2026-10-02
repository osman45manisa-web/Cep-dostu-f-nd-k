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

    private final Bitmap sprite;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int frameCount = 6;
    private int currentFrame = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private float touchStartX;
    private float touchStartY;

    private int windowStartX;
    private int windowStartY;

    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            currentFrame++;

            if (currentFrame >= frameCount) {
                currentFrame = 0;
            }

            invalidate();
            handler.postDelayed(this, 180);
        }
    };

    public PetView(Context context) {
        super(context);

        setBackgroundColor(android.graphics.Color.TRANSPARENT);

        sprite = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_idle
        );

        handler.post(animator);
    }

    public void setWindowManager(
            WindowManager manager,
            WindowManager.LayoutParams params
    ) {
        windowManager = manager;
        windowParams = params;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (sprite == null) {
            return;
        }

        int frameWidth = sprite.getWidth() / frameCount;
        int frameHeight = sprite.getHeight();

        int left = currentFrame * frameWidth;

        Rect source = new Rect(
                left,
                0,
                left + frameWidth,
                frameHeight
        );

        int padding = 8;

        Rect destination = new Rect(
                padding,
                padding,
                getWidth() - padding,
                getHeight() - padding
        );

        canvas.drawBitmap(
                sprite,
                source,
                destination,
                paint
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (windowManager == null || windowParams == null) {
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                touchStartX = event.getRawX();
                touchStartY = event.getRawY();

                windowStartX = windowParams.x;
                windowStartY = windowParams.y;

                return true;

            case MotionEvent.ACTION_MOVE:

                float dx = event.getRawX() - touchStartX;
                float dy = event.getRawY() - touchStartY;

                windowParams.x = windowStartX + (int) dx;
                windowParams.y = windowStartY + (int) dy;

                windowManager.updateViewLayout(
                        this,
                        windowParams
                );

                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(animator);
    }
}
