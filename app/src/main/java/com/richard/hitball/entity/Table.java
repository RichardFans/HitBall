package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

public class Table {
    private static final int BOUNDARY_HIT_NONE = 0;
    private static final int BOUNDARY_HIT_TOP = 1;
    private static final int BOUNDARY_HIT_RIGHT = 2;
    private static final int BOUNDARY_HIT_LEFT = 4;
    private Ball mBall;
    private Bat mBat;
    private Paint mPaint;
    private Rect mBoundary;
    private Path mBoundaryPath;

    public Table(Rect boundary) {
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);

        mBoundary = boundary;

        mBoundaryPath = new Path();
        mBoundaryPath.moveTo(boundary.left, boundary.bottom);
        mBoundaryPath.lineTo(boundary.left, boundary.top);
        mBoundaryPath.lineTo(boundary.right, boundary.top);
        mBoundaryPath.lineTo(boundary.right, boundary.bottom);
    }

    public void setBall(Ball ball) {
        mBall = ball;
    }

    public void setBat(Bat bat) {
        mBat = bat;
        int left = mBoundary.centerX() - Bat.DEFAULT_WIDTH / 2;
        int top = mBoundary.bottom - Bat.DEFAULT_HEIGHT;
        int right = mBoundary.centerX() + Bat.DEFAULT_WIDTH / 2;
        int bottom = mBoundary.bottom;
        Rect body = new Rect(left, top, right, bottom);
        bat.setBodyPosition(body);
    }

    public Ball getBall() {
        return mBall;
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(mBoundaryPath, mPaint);
        int hitType = getBoundaryHitType();
        if ((hitType & BOUNDARY_HIT_TOP) == BOUNDARY_HIT_TOP) {
            mBall.reverseYSpeed();
        }
        if ((hitType & (BOUNDARY_HIT_LEFT | BOUNDARY_HIT_RIGHT)) > 0) {
            mBall.reverseXSpeed();
        }
        mBall.draw(canvas);
        mBat.draw(canvas);
    }

    private int getBoundaryHitType() {
        int type = BOUNDARY_HIT_NONE;
        Point c = mBall.getCenter();
        float r = mBall.getRadius();
        if (mBall.isToTop() && c.y - r <= 0) {
            type |= BOUNDARY_HIT_TOP;
        }
        if (mBall.isToRight() && c.x + r >= mBoundary.right && c.y < mBoundary.bottom) {
            type |= BOUNDARY_HIT_RIGHT;
        }
        if (mBall.isToLeft() && c.x - r <= 0 && c.y < mBoundary.bottom) {
            type |= BOUNDARY_HIT_LEFT;
        }
        return type;
    }
}
