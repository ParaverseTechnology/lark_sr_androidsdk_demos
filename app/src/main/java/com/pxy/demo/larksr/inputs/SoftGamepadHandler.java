package com.pxy.demo.larksr.inputs;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.WindowsKeyCodes;
import com.pxy.demo.larksr.R;


public class SoftGamepadHandler implements View.OnTouchListener {
    private static final String TAG = "SoftGamepadHandler";

    private final Activity mActivity;
    private final RtcClient mRtcClient;
    public SoftGamepadHandler(Activity rtcActivity, RtcClient rtcClient) {
        mActivity = rtcActivity;
        mRtcClient = rtcClient;
        // 手柄按钮
        Button handleButtonUp = rtcActivity.findViewById(R.id.handle_button_up);
        Button handleButtonDown = rtcActivity.findViewById(R.id.handle_button_down);
        Button handleButtonLeft = rtcActivity.findViewById(R.id.handle_button_left);
        Button handleButtonRight = rtcActivity.findViewById(R.id.handle_button_right);
        handleButtonUp.setOnTouchListener(this);
        handleButtonDown.setOnTouchListener(this);
        handleButtonLeft.setOnTouchListener(this);
        handleButtonRight.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mRtcClient == null) {
            return true;
        }
        int vkey = 0;
        if (view.getId() == R.id.handle_button_up) {
            // key w
            vkey = WindowsKeyCodes.KEYCODE_W;
        } else if (view.getId() == R.id.handle_button_down) {
            // key s
            vkey = WindowsKeyCodes.KEYCODE_S;
        } else if (view.getId() == R.id.handle_button_left) {
            // key a
            vkey = WindowsKeyCodes.KEYCODE_A;
        } else if (view.getId() == R.id.handle_button_right) {
            // key d
            vkey = WindowsKeyCodes.KEYCODE_D;
        }

        int action = motionEvent.getActionMasked();
        Log.d(TAG, "on touch handle " + action + " " + vkey);
        if (action == MotionEvent.ACTION_DOWN) {
            // 3.1.1.0 添加
            mRtcClient.sendKeyDown(vkey, false);
        } else if (action == MotionEvent.ACTION_MOVE) {
            // 3.1.1.0 添加
            mRtcClient.sendKeyDown(vkey, true);
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // 3.1.1.0 添加
            mRtcClient.sendKeyUp(vkey);
        }
        return true;
    }
}
