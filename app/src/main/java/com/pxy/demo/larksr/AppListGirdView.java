package com.pxy.demo.larksr;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.pxy.larkcore.request.AppListItem;
import com.pxy.larkcore.request.PageInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListGirdView extends GridView {
    private static final String TAG = "AppListGirdView";

    public interface OnChangePageListener {
        void onChangePage(int pageNum);
    }

    private View mLoadingView;
    private View mEmptyListView;
    private View mFailedView;
    private AppListAdapter mAdapter;
    private PageInfo mPageInfo = null;

    public FullWidthFixedViewLayout getmPageView() {
        return mPageView;
    }

    public void setmPageView(FullWidthFixedViewLayout mPageView) {
        this.mPageView = mPageView;
    }

    private FullWidthFixedViewLayout mPageView = null;
    private PageComponentHolder mPageComponentHolder;
    private OnChangePageListener mListener = null;
    private boolean mTVMode = BuildConfig.tvMode;

    public AppListGirdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AppListGirdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AppListGirdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public AppListGirdView(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        mAdapter = new AppListAdapter(context, R.layout.layout_item, new ArrayList<>());
        int numColumns = getNumColumns();
        if (numColumns > 1) {
            mAdapter.setColumns(numColumns);
        }
        setAdapter(mAdapter);
        // 电视盒子隐藏翻页
        if (!mTVMode) {
            mPageView = new FullWidthFixedViewLayout(context);
        }
    }

    public void setPageComponentHolder(ImageButton preButton, ImageButton nextButton, TextView currentNum) {
        mPageComponentHolder = new PageComponentHolder(preButton, nextButton, currentNum);
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

    public void setPageInfo(PageInfo pageInfo) {
        mPageInfo = pageInfo;
        mAdapter.setPageInfo();
        if (mPageView != null) {
            mPageView.updatePageInfo();
        }
        if (mPageComponentHolder != null) {
            mPageComponentHolder.updatePageInfo();
        }
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure " + getNumColumns() + " " + getColumnWidth() + " " + getRequestedColumnWidth());
        mAdapter.setColumns(getNumColumns());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int bottom = AppListGirdView.this.getMeasuredHeight();
        if (bottom - ev.getY() < 120) {
            mPageView.onTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    class FullWidthFixedViewLayout extends FrameLayout {
        private PageComponentHolder mHolder;

        public FullWidthFixedViewLayout(Context context) {
            super(context);
            View containerView = LayoutInflater.from(context).inflate(R.layout.layout_applist_page_footer, this, true);
            mHolder = new PageComponentHolder(
                    containerView.findViewById(R.id.button_pre_page),
                    containerView.findViewById(R.id.button_next_page),
                    containerView.findViewById(R.id.textView_current_page)
            );
            mHolder.mPrePageButton.setOnClickListener((View view) -> {
                Log.d(TAG, "on pre page");
                if (mListener != null && mPageInfo.hasPreviousPage()) {
                    mListener.onChangePage(mPageInfo.getPrePage());
                }
            });
            mHolder.mNextPageButton.setOnClickListener((View view) -> {
                Log.d(TAG, "on next page");
                if (mListener != null && mPageInfo.hasNextPage()) {
                    mListener.onChangePage(mPageInfo.getNextPage());
                }
            });
        }

        public void updatePageInfo() {
            mHolder.updatePageInfo();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int targetWidth = AppListGirdView.this.getMeasuredWidth()
                    - AppListGirdView.this.getPaddingLeft()
                    - AppListGirdView.this.getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth,
                    MeasureSpec.getMode(widthMeasureSpec));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // Log.e("footdispatch", super.dispatchTouchEvent(ev) + "");
            return super.dispatchTouchEvent(ev);
        }
    }

    public class PageComponentHolder {
        private final ImageButton mPrePageButton;
        private final ImageButton mNextPageButton;
        private final TextView mCurrentPageNum;

        PageComponentHolder(ImageButton prePageButton, ImageButton nextPageButton, TextView currentPageNum) {
            mPrePageButton = prePageButton;
            if (mPrePageButton != null) {
                mPrePageButton.setOnClickListener((View view) -> {
                    Log.d(TAG, "on pre page");
                    if (mListener != null && mPageInfo.hasPreviousPage()) {
                        mListener.onChangePage(mPageInfo.getPrePage());
                    }
                });
            }

            mNextPageButton = nextPageButton;
            if (mNextPageButton != null) {
                mNextPageButton.setOnClickListener((View view) -> {
                    Log.d(TAG, "on next page");
                    if (mListener != null && mPageInfo.hasNextPage()) {
                        mListener.onChangePage(mPageInfo.getNextPage());
                    }
                });
            }

            mCurrentPageNum = currentPageNum;
        }

        public void updatePageInfo() {
            if (mPrePageButton != null) {
                mPrePageButton.setEnabled(mPageInfo.hasPreviousPage());
            }
            if (mNextPageButton != null) {
                mNextPageButton.setEnabled(mPageInfo.hasNextPage());
            }
            if (mCurrentPageNum != null) {
                mCurrentPageNum.setText(String.valueOf(mPageInfo.getPageNum()));
            }
        }
    }

    static class ViewHolder {
        ImageView cover;
        ImageView useGamepadIcon;
        TextView title;
        TextView runCountText;
        String coverUrl = "";
    }

    static class PagesHolder {
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
            // page dots
            if (mPageView != null && showPages() && position == mList.size() - 1) {
                if (convertView == null
                        || !(convertView.getTag() instanceof PagesHolder)
                ) {
                    PagesHolder pagesHolder = new PagesHolder();
                    mPageView.setTag(pagesHolder);
                    mPageView.bringToFront();
                    return mPageView;
                }
                return convertView;
            }

            final AppListItem item = getItem(position);
            assert item != null;
            View view;
            ViewHolder viewHolder;
            if (convertView == null || convertView.getTag() instanceof PagesHolder) {
                view = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);
                view.setFocusable(false);
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
            if (mTVMode) {
                return;
            }
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
