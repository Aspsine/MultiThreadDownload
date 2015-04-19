package com.aspsine.multithreaddownload.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aspsine.multithreaddownload.R;
import com.aspsine.multithreaddownload.entity.FileInfo;
import com.aspsine.multithreaddownload.service.DownloadService;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String DOWNLOAD_URL = "http://124.205.69.171/files/A2270000000477CE/dl.ctxy.cn/Public/Download/62/yxt.apk";
    TextView tvName;
    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnPause).setOnClickListener(this);
        findViewById(R.id.btnStart).setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvFileName);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
    }

    @Override
    public void onClick(View v) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUrl(DOWNLOAD_URL);
        Intent intent = new Intent(this, DownloadService.class);
        if (v.getId() == R.id.btnStart) {
            intent.setAction(DownloadService.ACTION_START);
            intent.putExtra(DownloadService.EXTRA_FILE_INFO, fileInfo);
        } else if (v.getId() == R.id.btnPause) {
            intent.setAction(DownloadService.ACTION_PAUSE);
        }
        startService(intent);
    }
}
