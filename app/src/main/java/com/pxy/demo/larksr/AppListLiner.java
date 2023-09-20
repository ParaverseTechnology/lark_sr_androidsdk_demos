package com.pxy.demo.larksr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pxy.larkcore.request.PageInfo;

public class AppListLiner extends LinearLayout {
    private String TAG = "AppListLiner";
    private Context context;
    private FullWidthFixedViewLayout mPageView;
    private AppListGirdViewWithoutBtn appListGirdViewWithoutBtn;
    private PageComponentHolder mPageComponentHolder;
    private PageInfo mPageInfo = null;
    public static ChangeVisiable changeVisiable;

    public static ChangeVisiable getChangeVisiable() {
        return changeVisiable;
    }

    public void setChangeVisiable(ChangeVisiable changeVisiable) {
        this.changeVisiable = changeVisiable;
    }

    public AppListGirdViewWithoutBtn getAppListGirdViewWithoutBtn() {
        return appListGirdViewWithoutBtn;
    }

    private AppListGirdView.OnChangePageListener mListener = null;

    public void setChangePageListener(AppListGirdView.OnChangePageListener eventListener) {
        mListener = eventListener;
    }

    public void setAppListGirdViewWithoutBtn(AppListGirdViewWithoutBtn appListGirdViewWithoutBtn) {
        this.appListGirdViewWithoutBtn = appListGirdViewWithoutBtn;
    }

    public void setPageInfo(PageInfo pageInfo) {
        mPageInfo = pageInfo;
        AppListGirdViewWithoutBtn.AppListAdapter adapter = (AppListGirdViewWithoutBtn.AppListAdapter) appListGirdViewWithoutBtn.getAdapter();
        adapter.setPageInfo();

        if (mPageView != null) {
            mPageView.updatePageInfo();
        }
        if (mPageComponentHolder != null) {
            mPageComponentHolder.updatePageInfo();
        }
    }

    public void setPageComponentHolder(ImageButton preButton, ImageButton nextButton, TextView currentNum) {
        mPageComponentHolder = new PageComponentHolder(preButton, nextButton, currentNum);
    }

    public AppListLiner(Context context) {
        super(context);
        this.context = context;
        initview();
    }

    public AppListLiner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initview();
    }

    public AppListLiner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initview();
    }

    public interface ChangeVisiable{
        void setvisiable(boolean f);
    }

    private void initview() {
        setOrientation(VERTICAL);
        appListGirdViewWithoutBtn = new AppListGirdViewWithoutBtn(context);
        addView(appListGirdViewWithoutBtn);

        changeVisiable= f -> {
            if (f
                    && mPageView==null
                   // && !BuildConfig.tvMode
            ){
                    mPageView = new FullWidthFixedViewLayout(context);
                    addView(mPageView);
            }
        };
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
        }

        public void updatePageInfo() {
            mHolder.updatePageInfo();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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

}
