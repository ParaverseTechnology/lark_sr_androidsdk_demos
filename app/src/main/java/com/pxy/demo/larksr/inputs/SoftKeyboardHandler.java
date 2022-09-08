package com.pxy.demo.larksr.inputs;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.View;

import com.pxy.lib_sr.RtcClient;
import com.pxy.demo.larksr.R;

public class SoftKeyboardHandler implements KeyboardView.OnKeyboardActionListener {
    private static final String TAG = "SoftKeyboardHandler";

    /**
     * 保存当前键盘状态
     */
    private int mCurrentKeyboard = 0;
    /**
     * 虚拟键盘
     */
    private KeyboardView mKeyboardView = null;
    /**
     * 数字键盘
     */
    private Keyboard mNumKeyboard = null;
    /**
     * 字符键盘
     */
    private Keyboard mLetterKeyboard = null;
    private final Activity mActivity;
    private final RtcClient mRtcClient;
    public SoftKeyboardHandler(Activity activity, RtcClient rtcClient) {
        mActivity = activity;
        mRtcClient = rtcClient;
        // 数组键盘
        mNumKeyboard = new Keyboard(mActivity, R.xml.cloudlark_keys_number);
        // 字母键盘
        mLetterKeyboard = new Keyboard(mActivity, R.xml.cloudlark_keys_letter);

        // 虚拟键盘. 默认使用字母键
        mKeyboardView = mActivity.findViewById(R.id.keyboardView);
        mKeyboardView.setOnKeyboardActionListener(this);
        mKeyboardView.setKeyboard(mLetterKeyboard);
        mKeyboardView.setEnabled(true);
        mKeyboardView.setFocusable(true);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setSelected(true);
    }

    public boolean isActive() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    public void toggle() {
        if (mKeyboardView.getVisibility() == View.VISIBLE) {
            mKeyboardView.setVisibility(View.INVISIBLE);
        } else {
            mKeyboardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void swipeUp() {
        Log.d(TAG, "swipeUp");
    }

    @Override
    public void swipeRight() {
        Log.d(TAG, "swipeRight");
    }

    @Override
    public void swipeLeft() {
        Log.d(TAG, "swipeLeft");
    }

    @Override
    public void swipeDown() {
        Log.d(TAG, "swipeDown");
    }

    @Override
    public void onText(CharSequence text) {
        Log.d(TAG, "onText:" + text);
    }

    @Override
    public void onPress(final int primaryCode) {
        Log.d(TAG, "on prerss" + primaryCode);
        if (primaryCode == -1) {
            return;
        }
        if (mRtcClient != null) {
            // 按键按下
            // 3.1.1.0 新增
            mRtcClient.sendKeyDown(primaryCode, false);
        }
    }

    @Override
    public void onRelease(final int primaryCode) {
        Log.d(TAG, "on release" + primaryCode);
        mActivity.runOnUiThread(() -> {
            // 切换键盘模式
            if (primaryCode == -1) {
                if (mCurrentKeyboard == 0 && mKeyboardView != null) {
                    mKeyboardView.setKeyboard(mNumKeyboard);
                    mCurrentKeyboard = 1;
                } else {
                    if (mKeyboardView != null) {
                        mKeyboardView.setKeyboard(mLetterKeyboard);
                    }
                    mCurrentKeyboard = 0;
                }
                return;
            }
            if (mRtcClient != null) {
                // 按键抬起
                // 3.1.1.0 新增
                mRtcClient.sendKeyUp(primaryCode);
            }
        });
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
    }
}
