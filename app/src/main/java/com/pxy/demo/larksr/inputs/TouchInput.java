package com.pxy.demo.larksr.inputs;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;

import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.ClientInput;
import com.pxy.lib_sr.unit.Positon;
import com.pxy.demo.larksr.components.Gesture;

public class TouchInput {
    private static final String TAG = "TouchInput";

    private int mAppWidth = RtcClient.WIDTH;
    private int mAppHeight = RtcClient.HEIGHT;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public TouchInput() {
        mHandlerThread = new HandlerThread("touch_input_thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void setAppSize(int width, int height) {
        this.mAppWidth = width;
        this.mAppHeight = height;
    }

    public void handleGestureEvent(RtcClient rtcClient, Gesture.GestureMovement gestureMovement, View view) {
        if (rtcClient == null) {
            return;
        }
        int stageW = view.getWidth();
        int stageH = view.getHeight();
        Positon p = new Positon(gestureMovement.getX(), gestureMovement.getY());
        // 获取云端应用绝对坐标系
        p = Positon.getScaledAbsPosition(p, mAppWidth, mAppHeight, stageW, stageH);
        int x = p.getX();
        int y = p.getY();
        int rx = gestureMovement.getRx() * Gesture.LOCK_MOUSE_MOVE_SPEED;
        int ry = gestureMovement.getRy() * Gesture.LOCK_MOUSE_MOVE_SPEED;

        switch (gestureMovement.getType()) {
            // 单指触摸屏幕开始
            case Gesture.SINGLE_FINGER_TOUCH_START:
                // 移动鼠标
                rtcClient.sendMouseMove(x, y, rx, ry);
                break;
            // 单指点击事件。单指离开屏幕并且未出发移动事件时触发。
            case Gesture.SINGLE_FINGER_TAP:
                Log.v(TAG, "SINGLE_FINGER_TAP ");
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                mHandler.post(() -> {
                    rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                });
                mHandler.postDelayed(() -> {
                    rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                }, 100);
                break;
            // 单指触摸抬起
            case Gesture.SINGLE_FINGER_TAP_DOWN:
                Log.v(TAG, "SINGLE_FINGER_TAP_DOWN ");
                // 按下鼠标
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                break;
            case Gesture.SINGLE_FINGER_TAP_UP:
                Log.v(TAG, "SINGLE_FINGER_TAP_UP ");
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                break;
            // 单指在屏幕上滑动时触发。
            case Gesture.SINGLE_FINGER_SWIPE:
                Log.v(TAG, "SINGLE_FINGER_SWIPE ");
                // 鼠标移动
                boolean res = rtcClient.sendMouseMove(x, y, rx, ry);
                break;
            // 单指在屏幕上滑动开始时触发。
            case Gesture.SINGLE_FINGER_SWIPE_START:
                Log.v(TAG, "SINGLE_FINGER_SWIPE_START ");
                // 3.1.1.0 修改
                rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                break;
            // 单指在屏幕上滑动离开屏幕时触发。
            case Gesture.SINGLE_FINGER_SWIPE_END:
                Log.v(TAG, "SINGLE_FINGER_SWIPE_END ");
                // 3.1.1.0 修改
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                break;
            // 双指点击事件。单指离开屏幕并且未出发移动事件时触发。
            case Gesture.DOUBLE_FINGER_TAP:
                // 右键单击
                rtcClient.sendMouseMove(x, y, rx, ry);
                mHandler.post(() -> {
                    // 3.1.1.0 修改
                    rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                });
                mHandler.postDelayed(() -> {
                    // 3.1.1.0 修改
                    rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                }, 100);
                break;
            //  双指单指在屏幕上滑动时触发。
            case Gesture.DOUBLE_FINGER_SWIPE:
                // 滚轮滚动
                if (gestureMovement.getDistance() > 0) {
                    // 滚轮向上移动
                    rtcClient.sendMouseWheel(x, y, 120);
                } else if (gestureMovement.getDistance() < 0){
                    // 滚轮向下移动
                    rtcClient.sendMouseWheel(x, y, -120);
                } else {
                    rtcClient.sendMouseMove(x, y, rx, ry);
                }
                // 滚轮滚动时不移动鼠标。
                return;
            // 双指在屏幕上滑动开始时触发。
            case Gesture.DOUBLE_FINGER_SWIPE_START:
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                break;
            // 双指在屏幕上滑动离开屏幕时触发。
            case Gesture.DOUBLE_FINGER_SWIPE_END:
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                break;
            // 三指点击事件。单指离开屏幕并且未出发移动事件时触发。
            case Gesture.TRIPLE_FINGER_TAP:
                // 鼠标中键单击
                rtcClient.sendMouseMove(x, y, rx, ry);
                mHandler.post(() -> {
                    rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_MIDDLE);
                });
                mHandler.postDelayed(() -> {
                    rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_MIDDLE);
                },  100);
                // 3.1.1.0 修改
                break;
            // 三指单指在屏幕上滑动时触发。
            case Gesture.TRIPLE_FINGER_SWIPE:
                // 移动鼠标
                rtcClient.sendMouseMove(x, y, rx, ry);
                break;
            // 三指在屏幕上滑动开始时触发。
            case Gesture.TRIPLE_FINGER_SWIPE_START:
                // 鼠标右键按下
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseDown(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                break;
            // 三指在屏幕上滑动离开屏幕时触发。
            case Gesture.TRIPLE_FINGER_SWIPE_END:
                // 鼠标右键抬起
                rtcClient.sendMouseMove(x, y, rx, ry);
                // 3.1.1.0 修改
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                break;
            // 手指离开屏幕或者点击区域或被打断时触发。
            case Gesture.FINGER_RELEASE:
                // 3.1.1.0 修改
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_LEFT);
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_RIGHT);
                rtcClient.sendMouseUp(x, y, ClientInput.MouseKey.MOUSE_KEY_MIDDLE);
                break;
        }
    }
}
