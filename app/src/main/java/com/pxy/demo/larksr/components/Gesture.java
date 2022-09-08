package com.pxy.demo.larksr.components;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pxy.lib_sr.unit.Vector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Gesture implements View.OnTouchListener
{
    private static final String TAG = "Gesture";
    private static final int DOUBLE_CLICK_TIEMLIMIT = 300;
    private static final int DOUBLE_CLICK_POSITION_LIMIT = 40;
    private static final int TOUCH_STATUS_NONE = 0;
    private static final int TOUCH_STATUS_SINGLE_FINGER = 1;
    private static final int TOUCH_STATUS_DOUBLE_FINGER = 2;
    private static final int TOUCH_STATUS_TRIPLE_FINGER = 3;
    /**
     * 鼠标相对移动系数。 极慢。
     */
    public static final int LOCK_MOUSE_MOVE_SPEED_EX_SLOW = 2;
    /**
     *  鼠标相对移动系数。 慢。
     */
    public static final int LOCK_MOUSE_MOVE_SPEED_SLOW = 5;
    /**
     *  鼠标相对移动系数。
     */
    public static final int LOCK_MOUSE_MOVE_SPEED = 10;
    /**
     * 鼠标相对移动系数。移动速度快。
     */
    public static final int LOCK_MOUSE_MOVE_SPEED_FAST = 15;


    /**
     * 单指点击开始
     */
    public static final int SINGLE_FINGER_TOUCH_START = 1;
    /**
     *  单指点击事件。单指离开屏幕并且未出发移动事件时触发。
     */
    public static final int SINGLE_FINGER_TAP = 2;
    /**
     *  单指点击按下
     */
    public static final int SINGLE_FINGER_TAP_DOWN = 3;
    /**
     * 单指点击抬起
     */
    public static final int SINGLE_FINGER_TAP_UP = 4;
    /**
     *  单指在屏幕上滑动时触发。
     */
    public static final int SINGLE_FINGER_SWIPE = 5;
    /**
     *  单指在屏幕上滑动开始时触发。
     */
    public static final int SINGLE_FINGER_SWIPE_START = 6;
    /**
     *  单指在屏幕上滑动离开屏幕时触发。
     */
    public static final int SINGLE_FINGER_SWIPE_END = 7;
    /**
     *  双指点击事件。单指离开屏幕并且未出发移动事件时触发。
     */
    public static final int DOUBLE_FINGER_TAP = 8;
    /**
     *  双指单指在屏幕上滑动时触发。
     */
    public static final int DOUBLE_FINGER_SWIPE = 9;
    /**
     *  双指在屏幕上滑动开始时触发。
     */
    public static final int DOUBLE_FINGER_SWIPE_START = 10;
    /**
     *  双指在屏幕上滑动离开屏幕时触发。
     */
    public static final int DOUBLE_FINGER_SWIPE_END = 11;
    /**
     *  三指点击事件。单指离开屏幕并且未出发移动事件时触发。
     */
    public static final int TRIPLE_FINGER_TAP = 12;
    /**
     *  三指单指在屏幕上滑动时触发。
     */
    public static final int TRIPLE_FINGER_SWIPE = 13;
    /**
     *  三指在屏幕上滑动开始时触发。
     */
    public static final int TRIPLE_FINGER_SWIPE_START = 14;
    /**
     *  三指在屏幕上滑动离开屏幕时触发。
     */
    public static final int TRIPLE_FINGER_SWIPE_END = 15;
    /**
     * 手指离开屏幕或者点击区域或被打断时触发。
     */
    public static final int FINGER_RELEASE = 1000;

    public static class GestureMovement {
        private final int type;
        private final int x;
        private final int y;
        private final int rx;
        private final int ry;
        private final int distance;

        /**
         *  获取获取事件类型
         * @return 事件类型
         */
        public int getType() {
            return type;
        }

        /**
         *  获取事件的 x 轴绝对坐标。
         * @return 事件的 x 轴绝对坐标。
         */
        public int getX() {
            return x;
        }

        /**
         *  获取事件的 y 轴绝对坐标。
         * @return 事件的 y 轴绝对坐标。
         */
        public int getY() {
            return y;
        }

        /**
         *  获取事件 x 轴相对对移动的位置。
         * @return 事件 x 轴相对对移动的位置。
         */
        public int getRx() {
            return rx;
        }

        /**
         * 获取事件 y 轴相对移动的位置。
         * @return 事件 y 轴相对移动的位置。
         */
        public int getRy() {
            return ry;
        }

        /**
         *  获取双指事件中两个手指的距离。 其他事件为 0.
         * @return 双指事件中两个手指的距离。 其他事件为 0.
         */
        public int getDistance() {
            return distance;
        }

        /**
         * 手势移动事件。坐标系未本地坐标系。
         * @param type 事件的类型。
         * @param xPos 事件的 x 轴绝对坐标。
         * @param yPos 事件的 y 轴绝对坐标。
         * @param xRel 事件 x 轴相对对移动的位置。
         * @param yRel 事件 y 轴相对移动的位置。
         * @param distance 双指事件中两个手指的距离。 其他事件为 0.
         */
        public GestureMovement(int type, int xPos, int yPos, int xRel, int yRel, int distance) {
            this.type = type;
            this.x = xPos;
            this.y = yPos;
            this.rx = xRel;
            this.ry = yRel;
            this.distance = distance;
        }
    }

    public interface GestureEvent {
        void onGestureEvent(GestureMovement gestureMovement, View view);
    }

    private GestureEvent eventListener;
    private boolean tapStart = false;
    private boolean mutiStart = false;
    // 当前触发的事件
    private int touchStatus = TOUCH_STATUS_NONE;
    // 上一次单机鼠标单击事件时间戳。
    private long lastTapTimestamp = 0;
    private long touchStartTimestamp = 0;
    // 上次
    private GestureMovement lastTapGestureMovement;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     *  手势类
     * @param eventListener 监听手势事件。
     */
    public Gesture(GestureEvent eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * 设置监听事件
     * @param eventListener
     */
    public void setListener(GestureEvent eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * 取消监听事件
     */
    public void clearListener() {
        this.eventListener = null;
    }

    /**
     *  0ms----------------------->start touch
     *  touchend------------------>touch tap
     *  300ms--------------------->touch tap down
     *  touchend------------------>touch tap up
     *  1000ms-------------------->long touch tap down
     *  touchend------------------>long touch tap up
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int xpos = (int)event.getX();
        final int ypos = (int)event.getY();
        final int relX = event.getHistorySize() >= 1 ? Math.round(event.getX() - event.getHistoricalX(0)) : 0;
        final int relY = event.getHistorySize() >= 1 ? Math.round(event.getY() - event.getHistoricalY(0)) : 0;
        int distance = 0;

        switch (event.getActionMasked()) {
            /**
             * 点击的开始位置
             */
            case MotionEvent.ACTION_DOWN:
                tapStart = true;
                mutiStart = false;
                if (event.getPointerCount() == 1) {
                    touchStatus = TOUCH_STATUS_SINGLE_FINGER;
                    touchStartTimestamp = event.getEventTime();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (touchStatus == TOUCH_STATUS_SINGLE_FINGER && tapStart) {
                                emit(SINGLE_FINGER_TAP_DOWN, xpos, ypos, relX, relY, 0, view);
                            }
                        }
                    });
                    emit(SINGLE_FINGER_TOUCH_START, xpos, ypos, relX, relY, distance, view);
                } else if (event.getPointerCount() == 2) {
                    touchStatus = TOUCH_STATUS_DOUBLE_FINGER;
                } else if (event.getPointerCount() == 3){
                    touchStatus = TOUCH_STATUS_TRIPLE_FINGER;
                }
                break;
            /**
             * 触屏实时位置
             */
            case MotionEvent.ACTION_MOVE:
                // 单指移动
                if (event.getPointerCount() == 1) {
                    // 鼠标左键按下移动
                    if (Math.abs(relX) > 1 || Math.abs(relY) > 1) {
                        if (tapStart) {
                            emit(SINGLE_FINGER_SWIPE_START, xpos, ypos, relX, relY, distance, view);
                            tapStart = false;
                        } else {
                            emit(SINGLE_FINGER_SWIPE, xpos, ypos, relX, relY, distance, view);
                        }
                    }
                } else if (event.getPointerCount() == 2) {
                    // 双指移动距离
                    distance = (int) Math.round(getDoubleFingerDistenceMove(event));
                    if (tapStart) {
                        tapStart = false;
                        emit(DOUBLE_FINGER_SWIPE_START, xpos, ypos, relX, relY, distance, view);
                    } else {
                        emit(DOUBLE_FINGER_SWIPE, xpos, ypos, relX, relY, distance, view);
                    }
                } else if (event.getPointerCount() == 3) {
                    // 鼠标右键按下移动
                    if (tapStart) {
                        tapStart = false;
                        emit(TRIPLE_FINGER_SWIPE_START, xpos, ypos, relX, relY, distance, view);
                    } else {
                        emit(TRIPLE_FINGER_SWIPE, xpos, ypos, relX, relY, distance, view);
                    }
                }
                break;
            /**
             * 离开屏幕的位置
             */
            case MotionEvent.ACTION_UP:
                if (!mutiStart) {
                    if (tapStart) {
                        if (event.getEventTime() - touchStartTimestamp < 300) {
                            // 判断是否双击
                            // 小于 300 毫秒， 双击屏幕
                            if (checkDoubleClick(event)) {
                                Log.v(TAG, "got doubletap");
                                if (eventListener != null) {
                                    eventListener.onGestureEvent(lastTapGestureMovement, view);
                                }
                                lastTapTimestamp = 0;
                                lastTapGestureMovement = null;
                            } else {
                                Log.v(TAG, "got tap");
                                // 鼠标左键按下
                                GestureMovement movement = new GestureMovement(SINGLE_FINGER_TAP, xpos, ypos, relX, relY, distance);
                                if (eventListener != null) {
                                    eventListener.onGestureEvent(movement, view);
                                }
                                lastTapTimestamp = event.getEventTime();
                                lastTapGestureMovement = movement;
                            }
                        } else {
                            lastTapTimestamp = 0;
                            lastTapGestureMovement = null;
                            emit(SINGLE_FINGER_TAP_UP, xpos, ypos, relX, relY, distance, view);
                        }
                    } else {
                        emit(SINGLE_FINGER_SWIPE_END, xpos, ypos, relX, relY, distance, view);
                    }
                }
                initTouchStatus();
                emit(FINGER_RELEASE, xpos, ypos, relX, relY, distance, view);
                break;
            /**
             * 事件 被上层拦截 时触发。
             */
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                emit(FINGER_RELEASE, xpos, ypos, relX, relY, distance, view);
                initTouchStatus();
                break;
            /**
             * 有非主要的手指按下(即按下之前已经有手指在屏幕上)。
             * */
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    tapStart = true;
                    mutiStart = true;
                    touchStatus = TOUCH_STATUS_DOUBLE_FINGER;
                } else if (event.getPointerCount() == 3) {
                    tapStart = true;
                    mutiStart = true;
                    touchStatus = TOUCH_STATUS_TRIPLE_FINGER;
                }
                break;
            /**
             * 有非主要的手指抬起(即抬起之后仍然有手指在屏幕上)。
             * */
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    if (tapStart) {
                        // 鼠标右键按下
                        emit(DOUBLE_FINGER_TAP, xpos, ypos, relX, relY, distance, view);
                    }
                    // 鼠标右键抬起
                    emit(DOUBLE_FINGER_SWIPE_END, xpos, ypos, relX, relY, distance, view);
                } else if (event.getPointerCount() == 3) {
                    if (tapStart) {
                        // 鼠标中键键按下
                        emit(TRIPLE_FINGER_TAP, xpos, ypos, relX, relY, distance, view);
                    }
                    emit(TRIPLE_FINGER_SWIPE_END, xpos, ypos, relX, relY, distance, view);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private double getDoubleFingerDistenceMove(MotionEvent event) {
        if (event.getHistorySize() > 1 && event.getPointerCount() == 2) {
            Vector v1 = new Vector(event.getHistoricalX(1,1), event.getHistoricalY(1,1),
                    event.getHistoricalX(1), event.getHistoricalY(1));
            Vector v2 = new Vector(event.getX(1), event.getY(1),
                    event.getX(), event.getY());
            return v2.getSize() - v1.getSize();
        } else {
            return 0;
        }
    }

    private void  initTouchStatus() {
        tapStart = false;
        mutiStart = false;
        touchStatus = TOUCH_STATUS_NONE;
        touchStartTimestamp = 0;
    }

    private void emit(int type, int x, int y, int rx, int ry, int distance,View view) {
        if (eventListener != null) {
            eventListener.onGestureEvent(new GestureMovement(type, x, y, rx, ry, distance), view);
        }
    }

    private boolean checkDoubleClick(MotionEvent event) {
        if (lastTapGestureMovement == null)
            return false;
        if (event.getEventTime() - lastTapTimestamp > DOUBLE_CLICK_TIEMLIMIT)
            return false;
        int oldX = lastTapGestureMovement.x;
        int oldY = lastTapGestureMovement.y;
        int nX = (int)event.getX();
        int nY = (int)event.getY();
        int rx = nX - oldX;
        int ry = nY - oldY;
        double r = Math.sqrt(rx * rx + ry * ry);
        Log.v(TAG, "checkDoubleClick r" + r);
        return  r < DOUBLE_CLICK_POSITION_LIMIT;
    }
}
