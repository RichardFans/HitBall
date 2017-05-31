package com.richard.hitball.entity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private static final int HIT_NONE = 0;
    private static final int HIT_TOP = 1;
    private static final int HIT_RIGHT = 2;
    private static final int HIT_BOTTOM = 4;
    private static final int HIT_LEFT = 8;
    private static final int ROW_NUM = 20;
    private static final int COL_NUM = 5;
    private static final float BRICK_BORDER = 5f;
    private Ball mBall;

    private Bat mBat;
    private boolean isBatMoving;
    private boolean isBatMoveToLeft;

    private Paint mPaintBoundary, mPaintGameOver;
    private Rect mBoundary;
    private Path mBoundaryPath;

    private boolean mShowGameOver;
    private boolean mShowGamePass;

    private Cell[][] mCells;
    private int mCellWidth, mCellHeight;

    private SoundPool mSoundPool;
    private List<Integer> mSounds = new ArrayList<>();
    private AssetManager mAssetManager;
    private Rect mNormalBatBody;

    public Table(Context context, Rect boundary) {
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
        loadSound(context);
    }

    private void loadSound(Context context) {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        mAssetManager = context.getAssets();
        try {
            String[] filenames = mAssetManager.list("sounds");
            for (String filename : filenames) {
                AssetFileDescriptor fd = mAssetManager.openFd("sounds/" + filename);
                int soundId = mSoundPool.load(fd, 0);
                mSounds.add(soundId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLevel() {
        mCells = new Cell[ROW_NUM][COL_NUM];
        mCellWidth = mBoundary.width() / COL_NUM;
        mCellHeight = mBoundary.height() / ROW_NUM;
        try {
            String[] filenames = mAssetManager.list("levels");
            // TODO: 应该根据关卡加载
            String filename = filenames[0];
            loadLevel(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLevel(String filename) {
        try {
            InputStream inputStream = mAssetManager.open("levels/" + filename);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int row = 0;
            Paint paint = new Paint();
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(",");
                for (int col = 0; col < cells.length; col++) {
                    String cell = cells[col];
                    if (cell.equals("x")) {
                        int blood = (int) (Math.floor(Math.random() * 3));
                        Cell brick = new Brick(row, col, mCellWidth, mCellHeight, blood);
                        brick.setPaint(paint);
                        mCells[row][col] = brick;
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBall(Ball ball) {
        mBall = ball;
        mBall.setRadius(mBoundary.width() / 20);
    }

    public void setBat(Bat bat) {
        mBat = bat;
        mBat.setWidth(mBoundary.width() / 3);
    }

    public void showGameOver() {
        mShowGameOver = true;
        mBall.stop();
    }

    public void showGamePass() {
        mShowGamePass = true;
        mBall.stop();
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.LTGRAY);
        if (mShowGameOver) {
            canvas.drawText("Game Over!", mBoundary.centerX() - 218, mBoundary.centerY(), mPaintGameOver);
        } else if (mShowGamePass) {
            canvas.drawText("过关了!", mBoundary.centerX() - 168, mBoundary.centerY(), mPaintGameOver);
        }
        // 绘制边界
        canvas.drawPath(mBoundaryPath, mPaintBoundary);

        // 绘制砖块
        for (int row = 0; row < ROW_NUM; row++) {
            for (int col = 0; col < COL_NUM; col++) {
                Cell cell = mCells[row][col];
                if (cell != null) {
                    cell.draw(canvas);
                }
            }
        }

        // 判断球是否和边界碰撞
        int hitType = getHitType();
        if ((hitType & (HIT_TOP | HIT_BOTTOM)) > 0) {
            mBall.reverseYSpeed();
        }
        if ((hitType & (HIT_LEFT | HIT_RIGHT)) > 0) {
            mBall.reverseXSpeed();
        }
        if (isBatHit() && mBall.isToBottom()) {
            mBall.reverseYSpeed();
        }
        mBall.draw(canvas);

        moveBat();
        mBat.draw(canvas);
    }

    private boolean isBatHit() {
        Point c = mBall.getCenter();
        float r = mBall.getRadius();
        Rect batBody = mBat.getBody();
        if (c.x >= batBody.left && c.x <= batBody.right) {
            if (c.y - r < batBody.bottom && c.y + r > batBody.top) {
                return true;
            }
        }
        return false;
    }

    private int getHitType() {
        int type = HIT_NONE;
        Point c = mBall.getCenter();
        float r = mBall.getRadius();
        int row = c.y / mCellHeight;
        int col = c.x / mCellWidth;
        Cell cell = null;
        boolean hitCell = false;
        Rect body = null;
        boolean ballInTable = mBoundary.contains(c.x, c.y);
        // 判断撞头
        if (ballInTable && row > 0) {
            cell = mCells[row - 1][col];
            if (cell != null) {
                body = cell.getBody();
                hitCell = c.y > body.bottom && c.y - r <= body.bottom;
                if (hitCell) {
                    playHitBrickSound(cell);
                    if (cell.hit()) {
                        mCells[cell.row][cell.col] = null;
                    }
                }
            }
        }
        if (mBall.isToTop() && (c.y - r <= 0 || hitCell)) {
            type |= HIT_TOP;
        }
        // 判断撞右边
        hitCell = false;
        if (ballInTable && col < COL_NUM - 1) {
            cell = mCells[row][col + 1];
            if (cell != null) {
                body = cell.getBody();
                hitCell = c.x < body.left && c.x + r >= body.left;
                if (hitCell) {
                    playHitBrickSound(cell);
                    if (cell.hit()) {
                        mCells[cell.row][cell.col] = null;
                    }
                }
            }
        }
        if (mBall.isToRight() &&
                (c.x + r >= mBoundary.right && c.y < mBoundary.bottom || hitCell)) {
            type |= HIT_RIGHT;
        }
        // 判断撞左边
        hitCell = false;
        if (ballInTable && col > 0) {
            cell = mCells[row][col - 1];
            if (cell != null) {
                body = cell.getBody();
                hitCell = c.x > body.right && c.x - r <= body.right;
                if (hitCell) {
                    playHitBrickSound(cell);
                    if (cell.hit()) {
                        mCells[cell.row][cell.col] = null;
                    }
                }
            }
        }
        if (mBall.isToLeft() &&
                ((c.x - r <= 0 && c.y < mBoundary.bottom) || hitCell)) {
            type |= HIT_LEFT;
        }
        // 判断撞下边
        if (ballInTable && row < ROW_NUM - 1) {
            cell = mCells[row + 1][col];
            if (cell != null) {
                body = cell.getBody();
                hitCell = c.y < body.top && c.y + r >= body.top;
                if (hitCell) {
                    playHitBrickSound(cell);
                    if (cell.hit()) {
                        mCells[cell.row][cell.col] = null;
                    }
                }
            }
        }
        if (mBall.isToBottom() && hitCell) {
            type |= HIT_BOTTOM;
        }
        return type;
    }

    private void playHitBrickSound(Cell cell) {
        mSoundPool.play(mSounds.get(cell.getBlood()), 1f, 1f, 0, 0, 1);
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

    public void startBatMove(double roll) {
        if (isBatMoving) {
            if (isBatMoveToLeft) {
                if (roll < 8 && roll > -10) {
                    isBatMoving = false;
                } else if (roll <= -10) {
                    isBatMoveToLeft = true;
                }
            } else {
                if (roll > -8 && roll < 10) {
                    isBatMoving = false;
                } else if (roll >= 10) {
                    isBatMoveToLeft = false;
                }
            }
        } else {
            if (roll <= -10) {
                isBatMoving = true;
                isBatMoveToLeft = true;
            } else if (roll >= 10) {
                isBatMoving = true;
                isBatMoveToLeft = false;
            }
        }
    }

    public void changeBatBody(double pitch) {
        Rect body = mBat.getBody();
        boolean wider = body.width() == mBoundary.width();
        boolean higher = body.height() > mNormalBatBody.height();
        if (wider) {
            if (pitch > -25) {
                body.left = mNormalBatBody.left;
                body.right = mNormalBatBody.right;
            }
        } else {
            if (pitch < -30) {
                body.left = mBoundary.left;
                body.right = mBoundary.right;
            }
        }
        if (higher) {
            if (pitch < 10) {
                body.top = mNormalBatBody.top;
            }
        } else {
            if (pitch > 15) {
                body.top = body.bottom - 10 * body.height();
            }
        }
    }

    public void stopBatMove() {
        isBatMoving = false;
    }

    public void reset() {
        int left = mBoundary.centerX() - mBat.getWidth() / 2;
        int top = mBoundary.bottom - Bat.DEFAULT_HEIGHT;
        int right = mBoundary.centerX() + mBat.getWidth() / 2;
        int bottom = mBoundary.bottom;
        Rect body = new Rect(left, top, right, bottom);
        mNormalBatBody = new Rect(body);
        mBat.setBodyPosition(body);
        mBall.setPosition(mBoundary.centerX(), (int) (top - mBall.getRadius()));
        mBall.stop();
        loadLevel();
        mShowGameOver = false;
        mShowGamePass = false;
    }

    public void shotBall() {
        mBall.shot(20, -20);
    }

    public boolean isBallOutside() {
        Point c = mBall.getCenter();
        return c.y - 100 > mBoundary.bottom;
    }

    public boolean hasNoneBrick() {
        for (int row = 0; row < ROW_NUM; row++) {
            for (int col = 0; col < COL_NUM; col++) {
                Cell cell = mCells[row][col];
                if (cell instanceof Brick) {
                    return false;
                }
            }
        }
        return true;
    }
}
