package com.roy.downloadapktest;

import android.Manifest;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button button;

    private DownloadObserver mDownloadObserver;
    private DownloadManager mDownloadManager;
    private DownloadManager.Request mRequest;
    private long mDownloadIdFinal;
    private DialogFragmentHelper mNewFragment;

    private static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    public static final String gardenDownloadURL = "http://dev-api.nuwarobotics.com/v1/miboTheater/resource/10000224/10000224.apk?token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vZGV2LWFwaS5udXdhcm9ib3RpY3MuY29tIiwic3ViIjoiYXV0aC5kZXYubnV3YXJvYm90aWNzfDE3MjA4Mjk2MzY5MCIsImF1ZCI6IjlEMjcyNDY0LTAxMTQtNDQxQS05NEI4LTQ0OEE4NTQ1NkU3NSIsImlhdCI6MTUyMDgzNzc4MSwiZXhwIjoxNTI4NjEzNzgxLCJqdGkiOiIxOTU3ZTAyZS1mNTU2LTQ1NWYtOTcxMi1lMGM2NDY0NzY1MWEiLCJjb250ZXh0Ijp7InR5cGUiOiJhY2Nlc3MiLCJwcm92aWRlciI6InhpYW9taXwxNjI4MzA4NzQyIn0sInNjb3BlIjoicHJvZmlsZSBraXdpIGF1dGggYXV0aF9kZXZpY2UgYXV0aF9yZWZyZXNoLmdldCBraXdpZWNfY29uc3VtZXIga2l3aWVjX2NvbnN1bWVyLnNob3BwaW5nIG90YS5nZXQifQ.Hq2JYHz0m-hRT5nvGiZ9jwYVCxlfbq_pGCeldLO1j31wp97Gfz-7Q8A3Yc2ObQTwShcq4UKffUU8dc06cRD3yN91PX1nMIl7VeTsUgw_TKD27MW4_FbNauAAIs6tEZlEMiiL91yUea-EipuUjboNRi_fElSbpPySNHjZsGer947MQrCfx1T-VJsSk5Tp5ZHG5fJ2-_Zl_WEzrpYVc0Ssz9F8eB4gDojx3wJmzHdip6DpyPCb0th3LClYa13Hi5WOLe5sUUSTQfJd7g7FphB4JWS95gBkG7V4B2YlvDzH1WB3gc7q4wlKFrnuDYKpcCizMKEAaA0znf8YX_CnEUrGTQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadNewVersion();
            }
        });
    }

    private void DownloadNewVersion() {
        mNewFragment = new DialogFragmentHelper();
        mNewFragment.show(getSupportFragmentManager(), "download apk");
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(gardenDownloadURL);
        mRequest = new DownloadManager.Request(uri);
        mRequest.setMimeType("application/vnd.android.package-archive");    //設置MIME為Android APK檔
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //Android 6.0以上需要判斷使用者是否願意開啟權限
            CheckStoragePermission();
        } else {
            DownloadManagerEnqueue();
        }
    }

    private void DownloadManagerEnqueue() {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();     //建立目錄
        mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "garden.apk");  //設定APK儲存位置
        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver(getApplicationContext());
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));     //註冊DOWNLOAD_COMPLETE-BroadcastReceiver
        mDownloadObserver = new DownloadObserver(null);
        getContentResolver().registerContentObserver(CONTENT_URI, true, mDownloadObserver); //註冊ContentObserver
        mDownloadIdFinal = mDownloadManager.enqueue(mRequest);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(getApplicationContext());
        sharedPreferencesHelper.setDownloadId(mDownloadIdFinal);
    }

    private void CheckStoragePermission() {     //Android 6.0檢查是否開啟權限, 詢問視窗
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 20);
        } else {
            DownloadManagerEnqueue();
        }
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 20: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DownloadManagerEnqueue();
                } else {
                    CheckStoragePermission();
                }
                return;
            }
        }
    }

    class DownloadObserver extends ContentObserver {
        public DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mDownloadIdFinal);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            final Cursor cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                final int totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                final int currentColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalSize = cursor.getInt(totalColumn);
                int currentSize = cursor.getInt(currentColumn);
                float percent = (float) currentSize / (float) totalSize;
                final int progress = Math.round(percent * 100);
                runOnUiThread(new Runnable() {  //在UI Thread執行
                    @Override
                    public void run() {
                        mNewFragment.setProgress(progress);
                    }
                });
            }
        }
    }
}
