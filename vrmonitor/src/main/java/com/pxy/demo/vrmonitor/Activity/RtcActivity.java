package com.pxy.demo.vrmonitor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pxy.larkcore.request.EnterAppliInfo;
import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.AppNotification;
import com.pxy.lib_sr.render.RtcRender;
import com.pxy.demo.vrmonitor.R;

import java.util.List;

public class RtcActivity extends AppCompatActivity {
    private String TAG="RtcActivity";
    RtcRender render;
    private boolean mTouchScreenOperateMode = false;
    private RtcClient mRtcClient;
    private TextView X;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);
        initview();
        initData();
    }

    private void initData() {
        // 获取参数
        Intent intent = getIntent();
        EnterAppliInfo.Config rtcParams = intent.getParcelableExtra("param");
        if (rtcParams != null) {
            Log.d(TAG, "start task config " + rtcParams);
            // 判断云端应用是否支持触摸屏的方式
            mTouchScreenOperateMode = rtcParams.touchOperateMode.equals(RtcClient.TOUCH_OPERATE_MODE_TOUCHSCREEN);
            // new connection.
            if (mRtcClient == null) {
                mRtcClient = new RtcClient(rtcParams, render,
                        mRtcClientListener,
                        this);
                // 开始连接
                mRtcClient.connect();
            }

            // 互动模式。
            /*  if (rtcParams.playerMode != RtcClient.PLAYER_MODE_NORMAL) {
                mIsInteractive = true;
                findViewById(R.id.menu_button_player_list).setVisibility(View.VISIBLE);
                mPlayerListView = findViewById(R.id.player_list);
                mPlayerListAdapter = new PlayerAdapter(this, R.layout.layout_player_item, new ArrayList<>());
                mPlayerListView.setAdapter(mPlayerListAdapter);
                mPlayerListView.setOnItemClickListener(mPlayerlistSelected);
                TextView roomId = findViewById(R.id.room_id);
                String roomCode = "";
                if (rtcParams.roomCode != null && !rtcParams.roomCode.isEmpty()) {
                    roomCode = "分享码 " + rtcParams.roomCode;
                } else if (rtcParams.taskId != null && !rtcParams.taskId.isEmpty()) {
                    roomCode = "TaskId " + rtcParams.taskId;
                }
                roomId.setText(roomCode);
                roomId.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.menu_button_player_list).setVisibility(View.GONE);
                findViewById(R.id.room_id).setVisibility(View.GONE);
            }*/
        } else {
            Log.w(TAG, "got app client failed");
        }
    }

    private void initview() {
        render=findViewById(R.id.render);
        X=findViewById(R.id.X);
        X.setOnClickListener(v -> {
            finish();

        });
    }

    private RtcClient.RtcClientEvent mRtcClientListener=new RtcClient.RtcClientEvent() {
        @Override
        public void onConnect() {

        }

        @Override
        public void onLoginSuccess(int i) {

        }

        @Override
        public void onMediaReady() {

        }

        @Override
        public void onFrameResolutionChanged(int i, int i1, int i2) {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onNoOpreationTimeout() {

        }

        @Override
        public void onInfo(String s) {

        }

        @Override
        public void onError(String s) {

        }

        @Override
        public void onAppResize(AppNotification.AppResize appResize) {

        }

        @Override
        public void onMouseState(AppNotification.AppMouseMode appMouseMode) {

        }

        @Override
        public void onPlayerList(List<AppNotification.PlayerDesc> list) {

        }

        @Override
        public void onPeerStatusReport(RtcClient.SampleRTCStats sampleRTCStats) {

        }

        @Override
        public void onDataChannelOpen() {

        }

        @Override
        public void onDataChannelClose() {

        }

        @Override
        public void onDataChannelMessage(String s) {

        }

        @Override
        public void onDataChannelMessage(byte[] bytes) {

        }

        @Override
        public void onAppRequestInput(boolean b) {

        }

        @Override
        public void onAppRequestGamepadOutput() {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcClient!=null){
            mRtcClient.release();
            render.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}