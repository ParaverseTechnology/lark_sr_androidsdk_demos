package com.pxy.demo.larksr;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.pxy.larkcore.request.EnterAppliInfo;
import com.pxy.larkcore.request.GetTask;
import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.AppNotification;
import com.pxy.lib_sr.input.ClientInput;
import com.pxy.lib_sr.input.WindowsKeyCodes;
import com.pxy.lib_sr.input.WindowsXInputGamepad;
import com.pxy.lib_sr.render.RtcEglRenderer;
import com.pxy.lib_sr.render.RtcRender;
import com.pxy.lib_sr.unit.Positon;
import com.pxy.demo.larksr.components.Gesture;
import com.pxy.demo.larksr.components.SoftJoystick;
import com.pxy.demo.larksr.components.VCursorWithVMouse;
import com.pxy.demo.larksr.inputs.Dpad;
import com.pxy.demo.larksr.inputs.Gamepad;
import com.pxy.demo.larksr.inputs.Joystick;
import com.pxy.demo.larksr.inputs.SoftGamepadHandler;
import com.pxy.demo.larksr.inputs.SoftKeyboardHandler;
import com.pxy.demo.larksr.inputs.TouchInput;
import com.pxy.demo.larksr.inputs.TouchScreenHandler;
import com.pxy.demo.larksr.inputs.VCursorHandler;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class RtcActivity extends AppCompatActivity {
    private static final String TAG = "RtcActivity";
    /**
     * rtc 客户端
     */
    private RtcClient mRtcClient;
    /**
     * 手势操作,用于将触摸事件对应成鼠标的操作
     */
    private Gesture mGesture;
    /**
     * 一般触摸操作，当云端应用支持时，将触摸操作对应成云端的触摸屏操作
     */
    private TouchScreenHandler mTouchScreenHandler;
    /**
     * 渲染组件列表
     **/
    private RtcRender mRender = null;
    /**
     * 云端应用的宽度
     */
    private int mAppWidth = RtcClient.WIDTH;
    /**
     * 云端应用的高度
     */
    private int mAppHeight = RtcClient.HEIGHT;
    /**
     * 显示用户触摸位置
     */
    private View mTouchPointer = null;
    /**
     * 手柄按钮
     */
    private View mHandle = null;
    /**
     * 虚拟鼠标指针。
     */
    private VCursorWithVMouse mVCursor = null;
    /**
     * 摇杆
     */
    private View mSoftJoystickView = null;
    /**
     * 折叠控制栏按钮
     */
    private ImageButton mToggleControlBarButton = null;
    /**
     * 控制栏
     */
    private LinearLayout mControlBar = null;
    /**
     * 控制栏状态
     */
    private boolean mIsControlBarToggle = false;
    /**
     * 本地控件的大小和位置
     */
    private Stage mStage = new Stage(0, 0, 0, 0);

    private boolean mIsConnect = false;
    // inputs
    private Dpad mDpad = new Dpad();
    private Gamepad mGamepad = new Gamepad();
    private Joystick mJoystick = new Joystick();
    private TouchInput mTouchInput = null;
    private SoftKeyboardHandler mSoftKeyboardHandler = null;
    private SoftGamepadHandler mSoftGamepadHandler = null;
    private VCursorHandler mVCursorHandler = null;
    private View mMenu;
    // mode
    private boolean mShowMenu = false;
    //
    private boolean mHWGamepad = true;
    private boolean mTVMode = BuildConfig.tvMode;

    private Button mGamePadMappingModeButton;
    private Button mJoystickMapping1Button;
    private Button mJoystickMapping2Button;
    private Button mDpadMapping1Button;
    private Button mDpadMapping2Button;

    private View mMenuViewList;
    private View mMenuViewInfo;
    private View mMenuViewPlayerList;
    private ListView mPlayerListView;
    private com.pxy.demo.larksr.PlayerAdapter mPlayerListAdapter;

    private TextView mRttText;
    private TextView mBitrateText;
    private TextView mFPSText;
    private TextView mPackgelost;
    private TextView mServerCaptureDelayText;
    private TextView mServerRenderDelayText;
    private TextView mServerEncoderDelay;

    private View mChatViewLayout;
    private RecyclerView mAiChatRecyclerView;
    private AiChatAdapter mAiChatAdapter;

    private TextInputEditText mAiChatTextInput;

    private boolean mIsInteractive = false;

    private int mUserId = 0;
    private ImageView mTestCaptureImageView = null;
    private boolean mTouchScreenOperateMode = false;

    private LiveStreamingSetupDialog mLiveStreamingSetupDialog;

    /**
     * open media > next version
    private View mMediaShareLayout = null;
    private ImageButton mVideoButton = null;
    private Spinner mVideoSpinner = null;
    private ImageButton mMicButton = null;
     */

    private EditText mEditText;
    private boolean mInputStart = false;
    private boolean mSoftKeyboardShown = false;

    private int strect = 0;

    // 防止重新创建 activity.
    // android:configChanges="keyboardHidden|orientation|screenSize"
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create.");
        setContentView(R.layout.larkrender_layout_single_render);
        // 渲染组件
        mRender = findViewById(R.id.signal_render);

        // 设置触摸组件。
        mGesture = new Gesture(mGestureListener);
        mTouchScreenHandler = new TouchScreenHandler();
        // 触摸位置
        mTouchPointer = findViewById(R.id.touchPoint);
        // 控制栏按钮
        mToggleControlBarButton = findViewById(R.id.toggle_control_button);
        // 控制栏
        mControlBar = findViewById(R.id.control_bar);
        syncControlBarStatus(false);
        if (mTVMode) {
            // 同步控制栏状态
            mControlBar.setFocusable(false);
            mControlBar.setVisibility(View.INVISIBLE);
        }
        // 手柄和摇杆
        mHandle = findViewById(R.id.handle);
        mSoftJoystickView = findViewById(R.id.softJoystick);
        // 摇杆控制器
        SoftJoystick softJoystick = new SoftJoystick(mSoftJoystickView, mSoftJoystickListener);
        softJoystick.startListenTouch();

        mMenu = findViewById(R.id.layout_menu);

        mMenu.setVisibility(View.INVISIBLE);
        Button backButton = findViewById(R.id.menu_button_back);
        backButton.setOnClickListener(this::onHideMenu);
        Button exitButton = findViewById(R.id.menu_button_exit);
        exitButton.setOnClickListener(this::onQuit);

        mGamePadMappingModeButton = findViewById(R.id.menu_button_mepping_mode);
        mJoystickMapping1Button = findViewById(R.id.menu_button_joystick_1);
        mJoystickMapping2Button = findViewById(R.id.menu_button_joystick_2);
        mDpadMapping1Button = findViewById(R.id.menu_button_dpad_1);
        mDpadMapping2Button = findViewById(R.id.menu_button_dpad_2);

        setDapdMode(1);
        setJoystickMode(2);

        mMenuViewList = findViewById(R.id.menu_list);
        // 手机上隐藏摇杆设置
        if (!mTVMode) {
            mGamePadMappingModeButton.setVisibility(View.GONE);
            mDpadMapping1Button.setVisibility(View.GONE);
            mDpadMapping2Button.setVisibility(View.GONE);
            mJoystickMapping1Button.setVisibility(View.GONE);
            mJoystickMapping2Button.setVisibility(View.GONE);
        }
        mMenuViewInfo = findViewById(R.id.menu_info);
        mMenuViewPlayerList = findViewById(R.id.menu_player_list);
        setMenuView(0);

        mRttText = findViewById(R.id.textView_statics_rtt);
        mBitrateText = findViewById(R.id.textView_statics_bytesPerSec);
        mFPSText = findViewById(R.id.textView_statics_frameDeocdedPreSec);
        mPackgelost = findViewById(R.id.textView_statics_packageLostRatePerSec);
        mServerCaptureDelayText = findViewById(R.id.textView_statics_serverCaptureDelay);
        mServerRenderDelayText = findViewById(R.id.textView_statics_serverRenderDelay);
        mServerEncoderDelay = findViewById(R.id.textView_statics_serverEncoderDelay);

        mTestCaptureImageView = findViewById(R.id.textureView_test_capture);

        mEditText = findViewById(R.id.edit_text_inputcloud);

        mEditText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }
            Log.d(TAG, "on key up " + keyCode);
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideSoftKeyboard(mEditText);
                mEditText.setText("");
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                Log.d(TAG, "on keycode del " + mEditText.getText().toString());
                if (!mInputStart && mRtcClient != null) {
                    Log.d(TAG, "send del " + mEditText.getText().toString());
                    mRtcClient.sendKeyDown(WindowsKeyCodes.KEYCODE_DEL, false);
                    mRtcClient.sendKeyUp(WindowsKeyCodes.KEYCODE_DEL);
                    // send key delete
                    return false;
                }
                return false;
            }
            return false;
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mInputStart = true;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String changed = charSequence.subSequence(i, i + i2).toString();
                Log.d(TAG, "onTextChanged " + changed);
                if (!changed.equals("") && mRtcClient != null) {
                    mRtcClient.sendInputText(changed);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mInputStart = false;
            }
        });



        /**
         * open media > next version
        // media share video/mic
        mMediaShareLayout = findViewById(R.id.layout_media_video);
        mVideoButton = findViewById(R.id.button_open_camera);
        mVideoSpinner = findViewById(R.id.spinner_choose_camera);
        mMicButton = findViewById(R.id.button_open_mic);

        mMediaShareLayout.setVisibility(View.GONE);
        mVideoButton.setVisibility(View.GONE);
        mVideoSpinner.setVisibility(View.GONE);
        mMicButton.setVisibility(View.GONE);

        mVideoButton.setOnClickListener(view -> {
            if (mRtcClient != null) {
                mRtcClient.GetMediaSharePeerConnection(0).startVideoSource();
            }
        });

        mMicButton.setOnClickListener(view -> {
            if (mRtcClient != null) {
                mRtcClient.GetMediaSharePeerConnection(0).startAudio();
            }
        });
         */

        // aivoice
        mChatViewLayout = findViewById(R.id.linearLayout_aichat);
        mChatViewLayout.setVisibility(View.GONE);

        mAiChatRecyclerView = (RecyclerView)findViewById(R.id.recyclerView_aichat);

        mAiChatAdapter = new AiChatAdapter();

        mAiChatRecyclerView.setAdapter(mAiChatAdapter);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAiChatRecyclerView.setLayoutManager(layoutManager);

        mAiChatTextInput = findViewById(R.id.textInput_AiChat_Text);

        Button sendButton = findViewById(R.id.button_AiChat_Send);
        sendButton.setOnClickListener(view -> {
            String content = mAiChatTextInput.getText().toString();

            if (!"".equals(content)) {
                mAiChatAdapter.addUserSend(content);
                mAiChatRecyclerView.scrollToPosition(mAiChatAdapter.end());
                mAiChatTextInput.setText("");
                if (mRtcClient != null) {
                    mRtcClient.aiDmTextInput(content);
                }
            }
        });

        ImageButton recodeButton = findViewById(R.id.button_AiChat_VoiceRecode);
        recodeButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_DOWN:
                    if (mRtcClient != null) {
                        mRtcClient.startAiDmVoiceInput();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_POINTER_UP:
                    if (mRtcClient != null) {
                        mRtcClient.stopAiDmVoiceInput();
                    }
                    break;
            }
            return true;
        });

        Button aichatMenuButton = findViewById(R.id.menu_button_aichat);
        aichatMenuButton.setOnClickListener(view -> {
            if (mChatViewLayout.getVisibility() == View.VISIBLE) {
                mChatViewLayout.setVisibility(View.GONE);
            } else {
                mChatViewLayout.setVisibility(View.VISIBLE);
            }
        });
        /// aicaht end

        /// live streaming
        // 直播推流设置对话框
        mLiveStreamingSetupDialog = new LiveStreamingSetupDialog(this, mLiveStreamingSetupDialogCallback);

        Button livestreamingButton = findViewById(R.id.menu_button_livestreaming);
        livestreamingButton.setOnClickListener(view -> {
           hideMenu();
            mLiveStreamingSetupDialog.Show();
        });

        //
        // @sample 演示监听获取原始视频帧
        //
//        mRender.addRawVideoFrameListener(new VideoSink() {
//            @Override
//            public void onFrame(VideoFrame videoFrame) {
//                if (videoFrame.getBuffer() instanceof VideoFrame.I420Buffer) {
//                    VideoFrame.I420Buffer i420Buffer = (VideoFrame.I420Buffer) videoFrame.getBuffer();
//                    Log.d(TAG, "on i420 buffer ");
//                }  else if (videoFrame.getBuffer() instanceof VideoFrame.TextureBuffer) {
//                    VideoFrame.TextureBuffer textureBuffer = (VideoFrame.TextureBuffer) videoFrame.getBuffer();
//                    Log.d(TAG, "on texture buffer id " + textureBuffer.getTextureId() + " " + textureBuffer.getType());
//                }
//                Log.d(TAG, "on frame " + videoFrame.getBuffer().getWidth() + " " + videoFrame.getBuffer().getHeight());
//            }
//        });
        // 获取参数
        Intent intent = getIntent();
        EnterAppliInfo.Config rtcParams = intent.getParcelableExtra(EnterAppliInfo.Config.name);
        if (rtcParams != null) {
            Log.d(TAG, "start task config " + rtcParams);
            strect = rtcParams.initWinSize;
            // 判断云端应用是否支持触摸屏的方式
            mTouchScreenOperateMode = rtcParams.touchOperateMode.equals(RtcClient.TOUCH_OPERATE_MODE_TOUCHSCREEN);
            // new connection.
            if (mRtcClient == null) {
                mRtcClient = new RtcClient(rtcParams, mRender,
                        mRtcClientListener,
                        this);
                // 直播推流回调
                mRtcClient.SetRtmpCallback(mRtmpEvent);
                // 语音对话回调
                mRtcClient.SetAiVoiceCallback(mAiVoiceEvent);
                // task info callback
                mRtcClient.SetTaskInfoEvent(mTaskInfoCallback);
                // 开始连接
                connect();
            }

            // 互动模式
            if (rtcParams.playerMode != RtcClient.PLAYER_MODE_NORMAL) {
                mIsInteractive = true;
                findViewById(R.id.menu_button_player_list).setVisibility(View.VISIBLE);
                mPlayerListView = findViewById(R.id.player_list);
                mPlayerListAdapter = new com.pxy.demo.larksr.PlayerAdapter(this, R.layout.layout_player_item, new ArrayList<>());
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
            }

            // init touch input.
            mTouchInput = new TouchInput();
            // 虚拟键盘
            mSoftKeyboardHandler = new SoftKeyboardHandler(this, mRtcClient);
            // 手柄按钮
            mSoftGamepadHandler = new SoftGamepadHandler(this, mRtcClient);
            // 虚拟鼠标
            mVCursorHandler = new VCursorHandler(mRtcClient);
            mVCursor = new VCursorWithVMouse(this, findViewById(R.id.vcursor), mVCursorHandler);
            // mVCursor.getVCursorView().setVisibility(View.VISIBLE);
            mRtcClient.SetCloudCursorCallback(mVCursor);

            // 是否使用硬件手柄
            mHWGamepad = rtcParams.useGamepad;
            setMappingMode(mHWGamepad);

            // set bg color.
            if (!rtcParams.bgColor.isEmpty()) {
                try {
                    int color = Color.parseColor(rtcParams.bgColor);
                    findViewById(R.id.single_render_container).setBackgroundColor(color);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "parse bg color failed " + rtcParams.bgColor);
                }
            }
        } else {
            Log.w(TAG, "got app client failed");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on pause");
        if (mRtcClient != null) {
            mRtcClient.setPause(true);
            mRtcClient.setAudioEnable(false);
            mRtcClient.setVideoEnable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume");
        if (mRtcClient != null) {
            mRtcClient.setPause(false);
            mRtcClient.setAudioEnable(true);
            mRtcClient.setVideoEnable(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        disconnect(false);
        // 释放 gl 资源。
        mRender.release();
        mRender = null;
        if (mGesture != null) {
            mGesture.clearListener();
        }
        mTouchPointer = null;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 开始连接
     */
    private void connect() {
        if (mRtcClient != null) {
            mRtcClient.connect();
            // 设置手势监听
            if (!mTVMode) {
                // 云端应用支持使用触摸屏的映射方式
                if (mTouchScreenOperateMode) {
                    mTouchScreenHandler.setRtcClient(mRtcClient);
                    mRender.setOnTouchListener(mTouchScreenHandler);
                } else {
                    mRender.setOnTouchListener(mGesture);
                }
            }
        }
    }

    /**
     * 主动断开连接
     */
    private void disconnect(boolean finish) {
        if (finish) {
            finish();
        }
        if (mRtcClient != null) {
            mTouchScreenHandler.setRtcClient(null);
            mRtcClient.close();
            mRtcClient.release();
        }
        if (mRender != null) {
            mRender.setOnTouchListener(null);
        }
        clearConnection();
    }

    private void clearConnection() {
        mIsConnect = false;
        mTouchInput = null;
        mSoftKeyboardHandler = null;
        mRtcClient = null;
    }

    /**
     * 退出
     */
    public void onQuit(View view) {
        quit();
    }

    /**
     * 切换虚拟键盘显示
     */
    public void onKeyBoard(View view) {
        if (mSoftKeyboardHandler != null) {
            mSoftKeyboardHandler.toggle();
        }
    }

    /**
     * 监听并获取一帧视频
     */
    public void onTestCaptureVideoFrame(View view) {
        mRender.addFrameListener(new RtcEglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap frame) {
                Log.d(TAG, "on frame width " + frame.getWidth() + " height " + frame.getHeight());
                runOnUiThread(() -> {
                    mTestCaptureImageView.setImageBitmap(frame);
                });
            }
        }, 1.0f);
    }

    /**
     * 切换手柄映射模式
     * 直接映射手柄消息和映射为键盘消息
     */
    public void onToggleGamepadMappingMode(View view) {
        mHWGamepad = !mHWGamepad;
        setMappingMode(mHWGamepad);
    }

    private void setMappingMode(boolean directMode) {
        mGamePadMappingModeButton.setText(directMode ? R.string.ui_menu_keyboard_mapping_gamepad_mode : R.string.ui_menu_direct_gamepad_mode);
        if (mTVMode) {
            int visiblity = directMode ? View.GONE : View.VISIBLE;
            mJoystickMapping1Button.setVisibility(visiblity);
            mJoystickMapping2Button.setVisibility(visiblity);
        }
    }

    /**
     * 切换显示手柄和摇杆
     */
    public void onToggleHandleAndRocker(View view) {
        assert mHandle != null;
        assert mSoftJoystickView != null;
        if (mHandle.getVisibility() == View.VISIBLE && mSoftJoystickView.getVisibility() == View.VISIBLE) {
            mHandle.setVisibility(View.INVISIBLE);
            mSoftJoystickView.setVisibility(View.INVISIBLE);
        } else {
            mHandle.setVisibility(View.VISIBLE);
            mSoftJoystickView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 切换显示虚拟鼠标
     */
    public void onToggleVmouse(View view) {
        mVCursor.toggle();
    }

    /**
     * 切换控制栏显示
     */
    public void onToggleControlBar(View view) {
        syncControlBarStatus(!mIsControlBarToggle);
    }

    /**
     * 显示菜单
     *
     * @param view
     */
    public void onToggleMenu(View view) {
        if (mShowMenu) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    public void onHideMenu(View view) {
        hideMenu();
    }

    public void onJoystickMapping1(View view) {
        setJoystickMode(1);
    }

    public void onJoystickMapping2(View view) {
        setJoystickMode(2);
    }

    public void onDapdMapping1(View view) {
        setDapdMode(1);
    }

    public void onDapdMapping2(View view) {
        setDapdMode(2);
    }

    public void setJoystickMode(int mode) {
        String btnMode1Str = "";
        String btnMode2Str = "";
        int joystickMapping;
        int gamepadMapping;
        switch (mode) {
            case 1:
                joystickMapping = R.xml.joystick_mapping_1;
                gamepadMapping = R.xml.gamepad_mapping_1;
                btnMode1Str = getString(R.string.ui_menu_joystick_mode_1, getString(R.string.ui_menu_mode_current));
                btnMode2Str = getString(R.string.ui_menu_joystick_mode_2, "");
                break;
            case 2:
                joystickMapping = R.xml.joystick_mapping_2;
                gamepadMapping = R.xml.gamepad_mapping_2;
                btnMode1Str = getString(R.string.ui_menu_joystick_mode_1, "");
                btnMode2Str = getString(R.string.ui_menu_joystick_mode_2, getString(R.string.ui_menu_mode_current));
                break;
            default:
                // not support mode.
                return;
        }
        mJoystick.parseWinkeyCodeMappingXml(getResources(), joystickMapping);
        mGamepad.parseWinkeyCodeMappingXml(getResources(), gamepadMapping);
        mJoystickMapping1Button.setText(btnMode1Str);
        mJoystickMapping2Button.setText(btnMode2Str);
    }

    public void setDapdMode(int mode) {
        String btnMode1Str = "";
        String btnMode2Str = "";
        int dpadMapping;
        switch (mode) {
            case 1:
                dpadMapping = R.xml.dpad_mapping_1;
                btnMode1Str = getString(R.string.ui_menu_dpad_mode_1, getString(R.string.ui_menu_mode_current));
                btnMode2Str = getString(R.string.ui_menu_dpad_mode_2, "");
                break;
            case 2:
                dpadMapping = R.xml.dpad_mapping_2;
                btnMode1Str = getString(R.string.ui_menu_dpad_mode_1, "");
                btnMode2Str = getString(R.string.ui_menu_dpad_mode_2, getString(R.string.ui_menu_mode_current));
                break;
            default:
                return;
        }
        mDpadMapping1Button.setText(btnMode1Str);
        mDpadMapping2Button.setText(btnMode2Str);
        mDpad.parseWinkeyCodeMappingXml(getResources(), dpadMapping);
    }


    public void onMenuList(View view) {
        setMenuView(0);
        findViewById(R.id.menu_button_back).requestFocus();
    }

    public void onMenuInfo(View view) {
        setMenuView(1);
        findViewById(R.id.menu_button_back_1).requestFocus();
    }

    public void onMenuPlayerList(View view) {
        setMenuView(2);
        findViewById(R.id.menu_button_back_2).requestFocus();
    }

    private void setMenuView(int current) {
        toggleMenuView(mMenuViewList, current, 0);
        toggleMenuView(mMenuViewInfo, current, 1);
        toggleMenuView(mMenuViewPlayerList, current, 2);
    }

    private void toggleMenuView(View view, int current, int flag) {
        view.setVisibility(flag == current ? View.VISIBLE : View.INVISIBLE);
    }

    private void quit() {
        alertInnner(getString(R.string.ui_alert_quit), "", true,
                (dialog, which) -> {
                    finish();
                }, (dialog, which) -> {
                }
        );
    }

    private final ListView.OnItemClickListener mPlayerlistSelected = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AppNotification.PlayerDesc playerDesc = mPlayerListAdapter.getItem(position);
            Log.d(TAG, "select player.");
            AppNotification.PlayerDesc currentUser = mPlayerListAdapter.findUser(mUserId);
            if (currentUser == null) {
                Log.d(TAG, "find current user faild " + mUserId);
                return;
            }
            if (!currentUser.isTaskOwner()) {
                toastInner("您不是房主");
                return;
            }
            boolean res = mRtcClient.dispatchController(playerDesc.getId());
            if (!res) {
                Log.d(TAG, "send dispatch controller failed.");
            } else {
                toastInner("请求赋予观看者 " + playerDesc.getNickName() + " 操作权限");
            }
        }
    };

    /**
     * RtcClientEvent. rtc 客户端回调事件
     */
    private final RtcClient.RtcClientEvent mRtcClientListener = new RtcClient.RtcClientEvent() {
        /**
         *  socket 连接成功时回调
         */
        @Override
        public void onConnect() {
            Log.d(TAG, "app rtc connected");
            mIsConnect = true;
        }

        @Override
        public void onLoginSuccess(int uid) {
            Log.d(TAG, "onLoginSuccess " + uid);
            mUserId = uid;
        }

        /**
         * 首次收到视频帧时回调
         * 3.1.3.3 新增
         */
        @Override
        public void onMediaReady() {
            Log.d(TAG, "on media ready");
            mRtcClient.setVolume(10);
        }

        /**
         * 视频帧大小变化时回调
         * 3.1.3.3 新增
         * @param videoWidth
         * @param videoHeight
         * @param rotation
         */
        @Override
        public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
            Log.d(TAG, "onFrameResolutionChanged video width " + videoWidth + " video height " + videoHeight + " rotation " + rotation);
        }

        /**
         * socket 断开连接
         */
        @Override
        public void onDisconnect() {
            Log.d(TAG, "app rtc onDisconnect");
            if (!mIsConnect) {
                return;
            }
            mIsConnect = false;
            alertInnner(getString(R.string.msg_rtc_close), false, (dialog1, id) -> finish());
        }

        /**
         * 出现错误时回调
         * @param err 错误描述
         */
        @Override
        public void onError(String err) {
            Log.w(TAG, "app rtc client error:" + err);
            mIsConnect = false;
            alertInnner(err, false, (dialog1, id) -> finish());
        }

        /**
         * 无操作超时。
         */
        @Override
        public void onNoOpreationTimeout() {
            Log.w(TAG, "onNoOpreationTimeout");
            alertInnner(getString(R.string.msg_rtc_nooperation_timeout), false, (dialog1, id) -> finish());
        }

        /**
         *
         * @param msg 描述
         */
        @Override
        public void onInfo(String msg) {
            Log.d(TAG, "app rtc client info:" + msg);
            toastInner(msg);
        }

        /**
         * 采集状态
         * @param sampleRTCStats 状态
         */
        @Override
        public void onPeerStatusReport(RtcClient.SampleRTCStats sampleRTCStats) {
            Log.d(TAG, "onPeerStatusReport " + sampleRTCStats);
            runOnUiThread(() -> {
                if (mRttText != null) {
                    mRttText.setText(String.valueOf(sampleRTCStats.rtt * 1000) );
                }
                if (mBitrateText != null) {
                    mBitrateText.setText(String.valueOf(sampleRTCStats.bytesPerSec * 1024));
                }
                if (mFPSText != null) {
                    mFPSText.setText(String.valueOf(sampleRTCStats.frameDeocdedPreSec));
                }
                if (mPackgelost != null) {
                    mPackgelost.setText(String.valueOf(sampleRTCStats.packageLostRatePerSec));
                }
                if (mServerCaptureDelayText != null) {
                    mServerCaptureDelayText.setText(String.valueOf(sampleRTCStats.serverCaptureDelay));
                }
                if (mServerRenderDelayText != null) {
                    mServerRenderDelayText.setText(String.valueOf(sampleRTCStats.serverRenderDelay));
                }
                if (mServerEncoderDelay != null) {
                    mServerEncoderDelay.setText(String.valueOf(sampleRTCStats.serverEncoderDelay));
                }
            });
        }

        /**
         *  数据通道打开
         *  3.1.3.3 新增
         */
        @Override
        public void onDataChannelOpen() {
            Log.d(TAG, "onDataChannelOpen");
        }

        /**
         *  数据通道关闭
         * 3.1.3.3 新增
         */
        @Override
        public void onDataChannelClose() {
            Log.d(TAG, "onDataChannelClose");
        }

        /**
         * 数据通道字符消息
         * 3.1.3.3 新增
         */
        @Override
        public void onDataChannelMessage(String msg) {
            Log.d(TAG, "onDataChannelMessage str " + msg);
        }

        /**
         * 数据通道字节消息
         * 3.1.3.3 新增
         */
        @Override
        public void onDataChannelMessage(byte[] buffer) {
            Log.d(TAG, "onDataChannelMessage binary");
        }

        /**
         * 应用请求输入字符
         * 3.1.3.3 新增
         */
        @Override
        public void onAppRequestInput(boolean enable) {
            Log.d(TAG, "onAppRequestInput " + enable);
            if (enable) {
                showSoftKeyboard(mEditText);
            } else {
                hideSoftKeyboard(mEditText);
            }
        }

        /**
         * 应用请求手柄震动
         * 3.1.3.3 新增
         */
        @Override
        public void onAppRequestGamepadOutput() {
            Log.d(TAG, "onAppRequestGamepadOutput");
        }

        /**
         * 云端应用大小变化时
         * @param mouseLockRect 3.1.1.0 新增 mouseLockRect
         */
        @Override
        public void onAppResize(AppNotification.AppResize mouseLockRect) {
            mAppWidth = mouseLockRect.getWidth();
            mAppHeight = mouseLockRect.getHeight();
            if (mTouchInput != null) {
                mTouchInput.setAppSize(mAppWidth, mAppHeight);
            }
            if (mVCursorHandler != null) {
                mVCursorHandler.setAppWidth(mAppWidth);
                mVCursorHandler.setAppHeight(mAppHeight);
            }
            mTouchScreenHandler.setAppSize(mAppWidth, mAppHeight);
            resize();
        }

        /**
         * 云端应用鼠标状态变化时回调.第一人称模式时隐藏触摸点。
         * @param mouseMode 3.1.1.0 新增
         */
        @Override
        public void onMouseState(AppNotification.AppMouseMode mouseMode) {
            Log.d(TAG, "onMouseState:" + mouseMode.isMouseLock());
            if (mouseMode.isMouseLock()) {
                toastInner(R.string.msg_rtc_enter_first_person_mode);
                setTouchPointVisibility(View.INVISIBLE);
            } else {
                setTouchPointVisibility(View.VISIBLE);
            }
        }

        /**
         * 3.1.1.0 新增
         * @param playerList 多人互动模式时用户列表
         */
        @Override
        public void onPlayerList(List<AppNotification.PlayerDesc> playerList) {
            if (mPlayerListAdapter == null) {
                Log.w(TAG, "no playerlist adapter found");
                return;
            }
            Log.d(TAG, "onPlayerList " + playerList);
            runOnUiThread(() -> {
                if (mPlayerListAdapter.getCount() < playerList.size()) {
                    toastInner("有观看者加入");
                }
                mPlayerListAdapter.fresh(playerList);
            });
        }
    };

    private final Gesture.GestureEvent mGestureListener = new Gesture.GestureEvent() {
        /**
         *  触摸手势事件回调
         * @param gestureMovement 手势事件。
         * @param view 触发事件的组件。
         */
        @Override
        public void onGestureEvent(Gesture.GestureMovement gestureMovement, View view) {
            if (mRtcClient == null || mTouchInput == null) {
                Log.d(TAG, "skip getsture when client not inited");
                return;
            }
            mTouchInput.handleGestureEvent(mRtcClient, gestureMovement, view);
            // 同步触摸点位置
            syncTouchPointPosition(gestureMovement, view);
        }
    };

    /**
     * 同步显示触摸点位置
     *
     * @param gestureMovement 手势事件
     */
    private void syncTouchPointPosition(final Gesture.GestureMovement gestureMovement, final View view) {
        runOnUiThread(() -> {
            if (mTouchPointer != null) {
                float ox = view.getLeft();
                float oy = view.getTop();
                float x = gestureMovement.getX();
                float y = gestureMovement.getY();
                float pointRadius = (float) (mTouchPointer.getWidth() / 2.0);
                mTouchPointer.setX(x + ox - pointRadius);
                mTouchPointer.setY(y + oy - pointRadius);
            }
        });
    }

    /**
     * 设置触摸点位置。相对本地坐标。
     *
     * @param x 触摸点 x
     * @param y 触摸点 y
     */
    private void setTouchPointerLocalPosition(final int x, final int y) {
        runOnUiThread(() -> {
            if (mTouchPointer != null) {
                mTouchPointer.setX(mStage.stageX + x);
                mTouchPointer.setY(mStage.stageY + y);
            }
        });
    }

    /**
     * 获取触摸点相对云端应用缩放后的坐标。
     *
     * @return 触摸点
     */
    private Positon getTouchPointerPosition() {
        Positon p = new Positon(0, 0);
        if (mTouchPointer != null) {
            p = getRelCloudAppPositon((int) mTouchPointer.getX(), (int) mTouchPointer.getY());
        }
        return p;
    }

    /**
     * 获取相对云端的位置
     *
     * @param x 位置 x
     * @param y 位置 y
     * @return 云端点的位置
     */
    private Positon getRelCloudAppPositon(int x, int y) {
        return getRelCloudAppPositon(x, y, 0, 0);
    }

    /**
     * 获取相对云端的位置
     *
     * @param x 位置 x
     * @param y 位置 y
     * @return 云端点的位置
     */
    private Positon getRelCloudAppPositon(int x, int y, int rx, int ry) {
        Positon p = new Positon(x - mStage.stageX, y - mStage.stageY, rx, ry);
        float scaleX = (float) mAppWidth / (float) mStage.stageW;
        float scaleY = (float) mAppHeight / (float) mStage.stageH;
        p.setScale(scaleX, scaleY);
        return p;
    }

    /**
     * 设置触摸点是否显示。
     *
     * @param visibility 是否可见
     */
    private void setTouchPointVisibility(final int visibility) {
        runOnUiThread(() -> {
            assert mTouchPointer != null;
            mTouchPointer.setVisibility(visibility);
        });
    }


    // 摇杆事件监听
    private final SoftJoystick.SoftJoystickEvent mSoftJoystickListener = new SoftJoystick.SoftJoystickEvent() {
        /**
         * 摇杆移动事件
         * @param rx 摇杆 x 轴相对移动
         * @param ry 摇杆 y 轴相对移动
         */
        @Override
        public void onRockerMove(final double rx, final double ry) {
            runOnUiThread(() -> {
                Positon op = getTouchPointerPosition();
                int nx = op.getX() + (int) rx;
                int ny = op.getY() + (int) ry;
                if (nx > mStage.stageW || nx < 0) {
                    nx = op.getX();
                }
                if (ny > mStage.stageH || nx < 0) {
                    ny = op.getY();
                }
                int xPos = 0;
                int yPos = 0;
                int xRel = 0;
                int yRel = 0;
                assert mRtcClient != null;
                if (!mRtcClient.getIsLockMouse()) {
                    xPos = Math.round(nx * op.getScaleX());
                    yPos = Math.round(ny * op.getScaleY());
                }
                xRel = (int) rx * Gesture.LOCK_MOUSE_MOVE_SPEED_EX_SLOW;
                yRel = (int) ry * Gesture.LOCK_MOUSE_MOVE_SPEED_EX_SLOW;
                if (mRtcClient != null) {
                    mRtcClient.sendMouseMove(xPos, yPos, xRel, yRel);
                }
                setTouchPointerLocalPosition(nx, ny);
            });
        }
    };

    /**
     * 同步控制栏状态。
     *
     * @param toggle 是否折叠
     */
    private void syncControlBarStatus(boolean toggle) {
        assert mToggleControlBarButton != null;
        assert mControlBar != null;
        ViewGroup.LayoutParams layout = mControlBar.getLayoutParams();
        if (toggle) {
            layout.width = 150;
            mControlBar.setLayoutParams(layout);
            mToggleControlBarButton.setImageResource(R.mipmap.cloudlark_arror_left_white);
        } else {
            layout.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mControlBar.setLayoutParams(layout);
            mToggleControlBarButton.setImageResource(R.mipmap.cloudlark_arror_right_white);
        }
        mIsControlBarToggle = toggle;
    }



    /**
     * 缩放画面。 适应屏幕大小，，完全显示内容，
     */
    private void resize() {
        runOnUiThread(() -> {
            int stageW = mAppWidth > 0 ? mAppWidth : RtcClient.WIDTH;
            int stageH = mAppHeight > 0 ? mAppHeight : RtcClient.HEIGHT;
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int containerWidth = frame.width();
            int containerHeight = frame.height();

            float scale = Math.min((float) containerWidth / (float) stageW, (float) containerHeight / (float) stageH);
            int scaleW = Math.round(stageW * scale);
            int scaleH = Math.round(stageH * scale);
            int stageX = Math.round((containerWidth - scaleW) / 2.0f);
            int stageY = Math.round((containerHeight - scaleH) / 2.0f);
//            初始化窗口方式
//            0：按照原始窗口大小显示
//            2：尽量填充容器（保存宽高比）
//            3：完全填充容器（裁剪）
//            4：完全填充容器（拉伸）
            switch (strect) {
                case 0: {
                    // 同步本地渲染控件大小和位置。
                    mStage.set(scaleW, scaleH, stageX, stageY);
                    if (mVCursorHandler != null) {
                        mVCursorHandler.setStage(mStage);
                    }
                    ViewGroup.LayoutParams layout;
                    layout = mRender.getLayoutParams();
                    layout.width = scaleW;
                    layout.height = scaleH;
                    mRender.setLayoutParams(layout);
                }
                break;
                case 2: {
                    // 同步本地渲染控件大小和位置。
                    mStage.set(scaleW, scaleH, stageX, stageY);
                    if (mVCursorHandler != null) {
                        mVCursorHandler.setStage(mStage);
                    }
                    ViewGroup.LayoutParams layout;
                    layout = mRender.getLayoutParams();
                    layout.width =  scaleW;
                    layout.height = scaleH;
                    mRender.setLayoutParams(layout);
                }
                break;
                case 3: {
                    // 同步本地渲染控件大小和位置。
                    mStage.set(scaleW, scaleH, stageX, stageY);
                    if (mVCursorHandler != null) {
                        mVCursorHandler.setStage(mStage);
                    }
                    ViewGroup.LayoutParams layout;
                    layout = mRender.getLayoutParams();
                    layout.width =  scaleW;
                    layout.height = scaleH;
                    mRender.setLayoutParams(layout);
                }
                break;
                case 4: {
                    // 同步本地渲染控件大小和位置。
                    mStage.set(containerWidth, containerHeight, stageX, stageY);
                    if (mVCursorHandler != null) {
                        mVCursorHandler.setStage(mStage);
                    }
                    ViewGroup.LayoutParams layout;
                    layout = mRender.getLayoutParams();
                    layout.width =  containerWidth;
                    layout.height = containerHeight;
                    mRender.setLayoutParams(layout);
                }
                break;
                default: {
                    // 同步本地渲染控件大小和位置。
                    mStage.set(scaleW, scaleH, stageX, stageY);
                    if (mVCursorHandler != null) {
                        mVCursorHandler.setStage(mStage);
                    }
                    ViewGroup.LayoutParams layout;
                    layout = mRender.getLayoutParams();
                    layout.width = scaleW;
                    layout.height = scaleH;
                    mRender.setLayoutParams(layout);
                };
            }

        });
    }

    /**
     * 处理实体鼠标摇杆等.
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        if (mShowMenu) {
            return super.dispatchGenericMotionEvent(motionEvent);
        }
        if (mSoftKeyboardHandler.isActive()) {
            return super.dispatchGenericMotionEvent(motionEvent);
        }
        // handle
        if (mRtcClient == null) {
            return super.dispatchGenericMotionEvent(motionEvent);
        }
//        Log.d(TAG, "keyevent dpad " + WindowsXInputGamepad.isDpadDevice(motionEvent) +
//                " joy " + WindowsXInputGamepad.isJoystickDevice(motionEvent) +
//                " mouse " + WindowsXInputGamepad.isMouseDevice(motionEvent) +
//                " keyboard " + WindowsXInputGamepad.isKeyboardDevice(motionEvent) +
//                " gamepad " + WindowsXInputGamepad.isGamepadDevice(motionEvent));
        // process hardware gampad input.
        if (mVCursor.isActive() && mVCursor.handleMotionEvent(motionEvent)) {
            // handle nothing. allow all input event.
            return false;
        }
        if (mHWGamepad && WindowsXInputGamepad.processGenericMotionEvent(motionEvent, mRtcClient)) {
            // handle nothing. allow all input event.
            return false;
        }
        if (WindowsXInputGamepad.isJoystickDevice(motionEvent)) {
//            Log.d(TAG, "joystick event.");
            return handleJoystick(motionEvent);
        } else if (WindowsXInputGamepad.isMouseDevice(motionEvent)) {
            handleMouse(motionEvent);
        } else if (WindowsXInputGamepad.isDpadDevice(motionEvent)) {
//            Log.d(TAG, "dpad motion event");
            return handleDpad(motionEvent);
        } else if (WindowsXInputGamepad.isGamepadDevice(motionEvent)) {
            Log.d(TAG, "gamepad event.");
            return handleDpad(motionEvent);
        }
        return false;
    }

   @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (mSoftKeyboardShown) {
            return super.dispatchKeyEvent(keyEvent);
        }

        // handle local operate
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK || keyEvent.getKeyCode() == KeyEvent.KEYCODE_BUTTON_SELECT) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (mShowMenu) {
                    hideMenu();
                } else {
                    showMenu();
//                    finish();
                }
                return false;
            } else {
                return super.dispatchKeyEvent(keyEvent);
            }
        }
        if (mShowMenu) {
            return super.dispatchKeyEvent(keyEvent);
        }
        if (mSoftKeyboardHandler.isActive()) {
            return super.dispatchKeyEvent(keyEvent);
        }
//        Log.d(TAG, "keyevent dpad " + WindowsXInputGamepad.isDpadDevice(keyEvent) +
//                " joy " + WindowsXInputGamepad.isJoystickDevice(keyEvent) +
//                " mouse " + WindowsXInputGamepad.isMouseDevice(keyEvent) +
//                " keyboard " + WindowsXInputGamepad.isKeyboardDevice(keyEvent) +
//                " gamepad " + WindowsXInputGamepad.isGamepadDevice(keyEvent));
//        Log.d(TAG, "keyevent " + keyEvent);
        // handle
        if (mRtcClient == null) {
            return super.dispatchKeyEvent(keyEvent);
        }
        if (mVCursor.isActive() && mVCursor.handelKeyEvent(keyEvent)) {
            return true;
        }
        if (mHWGamepad && WindowsXInputGamepad.processKeyInput(keyEvent, mRtcClient)) {
            return true;
        }
        int winCode = -1;
        if (WindowsXInputGamepad.isJoystickDevice(keyEvent)) {
            // joystick event.
            winCode = mJoystick.findWindKeyCode(keyEvent.getKeyCode());
        } else if (WindowsXInputGamepad.isDpadDevice(keyEvent)) {
            // dpad
            int code = mDpad.handleInput(keyEvent);
            winCode = mDpad.findWindKeyCode(code);
        } else if (WindowsXInputGamepad.isGamepadDevice(keyEvent)) {
            // gamepad
            winCode = mGamepad.findWindKeyCode(keyEvent.getKeyCode());
        } else if (WindowsXInputGamepad.isKeyboardDevice(keyEvent)) {
            // keyboard.
            winCode = WindowsKeyCodes.getWindowsKeycodeFromeKeyEvent(keyEvent);
        }
        if (winCode == -1) {
            return false;
        }
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            mRtcClient.sendKeyDown(winCode, keyEvent.getRepeatCount() > 0);
        } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            mRtcClient.sendKeyUp(winCode);
        }
        return true;
    }

    void handleMouse(MotionEvent event) {
        if (!mIsConnect || mRtcClient == null) {
            return;
        }
        final int localX = (int) event.getX();
        final int localY = (int) event.getY();
        final int localRX = event.getHistorySize() >= 1 ? Math.round(event.getX() - event.getHistoricalX(0)) : 0;
        final int localRY = event.getHistorySize() >= 1 ? Math.round(event.getY() - event.getHistoricalY(0)) : 0;
        Positon op = getRelCloudAppPositon(localX, localY, localRX, localRY);
        if (op.getX() > mStage.stageW || op.getX() < 0) {
            return;
        }
        if (op.getY() > mStage.stageH || op.getY() < 0) {
            return;
        }
        int xPos = op.getScaledX();
        int yPos = op.getScaledY();
        if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
//            Log.d(TAG, "send mouse down" + xPos + " " + yPos + " " + event.getSource());
            mRtcClient.sendMouseDown(xPos, yPos, ClientInput.MouseKey.MOUSE_KEY_LEFT);
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            int rx = op.getRx();
            int ry = op.getRy();
//            Log.d(TAG, "send mouse move " + xPos + " " + yPos + " " + rx + " " + ry);
            mRtcClient.sendMouseMove(xPos, yPos, rx, ry);
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
//            Log.d(TAG, "send mouse up" + xPos + " " + yPos);
            mRtcClient.sendMouseUp(xPos, yPos, ClientInput.MouseKey.MOUSE_KEY_LEFT);
        }
    }

    private boolean handleDpad(InputEvent inputEvent) {
        if (mRtcClient == null) {
            return false;
        }
        int keyCode = mDpad.handleInput(inputEvent);
        if (keyCode == Dpad.BACK) {
            disconnect(true);
            return true;
        } else if (keyCode == -1) {
            Log.d(TAG, "not handle dpad event.");
            return false;
        }
        int winCode = mDpad.findWindKeyCode(keyCode);
        if (winCode == Dpad.DPAD_KEY_NONE) {
            return false;
        }
        if (mDpad.isDown()) {
            // send presssed key
            mRtcClient.sendKeyDown(winCode, mDpad.isRepeat());
        } else {
            mRtcClient.sendKeyUp(winCode);
        }
        return true;
    }

    private boolean handleJoystick(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                handleJoyStickStatus(mJoystick.processJoystickInput(event, i));
            }

            // Process the current movement sample in the batch (position -1)
            handleJoyStickStatus(mJoystick.processJoystickInput(event, -1));
        } else {
            Log.d(TAG, event.toString());
        }
        return true;
    }

    private void handleJoyStickStatus(Joystick.JoyStickStatus joyStickStatus) {
        if (mRtcClient == null) {
            return;
        }

        for (Joystick.AxisMappingStatus status : joyStickStatus.toAllMappingKeyCodeList()) {
            if (status.keyCode == Joystick.JOYSTICK_KEY_NONE || status.status == Joystick.AxisStatus.AXIS_STATUS_NONE) {
                continue;
            }
            if (status.status == Joystick.AxisStatus.AXIS_STATUS_END) {
                mRtcClient.sendKeyUp(status.keyCode);
            } else {
                mRtcClient.sendKeyDown(status.keyCode, status.status == Joystick.AxisStatus.AXIS_STATUS_REPEAT);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 直播推流回调
    private final RtcClient.RtmpEvent mRtmpEvent = new RtcClient.RtmpEvent() {
        @Override
        public void onRtmpState(int i, String s) {
            if (s != null) {
                toastInner(s);
            }
        }

        @Override
        public void onRtmpError(int i, String s) {
            if (s != null) {
                toastInner(s);
            }
        }
    };

    private final LiveStreamingSetupDialog.LiveStreamingSetupDialogCallback mLiveStreamingSetupDialogCallback =
            new LiveStreamingSetupDialog.LiveStreamingSetupDialogCallback() {
        @Override
        public void OnStartLiveStreaming(LiveStreamingSetupDialog dialog) {
            if (dialog.getPath().isEmpty()) {
                toastInner(R.string.ui_setup_livestreaming_empty_path);
                return;
            }

            if (mRtcClient != null) {
                mRtcClient.StartCloudLiveStreaming(dialog.getPath(),
                        dialog.getKey(),
                        dialog.getWidthValue(),
                        dialog.getHeightValue(),
                        dialog.getCodeRateValue(),
                        dialog.getFrameRateValue());
                toastInner(R.string.ui_setup_livestreaming_starting);
            }
        }

        @Override
        public void OnStopLiveStreaming() {
            if (mRtcClient != null) {
                mRtcClient.StopLiveStreaming();
                toastInner(R.string.ui_setup_livestreaming_stoping);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void showMenu() {
        runOnUiThread(() -> {
            // showSoftKeyboard(mEditText);
            NewCapture();

//            mShowMenu = true;
//            mMenu.setVisibility(View.VISIBLE);
//            mMenu.setFocusable(true);
//            findViewById(R.id.menu_button_back).requestFocus();
        });
    }

    private void hideMenu() {
        runOnUiThread(() -> {
            setMenuView(0);
            mShowMenu = false;
            mMenu.setVisibility(View.INVISIBLE);
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// ai voice
    private RtcClient.AiVoiceEvent mAiVoiceEvent = new RtcClient.AiVoiceEvent() {
        @Override
        public void onAiVoiceResult(int voiceId, String text) {
            Log.d(TAG, "onAiVoiceResult " + voiceId + " " + text);
            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);

                    JSONObject dm = jsonObject.optJSONObject("dm");

                    if (dm != null) {
                        String ngl = dm.optString("nlg");
                        if (!ngl.isEmpty()) {
                            mAiChatAdapter.addAiReceive(ngl);
                            mAiChatRecyclerView.scrollToPosition(mAiChatAdapter.end());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onAiVoiceRealTimeParseResult(int voiceId, String text) {
            Log.d(TAG, "onAiVoiceRealTimeParseResult " + voiceId + " " + text);

            runOnUiThread(() -> {
                mAiChatAdapter.addUserSend(text);
            });
        }

        @Override
        public void onAiVoiceServerStatus(boolean success, String reason) {
            Log.d(TAG, "onAiVoiceServerStatus " + success + " " + reason);
            runOnUiThread(() -> {
                if (success) {
                    mChatViewLayout.setVisibility(View.VISIBLE);
                }
                mAiChatAdapter.addAiReceive(reason);
            });
        }

        @Override
        public void onAiVoiceError(int voiceId, String text) {
            Log.d(TAG, "onAiVoiceError " + voiceId + " " + text);
            runOnUiThread(() -> {
                mAiChatAdapter.addAiReceive(text);
            });
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private final RtcClient.TaskInfoEvent mTaskInfoCallback = new RtcClient.TaskInfoEvent() {
        @Override
        public void onTaskSuccess(GetTask.TaskInfo taskInfo) {

            /**
             * open media > next version
            if (taskInfo.videoInput == 1) {
                runOnUiThread(() -> {
                    mMediaShareLayout.setVisibility(View.VISIBLE);
                    mVideoButton.setVisibility(View.VISIBLE);
                    mVideoSpinner.setVisibility(View.VISIBLE);
                });
            }
            if (taskInfo.audioInput == 1) {
                runOnUiThread(() -> {
                    mMediaShareLayout.setVisibility(View.VISIBLE);
                    mMicButton.setVisibility(View.VISIBLE);
                });
            }
             */
        }

        @Override
        public void onSync() {
            Log.d(TAG, "task on sync");
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// handle keyboard text input
    private void showSoftKeyboard(View view) {
        runOnUiThread(() -> {
            Log.d(TAG, "show soft keyboard");
            InputMethodManager imm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                view.requestFocus();
                imm = getSystemService(InputMethodManager.class);
                imm.showSoftInput(view, 0);
                mSoftKeyboardShown = true;
            } else {
                Log.w(TAG, "not support");
            }
        });
    }
    private void hideSoftKeyboard(View view) {
        runOnUiThread(() -> {
            InputMethodManager imm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                imm = getSystemService(InputMethodManager.class);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                mSoftKeyboardShown = false;
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void toastInner(int resId) {
        toastInner(getString(resId));
    }

    private void toastInner(final String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void alertInnner(final String title, boolean cancleAble, DialogInterface.OnClickListener positive) {
        alertInnner(title, "", cancleAble, positive, null);
    }

    private void alertInnner(final String title, final String msg, boolean cancleAble,
                             DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            if (msg != null && !msg.isEmpty()) {
                builder.setMessage(msg);
            }
            builder.setCancelable(cancleAble);
            if (positive != null) {
                builder.setPositiveButton(R.string.summit, positive);
            }
            if (negative != null) {
                builder.setNegativeButton(R.string.cancle, negative);
            }
            builder.create().show();
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    boolean capturing  = false;
    String assetImgUploadID = "1";
    String xToken = "2";
    // test
    public void NewCapture() {
        Log.d(TAG,  "start");

        if (mRtcClient != null) {
            mRtcClient.sendToAppDataChannel("111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa");
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        capturing=true;
        captureLanucher.launch(takePictureIntent);

        Log.i(TAG, "DaemonService---->onCreate被调用，启动前台service");
    }


    public final ActivityResultLauncher<Intent> captureLanucher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG,  "result " + result.getResultCode());
                if (mRtcClient != null) {
                    mRtcClient.sendToAppDataChannel("111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa");
                }
                if (result.getResultCode() == RESULT_OK) {

                    long startTime=System.currentTimeMillis();
                    Intent rst = result.getData();
                    Bitmap thumbnail = (Bitmap) rst.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    String _base64 = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
                    long endTime=System.currentTimeMillis();
                    Log.d("ConsumpTionTIme","计算base64耗时(ms)："+(endTime-startTime));

                    if (assetImgUploadID != null&&xToken!=null) {
                        PostEncodeImageData(assetImgUploadID, xToken, _base64);
                    }
                }
                capturing=false;
                try {
                    Thread.sleep(2000);
                    if (mRtcClient != null) {
                        mRtcClient.sendToAppDataChannel("111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa111111111111111111112323232323232323232323232323fdsfsdafasfasfasfasfasfasfasfasdfasfasfsa");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
    );


    public void PostEncodeImageData(String assetID, String token, String base64Data) {
        Log.d(TAG,  "base64Data " + base64Data);
    }
}