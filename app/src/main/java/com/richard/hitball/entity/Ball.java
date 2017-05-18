package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Ball {
    private static final float RADIUS = 30.0f;
    private static final int INIT_POS_CX = 5;
    private static final int INIT_POS_CY = 5;
    private static final int DEFAULT_SPEED = 1;
    private Paint mPaint;

    private Point mCenter;
    private float mRadius;

    private Point mSpeed;

    public Ball(Paint paint) {
        mPaint = paint;
        mCenter = new Point(INIT_POS_CX, INIT_POS_CY);
        mRadius = RADIUS;
        mSpeed = new Point(0, 0);
    }

    public void shot(int x, int y) {
        mSpeed.x = x;
        mSpeed.y = y;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mPaint);
        mCenter.offset(mSpeed.x, mSpeed.y);
    }
}
