package com.pxy.demo.larksr;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.pxy.larkcore.request.AppListItem;
import com.pxy.larkcore.request.EnterAppliInfo;
import com.pxy.larkcore.request.RoomCode;
import com.pxy.lib_sr.RtcClient;

class InterActiveDialog {
    private Activity mContext;
    private EnterAppliInfo.Callback mCallback;
    private AppListItem mItem;
    private AlertDialog mDialog;
    private RadioGroup mRoleGroup;
    private RadioGroup mObModeGroup;
    private EditText mNickNamInput;
    private EditText mRoomCodeInput;
    private Button mGenRoomCodeButton;
    private View mGenRoomCodeGroup;
    private TextView mRoomCodeTextView;
    private Button mEnterAppliButton;
    private boolean mObMode = false;
    private boolean mObTaskIdMode = false;
    private int mFrameRate = 30;
    private int mCodeRate = 5000;
    private String mRoomCode = "";

    public InterActiveDialog(Activity context, EnterAppliInfo.Callback callback) {
        mContext = context;
        mCallback = callback;
        setUpView();
    }

    public void enter(int frameRate, int codeRate, AppListItem item) {
        mContext.runOnUiThread(() -> {
            playerMode(true);
            mFrameRate = frameRate;
            mCodeRate = codeRate;
            mRoomCodeTextView.setVisibility(View.GONE);
            mRoomCode = "";
            mRoomCodeTextView.setText("");
            mRoomCodeInput.setText("");
            mObMode = false;
            mItem = item;
            mDialog.show();
        });
    }

    public void close() {
        mRoomCode = "";
        mObMode = false;
        mItem = null;
        if (mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    private void setUpView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View dialogView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_setup_player, null);
        mRoleGroup = dialogView.findViewById(R.id.radio_group_role);
        mRoleGroup.setOnCheckedChangeListener(mRoleChange);
        mObModeGroup = dialogView.findViewById(R.id.radio_group_ob_auth_mode);
        mObModeGroup.setOnCheckedChangeListener(mObModeChange);
        mNickNamInput = dialogView.findViewById(R.id.input_text_live_path);
        mRoomCodeInput = dialogView.findViewById(R.id.input_text_ob_mode_roomcode);
        mGenRoomCodeButton = dialogView.findViewById(R.id.button_gen_room_code);
        mGenRoomCodeButton.setOnClickListener(this::genRoomCode);
        mGenRoomCodeGroup = dialogView.findViewById(R.id.gen_room_code_group);
        mRoomCodeTextView = dialogView.findViewById(R.id.text_room_code);
        mRoomCodeTextView.setVisibility(View.GONE);
        mEnterAppliButton = dialogView.findViewById(R.id.button_setup_live_submit);
        mEnterAppliButton.setOnClickListener(this::onEnterAppli);
        builder.setView(dialogView);
        mDialog = builder.create();
        playerMode(true);
    }

    private RadioGroup.OnCheckedChangeListener mRoleChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_button_player) {  // change to player mode
                playerMode(false);
            } else if (checkedId == R.id.radio_button_ob) { // change to ob mode
                obMode(false);
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mObModeChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.radio_button_room_code) {  // change to use room code
                mObTaskIdMode = false;
                mRoomCodeInput.setHint("口令");
            } else if (checkedId == R.id.radio_button_task_id) {  // change to user taskid
                mObTaskIdMode = true;
                mRoomCodeInput.setHint("TaskId");
            }
        }
    };

    private void playerMode(boolean forceCheck) {
        mObMode = false;
        if (forceCheck) {
            mRoleGroup.check(R.id.radio_button_player);
        }
        toggleView(mObModeGroup, false);
        toggleView(mRoomCodeInput, false);
        toggleView(mGenRoomCodeGroup, true);
    }

    private void obMode(boolean forceCheck) {
        mObMode = true;
        if (forceCheck) {
            mRoleGroup.check(R.id.radio_button_ob);
        }
        toggleView(mObModeGroup, true);
        toggleView(mRoomCodeInput, true);
        toggleView(mGenRoomCodeGroup, false);
    }

    private void toggleView(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void genRoomCode(final View view) {
        Log.e("getRoomCode", "top");
        //RoomCode roomCode = new RoomCode();
        new RoomCode().getRoomCode(new RoomCode.Callback() {
            @Override
            public void onSuccess(String roomCode) {
                Log.e("genRoomCode", roomCode);
                mRoomCode = roomCode;
                mContext.runOnUiThread(() -> {
                    mRoomCodeTextView.setVisibility(View.VISIBLE);
                    mRoomCodeTextView.setText(mRoomCode);
                });
            }

            @Override
            public void onFail(String err) {
                Log.e("gegroomcode", err);
                toastInner("口令生成失败 " + err);
            }
        });
    }

    private void onEnterAppli(final View view) {
        if (mItem == null) {
            return;
        }

        String nickName = mNickNamInput.getText().toString();
        String obRoomCode = mRoomCodeInput.getText().toString();
        if (nickName.isEmpty()) {
            toastInner("请输入昵称");
            return;
        }

        EnterAppliInfo enterAppliInfo = new EnterAppliInfo(mCallback);
        enterAppliInfo.setNickName(nickName);
        enterAppliInfo.setFrameRate(mFrameRate);
        enterAppliInfo.setCodeRate(mCodeRate);
        // 互动模式
        enterAppliInfo.setPlayerMode(RtcClient.PLAYER_MODE_INTERACTIVE);
        if (mObMode) {
            enterAppliInfo.setUserType(RtcClient.USER_TYPE_OBSERVER);
            if (mObTaskIdMode) {
                enterAppliInfo.setTaskId(obRoomCode);
            } else {
                enterAppliInfo.setRoomCode(obRoomCode);
            }
        } else {
            enterAppliInfo.setUserType(RtcClient.USER_TYPE_PLAYER);
            enterAppliInfo.setRoomCode(mRoomCode);
        }
        enterAppliInfo.enterApp(mItem);
    }

    private void toastInner(int resId) {
        toastInner(mContext.getString(resId));
    }

    /**
     * 显示 toast
     */
    private void toastInner(final String msg) {
        mContext.runOnUiThread(() -> Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show());
    }
}
