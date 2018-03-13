package com.roy.downloadapktest;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by roy on 2018/3/13.
 */

public class SharedPreferencesHelper {
    private static final String SP_Name = "OpenApk_SP";
    public final String mDownloadId = "DownloadId";
    public SharedPreferences mSettings;
    public SharedPreferences.Editor mPreEdit;

    public SharedPreferencesHelper(Context context) {
        mSettings = context.getSharedPreferences(SP_Name, 0);
        mPreEdit = mSettings.edit();
    }

    public void setDownloadId(long id) {
        mPreEdit.putLong(mDownloadId, id);
        mPreEdit.commit();
    }

    public long getDownloadId() {
        return mSettings.getLong(mDownloadId, -1);
    }
}
