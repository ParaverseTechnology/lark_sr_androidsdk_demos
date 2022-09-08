package com.pxy.demo.larksr.inputs;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.InputDevice;
import android.view.InputEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Gamepad {
    private static final String TAG = "Gamepad";
    public static final int GAME_PAD_NONE = -1;
    public static boolean isGamepadDevice(InputEvent event) {
        // Check that input comes from a device with directional pads.
        return (event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD;
    }

    private Map<Integer, Integer> mWinMap = new HashMap<Integer, Integer>();
    private final Object mLock = new Object();
    public Gamepad() {
    }

    public int findWindKeyCode(int dpadKey) {
        synchronized (mLock) {
            Integer winKey = mWinMap.get(dpadKey);
            if (winKey == null) {
                return GAME_PAD_NONE;
            } else {
                return winKey;
            }
        }
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
                            int dpadKey = GAME_PAD_NONE;
                            int winKey = GAME_PAD_NONE;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if ("code".equals(parser.getAttributeName(i))) {
                                    dpadKey = parser.getAttributeIntValue(i, GAME_PAD_NONE);
                                } else if ("winKeyCode".equals(parser.getAttributeName(i))) {
                                    winKey = parser.getAttributeIntValue(i, GAME_PAD_NONE);
                                }
                            }
                            if (dpadKey != GAME_PAD_NONE && winKey != GAME_PAD_NONE) {
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
