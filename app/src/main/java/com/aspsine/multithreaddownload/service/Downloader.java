package com.aspsine.multithreaddownload.service;

import android.os.Handler;
import android.os.Looper;

import com.aspsine.multithreaddownload.entity.DownloadInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class Downloader {

    /**
     * singleton of Downloader
     */
    private static Downloader sDownloader;

    /**
     * map to store DownloadTask with a unique tag
     * key: Tag
     * value: DownloadTask
     */
    private Map<String, DownloadTask> mDownloadTaskMap;

    private DownloadConfiguration mConfig;

    private ExecutorService mExecutorService;

    private DownloadStatusDelivery mDelivery;

    public static Downloader getInstance() {
        if (sDownloader == null) {
            synchronized (Downloader.class) {
                sDownloader = new Downloader();
            }
        }
        return sDownloader;
    }

    /**
     * private construction
     */
    private Downloader() {
        mDownloadTaskMap = new LinkedHashMap<>();
    }

    public void init(DownloadConfiguration configuration) {
        if (configuration == null) {
            new RuntimeException("configuration can not be null!");
            return;
        }
        this.mConfig = configuration;
        mExecutorService = Executors.newFixedThreadPool(configuration.maxThreadNum);
        mDelivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));
    }

    private void addTask(String url, DownloadTask downloadTask) {
        mDownloadTaskMap.put(createTag(url), downloadTask);
    }

    private DownloadTask getTask(String url) {
        return mDownloadTaskMap.get(createTag(url));
    }

    public void download(DownloadInfo downloadInfo, CallBack callBack) {
        if (mConfig == null) {
            new RuntimeException("Please config first!");
            return;
        }
        final DownloadTask task = new DownloadTask(downloadInfo, mConfig.downloadDir, mExecutorService, new DownloadStatus(callBack), mDelivery);
        addTask(downloadInfo.getUrl(), task);
        task.start();
    }

    private static String createTag(String url) {
        return String.valueOf(url.hashCode());
    }

}
