package com.pxy.demo.larksr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pxy.larkcore.CloudlarkManager;
import com.pxy.larkcore.request.Base;

public class SetupDialog {
    public interface SetupDialogCallback {
        void onUpdateAppKey(String appKey, String appSecret);
        void onUpdateServerSetting(boolean useHttps, String addr);
    }
    private Context mContext;
    private AlertDialog mDialog;
    private SetupDialogCallback mCallback;
    private TextInputLayout mServerIpTextInput;
    private TextInputLayout mAppKeyEdit;
    private TextInputLayout mAppSecretEdit;
    SetupDialog(Context context, SetupDialogCallback callback) {
        mContext = context;
        mCallback = callback;
        setupView();
    }

    public void show(String serverIp) {
        mServerIpTextInput.getEditText().setText(serverIp);
        // 读取 app secret/appkey
        if (CloudlarkManager.get().useAppSecret()) {
            String appKeyStr = CloudlarkManager.get().getAppKey();
            String appSecret = CloudlarkManager.get().getAppSecret();
            mAppKeyEdit.getEditText().setText(appKeyStr);
            mAppSecretEdit.getEditText().setText(appSecret);
        } else if (CloudlarkManager.get().useAppkey()){
            String appKeyStr = CloudlarkManager.get().getAppKey();
            mAppKeyEdit.getEditText().setText(appKeyStr);
            mAppSecretEdit.getEditText().setText("");
        }
        mDialog.show();
    }

    private void setupView() {
        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(mContext);
        final View dialogView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_setup, null);
        final CheckBox useHttpsButton = dialogView.findViewById(R.id.setup_checkbox_use_https);
        useHttpsButton.setChecked(Base.getServerUrl().useSecurityProtocol());
        final TextInputLayout textInput = dialogView.findViewById(R.id.text_input);
        // advance setup.
        mAppKeyEdit = dialogView.findViewById(R.id.text_input_app_key);
        mAppSecretEdit = dialogView.findViewById(R.id.text_input_app_secret);

        Button advanceButton = dialogView.findViewById(R.id.button_setup_advance);
        advanceButton.setOnClickListener(view -> {
            if (mAppKeyEdit.getVisibility() == View.VISIBLE) {
                mAppKeyEdit.setVisibility(View.GONE);
            } else {
                mAppKeyEdit.setVisibility(View.VISIBLE);
            }
            if (mAppSecretEdit.getVisibility() == View.VISIBLE) {
                mAppSecretEdit.setVisibility(View.GONE);
            } else {
                mAppSecretEdit.setVisibility(View.VISIBLE);
            }
        });
        // 读取 app secret/appkey
        if (CloudlarkManager.get().useAppSecret()) {
            String appKeyStr = CloudlarkManager.get().getAppKey();
            String appSecret = CloudlarkManager.get().getAppSecret();
            mAppKeyEdit.getEditText().setText(appKeyStr);
            mAppSecretEdit.getEditText().setText(appSecret);
        } else if (CloudlarkManager.get().useAppkey()){
            String appKeyStr = CloudlarkManager.get().getAppKey();
            mAppKeyEdit.getEditText().setText(appKeyStr);
            mAppSecretEdit.getEditText().setText("");
        }
        mServerIpTextInput = textInput;

        customizeDialog.setTitle(R.string.ui_setup_title);
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton(R.string.ui_setup_summit,
                (dialog, which) -> {
                    // hide by defalult
                    mAppKeyEdit.setVisibility(View.GONE);
                    mAppSecretEdit.setVisibility(View.GONE);

                    if (mCallback != null) {
                        // 更新appkey 设置。
                        mCallback.onUpdateAppKey(mAppKeyEdit.getEditText().getText().toString(),
                                mAppSecretEdit.getEditText().getText().toString());
                        // 更新服务器地址。
                        mCallback.onUpdateServerSetting(useHttpsButton.isChecked(),
                                textInput.getEditText().getText().toString());
                    }
                });

        mDialog = customizeDialog.create();
    }
}
