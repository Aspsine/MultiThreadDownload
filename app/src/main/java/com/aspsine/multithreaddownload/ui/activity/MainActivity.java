package com.aspsine.multithreaddownload.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.R;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.service.CallBack;
import com.aspsine.multithreaddownload.service.DownloadConfiguration;
import com.aspsine.multithreaddownload.service.DownloadService;
import com.aspsine.multithreaddownload.service.DownloadTask;
import com.aspsine.multithreaddownload.service.Downloader;
import com.aspsine.multithreaddownload.util.FileUtils;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, CallBack {
    public static final String DOWNLOAD_URL = "http://apps.wandoujia.com/apps/com.youku.phone/download";
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
        DownloadConfiguration configuration = new DownloadConfiguration(this);
        configuration.setDownloadDir(FileUtils.getDownloadDir(this));
        configuration.setMaxThreadNum(10);
        Downloader.getInstance().init(configuration);
    }

    @Override
    public void onClick(View v) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setUrl(DOWNLOAD_URL);
        downloadInfo.setName("youku.apk");
        if (v.getId() == R.id.btnStart) {
            Downloader.getInstance().download(downloadInfo, this);
        } else if (v.getId() == R.id.btnPause) {
        }
    }

    @Override
    public void onFinishInit(int total) {
        Log.i("MainActivity", "onFinishInit=" + total);
    }

    @Override
    public void onProgressUpdate(int finished, int total) {
        try {
            pb.setProgress((finished * 100 / total));
            tvProgress.setText((finished * 100 / total) + "%");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("MainActivity", "onProgressUpdate:" + "finished = " + finished + "; total=" + total);
    }

    @Override
    public void onComplete() {
        Log.i("MainActivity", "onComplete");
    }

    @Override
    public void onFailure(Exception e) {
        Log.i("MainActivity", "onFailure");
        e.printStackTrace();
    }
}
