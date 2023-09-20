package com.pxy.demo.larksr;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pxy.lib_sr.input.AppNotification;

import java.util.ArrayList;
import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.ViewHolder> {

    private static final String TAG = "AiChatAdapter";

    private List<AiChatMsg> mList = new ArrayList<>();

    private final Object mLock = new Object();
    private boolean mNotifyDataSetChanged = true;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rihgtMsg;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rihgtMsg = (TextView) view.findViewById(R.id.right_msg);
        }
    }

    public AiChatAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_msg_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder " + position);
        AiChatMsg msg = mList.get(position);
        if (msg.getType() == AiChatMsg.TYPE_RECEIVED_AI) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if (msg.getType() == AiChatMsg.TYPE_SENT_USER) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rihgtMsg.setText(msg.getContent());
        }
    }


    public int end() {
        return mList.isEmpty() ? 0 : mList.size() - 1;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void clear() {
        synchronized (mLock) {
            mList.clear();
            if (mNotifyDataSetChanged) {
                notifyDataSetChanged();
            }
        }
    }

    public void fresh(List<AiChatMsg> list) {
        synchronized (mLock) {
            if (!mList.equals(list)) {
                mList.clear();
                mList.addAll(list);
                if (mNotifyDataSetChanged) {
                    notifyDataSetChanged();
                }
            }
        }
    }

    public void add(AiChatMsg msg) {
        synchronized (mLock) {
            mList.add(msg);
            if (mNotifyDataSetChanged) {
                notifyItemInserted(mList.size() - 1);
            }
        }
    }

    public void add(String content,int type)  {
        add(new AiChatMsg(content, type));
    }

    public void addUserSend(String content) {
        add(new AiChatMsg(content, AiChatMsg.TYPE_SENT_USER));
    }

    public void addAiReceive(String content) {
        add(new AiChatMsg(content, AiChatMsg.TYPE_RECEIVED_AI));
    }
}
