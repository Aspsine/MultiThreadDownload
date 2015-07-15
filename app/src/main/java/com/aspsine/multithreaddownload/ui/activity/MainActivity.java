package com.aspsine.multithreaddownload.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.R;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.service.DownloadService;
import com.aspsine.multithreaddownload.service.DownloadTask;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{
    public static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/Aspsine/Daily/master/art/daily.apk";
    TextView tvName;
    TextView tvProgress;
    ProgressBar pb;
    DownloadProgressReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnPause).setOnClickListener(this);
        findViewById(R.id.btnStart).setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvFileName);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        pb = (ProgressBar)findViewById(R.id.progressBar);
        pb.setMax(100);

        IntentFilter intentFilter = new IntentFilter(DownloadService.ACTION_UPDATE);
        if (mReceiver == null){
            mReceiver = new DownloadProgressReceiver();
            mReceiver.setProgressCallBacks(this);
        }
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setUrl(DOWNLOAD_URL);
        downloadInfo.setName("daily.apk");
        Intent intent = new Intent(this, DownloadService.class);
        if (v.getId() == R.id.btnStart) {
            intent.setAction(DownloadService.ACTION_START);
            intent.putExtra(DownloadService.EXTRA_DOWNLOAD_INFO, downloadInfo);
        } else if (v.getId() == R.id.btnPause) {
            intent.setAction(DownloadService.ACTION_PAUSE);
            intent.putExtra(DownloadService.EXTRA_DOWNLOAD_INFO, downloadInfo);
        }
        startService(intent);
    }

    @Override
    public void onProgress(int progress) {
        pb.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    private static class DownloadProgressReceiver extends BroadcastReceiver{
        DownloadTask.ProgressCallBacks mCallBacks;

        void setProgressCallBacks(DownloadTask.ProgressCallBacks callBacks){
            this.mCallBacks = callBacks;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            int finished = intent.getIntExtra(DownloadService.EXTRA_FINISHED, 0);
            mCallBacks.onProgress(finished);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
