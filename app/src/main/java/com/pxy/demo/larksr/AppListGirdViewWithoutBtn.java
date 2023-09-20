package com.pxy.demo.larksr;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.pxy.larkcore.request.AppListItem;
import com.pxy.larkcore.request.PageInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListGirdViewWithoutBtn extends GridView {
    private static final String TAG = "AppListGirdView";
    int VerticalSpacin = 40;
    int columnts=2;

    public interface OnChangePageListener {
        void onChangePage(int pageNum);
    }

    private View mLoadingView;
    private View mEmptyListView;
    private View mFailedView;
    private AppListAdapter mAdapter;
    private PageInfo mPageInfo = null;

    private OnChangePageListener mListener = null;

    //private boolean mTVMode = BuildConfig.tvMode;

    public AppListGirdViewWithoutBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AppListGirdViewWithoutBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AppListGirdViewWithoutBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public AppListGirdViewWithoutBtn(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        mAdapter = new AppListAdapter(context, R.layout.layout_item, new ArrayList<>());
        setAdapter(mAdapter);
        setVerticalSpacing(VerticalSpacin);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取宽度的模式与具体大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //获取高度的模式与具体大小
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        /*if (!BuildConfig.tvMode) {
            setMeasuredDimension(widthSize, (int) (heightSize - Util.convertDpToPixel(50, getContext())));
        }*/
        setMeasuredDimension(widthSize, (int) (heightSize - com.pxy.demo.larksr.Util.convertDpToPixel(50, getContext())));

        columnts= (int) ((widthSize-VerticalSpacin)/ com.pxy.demo.larksr.Util.convertDpToPixel(150,getContext()));
    }


    public void setChangePageListener(OnChangePageListener eventListener) {
        mListener = eventListener;
    }

    public void clear() {
        mAdapter.clear();
    }

    public void getListFailed() {
        mLoadingView.setVisibility(View.GONE);
        mEmptyListView.setVisibility(View.GONE);
        mFailedView.setVisibility(View.VISIBLE);
        clear();
    }

    public void setListEmpty() {
        mLoadingView.setVisibility(View.GONE);
        mEmptyListView.setVisibility(View.VISIBLE);
        mFailedView.setVisibility(View.GONE);
        clear();
    }

    public void fresh(List<AppListItem> list) {
        mAdapter.fresh(list);
    }

    public AppListItem getItem(int position) {
        return mAdapter.getItem(position);
    }

    private boolean showPages() {
        return mPageInfo != null && mPageInfo.getPages() > 1;
    }

    @Override
    public void setEmptyView(View emptyView) {
        super.setEmptyView(emptyView);
        mLoadingView = emptyView.findViewById(R.id.view_empty_loading);
        mEmptyListView = emptyView.findViewById(R.id.view_empty_app_list_text);
        mFailedView = emptyView.findViewById(R.id.view_empty_app_list_failed_button);
    }

    static class ViewHolder {
        ImageView cover;
        ImageView useGamepadIcon;
        TextView title;
        TextView runCountText;
        String coverUrl = "";
    }

    class AppListAdapter extends BaseAdapter {
        private final List<AppListItem> mList;
        private final Context mContext;
        private final int mItemLayoutId;
        private final Object mLock = new Object();
        private boolean mNotifyDataSetChanged = true;
        private int mColumns = -1;

        AppListAdapter(Context context, int itemLayoutId, List<AppListItem> list) {
            super();
            mContext = context;
            mItemLayoutId = itemLayoutId;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public AppListItem getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppListItem item = getItem(position);
            assert item != null;
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);
                view.setFocusable(true);
                view.requestFocus();
                viewHolder = new ViewHolder();
                viewHolder.title = view.findViewById(R.id.item_title);
                viewHolder.runCountText = view.findViewById(R.id.runCount);
                viewHolder.cover = view.findViewById(R.id.app_cover);
                viewHolder.cover.setImageResource(R.mipmap.default_cover);
                viewHolder.useGamepadIcon = view.findViewById(R.id.icon_use_gamepad);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            if (item.isEmpty()) {
                view.setVisibility(View.INVISIBLE);
                return view;
            } else if (view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
            String url = item.getPicUrl();
            if (!url.isEmpty() && !url.equals(viewHolder.coverUrl)) {
                Glide.with(mContext)
                        .load(url)
                        .error(R.mipmap.default_cover)
                        .centerCrop()
                        .into(viewHolder.cover);
                viewHolder.coverUrl = url;
            }
            viewHolder.title.setText(item.getAppliName());
            viewHolder.runCountText.setText(item.getRunCnt() + "/" + item.getInstanceMax());
            viewHolder.useGamepadIcon.setVisibility(item.useGamepad() ? View.VISIBLE : View.INVISIBLE);
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

        public void fresh(List<AppListItem> list) {
            synchronized (mLock) {
                if (list.size() == 0) {
                    setListEmpty();
                }
                if (!mList.equals(list)) {
                    mList.clear();
                    mList.addAll(list);
                    addEmpty();
                    if (mNotifyDataSetChanged) {
                        notifyDataSetChanged();
                    }
                }
                com.pxy.demo.larksr.AppListLiner.ChangeVisiable changeVisiable = com.pxy.demo.larksr.AppListLiner.getChangeVisiable();
                changeVisiable.setvisiable(true);
                setNumColumns(columnts);
                setColumns(columnts);
            }
        }

        public void setColumns(int columns) {
            synchronized (mLock) {
                mColumns = columns;
                addEmpty();
                if (mNotifyDataSetChanged) {
                    notifyDataSetChanged();
                }
            }
        }

        public void setPageInfo() {
            synchronized (mLock) {
                addEmpty();
                if (mNotifyDataSetChanged) {
                    notifyDataSetChanged();
                }
            }
        }

        private void addEmpty() {
            // 电视盒子隐藏翻页
            /*if (mTVMode) {
                return;
            }*/
            if (mColumns == -1) {
                return;
            }
            if (showPages() && mList.size() == mPageInfo.getSize()) {
                int remain = mList.size() % mColumns;
                int count = remain == 0 ? 1 : mColumns - remain + 1;
                Log.d(TAG, "add empty to footer " + count + " " + mColumns + " " + mList.size());
                for (int i = 0; i < count; i++) {
                    mList.add(AppListItem.empty());
                }
            }
        }
    }
}
