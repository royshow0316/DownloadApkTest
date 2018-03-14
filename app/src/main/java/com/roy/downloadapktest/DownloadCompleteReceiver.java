package com.roy.downloadapktest;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by roy on 2018/3/13.
 */

public class DownloadCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();
    private static SharedPreferencesHelper mSharedPreferencesHelper;
    private Context mContext;

    public DownloadCompleteReceiver(Context context) {
        this.mContext = context;
        mSharedPreferencesHelper = new SharedPreferencesHelper(mContext);
    }

    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        long downLoadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        long cacheDownLoadId = mSharedPreferencesHelper.getDownloadId();
        if (cacheDownLoadId == downLoadId) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            File apkFile = queryDownloadedApk(context);
//            File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + queryFile.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //Android 7.0 需要透過FileProvider來取得APK檔的Uri
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
                Log.d(TAG, "onReceive: " + contentUri);
                install.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(install);
        }
    }

    //透過downLoadId尋找下載的apk檔, 解決6.0以上版本安裝的問題
    public static File queryDownloadedApk(Context context) {
        File targetApkFile = null;
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId = mSharedPreferencesHelper.getDownloadId();
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cursor = downloadManager.query(query);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cursor.close();
            }
        }
        return targetApkFile;
    }
}
