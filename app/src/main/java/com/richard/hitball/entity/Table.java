package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

public class Table {
    private static final int BOUNDARY_HIT_NONE = 0;
    private static final int BOUNDARY_HIT_TOP = 1;
    private static final int BOUNDARY_HIT_RIGHT = 2;
    private static final int BOUNDARY_HIT_LEFT = 4;
    private Ball mBall;

    private Bat mBat;
    private boolean isBatMoving;
    private boolean isBatMoveToLeft;

    private Paint mPaintBoundary, mPaintGameOver;
    private Rect mBoundary;
    private Path mBoundaryPath;

    private boolean mShowGameOver;

    public Table(Rect boundary) {
        mPaintBoundary = new Paint();
        mPaintBoundary.setStrokeWidth(6);
        mPaintBoundary.setStyle(Paint.Style.STROKE);
        mPaintBoundary.setColor(Color.GREEN);

        mPaintGameOver = new Paint();
        mPaintGameOver.setStyle(Paint.Style.FILL);
        mPaintGameOver.setTextSize(78);
        mPaintGameOver.setColor(Color.RED);

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
    }

    public void showGameOver() {
        mShowGameOver = true;
        mBall.stop();
    }

    public void draw(Canvas canvas) {
        if (mShowGameOver) {
            canvas.drawText("Game Over!", mBoundary.centerX() - 218, mBoundary.centerY(), mPaintGameOver);
        }

        canvas.drawPath(mBoundaryPath, mPaintBoundary);
        int hitType = getBoundaryHitType();
        if ((hitType & BOUNDARY_HIT_TOP) == BOUNDARY_HIT_TOP) {
            mBall.reverseYSpeed();
        }
        if ((hitType & (BOUNDARY_HIT_LEFT | BOUNDARY_HIT_RIGHT)) > 0) {
            mBall.reverseXSpeed();
        }
        mBall.draw(canvas);

        moveBat();
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

    public void moveBat() {
        if (isBatMoving) {
            if (isBatMoveToLeft) {
                if (mBat.getBody().left > mBoundary.left) mBat.moveLeft();
            } else {
                if (mBat.getBody().right < mBoundary.right) mBat.moveRight();
            }
        }
    }

    public void startBatMove(MotionEvent e) {
        if (mBoundary.contains((int) e.getX(), (int) e.getY())) {
            isBatMoving = true;
            if (e.getX() > mBoundary.centerX()) { // move right
                if (mBat.getBody().right < mBoundary.right) isBatMoveToLeft = false;
            } else {
                if (mBat.getBody().left > mBoundary.left) isBatMoveToLeft = true;
            }
        }
    }

    public void stopBatMove() {
        isBatMoving = false;
    }

    public void reset() {
        int left = mBoundary.centerX() - Bat.DEFAULT_WIDTH / 2;
        int top = mBoundary.bottom - Bat.DEFAULT_HEIGHT;
        int right = mBoundary.centerX() + Bat.DEFAULT_WIDTH / 2;
        int bottom = mBoundary.bottom;
        Rect body = new Rect(left, top, right, bottom);
        mBat.setBodyPosition(body);
        mBall.setPosition(mBoundary.centerX(), (int) (top - mBall.getRadius()));
        mBall.stop();
        mShowGameOver = false;
    }

    public void shotBall() {
        mBall.shot(20, -20);
    }

    public boolean isBallOutside() {
        Point c = mBall.getCenter();
        return c.y - 100 > mBoundary.bottom;
    }
}
