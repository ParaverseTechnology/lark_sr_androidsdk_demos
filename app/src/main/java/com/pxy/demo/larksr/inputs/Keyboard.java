package com.pxy.demo.larksr.inputs;

import android.view.InputDevice;
import android.view.InputEvent;

public class Keyboard {
    private static final String TAG = "Dpad";

    public static boolean isKeyboardDevice(InputEvent event) {
        // Check that input comes from a device with directional pads.
        if ((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
            return true;
        } else {
            return false;
        }
    }
}
