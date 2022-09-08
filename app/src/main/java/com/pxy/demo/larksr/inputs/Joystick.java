package com.pxy.demo.larksr.inputs;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Joystick {
    private static final String TAG = "Joystick";

    public static boolean isJoystickDevice(InputEvent event) {
        return (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
    }
    public final static int JOYSTICK_KEY_NONE = -1;
    public final static AxisMappingStatus[] EMPTY_KEY_CODE_MAPPING = {new AxisMappingStatus(), new AxisMappingStatus()};

    public static float getCenteredAxis(MotionEvent event,
                                        InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public static class AxisMappingStatus {
        public int keyCode = JOYSTICK_KEY_NONE;
        public int status = AxisStatus.AXIS_STATUS_NONE;
    }

    public static class AxisStatus {
        public static final int AXIS_STATUS_NONE = -1;
        public static final int AXIS_STATUS_START = 0;
        public static final int AXIS_STATUS_REPEAT = 1;
        public static final int AXIS_STATUS_END = 2;
        public static int getAxisStatusFromHistory(float oldAxis, float newAxis) {
            if (oldAxis == 0 && newAxis != 0) {
                return AXIS_STATUS_START;
            } else if (oldAxis != 0 && newAxis == 0) {
                return AXIS_STATUS_END;
            } else if (oldAxis == 0 && newAxis == 0) {
                return AXIS_STATUS_NONE;
            } else {
                return AXIS_STATUS_REPEAT;
            }
        }

        private final float x;
        private final float y;
        private final float lastX;
        private final float lastY;
        private final int xStatus;
        private final int yStatus;

        public AxisStatus(float x, float y, float lastX, float lastY) {
            this.x = x;
            this.y = y;
            this.lastX = lastX;
            this.lastY = lastY;
            this.xStatus = getAxisStatusFromHistory(lastX, x);
            this.yStatus = getAxisStatusFromHistory(lastY, y);
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getXStatus() {
            return xStatus;
        }

        public int getYStatus() {
            return yStatus;
        }

        public float getLastX() {
            return lastX;
        }

        public float getLastY() {
            return lastY;
        }

        public List<AxisMappingStatus> toWinMappingCodeList(JoyStickAxisMapping mapping) {
            List<AxisMappingStatus> res = new ArrayList<>();
            float axisX = xStatus == AXIS_STATUS_END ? lastX : x;
            AxisMappingStatus left = new AxisMappingStatus();
            left.keyCode = axisX > 0 ? mapping.right : mapping.left;
            left.status = xStatus;
            res.add(left);

            float axisY = yStatus == AXIS_STATUS_END ? lastY : y;
            AxisMappingStatus right = new AxisMappingStatus();
            right.keyCode = axisY > 0 ? mapping.down : mapping.up;
            right.status = yStatus;
            res.add(right);
            return res;
        }
    }

    public class JoyStickStatus {
        private final AxisStatus left;
        private final AxisStatus right;
        JoyStickStatus(AxisStatus left, AxisStatus right) {
            this.left = left;
            this.right = right;
        }

        public AxisStatus getLeft() {
            return left;
        }

        public AxisStatus getRight() {
            return right;
        }

        public List<AxisMappingStatus> getLeftMappingKeyCodeList() {
            return left.toWinMappingCodeList(joystickLeftMapping);
        }
        public List<AxisMappingStatus> getRightMappingKeyCodeList() {
            return right.toWinMappingCodeList(joystickRightMapping);
        }
        public List<AxisMappingStatus> toAllMappingKeyCodeList() {
            List<AxisMappingStatus> list = new ArrayList<>();
            list.addAll(getLeftMappingKeyCodeList());
            list.addAll(getRightMappingKeyCodeList());
            return list;
        }
    }

    static class JoyStickAxisMapping {
        public int up = JOYSTICK_KEY_NONE;
        public int down = JOYSTICK_KEY_NONE;
        public int left = JOYSTICK_KEY_NONE;
        public int right = JOYSTICK_KEY_NONE;
    }
    private JoyStickAxisMapping joystickLeftMapping = new JoyStickAxisMapping();
    private JoyStickAxisMapping joystickRightMapping = new JoyStickAxisMapping();

    private Map<Integer, Integer> mWinMap = new HashMap<Integer, Integer>();
    private final Object mLock = new Object();

    private float lastX = 0;
    private float lastY = 0;
    private float lastRightX = 0;
    private float lastRightY = 0;

    public int findWindKeyCode(int dpadKey) {
        synchronized (mLock) {
            Integer winKey = mWinMap.get(dpadKey);
            if (winKey == null) {
                return JOYSTICK_KEY_NONE;
            } else {
                return winKey;
            }
        }
    }

    public JoyStickStatus processJoystickInput(MotionEvent event,
                                     int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }

        AxisStatus left = new AxisStatus(x, y, lastX, lastY);

        lastX = x;
        lastY = y;

        // right x
        float rx = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Z, historyPos);
        // right y
        float ry = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);

        AxisStatus right = new AxisStatus(rx, ry, lastRightX, lastRightY);

        lastRightX = rx;
        lastRightY = ry;

//        Log.d(TAG, "joy stick lx " + x + " joy stick ly " + y + " rx " + rx + " ry " + ry);
        // Update the ship object based on the new x and y values
        return new JoyStickStatus(left, right);
    }

    public void parseWinkeyCodeMappingXml(Resources resources, int id) {
        synchronized (mLock) {
            // clear old map.
            mWinMap.clear();
            try {
                XmlResourceParser parser = resources.getXml(id);
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        if ("Key".equals(parser.getName())) {
                            int code =  JOYSTICK_KEY_NONE;
                            int winKeyCode = JOYSTICK_KEY_NONE;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attrName = parser.getAttributeName(i);
                                if ("code".equals(attrName)) {
                                    code = parser.getAttributeIntValue(i, JOYSTICK_KEY_NONE);
                                } else if ("winKeyCode".equals(attrName)) {
                                    winKeyCode = parser.getAttributeIntValue(i, JOYSTICK_KEY_NONE);
                                }
                            }
                            if (winKeyCode != JOYSTICK_KEY_NONE && code != JOYSTICK_KEY_NONE) {
                                mWinMap.put(code, winKeyCode);
                            }
                        } else if ("axis".equals(parser.getName())) {
//                        Log.d(TAG, "parser " + parser.getName() + " " + event + " ");
                            String type = parser.getAttributeValue(0);
                            if ("left".equals(type)) {
                                event = parseAxis(parser, joystickLeftMapping);
                                continue;
                            } else if ("right".equals(type)) {
                                event = parseAxis(parser,  joystickRightMapping);
                                continue;
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

    private int parseAxis(XmlResourceParser parser, JoyStickAxisMapping mapping) {
        int next = XmlPullParser.END_DOCUMENT;
        try {
            // skip first.
            if ("axis".equals(parser.getName())) {
                parser.next();
            }
            int event = parser.getEventType();
            while (event != XmlPullParser.END_TAG || !"axis".equals(parser.getName())) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    int winKeyCode = JOYSTICK_KEY_NONE;
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if ("winKeyCode".equals(parser.getAttributeName(i))) {
                            winKeyCode = parser.getAttributeIntValue(i, JOYSTICK_KEY_NONE);
                        }
                    }
                    if ("up".equals(parser.getName())) {
                        mapping.up = winKeyCode;
                    } else if ("down".equals(parser.getName())) {
                        mapping.down = winKeyCode;
                    } else if ("left".equals(parser.getName())) {
                        mapping.left = winKeyCode;
                    } else if ("right".equals(parser.getName())) {
                        mapping.right = winKeyCode;
                    }
                    Log.d(TAG, "axis" + " " + parser.getName() + " " + winKeyCode);
                }
                event = parser.next();
            }
            next = parser.next();
        } catch (Resources.NotFoundException | XmlPullParserException | IOException ex) {
            ex.printStackTrace();
        }
        return next;
    }
}
