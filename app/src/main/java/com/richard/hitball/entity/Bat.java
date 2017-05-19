package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class Bat {
    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 20;
    private static final int DEFAULT_SPEED = 20;

    private Paint mPaint;
    private Rect mBody;
    private int mSpeed;

    public Bat() {
        mPaint = new Paint();
        mPaint.setColor(Color.MAGENTA);
        mSpeed = DEFAULT_SPEED;
    }

    public void moveLeft() {
        mBody.offset(-mSpeed, 0);
    }

    public void moveRight() {
        mBody.offset(mSpeed, 0);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(mBody, mPaint);
    }

    public void setBodyPosition(Rect body) {
        mBody = body;
    }

    public Rect getBody() {
        return mBody;
    }
}
