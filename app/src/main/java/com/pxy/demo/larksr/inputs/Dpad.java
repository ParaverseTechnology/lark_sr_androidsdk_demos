package com.pxy.demo.larksr.inputs;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * * process dpad intput. Remote controle or Gamepad devices left joystick.
 * * Remote control device
 *      ^                 UP
 * < center > ===> LEFT CENTER RIGHT
 *     V                 DOWN
 *
 * MENU BUTTON ===> MENU
 * BACK BUTTON ===> BACK
 *
 * * Gamepad device left
 *        +AXISY                     UP
 * -AXISX center +AXISX  ===> LEFT CENTER RIGHT
 *        -AXISY                    DOWN
 */
public class Dpad {
    private static final String TAG = "Dpad";

    public static boolean isDpadDevice(InputEvent event) {
        // Check that input comes from a device with directional pads.
        return (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
                (event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD;
    }

    public final static int UP            = 1;
    public final static int LEFT          = 2;
    public final static int RIGHT         = 3;
    public final static int DOWN          = 4;
    public final static int CENTER        = 5;
    public final static int BACK          = 6;
    public final static int MENU          = 7;
    public final static int DPAD_KEY_NONE = -1;

    private Map<Integer, Integer> mWinMap = new HashMap<Integer, Integer>();

    private int mDirectionPressed = DPAD_KEY_NONE; // initialized to -1
    private boolean mIsDown = false;
    private boolean mIsRepeat = false;
    private final Object mLock = new Object();
    /**
     *
     * @param event intput event.
     * @return windows virtual key code.
     */
    public int handleInput(InputEvent event) {
        if (!isDpadDevice(event)) {
            return -1;
        }

        mDirectionPressed = -1;

        // If the input event is a MotionEvent, check its hat axis values.
        if (event instanceof MotionEvent) {

            // Use the hat axis value to find the D-pad direction
            MotionEvent motionEvent = (MotionEvent) event;

            if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT
                    || motionEvent.getAction() == MotionEvent.ACTION_DOWN
                    || motionEvent.getAction() == MotionEvent.ACTION_HOVER_MOVE
                    || motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS
            ) {
                mIsRepeat = mIsDown;
                mIsDown = true;
            } else {
                mIsDown = false;
                mIsRepeat = false;
            }

            float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
            float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

            // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
            // LEFT and RIGHT direction accordingly.
            if (Float.compare(xaxis, -1.0f) == 0) {
                mDirectionPressed = LEFT;
            } else if (Float.compare(xaxis, 1.0f) == 0) {
                mDirectionPressed = RIGHT;
            }
            // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
            // UP and DOWN direction accordingly.
            else if (Float.compare(yaxis, -1.0f) == 0) {
                mDirectionPressed = UP;
            } else if (Float.compare(yaxis, 1.0f) == 0) {
                mDirectionPressed = DOWN;
            }
        }
        // If the input event is a KeyEvent, check its key code.
        else if (event instanceof KeyEvent) {
            // Use the key code to find the D-pad direction.
            KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                mIsRepeat = mIsDown;
                mIsDown = true;
            } else {
                mIsDown = false;
                mIsRepeat = false;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                mDirectionPressed = LEFT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mDirectionPressed = RIGHT;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                mDirectionPressed = UP;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                mDirectionPressed = DOWN;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                mDirectionPressed = CENTER;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                mDirectionPressed = BACK;
            } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                mDirectionPressed = MENU;
            }
        }
        return mDirectionPressed;
    }

    public int findWindKeyCode(int dpadKey) {
        synchronized (mLock) {
            Integer winKey = mWinMap.get(dpadKey);
            if (winKey == null) {
                return DPAD_KEY_NONE;
            } else {
                return winKey;
            }
        }
    }

    public boolean isRepeat() {
        return mIsRepeat;
    }

    public boolean isDown() {
        return mIsDown;
    }

    public void parseWinkeyCodeMappingXml(Resources resources, int id) {
        // clear old map.
        synchronized (mLock) {
            mWinMap.clear();
            try {
                XmlResourceParser parser = resources.getXml(id);
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        if ("Key".equals(parser.getName())) {
                            int dpadKey = DPAD_KEY_NONE;
                            int winKey = DPAD_KEY_NONE;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if ("code".equals(parser.getAttributeName(i))) {
                                    dpadKey = parser.getAttributeIntValue(i, DPAD_KEY_NONE);
                                } else if ("winKeyCode".equals(parser.getAttributeName(i))) {
                                    winKey = parser.getAttributeIntValue(i, DPAD_KEY_NONE);
                                }
                            }
                            if (dpadKey != DPAD_KEY_NONE && winKey != DPAD_KEY_NONE) {
                                mWinMap.put(dpadKey, winKey);
                            }
                        }
                    }
                    event = parser.next();
                }
            } catch (Resources.NotFoundException | XmlPullParserException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
