package com.pxy.demo.larksr.inputs;

import android.view.MotionEvent;
import android.view.View;

import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.unit.Positon;

import java.util.HashMap;

public class TouchScreenHandler implements View.OnTouchListener {
    private static String TAG = "TouchScreenHandler";

    private int mAppWidth = RtcClient.WIDTH;
    private int mAppHeight = RtcClient.HEIGHT;
    // 记录触摸点并分配给云端应用触摸点的 id
    // 当前触摸事件按下手指的 id，同一个手指 id 相同。id 应每次触摸增加。即 touchdown 时 +1，touch
    // move 和 touchup 时保持。下一次又手指 touchdown 时再 +1 获得新的 id
    private HashMap<Integer, Integer> mTouchMap = new HashMap<>();
    private int mTouchId = 0;
    private RtcClient mRtcClient = null;

    public void setRtcClient(RtcClient rtcClient) {
        mRtcClient = rtcClient;
    }
    public void setAppSize(int width, int height) {
        this.mAppWidth = width;
        this.mAppHeight = height;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (mRtcClient == null) {
            return false;
        }
        for (int i = 0; i < event.getPointerCount(); i ++) {
            processMotionEvent(view, event, i);
        }
        return true;
    }

    private void processMotionEvent(View view, MotionEvent event, int pointerIndex) {
        // event.getPointerId();
        int actionIndex = event.getActionIndex();
        int id = event.getPointerId(pointerIndex);
        long timestamp = System.currentTimeMillis();

        int stageW = view.getWidth();
        int stageH = view.getHeight();
        Positon p = new Positon((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
        // 获取云端应用绝对坐标系
        p = Positon.getScaledAbsPosition(p, mAppWidth, mAppHeight, stageW, stageH);
        int x = p.getX();
        int y = p.getY();

        // 对应云端的 id
        int touchId = 0;

        switch (event.getActionMasked()) {
            // 触摸按下
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (actionIndex == pointerIndex) {
                    mTouchId++;
                    // reset
                    if (mTouchId < 0) {
                        mTouchId = 0;
                    }
                    mTouchMap.put(id, mTouchId);
                    touchId = mTouchId;
                    mRtcClient.sendTouchDown(x, y, timestamp, touchId);
                }
                break;

            // 触摸移动
            case MotionEvent.ACTION_MOVE:
                touchId = mTouchMap.get(id);
                mRtcClient.sendTouchMove(x, y, timestamp, touchId);
                break;

            // 触摸抬起
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_POINTER_UP:
                if (actionIndex == pointerIndex) {
                    touchId = mTouchMap.get(id);
                    mTouchMap.remove(id);
                    mRtcClient.sendTouchUp(x, y, timestamp, touchId);
//                    Log.d(TAG, "send touch up " + x + " " + y + " " + timestamp + " " + touchId);
                }
                break;
        }
    }
}
