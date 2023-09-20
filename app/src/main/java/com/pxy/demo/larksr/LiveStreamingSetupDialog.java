package com.pxy.demo.larksr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputLayout;

public class LiveStreamingSetupDialog {
    public interface LiveStreamingSetupDialogCallback {
        void OnStartLiveStreaming(LiveStreamingSetupDialog dialog);
        void OnStopLiveStreaming();
    }

    private Context mContext;
    private AlertDialog mDialog;

    private AppCompatEditText mLiveStreamingPath;
    private AppCompatEditText mLiveStreamingKey;

    private Spinner mCodeRate;
    private Spinner mFps;
    private Spinner mResolution;
    private Button mSubmit;
    private Button mCancle;
    private Button mStop;

    private String[] FRAME_RATES;
    private String[] CODE_RATES;
    private String[] RESOLUTION_WIDTHS;
    private String[] RESOLUTION_HEIGHTS;

    public int getFrameRateValue() {
        return mFrameRateValue;
    }

    public int getCodeRateValue() {
        return mCodeRateValue;
    }

    public int getWidthValue() {
        return mWidthValue;
    }

    public int getHeightValue() {
        return mHeightValue;
    }

    public String getPath() {
        return mPath;
    }

    public String getKey() {
        return mKey;
    }

    private String mPath = "";
    private String mKey = "";
    private int mFrameRateValue = 0;
    private int mCodeRateValue = 0;
    private int mWidthValue = 0;
    private int mHeightValue = 0;

    private LiveStreamingSetupDialogCallback mCallback;

    public LiveStreamingSetupDialog(Context context, LiveStreamingSetupDialogCallback callback) {
        mContext = context;
        mCallback = callback;

        setupView();
    }

    public void Show() {
        mDialog.show();
    }

    public void Hide() {
        mDialog.hide();
    }

    private void setupView() {
        FRAME_RATES = mContext.getResources().getStringArray(R.array.livestreamingFrameRate);
        CODE_RATES = mContext.getResources().getStringArray(R.array.livestreamingCodeRateValue);
        RESOLUTION_WIDTHS = mContext.getResources().getStringArray(R.array.livestreamingResValueWidth);
        RESOLUTION_HEIGHTS = mContext.getResources().getStringArray(R.array.livestreamingResValueHeight);

        mFrameRateValue = Integer.parseInt(FRAME_RATES[0]);
        mCodeRateValue = Integer.parseInt(CODE_RATES[0]);
        mWidthValue = Integer.parseInt(RESOLUTION_WIDTHS[0]);
        mHeightValue = Integer.parseInt(RESOLUTION_HEIGHTS[0]);

        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(mContext);

        final View dialogView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_setup_livestreaming, null);

        customizeDialog.setView(dialogView);
        mDialog = customizeDialog.create();

        mLiveStreamingPath = dialogView.findViewById(R.id.input_text_live_path);
        mLiveStreamingKey = dialogView.findViewById(R.id.input_text_live_key);

        mCodeRate = dialogView.findViewById(R.id.spinner_setup_live_coderate);
        mFps = dialogView.findViewById(R.id.spinner_setup_live_fps);
        mResolution = dialogView.findViewById(R.id.spinner_setup_live_res);

        mSubmit = dialogView.findViewById(R.id.button_setup_live_submit);
        mCancle = dialogView.findViewById(R.id.button_setup_live_cancle);
        mStop = dialogView.findViewById(R.id.button_setup_live_stop);

        mCodeRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mCodeRateValue = Integer.parseInt(CODE_RATES[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mFps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mFrameRateValue = Integer.parseInt(FRAME_RATES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mWidthValue = Integer.parseInt(RESOLUTION_WIDTHS[position]);
                mHeightValue = Integer.parseInt(RESOLUTION_HEIGHTS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPath = mLiveStreamingPath.getText().toString();
                mKey = mLiveStreamingKey.getText().toString();
                mCallback.OnStartLiveStreaming(LiveStreamingSetupDialog.this);
                mDialog.hide();
            }
        });

        mCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.hide();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.OnStopLiveStreaming();
                mDialog.hide();
            }
        });
    }
}
