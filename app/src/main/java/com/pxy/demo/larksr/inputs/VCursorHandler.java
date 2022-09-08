package com.pxy.demo.larksr.inputs;

import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.ClientInput;
import com.pxy.lib_sr.unit.Positon;
import com.pxy.demo.larksr.Stage;
import com.pxy.demo.larksr.components.VCursorWithVMouse;

public class VCursorHandler implements VCursorWithVMouse.VCursorEvent {
    private int mAppWidth;
    private int mAppHeight;
    private Stage mStage;
    private final RtcClient mRtcClient;
    public VCursorHandler(RtcClient rtcClient) {
        mRtcClient = rtcClient;
    }

    public void setAppWidth(int appWidth) {
        mAppWidth = appWidth;
    }

    public void setAppHeight(int appHeight) {
        mAppHeight = appHeight;
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    /**
     * 获取相对云端的位置
     * @param x 位置 x
     * @param y 位置 y
     * @return 云端点的位置
     */
    private Positon getRelCloudAppPositon(int x, int y) {
        return getRelCloudAppPositon(x, y, 0, 0);
    }
    /**
     * 获取相对云端的位置
     * @param x 位置 x
     * @param y 位置 y
     * @return 云端点的位置
     */
    private Positon getRelCloudAppPositon(int x, int y, int rx, int ry) {
        Positon p = new Positon(x - mStage.stageX,y - mStage.stageY, rx, ry);
        float scaleX = (float)mAppWidth / (float)mStage.stageW;
        float scaleY = (float)mAppHeight / (float)mStage.stageH;
        p.setScale(scaleX, scaleY);
        // invalid position
        if (p.getX() > mStage.stageW || p.getX() < 0) return Positon.createInValidPosition();
        if (p.getY() > mStage.stageH || p.getY() < 0) return Positon.createInValidPosition();
        return p;
    }

    @Override
    public void onMouseLeftDown(float x, float y) {
        Positon op = getRelCloudAppPositon((int)x, (int)y);
        if (mRtcClient != null && op.isValid()) {
            // 3.1.1.0 新增
            mRtcClient.sendMouseDown(op.getScaledX(), op.getScaledY(), ClientInput.MouseKey.MOUSE_KEY_LEFT);
        }
    }

    @Override
    public void onMouseLeftUp(float x, float y) {
        Positon op = getRelCloudAppPositon((int) x, (int) y);
        if (mRtcClient != null && op.isValid()) {
            // 3.1.1.0 新增
            mRtcClient.sendMouseUp(op.getScaledX(), op.getScaledY(), ClientInput.MouseKey.MOUSE_KEY_LEFT);
        }
    }

    @Override
    public void onMouseRightDown(float x, float y) {
        Positon op = getRelCloudAppPositon((int) x, (int) y);
        if (mRtcClient != null && op.isValid()) {
            // 3.1.1.0 新增
            mRtcClient.sendMouseDown(op.getScaledX(), op.getScaledY(), ClientInput.MouseKey.MOUSE_KEY_RIGHT);
        }
    }

    @Override
    public void onMouseRightUp(float x, float y) {
        Positon op = getRelCloudAppPositon((int) x, (int) y);
        if (mRtcClient != null && op.isValid()) {
            // 3.1.1.0 新增
            mRtcClient.sendMouseUp(op.getScaledX(), op.getScaledY(), ClientInput.MouseKey.MOUSE_KEY_RIGHT);
        }
    }

    @Override
    public void onMouseMove(float x, float y, float rx, float ry) {
        Positon op = getRelCloudAppPositon((int) x, (int) y);
        if (mRtcClient != null && op.isValid()) {
            mRtcClient.sendMouseMove(op.getScaledX(), op.getScaledY(), (int)rx, (int)ry);
        }
    }

    @Override
    public void onMouseWheel(float x, float y, float delta) {
        Positon op = getRelCloudAppPositon((int) x, (int) y);
        if (mRtcClient != null && op.isValid()) {
            mRtcClient.sendMouseWheel(op.getScaledX(), op.getScaledY(), (int)delta);
        }
    }
}
