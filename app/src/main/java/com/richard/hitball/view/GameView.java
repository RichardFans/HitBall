package com.richard.hitball.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.richard.hitball.entity.Ball;
import com.richard.hitball.entity.Bat;
import com.richard.hitball.entity.Table;


public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    public static int STAGE_READY = 1;
    public static int STAGE_PLAY = 2;
    public static int STAGE_BIRD_FALLING = 3;
    public static int STAGE_OVER = 4;

    private Table mTable;
    private Ball mBall;
    private Bat mBat;

    private boolean mIsRunning;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        Rect screenRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(screenRect);
        mTable = new Table(screenRect);
        mBall = new Ball();
        mTable.setBall(mBall);
        mBat = new Bat();
        mTable.setBat(mBat);

        mBall.setPosition(0, 600);
        mBall.shot(10, -20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mIsRunning) {
                    mTable.startBatMove(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTable.stopBatMove();
                break;
        }
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mTable.draw(canvas);
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
