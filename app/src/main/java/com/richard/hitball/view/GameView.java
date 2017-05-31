package com.richard.hitball.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.richard.hitball.entity.Ball;
import com.richard.hitball.entity.Bat;
import com.richard.hitball.entity.Table;


public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback,
        SensorEventListener {
    public static int STATE_READY = 1;
    public static int STATE_PLAY = 2;
    public static int STATE_PASS = 3;
    public static int STATE_OVER = 4;

    private int mState;

    private Table mTable;
    private Ball mBall;
    private Bat mBat;

    private boolean mIsRunning;
    private GestureDetector mGestureDetector;
    private SensorManager mSensorManager;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        mGestureDetector = new GestureDetector(getContext(), new GameGestureDetector());

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        boolean ok = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        Log.d("mytag", "ok = " + ok);
        Log.d("mytag", "sensor = " + sensor);

        WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        Rect screenRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(screenRect);

        mTable = new Table(context, screenRect);
        mBall = new Ball();
        mTable.setBall(mBall);
        mBat = new Bat();
        mTable.setBat(mBat);
        mTable.reset();
        mState = STATE_READY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mIsRunning && mState == STATE_PLAY) {
                    mTable.startBatMove(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTable.stopBatMove();
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mIsRunning || mState != STATE_PLAY) return;
        int sensorType = event.sensor.getType();
        float[] rotationMatrix;
        switch (sensorType) {
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                float[] orientationValues = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationValues);

                double pitch = Math.toDegrees(orientationValues[1]);
                double roll = Math.toDegrees(orientationValues[2]);
                Log.e("mytag", "pitch = " + pitch + ", roll = " + roll);
                mTable.startBatMove(roll);
                mTable.changeBatBody(pitch);
                break;
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mTable.draw(canvas);
        if (mTable.isBallOutside()) {
            mState = STATE_OVER;
            mTable.showGameOver();
        } else if (mTable.hasNoneBrick()) {
            mState = STATE_PASS;
            mTable.showGamePass();
        }
    }

    @Override
    public void run() {
        while (mIsRunning) {
            Canvas canvas = getHolder().lockCanvas();
            synchronized (mTable) {
                draw(canvas);
            }
            getHolder().unlockCanvasAndPost(canvas);
            sleep(20);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class GameGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mIsRunning) {
                synchronized (mTable) {
                    if (mState == STATE_READY) {
                        mTable.shotBall();
                        mState = STATE_PLAY;
                    } else if (mState == STATE_OVER || mState == STATE_PASS) {
                        mState = STATE_READY;
                        mTable.reset();
                    }
                }
            }
            return true;
        }
    }
}
