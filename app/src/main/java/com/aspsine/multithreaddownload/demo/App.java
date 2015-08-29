package com.aspsine.multithreaddownload.demo;

import android.app.Application;
import android.content.Context;

import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.util.FileUtils;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class App extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        initDownloader();
    }

    private void initDownloader() {
        DownloadConfiguration configuration = new DownloadConfiguration();
        configuration.setDownloadDir(FileUtils.getDefaultDownloadDir(getApplicationContext()));
        configuration.setMaxThreadNum(10);
        DownloadManager.getInstance().init(getApplicationContext(), configuration);
    }

    public static Context getContext() {
        return sContext;
    }


}
