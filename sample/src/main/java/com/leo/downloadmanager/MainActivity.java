package com.leo.downloadmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.leo.download.DownloadError;
import com.leo.download.DownloadListener;
import com.leo.download.DownloadListenerAdapter;
import com.leo.download.DownloadManager;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "download";
    private static final String URL_1 = "http://www.xzhiliao.com/c";
    private static final String URL_2 = "http://www.xzhiliao.com/m";

    @BindView(R.id.btn_normal)
    Button mBtnNormal;
    @BindView(R.id.pg_normal)
    ProgressBar mPgNormal;
    @BindView(R.id.btn_toggle)
    Button mBtnToggle;
    @BindView(R.id.btn_cancel)
    Button mBtnCancel;
    @BindView(R.id.pg_break)
    ProgressBar mPgBreak;
    @BindView(R.id.content)
    LinearLayout mContent;

    private DownloadManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mManager = DownloadManager.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_record){
            startActivity(new Intent(this, DownloadRecordsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_normal)
    public void startNormal(View v) {
        mManager.start(URL_1, getExternalFilesDir("file") + File.separator + "xc.apk", new DownloadListenerAdapter() {

            @Override
            public void onProgress(int id, long currSize, long totalSize) {
                Log.d(TAG, currSize + "/" + totalSize);
                mPgNormal.setProgress((int) (currSize * 100 / totalSize));
            }

            @Override
            public void onComplete(int id, String dir, String name) {
                showCompleteSnackbar(dir, name);
            }
        });
    }

    @OnClick(R.id.btn_toggle)
    public void toggleDownload(View v) {
        mManager.enquene(URL_2, getExternalFilesDir("file") + File.separator + "xm.apk", new DownloadListener() {
            @Override
            public void onStart(int id, long size) {
                mBtnToggle.setText("暂停");
            }

            @Override
            public void onProgress(int id, long currSize, long totalSize) {
                Log.d(TAG, currSize + "/" + totalSize);
                mPgBreak.setProgress((int) (currSize * 100 / totalSize));
            }

            @Override
            public void onRestart(int id, long currSize, long totalSize) {
                mBtnToggle.setText("暂停");
            }

            @Override
            public void onPause(int id, long currSize) {
                mBtnToggle.setText("开始");
            }

            @Override
            public void onComplete(int id, final String dir, final String name) {
                Log.d(TAG, dir);
                Log.d(TAG, name);
                showCompleteSnackbar(dir, name);
            }

            @Override
            public void onCancel(int id) {

            }

            @Override
            public void onError(int id, DownloadError error) {
                Snackbar.make(mContent, String.format(Locale.getDefault(), "下载失败(%1$d,%2$s)", error.getCode(), error.getMessage()), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    @OnClick(R.id.btn_cancel)
    public void cancel(View v){
        mManager.cancel(URL_2, new DownloadListenerAdapter() {
            @Override
            public void onCancel(int id) {
                mBtnToggle.setText("开始");
                mPgBreak.setProgress(0);
            }
        });
    }

    private void showCompleteSnackbar(final String dir, final String name){
        Snackbar.make(mContent, String.format("文件已保存至%s", dir + File.separator + name), Snackbar.LENGTH_LONG)
                .setAction("打开", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FileNameMap fileNameMap = URLConnection.getFileNameMap();
                        String contentTypeFor = fileNameMap.getContentTypeFor(name);
                        if (contentTypeFor == null) {
                            contentTypeFor = "application/octet-stream";
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(dir, name)), contentTypeFor);
                        startActivity(intent);
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.cancel(URL_1, null);
        mManager.pause(URL_2, null);
    }
}
