package com.aspsine.multithreaddownload;

import android.app.Application;
import android.content.Context;

import com.aspsine.multithreaddownload.db.ThreadInfoRepository;
import com.aspsine.multithreaddownload.db.ThreadInfoRepositoryImpl;
import com.aspsine.multithreaddownload.service.DownloadConfiguration;
import com.aspsine.multithreaddownload.service.Downloader;
import com.aspsine.multithreaddownload.util.FileUtils;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class App extends Application {

    private static ThreadInfoRepository sThreadInfoRepository;

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        CrashHandler.getInstance(getApplicationContext());
        initDownloader();
    }

    private void initDownloader(){
        DownloadConfiguration configuration = new DownloadConfiguration(this);
        configuration.setDownloadDir(FileUtils.getDownloadDir(this));
        configuration.setMaxThreadNum(10);
        Downloader.getInstance().init(configuration);
    }

    public static ThreadInfoRepository getThreadInfoRepository() {
        if (sThreadInfoRepository == null) {
            synchronized (App.class) {
                sThreadInfoRepository = new ThreadInfoRepositoryImpl(sContext);
            }
        }
        return sThreadInfoRepository;
    }
}
