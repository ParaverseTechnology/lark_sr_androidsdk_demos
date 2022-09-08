package com.pxy.demo.larksr.components;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SoftJoystick implements View.OnTouchListener
{
    public static class Vector {
        public int x;
        public int y;
        public int r;
        public Vector(int x, int y, int r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }
        public void set(int x, int y, int r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }
    }

    public interface SoftJoystickEvent {
        void onRockerMove(double rx, double ry);
    }

    private static final float DAMPING = 0.1f;
    private static String TAG = "Rocker";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Vector mVector = new Vector(0,0,0);
    private final View mRockerView;
    private int moveId = 0;
    private final SoftJoystickEvent eventListener;

    public SoftJoystick(View rockerView, SoftJoystickEvent eventListener) {
        this.mRockerView = rockerView;
        this.eventListener = eventListener;
    }

    public void startListenTouch() {
        if (mRockerView != null) {
            mRockerView.setOnTouchListener(SoftJoystick.this);
        }
    }

    public void stopListenTouch() {
        if (mRockerView != null) {
            mRockerView.setOnTouchListener(null);
        }
    }
    private void stopMove() {
        moveId++;
    }
    private void startMove(int id) {
        final int _id = id;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (moveId != _id) {
                        break;
                    }
                    move();
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                Log.d(TAG, "rocker move finished");
            }
        });
    }

    private void move() {
        Vector vector = this.mVector;
        double deg = vector.x == 0 ? 0 : Math.atan((double) vector.y / (double) vector.x) * (180d / Math.PI);
        int deltaX = 0;
        int deltaY = 0;

        if (Math.abs(deg) <= 45) {
            deltaX = 1;
        } else {
            deltaY = 1;
        }

        int dx = vector.x == 0 ? 0 : vector.x / Math.abs(vector.x);
        int dy = vector.y == 0 ? 0 : vector.y / Math.abs(vector.y);

        double rx = vector.r * deltaX * dx * DAMPING;
        double ry = vector.r * deltaY * dy * DAMPING;

        if (eventListener != null) {
            eventListener.onRockerMove(rx, ry);
        }
    }

    // ontouch vent
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int stageW = v.getWidth();
        int stageH = v.getHeight();
        int radius = (int) stageH / 2;
        int xpos = (int) event.getX() - radius;
        int ypos = (int) event.getY() - radius;
        int absR = (int) Math.sqrt(xpos * xpos + ypos * ypos);
        int r = absR > radius ? radius : absR;
        mVector.set(xpos, ypos, r);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startMove(moveId);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                stopMove();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            default:
                break;
        }
        return true;
    }
}
