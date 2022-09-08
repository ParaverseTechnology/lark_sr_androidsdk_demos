package com.pxy.demo.vrmonitor;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.pxy.larkcore.CloudlarkManager;
import com.pxy.larkcore.request.Base;
import com.pxy.larkcore.request.Bean.CredentialBean;
import com.pxy.larkcore.request.DoLogin;
import com.pxy.larkcore.request.GetCaptcha;
import com.pxy.larkcore.request.GetClientSearch;
import com.pxy.larkcore.request.GetCredential;
import com.pxy.larkcore.request.PageInfo;
import com.pxy.larkcore.request.ScheduleTaskManager;
import com.pxy.demo.vrmonitor.Adapter.ClientListAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // 本地配置字段toastInner
    private static final String SETTING = "pxy_setting";
    private static final String SETTING_SERVER = "serverAddress";
    private static final String SETTING_SERVER_USE_HTTPS = "useHttps";
    private String mServerIp = "";

    // 码率 kbps
    private String[] mCodeRateSource;
    // 默认码率 kbps
    private int mCodeRate = 4000;
    // 帧率
    private String[] mFrameRateSource;
    // 默认帧率
    private int mFrameRate = 60;
    private boolean mStartAppLoop = true;
    // 获取应用列表
    private GetClientSearch getClientSearch;
    // 定时任务循环
    private ScheduleTaskManager mScheduleTaskManager;
    //验证码ID
    private String captchaIdstr="";
    //数据bean
    private CredentialBean credentialBean;
    private CredentialBean.RecordsBean credentialData;

    private RecyclerView recyclerView;
    ClientListAdapter clientListAdapter;
    Button confirm,confirmLogin,showLogin,showIp,closeip,closelogin,scanQr;

    private EditText inputIp,inputPort,inputUsername,inputPassword,inputCaptcha;
    private LinearLayout mIpSetting,mLogin;
    private ImageView captcha;
    private ListView chooseCre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SDKinitialization();
        findViewByid();
        initview();
        init();
    }

    private void findViewByid(){
        recyclerView=findViewById(R.id.recycler);

        confirm=findViewById(R.id.confirm);
        confirmLogin=findViewById(R.id.confirmLogin);
        inputIp=findViewById(R.id.inputIp);
        inputPort=findViewById(R.id.inputPort);
        mIpSetting =findViewById(R.id.IpSetting);

        captcha=findViewById(R.id.captcha);
        mLogin=findViewById(R.id.mLogin);
        inputUsername=findViewById(R.id.inputUserName);
        inputPassword=findViewById(R.id.inputPassword);
        inputCaptcha=findViewById(R.id.inputCaptcha);
        chooseCre=findViewById(R.id.chooseCre);

        showLogin=findViewById(R.id.showLogin);
        showIp=findViewById(R.id.showIp);
        scanQr=findViewById(R.id.scanQr);

        closeip=findViewById(R.id.closeip);
        closelogin=findViewById(R.id.closelogin);
    }

    private void initview() {
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：480px）
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：800p）
        int spancount=screenWidth>screenHeight?4:2;
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,spancount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.requestFocus();
            }
        });
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
              /*  RecyclerView.canScrollVertically(1); // false表示已经滚动到底部
                RecyclerView.canScrollVertically(-1); // false表示已经滚动到顶部*/
                if (newState==RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.canScrollVertically(-1)) {
                        //mark
                        Log.e("bottom", getClientSearch.getmPage() + "");
                        if(getClientSearch.getmPageInfo().hasNextPage()) {
                            getClientSearch.setmPage(getClientSearch.getmPage() + 1);
                        }
                    } else if (recyclerView.canScrollVertically(1)) {
                        Log.e("top", getClientSearch.getmPage() + "");
                        if (getClientSearch.getmPageInfo().hasPreviousPage()) {
                            getClientSearch.setmPage(getClientSearch.getmPage() - 1);
                        }
                    }
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipstess=inputIp.getText().toString();
                if (ipstess.isEmpty()){
                    toastInner("IP不能为空");
                    return;
                }
                mServerIp="http://"+inputIp.getText().toString()+":"+inputPort.getText().toString();
                SharedPreferences sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SETTING_SERVER, mServerIp);
                editor.putBoolean(SETTING_SERVER_USE_HTTPS, false);
                editor.apply();
                Base.setServerAddr(false, mServerIp);
                showLogin();
            }
        });
        captcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });
        confirmLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoLogin();
            }
        });
        chooseCre.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                credentialData=credentialBean.getRecords().get(position);
                //mark
                recyclerView.setAdapter(null);
                clientListAdapter=null;
                closeChooseCre();
                getClientSearch();
            }
        });
        showLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLogin.getVisibility()==View.VISIBLE){
                    closeLogin();
                }else {
                    stopAppInfoLoop();
                    showLogin();
                }
            }
        });
        showIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIpSetting.getVisibility()==View.VISIBLE){
                    mIpSetting.setVisibility(View.GONE);
                }else {
                    stopAppInfoLoop();
                    showIpSetting();
                }

            }
        });
        closelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogin.setVisibility(View.GONE);
            }
        });
        closeip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIpSetting.setVisibility(View.GONE);
            }
        });
        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
    }

    private void DoLogin(){
        new DoLogin(new DoLogin.Callback() {
            @Override
            public void onSuccess(com.pxy.larkcore.request.Bean.LoginBean loginBean) {
                String token=loginBean.getToken();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLogin.setVisibility(View.GONE);
                    }
                });
                getCredential(token);
                showChooseCre();
            }

            @Override
            public void onFail(String onFail) {
                Log.e("onFail",onFail);
                toastInner(onFail);
                showLogin();
            }
        }).dorequest(
                inputUsername.getText().toString(),
                inputPassword.getText().toString(),
           /*             "admin",
                        "123456",*/
                inputCaptcha.getText().toString(),
                captchaIdstr
        );
    }

    private void getCredential(String token){
        new GetCredential(new GetCredential.Callback() {
            @Override
            public void onSuccess(CredentialBean credentialBean) {
                MainActivity.this.credentialBean=credentialBean;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chooseCre.setAdapter(new MListAdapter(credentialBean));
                    }
                });
            }

            @Override
            public void onFail(String err) {
                Log.e("getCredentialFail",err);
                toastInner(err);
            }
        }).doget(token);
    }

    private void showChooseCre(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chooseCre.setVisibility(View.VISIBLE);
            }
        });

    }
    private void closeChooseCre(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chooseCre.setVisibility(View.GONE);
            }
        });
        SharedPreferences sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String data=JSON.toJSONString(credentialData);
        editor.putString("credentialData",data);
        editor.apply();
    }

    private void showLogin(){
        mIpSetting.setVisibility(View.GONE);
        if (mServerIp == null || mServerIp.isEmpty()) {
            toastInner("Ip地址为空");
            showIpSetting();
            return;
        }
        GetCaptcha getCaptcha=new GetCaptcha(new GetCaptcha.Callback() {
            @Override
            public void onSuccess(String res,String captchaId) {
                Log.e("captres",res);
                Log.e("captchaId",captchaId);
                captchaIdstr=captchaId;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RequestOptions options=new RequestOptions()
                                .skipMemoryCache(true) // 不使用内存缓存
                                .diskCacheStrategy(DiskCacheStrategy.NONE); // 不使用磁盘缓存
                        Glide.with(MainActivity.this)
                                .applyDefaultRequestOptions(options)
                                .load(res)
                                .into(captcha);
                    }
                });
            }

            @Override
            public void onSuccess(byte[] b, String captchaId) {
                captchaIdstr=captchaId;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RequestOptions options=new RequestOptions()
                                .skipMemoryCache(true) // 不使用内存缓存
                                .diskCacheStrategy(DiskCacheStrategy.NONE); // 不使用磁盘缓存
                        Glide.with(MainActivity.this)
                                .applyDefaultRequestOptions(options)
                                .load(b)
                                .into(captcha);
                    }
                });
            }

            @Override
            public void onFail(String err) {
                Log.e("captfail",err);
                toastInner(err);
                showIpSetting();
            }
        });
        getCaptcha.getcaptcha("");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogin.setVisibility(View.VISIBLE);
                mLogin.requestFocus();
                inputCaptcha.setText("");
                inputUsername.setText("");
                inputPassword.setText("");
            }
        });
    }

    private void closeLogin(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogin.setVisibility(View.GONE);
            }
        });
        getClientSearch();
    }

    private void showIpSetting(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIpSetting.setVisibility(View.VISIBLE);
                mIpSetting.requestFocus();
                mLogin.setVisibility(View.GONE);
            }
        });
    }

    private void SDKinitialization() {
        Log.e(TAG, "SDKinitialization");
        CloudlarkManager.init(this, CloudlarkManager.APP_TYPE_VR_MONITOR);
        String sdkId = "您的SDK ID. 如果没有请联系商务获取。";

        CloudlarkManager.initSdkAuthorization(this, sdkId);
        Log.d(TAG, "lark sdk auth success");

        CloudlarkManager.setLoadingBgBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.bg_dark));
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

    /**
     *  初始化设置
     */
    private void init() {
        Log.e(TAG, "init");
        mScheduleTaskManager = new ScheduleTaskManager(ScheduleTaskManager.SCHEDULE_TIME_SECOND_MS * 2);
        mCodeRateSource = getResources().getStringArray(R.array.codeRate);
        setCodeRate(2);

        SharedPreferences sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        mServerIp = sp.getString(SETTING_SERVER, "");
        boolean useHttps = sp.getBoolean(SETTING_SERVER_USE_HTTPS, false);

        Log.d(TAG, "cached server address use https " + useHttps + " serverIp " + mServerIp);

        if (mServerIp == null || mServerIp.isEmpty()) {
            Log.d(TAG, "unset serverAddress");
            showIpSetting();
        } else {
            String outhttp=mServerIp.substring(7);
            Log.e("outhttp",outhttp);
            inputIp.setText(outhttp.split(":")[0]);
            inputPort.setText(outhttp.split(":")[1]);
            Base.setServerAddr(useHttps, mServerIp);
            //startAppInfoLoop();
            if (sp.getString("credentialData","")!=null && !sp.getString("credentialData","").isEmpty()){
                credentialData=JSON.parseObject(sp.getString("credentialData",""),CredentialBean.RecordsBean.class);
            }else {
                Log.e("credentialData","credentialDataIsNull");
                showLogin();
            }
            getClientSearch();
        }
        //Base.setServerAddr(false, mServerIp);

    }

    /*
     *  设置码率
     *  @param position
     * */
    private void setCodeRate(int position) {
        mCodeRate = Integer.parseInt(mCodeRateSource[position]);
    }

    private void getClientSearch(){
        if (mServerIp == null || mServerIp.isEmpty()) {
            Log.d(TAG, "unset serverAddress");
            showIpSetting();
            return;
        }
        if (credentialData!=null){
            if (getClientSearch==null) {
                getClientSearch = new GetClientSearch(new GetClientSearch.Callback() {
                    @Override
                    public void onSuccess(List<com.pxy.larkcore.request.Bean.ClientListItem> list) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void run() {
                                if (clientListAdapter == null) {
                                    clientListAdapter = new ClientListAdapter(MainActivity.this, list);
                                    clientListAdapter.setHasStableIds(true);
                                    recyclerView.setAdapter(clientListAdapter);
                                } else {
                                    if (!clientListAdapter.getClientListItems().equals(list)) {
                                        clientListAdapter.setClientListItems(list);
                                        clientListAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });

                    }

                    @Override
                    public void onPageInfoChange(PageInfo pageInfo) {

                    }

                    @Override
                    public void onFail(String s) {
                        //stopAppInfoLoop();
                        Log.e("onFail", s + mServerIp);
                        toastInner(s);
                        if (s.equals("密钥对无效")) {
                            showLogin();
                        } else {
                            showIpSetting();
                        }
                        stopAppInfoLoop();
                    }
                });
            }
            mScheduleTaskManager.clearTask();
            mScheduleTaskManager.addTask(() -> {
                getClientSearch.dorequest(credentialData.getAdminKey(),credentialData.getAdminSecret());
            });
            startAppInfoLoop();
        }else {
            toastInner("登录状态失效，请重新登录");
            showLogin();
            return;
        }
    }


    class MListAdapter extends BaseAdapter {
        CredentialBean credentialBean;
        public MListAdapter(CredentialBean credentialBean) {
            this.credentialBean=credentialBean;
        }

        @Override
        public int getCount() {
            return credentialBean.getRecords().size();
        }

        @Override
        public CredentialBean.RecordsBean getItem(int position) {
            return credentialBean.getRecords().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView textView=new TextView(MainActivity.this);
            textView.setText(getItem(position).getName()+"：appKey:"+getItem(position).getAppKey());
            textView.setTextSize(30);
            return textView;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        getClientSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAppInfoLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}