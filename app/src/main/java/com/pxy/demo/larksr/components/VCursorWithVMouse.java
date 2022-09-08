package com.pxy.demo.larksr.components;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.pxy.demo.larksr.R;
import com.pxy.demo.larksr.inputs.Dpad;
import com.pxy.lib_sr.input.WindowsXInputGamepad;

public final class VCursorWithVMouse {
    private static String TAG = "VCursorWithVMouse";

    private static final int SPEED = 10;

    private final View mVCursorView;
    private final VCursorEvent mCallback;
    private float mTouchX = 0;
    private float mTouchY = 0;
    private Dpad mDpad = new Dpad();
    private ImageButton mVMouseLeft;
    private ImageButton mVMouseRight;
    private ImageButton mVMouseBottom;

    public interface VCursorEvent {
        void onMouseLeftDown(float x, float y);
        void onMouseLeftUp(float x, float y);
        void onMouseRightDown(float x, float y);
        void onMouseRightUp(float x, float y);
        void onMouseMove(float x, float y, float rx, float ry);
        void onMouseWheel(float x, float y, float delta);
    }

    @SuppressLint("ClickableViewAccessibility")
    public VCursorWithVMouse(View vCursorView, VCursorEvent callback) {
        mVCursorView = vCursorView;
        mCallback = callback;

        mVMouseLeft = (ImageButton) vCursorView.findViewById(R.id.vmouse_left);
        mVMouseRight = (ImageButton) vCursorView.findViewById(R.id.vmouse_right);
        mVMouseBottom = (ImageButton) vCursorView.findViewById(R.id.vmouse_bottom);

        mVMouseLeft.setOnTouchListener(handleListener);
        mVMouseRight.setOnTouchListener(handleListener);
        mVMouseBottom.setOnTouchListener(handleListener);
    }

    public View getVCursorView() {
        return mVCursorView;
    }

    public boolean isActive() {
        return mVCursorView.getVisibility() == View.VISIBLE;
    }
    public void hide() {
        mVCursorView.setVisibility(View.INVISIBLE);
    }
    public void show() {
        mVCursorView.setVisibility(View.VISIBLE);
    }
    public void toggle() {
        mVCursorView.setVisibility(isActive() ? View.INVISIBLE : View.VISIBLE);
    }
    /**
     *
     * @param keyEvent
     * @return
     */
    public boolean handelKeyEvent(KeyEvent keyEvent) {
        if (WindowsXInputGamepad.isJoystickDevice(keyEvent) || WindowsXInputGamepad.isDpadDevice(keyEvent)) {
            // handle dpad
            return handleDpad(keyEvent);
        } else if (WindowsXInputGamepad.isGamepadDevice(keyEvent)) {
            return handleGamepad(keyEvent);
        }
        return false;
    }

    /**
     *
     * @param motionEvent
     * @return
     */
    public boolean handleMotionEvent(MotionEvent motionEvent) {
        if (WindowsXInputGamepad.isJoystickDevice(motionEvent)) {
            return handleJoystick(motionEvent);
        } else if (WindowsXInputGamepad.isDpadDevice(motionEvent)) {
            // handle dpad
            return handleDpad(motionEvent);
        }
        return false;
    }

    private View.OnTouchListener handleListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            ImageButton btn = (ImageButton) view;
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            float oldX = mVCursorView.getX();
            float oldY = mVCursorView.getY();
            float rx = mTouchX != 0 ? rawX - mTouchX : 0;
            float ry = mTouchY != 0 ? rawY - mTouchY : 0;
            float nX = oldX + rx;
            float nY = oldY + ry;

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    Log.v(TAG, "ACTION_DOWN");
                    btn.setPressed(true);
                    if (btn.getId() == R.id.vmouse_left) {
                        mCallback.onMouseLeftDown(oldX, oldY);
                    } else if (btn.getId() == R.id.vmouse_right) {
                        mCallback.onMouseRightDown(oldX, oldY);
                    } else {
                        mCallback.onMouseMove(oldX, oldY, rx, ry);
                    }
                    mTouchX = rawX;
                    mTouchY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.v(TAG, "ACTION_MOVE. rx: " + rx + "; ry:" + ry + ";" + nX + ";" + nY);
                    if (nX > 0) mVCursorView.setX(nX);
                    if (nY > 0) mVCursorView.setY(nY);
                    if (Math.abs(rx) > 1 || Math.abs(ry) > 1) {
                        mCallback.onMouseMove(nX, nY, rx, ry);
                    }
                    mTouchX = rawX;
                    mTouchY = rawY;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.v(TAG, "ACTION_CANCEL");
                case MotionEvent.ACTION_UP:
                    Log.v(TAG, "ACTION_UP");
                    btn.setPressed(false);
                    if (btn.getId() == R.id.vmouse_left) {
                        mCallback.onMouseLeftUp(oldX, oldY);
                    } else if (btn.getId() == R.id.vmouse_right) {
                        mCallback.onMouseRightUp(oldX, oldY);
                    } else {
                        mCallback.onMouseMove(oldX, oldY, rx, ry);
                    }
                    // clear last touch.
                    mTouchX = 0;
                    mTouchY = 0;
                    break;
                default:
                    Log.v(TAG, "un handle movement");
                    break;
            }
            return true;
        }
    };

    private boolean handleJoystick(MotionEvent motionEvent) {
        // TODO skip for now.
        return true;
    }

    private boolean handleDpad(InputEvent keyEvent) {
        float oldX = mVCursorView.getX();
        float oldY = mVCursorView.getY();
        float rx = 0;
        float ry = 0;
        float nx = oldX;
        float ny = oldY;
        // handle dpad
        int code = mDpad.handleInput(keyEvent);
        switch (code) {
            case Dpad.LEFT:
                rx = -1 * SPEED;
                break;
            case Dpad.RIGHT:
                rx = 1 * SPEED;
                break;
            case Dpad.UP:
                ry = -1 * SPEED;
                break;
            case Dpad.DOWN:
                ry = 1 * SPEED;
                break;
            case Dpad.CENTER:
                if (mDpad.isDown()) {
                    mCallback.onMouseLeftDown(nx, ny);
                    mVMouseLeft.setPressed(true);
                } else {
                    mCallback.onMouseLeftUp(nx, ny);
                    mVMouseLeft.setPressed(false);
                }
                return true;
            case Dpad.MENU:
                if (mDpad.isDown()) {
                    mCallback.onMouseRightDown(nx, ny);
                    mVMouseRight.setPressed(true);
                } else {
                    mCallback.onMouseRightUp(nx, ny);
                    mVMouseRight.setPressed(false);
                }
                return true;
            default:
                return false;
        }
        nx = rx + oldX >= 0 ? rx + oldX : 0;
        ny = ry + oldY >= 0 ? ry + oldY : 0;
        mCallback.onMouseMove(nx, ny, rx, ry);
        mVCursorView.setX(nx);
        mVCursorView.setY(ny);
        return true;
    }

    /**
     *      Y                    WheelUp
     *      |                       |
     *   X-----B ===> MouseLeft-----------MouseRight
     *      |                       |
     *      A                    WheelDown
     * @param keyEvent
     */
    private boolean handleGamepad(KeyEvent keyEvent) {
        float x = mVCursorView.getX();
        float y = mVCursorView.getY();
        boolean isDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_X:
                if (isDown) {
                    mCallback.onMouseLeftDown(x, y);
                    mVMouseLeft.setPressed(true);
                } else {
                    mCallback.onMouseLeftUp(x, y);
                    mVMouseLeft.setPressed(false);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                if (isDown) {
                    mCallback.onMouseRightDown(x, y);
                    mVMouseRight.setPressed(true);
                } else {
                    mCallback.onMouseRightUp(x, y);
                    mVMouseRight.setPressed(false);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                // windows 消息 deltaY 上是 +120 下是 -120
                mCallback.onMouseWheel(x, y, 120);
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                // windows 消息 deltaY 上是 +120 下是 -120
                mCallback.onMouseWheel(x, y, -120);
                break;
            default:
                return false;
        }
        return true;
    }
}
