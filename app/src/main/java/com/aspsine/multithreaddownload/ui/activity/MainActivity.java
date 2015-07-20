package com.aspsine.multithreaddownload.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.R;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.service.CallBack;
import com.aspsine.multithreaddownload.service.DownloadManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CallBack {
    public static final String DOWNLOAD_URL = "http://js.soufunimg.com/industry/csis/app/CIQuestionnaire_android_-2000.apk";
    TextView tvName;
    TextView tvProgress;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnPause).setOnClickListener(this);
        findViewById(R.id.btnStart).setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvFileName);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setMax(100);
    }

    @Override
    public void onClick(View v) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setUrl(DOWNLOAD_URL);
        downloadInfo.setName("CIQuestionnaire_android.apk");
        if (v.getId() == R.id.btnStart) {
            DownloadManager.getInstance().download(downloadInfo, this);
        } else if (v.getId() == R.id.btnPause) {

        }
    }

    @Override
    public void onConnected(int total) {

    }

    @Override
    public void onProgress(int finished, int total, int progress) {
        pb.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    @Override
    public void onComplete() {
        Log.i("MainActivity", "onComplete");
    }

    @Override
    public void onDownloadPause() {

    }

    @Override
    public void onDownloadCancel() {

    }

    @Override
    public void onFailure(Exception e) {
        Log.i("MainActivity", "onFailure");
        e.printStackTrace();
    }
}
