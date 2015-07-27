package com.aspsine.multithreaddownload.demo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.core.DownloadException;
import com.aspsine.multithreaddownload.demo.R;


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
        findViewById(R.id.btnCancel).setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvFileName);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setMax(100);
    }

    int a;
    int b;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStart) {
            a = 0;
            b = 0;
            DownloadManager.getInstance().download("CIQuestionnaire_android.apk", DOWNLOAD_URL, null, this);
        } else if (v.getId() == R.id.btnPause) {
            DownloadManager.getInstance().pause(DOWNLOAD_URL);
        } else if (v.getId() == R.id.btnCancel) {
            DownloadManager.getInstance().cancel(DOWNLOAD_URL);
        }
    }

    @Override
    public void onDownloadStart() {

    }

    @Override
    public void onConnected(int total, boolean isRangeSupport) {
        b++;
        Log.i("MainActivity", "onConnected:" + total + " time:" + b);
    }

    @Override
    public void onProgress(int finished, int total, int progress) {
        pb.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    @Override
    public void onComplete() {
        a++;
        Log.i("MainActivity", "onComplete " + a);
    }

    @Override
    public void onDownloadPause() {
        Log.i("MainActivity", "onDownloadPause");
    }

    @Override
    public void onDownloadCancel() {
        Log.i("MainActivity", "onDownloadCancel");
    }

    @Override
    public void onFailure(DownloadException e) {
        Log.i("MainActivity", "onFailure");
        e.printStackTrace();
    }
}
