package com.richard.hitball.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.richard.hitball.entity.Ball;


public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    public static int STAGE_READY = 1;
    public static int STAGE_PLAY = 2;
    public static int STAGE_BIRD_FALLING = 3;
    public static int STAGE_OVER = 4;

    private Ball mBall;

    private Paint mPaint;
    private boolean mIsRunning;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);
        mPaint.setColor(Color.RED);

        getHolder().addCallback(this);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mBall.draw(canvas);
    }

    @Override
    public void run() {
        while (mIsRunning) {
            Canvas canvas = getHolder().lockCanvas();
            draw(canvas);
            getHolder().unlockCanvasAndPost(canvas);
//            sleep(20);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsRunning = true;
        mBall = new Ball(mPaint);
        mBall.shot(10, 20);
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }
}
