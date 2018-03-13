package com.roy.downloadapktest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by roy on 2018/3/13.
 */

public class DialogFragmentHelper extends DialogFragment {
    private static final String TAG = DialogFragmentHelper.class.getSimpleName();
    private ProgressBar mProgressBar;
    private TextView mTextViewProgressBar;
    private Button mButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);  //關閉Dialog title
        getDialog().setCanceledOnTouchOutside(false);   //不可點擊dialog以外區域
        View view = inflater.inflate(R.layout.download_apk_dialog, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.download_progressBar);
        mTextViewProgressBar = (TextView) view.findViewById(R.id.textView_progressBar);
        mButton = (Button) view.findViewById(R.id.complete_button);

        mTextViewProgressBar.setText("0%");
        mButton.setVisibility(View.INVISIBLE);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    protected void setProgress(int progress) {  //更新進度條
        mProgressBar.setProgress(progress);
        mTextViewProgressBar.setText(Integer.toString(progress) + "%");
        if (progress == 100) {
            Log.d(TAG, "setProgress: " + progress);
            mButton.setVisibility(View.VISIBLE);
        }
    }
}
