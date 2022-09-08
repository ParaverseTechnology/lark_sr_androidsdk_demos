package com.pxy.demo.larksr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pxy.larkcore.CloudlarkManager;
import com.pxy.larkcore.request.AppListItem;
import com.pxy.larkcore.request.Base;
import com.pxy.larkcore.request.EnterAppliInfo;
import com.pxy.larkcore.request.GetAppliList;
import com.pxy.larkcore.request.PageInfo;
import com.pxy.larkcore.request.ScheduleTaskManager;
import com.pxy.lib_sr.RtcClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // 本地配置字段toastInner
    private static final String SETTING = "pxy_setting";
    private static final String SETTING_SERVER = "serverAddress";
    private static final String SETTING_SERVER_USE_HTTPS = "useHttps";

    private AppListGirdView mAppListGirdView;
    private boolean mStartAppLoop = true;
    private String mServerIp = "";
    // 码率选择器
    private Spinner mCodeRateSpinner = null;
    // 分辨率选择器
    private Spinner mFrameRateSpinner = null;
    // 码率 kbps
    private String[] mCodeRateSource;
    // 默认码率 kbps
    private int mCodeRate = 4000;
    // 帧率
    private String[] mFrameRateSource;
    // 默认帧率
    private int mFrameRate = 60;
    //
    private InterActiveDialog mInterActiveDialog;
    // 获取应用列表
    private GetAppliList mGetAppliList;
    // 定时任务循环
    private ScheduleTaskManager mScheduleTaskManager;
    //
    private boolean mUseInterActiveMode = false;

    private com.pxy.demo.larksr.SetupDialog mSetupDialog;

    private boolean mTVMode = BuildConfig.tvMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "====================onCreate");
        // int views
        mCodeRateSpinner = findViewById(R.id.spinner_code_rate);
        mFrameRateSpinner = findViewById(R.id.spinner_frame_rate);
        mCodeRateSource = getResources().getStringArray(R.array.codeRate);
        mFrameRateSource = getResources().getStringArray(R.array.frameRate);

        // 是否使用一人操作多人看
        Switch userInterActiveMode = findViewById(R.id.switch_player_mode);
        userInterActiveMode.setChecked(mUseInterActiveMode);
        userInterActiveMode.setOnCheckedChangeListener((view, checked) -> {
            mUseInterActiveMode = checked;
        });

        // 应用列表
        mAppListGirdView = findViewById(R.id.gird_view_app_list);
        View emptyView = findViewById(R.id.view_empty_app_list);
        mAppListGirdView.setEmptyView(emptyView);
        mAppListGirdView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mAppListGirdView.setOnItemClickListener(new ItemClickListener());
        mAppListGirdView.setChangePageListener(pageNum -> {
            if (mGetAppliList != null) {
                mGetAppliList.setPage(pageNum);
                mGetAppliList.getAppliList();
            }
        });

        mGetAppliList = new GetAppliList(new GetAppListCallback());
        // 电视盒子分页处理
        if (mTVMode) {
            mGetAppliList.setPageSize(18);
            mAppListGirdView.setPageComponentHolder(findViewById(R.id.button_pre_page),
                    findViewById(R.id.button_next_page),
                    findViewById(R.id.textView_current_page));
        }

        mScheduleTaskManager = new ScheduleTaskManager(ScheduleTaskManager.SCHEDULE_TIME_SECOND_MS * 2);

        mScheduleTaskManager.addTask(() -> {
            mGetAppliList.getAppliListSync();
        });

        mInterActiveDialog = new InterActiveDialog(this, new EnterAppliInfoCallback());
        mSetupDialog = new com.pxy.demo.larksr.SetupDialog(this, new SetupDialogCallback());

        CloudlarkManager.init(this, CloudlarkManager.APP_TYPE_SR, BuildConfig.tvMode);
        String sdkId = "您的SDK授权码";
        CloudlarkManager.initSdkAuthorization(this, sdkId);

//        Log.d(TAG, "native sdk id " + sdkId);
        CloudlarkManager.setLoadingBgBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.bg_dark));

        // 初始化
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAppInfoLoop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startAppInfoLoop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScheduleTaskManager.release();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                runOnUiThread(() -> {
                    AlertDialog.Builder customizeDialog =
                            new AlertDialog.Builder(MainActivity.this);
                    customizeDialog.setTitle("确定退出？");
                    customizeDialog.setMessage("选择确定退出");
                    customizeDialog.setCancelable(true);
                    customizeDialog.setPositiveButton("确定", (dialog, which) -> {
                        finish();
                    });
                    customizeDialog.setNegativeButton("取消", (dialog, which) -> {
                    });
                    customizeDialog.show();
                });
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_START || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                runOnUiThread(this::showSetup);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void onStartFetchAppInfo(View view) {
        if (!mStartAppLoop) {
            startAppInfoLoop();
        }
    }

    /**
     * 初始化设置。检测是否设置过服务器 ip。 未设置过弹出设置。
     */
    private void init() {
        SharedPreferences sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        mServerIp = sp.getString(SETTING_SERVER, "");
        boolean useHttps = sp.getBoolean(SETTING_SERVER_USE_HTTPS, false);
        Log.d(TAG, "cached server address use https " + useHttps + " serverIp " + mServerIp);

        if (mCodeRateSpinner != null) {
            // set default code rate. positon
            if (mCodeRateSource != null) {
                setCodeRate(1);
            }
            mCodeRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "select code rate:" + position);
                    setCodeRate(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "didnt select code rate");
                }
            });
        }
        if (mFrameRateSpinner != null) {
            // set default frame rate position
            setFrameRate(1);
            mFrameRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "frame rate:" + position);
                    setFrameRate(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "didnt select frame rate");
                }
            });
        }
        if (mServerIp == null || mServerIp.isEmpty()) {
            Log.d(TAG, "unset serverAddress");
            showSetup();
        } else {
            Base.setServerAddr(useHttps, mServerIp);
            startAppInfoLoop();
        }
    }

    /*
     *  设置码率
     *  @param position
     * */
    private void setCodeRate(int position) {
        if (mCodeRateSource != null &&
                mCodeRateSpinner != null && mCodeRateSource.length > position) {
            mCodeRateSpinner.setSelection(position);
            mCodeRate = Integer.parseInt(mCodeRateSource[position]);
        }
    }

    /**
     * 设置帧率
     *
     * @param position 帧率id
     */
    private void setFrameRate(int position) {
        if (mFrameRateSource != null && mFrameRateSource.length > position) {
            mFrameRateSpinner.setSelection(position);
            mFrameRate = Integer.parseInt(mFrameRateSource[position]);
        }
    }

    class GetAppListCallback implements GetAppliList.Callback {
        @Override
        public void onSuccess(List<AppListItem> items) {
            runOnUiThread(() -> {
                mAppListGirdView.fresh(items);
            });
        }

        @Override
        public void onPageInfoChange(PageInfo pageInfo) {
            Log.v(TAG, "on page info change " + pageInfo.getPageNum());
            runOnUiThread(() -> {
                mAppListGirdView.setPageInfo(pageInfo);
            });
        }

        @Override
        public void onFail(String err) {
            Log.w(TAG, "get start app info failed");
            runOnUiThread(() -> {
                mAppListGirdView.getListFailed();
            });
            stopAppInfoLoop();
            toastInner(R.string.msg_get_appli_info_failed);
        }
    }

    class EnterAppliInfoCallback implements EnterAppliInfo.Callback {
        @Override
        public void onSuccess(EnterAppliInfo.Config rtcParams) {
            if (mUseInterActiveMode) {
                mInterActiveDialog.close();
            }
            Log.e("rtcParams",rtcParams.toString());
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(MainActivity.this, RtcActivity.class);
            intent.setComponent(componentName);
            // 进入课程页面。
//            rtcParams.regionId=regionid;
            intent.putExtra(RtcClient.Config.name, rtcParams);
            MainActivity.this.startActivity(intent);
        }

        @Override
        public void onFail(String err) {
            if (mUseInterActiveMode) {
                mInterActiveDialog.close();
            }
            toastInner(err);
        }
    }

    class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AppListItem item = mAppListGirdView.getItem(position);
            Log.d(TAG, "onItemClick " + item.getAppliId() + " " + position);
            if (item == null || item.isEmpty()) {
                return;
            }

            if (mServerIp.isEmpty()) {
                toastInner(R.string.msg_setup_serveraddr_request);
                return;
            }

            if (mUseInterActiveMode) {
                mInterActiveDialog.enter(mFrameRate, mCodeRate, item);
//                enterAppliInfo.setPlayerMode(RtcClient.PLAYER_MODE_INTERACTIVE);
//                enterAppliInfo.setUserType(RtcClient.USER_TYPE_PLAYER);
            } else {
                EnterAppliInfo enterAppliInfo = new EnterAppliInfo(new EnterAppliInfoCallback());
                enterAppliInfo.setFrameRate(mFrameRate);
                enterAppliInfo.setCodeRate(mCodeRate);
//                enterAppliInfo.setRegionId(regionid);
                enterAppliInfo.enterApp(item);
            }
        }
    }

    /**
     * 开始查询 app 状态循环
     */
    private void startAppInfoLoop() {
        runOnUiThread(() -> {
            mStartAppLoop = true;
            mScheduleTaskManager.startTask();
        });
    }

    /**
     * 停止查询 app 状态循环
     */
    private void stopAppInfoLoop() {
        runOnUiThread(() -> {
            mStartAppLoop = false;
            mScheduleTaskManager.stopTask();
        });
    }

    /**
     * 按钮回调函数。显示设置。
     */
    public void onSetupButton(View view) {
        showSetup();
    }

    /**
     * 显示设置。
     */
    private void showSetup() {
        mSetupDialog.show(mServerIp);
    }

    class SetupDialogCallback implements com.pxy.demo.larksr.SetupDialog.SetupDialogCallback {
        @Override
        public void onUpdateAppKey(String appKey, String appSecret) {
            Log.d(TAG, "updateAppKey appKey:" + appKey + ";appSecret:" + appSecret);
            CloudlarkManager.init(MainActivity.this, CloudlarkManager.APP_TYPE_SR, appKey, appSecret);
        }

        @Override
        public void onUpdateServerSetting(boolean useHttps, String addr) {
            if (addr.isEmpty()) {
                toastInner(R.string.msg_setup_serveraddr_empty);
            } else {
                toastInner(getString(R.string.msg_setup_serveraddr, addr));
                // update addr
                mServerIp = addr;
                // 设置是否使用 https。
                // 如果服务器地址后续 url 设置中带有http协议，将覆盖单独设置。
                Base.setServerAddr(useHttps, mServerIp);
                // update list
                mAppListGirdView.clear();
                mGetAppliList.setPage(0);
                stopAppInfoLoop();
                startAppInfoLoop();
                // user setting
                SharedPreferences sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SETTING_SERVER, addr);
                editor.putBoolean(SETTING_SERVER_USE_HTTPS, useHttps);
                editor.apply();
            }
        }
    }

    private void toastInner(int resId) {
        toastInner(getString(resId));
    }

    /**
     * 显示 toast
     */
    private void toastInner(final String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
