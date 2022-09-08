package com.pxy.demo.larksr;

/**
 * 本地渲染控件的大小和位置.
 */
public class Stage {
    public int stageW;
    public int stageH;
    public int stageX;
    public int stageY;
    public Stage(int w, int h, int x, int y) {
        this.stageW = w;
        this.stageH = h;
        this.stageX = x;
        this.stageY = y;
    }
    public void set(int w, int h, int x, int y) {
        this.stageW = w;
        this.stageH = h;
        this.stageX = x;
        this.stageY = y;
    }
}
