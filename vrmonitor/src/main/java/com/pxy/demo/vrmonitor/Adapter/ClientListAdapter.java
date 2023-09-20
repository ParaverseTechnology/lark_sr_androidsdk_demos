package com.pxy.demo.vrmonitor.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pxy.larkcore.request.Base;
import com.pxy.larkcore.request.Bean.ClientListItem;
import com.pxy.larkcore.request.EnterAppliInfo;
import com.pxy.larkcore.request.GetTask;
import com.pxy.lib_sr.RtcClient;
import com.pxy.lib_sr.input.AppNotification;
import com.pxy.lib_sr.render.RtcRender;
import com.pxy.demo.vrmonitor.Activity.RtcActivity;
import com.pxy.demo.vrmonitor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolder> {
    private Context context;
    private Map<String,RtcClient> rtcmap=new HashMap<>();
    public List<ClientListItem> getClientListItems() {
        return clientListItems;
    }

    public void setClientListItems(List<ClientListItem> clientListItems) {
        this.clientListItems = clientListItems;
    }

    List<ClientListItem> clientListItems;
    public ClientListAdapter(Context context, List<ClientListItem> clientListItems){
        this.context=context;
        this.clientListItems=clientListItems;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.client_item,viewGroup,false);
        ViewHolder holder =new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ClientListItem data=clientListItems.get(i);
        if (!data.equals(viewHolder.pic.getTag())){
            RequestOptions options=new RequestOptions()
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            Glide.with(context)
                    .applyDefaultRequestOptions(options)
                    .load(
                            data.getStatus().equals("1")?
                                    R.mipmap.inline:
                                    R.mipmap.outofline
                    ).into(viewHolder.pic);
            viewHolder.pic.setTag(data);
        }
        if (!data.getTaskId().equals("0")){
            ifMedia(viewHolder,true);
            viewHolder.item.requestFocus();
            if (!rtcmap.containsKey(i+"")){
                GetTask getTask = new GetTask(new GetTask.Callback() {
                    @Override
                    public void onSuccess(String res) {
                        viewHolder.setParam(res);
                        RtcClient rtcClient=new RtcClient(getparam(res), viewHolder.rtcRender,
                                new RtcClient.RtcClientEvent() {
                                    @Override
                                    public void onConnect() {
                                        Log.d(i+"--viewholder","onConnect");
                                    }

                                    @Override
                                    public void onLoginSuccess(int i) {
                                        Log.d(i+"--viewholder","onLoginSuccess--"+i);
                                    }

                                    @Override
                                    public void onMediaReady() {
                                        Log.d(i+"--viewholder","onMediaReady");
                                    }

                                    @Override
                                    public void onFrameResolutionChanged(int i, int i1, int i2) {

                                    }

                                    @Override
                                    public void onDisconnect() {
                                        if (rtcmap.containsKey(i+"")) {
                                            rtcmap.get(i+"").release();
                                        }
                                    }

                                    @Override
                                    public void onNoOpreationTimeout() {
                                        Log.d(i+"--viewholder","onNoOpreationTimeout");
                                    }

                                    @Override
                                    public void onInfo(String s) {
                                        Log.d(i+"--viewholder","onInfo--"+s);
                                    }

                                    @Override
                                    public void onError(String s) {
                                        Log.d(i+"--viewholder","onError--"+s);
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
                                        Log.d(i+"--viewholder","onDataChannelOpen");
                                    }

                                    @Override
                                    public void onDataChannelClose() {
                                        Log.d(i+"--viewholder","onDataChannelClose");
                                    }

                                    @Override
                                    public void onDataChannelMessage(String s) {
                                        Log.d(i+"--viewholder","onDataChannelMessage--"+s);
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
                                },
                                (Activity) context);
                        rtcClient.connect();
                        rtcmap.put(i+"",rtcClient);
                    }

                    @Override
                    public void onFail(String err) {

                    }
                });
                getTask.dorequest(data.getTaskId());
            }
        }else {
            ifMedia(viewHolder,false);
        }
        viewHolder.mac.setText("客户端ID："+data.getClientId());
        viewHolder.ip.setText(data.getClientName());
        viewHolder.item.setOnClickListener(v -> {
            if (viewHolder.getParam()!=null) {
                Intent intent = new Intent(context, RtcActivity.class);
                intent.putExtra("param", getparam(viewHolder.getParam()));
                if (rtcmap.containsKey(i+"")) {
                    rtcmap.get(i+"").release();
                    rtcmap.remove(i+"");
                    viewHolder.rtcRender.release();
                }
                context.startActivity(intent);
            }
        });
    }

    private void ifMedia(ViewHolder viewHolder,Boolean flag){
        if (flag){
            viewHolder.pic.setVisibility(View.GONE);
            viewHolder.rtcRender.setVisibility(View.VISIBLE);
        }else {
            viewHolder.pic.setVisibility(View.VISIBLE);
            viewHolder.rtcRender.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return clientListItems.size();
        //return 3;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.rtcRender.release();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        String res=holder.getParam();
        if (null!=res && !res.isEmpty()) {
            int i = holder.getPosition();
            RtcClient rtcClient = new RtcClient(getparam(res), holder.rtcRender,
                    new RtcClient.RtcClientEvent() {
                        @Override
                        public void onConnect() {
                            Log.d(i + "--viewholder", "onConnect");
                        }

                        @Override
                        public void onLoginSuccess(int i) {
                            Log.d(i + "--viewholder", "onLoginSuccess--" + i);
                        }

                        @Override
                        public void onMediaReady() {
                            Log.d(i + "--viewholder", "onMediaReady");
                        }

                        @Override
                        public void onFrameResolutionChanged(int i, int i1, int i2) {

                        }

                        @Override
                        public void onDisconnect() {
                            if (rtcmap.containsKey(i + "")) {
                                rtcmap.get(i + "").release();
                            }
                        }

                        @Override
                        public void onNoOpreationTimeout() {
                            Log.d(i + "--viewholder", "onNoOpreationTimeout");
                        }

                        @Override
                        public void onInfo(String s) {
                            Log.d(i + "--viewholder", "onInfo--" + s);
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(i + "--viewholder", "onError--" + s);
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
                            Log.d(i + "--viewholder", "onDataChannelOpen");
                        }

                        @Override
                        public void onDataChannelClose() {
                            Log.d(i + "--viewholder", "onDataChannelClose");
                        }

                        @Override
                        public void onDataChannelMessage(String s) {
                            Log.d(i + "--viewholder", "onDataChannelMessage--" + s);
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
                    },
                    (Activity) context);
            rtcClient.connect();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mac,ip;
        ImageView pic;
        LinearLayout item;
        RtcRender rtcRender;

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        String param;
        public ViewHolder (View view)
        {
            super(view);
            pic=view.findViewById(R.id.pic);
            mac=view.findViewById(R.id.mac);
            ip=view.findViewById(R.id.ip);
            item=view.findViewById(R.id.item);
            rtcRender=view.findViewById(R.id.render);
        }
    }

    private EnterAppliInfo.Config getparam(String result){
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(result);

            String appServer = jsonObject.getString("serverIp");
            //int appPort = jsonObject.optInt("renderServerPort");
            int appPort = 10002;
            String appliId = jsonObject.getString("appliId");
            String taskId = jsonObject.getString("taskId");
            int initWinSize = jsonObject.optInt("initWinSize");
            String preferPubOutIp = jsonObject.optString("preferPubOutIp");
            //int wsProxy = Integer.parseInt(jsonObject.optString("wsProxy"));
            String roomCode = jsonObject.optString("roomCode", "");
            String bgColor = jsonObject.optString("bgColor", "000");
            boolean useGamepad = jsonObject.optInt("useGamepad", 0) == 1;
            String touchScreenMode = jsonObject.optString("touchOperateMode", "mouse");
            EnterAppliInfo.Config params = new EnterAppliInfo.Config();
            params.appServer = appServer;
            params.appPort = appPort;
            params.taskId = taskId;
            params.preferPubOutIp = preferPubOutIp;
            //params.noOperationTimeout = noOperationTimeout;
            //params.wsProxy = wsProxy;
            params.wsProxy = 1;
            params.roomCode = "null".equals(roomCode) ? "" : roomCode;
            // add "#" to begin.
            params.bgColor = bgColor.isEmpty() ? "" : "#" + bgColor;;
            // 设置服务器地址
            HttpUrl serverUrl = Base.getServerUrl().getUrl();
            params.webServerIp = serverUrl.host();
            params.webServerPort = serverUrl.port();
            params.useSecurityProtocol = Base.getServerUrl().useSecurityProtocol();
            params.useGamepad = useGamepad;
            params.appilId = appliId;
            params.touchOperateMode = touchScreenMode;

            return params;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
