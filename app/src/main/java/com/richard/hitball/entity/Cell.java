package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

abstract public class Cell {
    protected Paint mPaint;

    protected Rect mBody;

    public int row, col;

    public Rect getBody() {
        return mBody;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public abstract void draw(Canvas canvas);

    public abstract boolean hit();

    public abstract int getBlood();
}
