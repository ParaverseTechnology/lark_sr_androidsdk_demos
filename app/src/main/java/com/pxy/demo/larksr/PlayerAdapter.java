package com.pxy.demo.larksr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pxy.lib_sr.input.AppNotification.PlayerDesc;

import java.util.List;

public class PlayerAdapter extends BaseAdapter {
    private static final String TAG = "PlayerAdapter";
    private final List<PlayerDesc> mList;
    private final Context mContext;
    private final int mLayoutId;
    private final Object mLock = new Object();
    private boolean mNotifyDataSetChanged = true;

    static class ViewHolder {
        TextView id;
        TextView nickName;
        TextView authority;
    }

    PlayerAdapter(Context context, int layoutId, List<PlayerDesc> list) {
        super();
        mContext = context;
        mLayoutId = layoutId;
        mList = list;
    }

    PlayerDesc findUser(int uid) {
        for (int i = 0; i < mList.size(); i ++) {
             if (uid == mList.get(i).getId()) {
                 return mList.get(i);
             }
        }
        return null;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PlayerDesc getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PlayerDesc item = getItem(position);
        Log.d(TAG, "player desc " + item);
        assert item != null;
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
            view.setFocusable(false);
            viewHolder = new ViewHolder();
            viewHolder.id = view.findViewById(R.id.player_id);
            viewHolder.nickName = view.findViewById(R.id.player_nickname);
            viewHolder.authority = view.findViewById(R.id.player_authority);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.id.setText(String.valueOf(item.getId()));
        viewHolder.nickName.setText(item.getNickName());
        String auth = " ";
        if (item.isTaskOwner()) {
            auth += " 房主 ";
        }
        if (item.isController()) {
            auth += " 操作者 ";
        } else {
            auth += " 观看者";
        }
        viewHolder.authority.setText(auth);
        return view;
    }

    public void clear() {
        synchronized (mLock) {
            mList.clear();
            if (mNotifyDataSetChanged) {
                notifyDataSetChanged();
            }
        }
    }

    public void fresh(List<PlayerDesc> list) {
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
}
