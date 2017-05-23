package com.richard.hitball.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class Brick extends Cell {
    private static final int BRICK_GAP = 10;
    private static final float BRICK_BORDER = 5f;

    private int mBlood;

    private static int[] sBloodColors = {
            Color.RED, Color.YELLOW, Color.GREEN
    };

    public Brick(int row, int col, int width, int height, int blood) {
        int left = col * width + BRICK_GAP / 2;
        int right = left + width - 3 * BRICK_GAP / 2;
        int top = row * height + BRICK_GAP;
        int bottom = top + height - BRICK_GAP;
        mBody = new Rect(left, top, right, bottom);
        mBlood = blood;
        this.row = row;
        this.col = col;
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setColor(sBloodColors[mBlood]);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(mBody, mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mBody.left + BRICK_BORDER,
                mBody.top + BRICK_BORDER,
                mBody.right - BRICK_BORDER,
                mBody.bottom - BRICK_BORDER,
                mPaint);
    }

    @Override
    public boolean hit() {
        mBlood--;
        return mBlood < 0;
    }

    @Override
    public int getBlood() {
        return mBlood;
    }
}
