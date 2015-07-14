package com.aspsine.multithreaddownload.service;

import android.os.Handler;
import android.os.Message;

import com.aspsine.multithreaddownload.entity.DownloadInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    Map<String, DownloadTask> mDownloadTaskMap;

    private DownloadConfiguration mConfig;

    private ExecutorService mExecutorService;

    private static CallBackHandler handler;

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
        if (mConfig == null) {
            new RuntimeException("configuration can not be null!");
            return;
        }
        this.mConfig = configuration;
        mExecutorService = Executors.newFixedThreadPool(configuration.maxThreadNum);
    }

    private void addTask(String url, DownloadTask downloadTask) {
        mDownloadTaskMap.put(createTag(url), downloadTask);
    }

    private DownloadTask getTask(String url) {
        return mDownloadTaskMap.get(createTag(url));
    }

    public void download(DownloadInfo downloadInfo) {
        List<DownloadInfo> downloadInfos = new ArrayList<>();
        downloadInfos.add(downloadInfo);
        download(downloadInfos);
    }

    public void download(List<DownloadInfo> downloadInfos) {
        if (mConfig == null) {
            new RuntimeException("Please config first!");
            return;
        }

        for (DownloadInfo downloadInfo : downloadInfos) {
            addTask(downloadInfo.getUrl(), new DownloadTask(downloadInfo, mConfig.downloadDir, mExecutorService));
        }

        for (DownloadTask task : mDownloadTaskMap.values()) {
            task.start();
        }
    }

    private static String createTag(String url) {
        return String.valueOf(url.hashCode());
    }

    private static final class CallBackHandler extends Handler {
        Map<String, CallBack> mCallBackMap;

        public CallBackHandler(Map<String, CallBack> callBackMap) {
            this.mCallBackMap = callBackMap;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadTask task = (DownloadTask) msg.obj;
            CallBack callBack = mCallBackMap.get(task.getDownloadInfo().getUrl());
            switch (msg.what) {
                case Constants.WHAT.ON_PROGRESS:
                    callBack.onProgressUpdate(task.getFinished(), task.getDownloadInfo().getLength(), (task.getFinished() / task.getDownloadInfo().getLength()) / 100);
                    break;
                case Constants.WHAT.ON_COMPLETE:
                    callBack.onComplete();
                    break;
                case Constants.WHAT.ON_FAILURE:
                    //TODO
                    callBack.onFailure(new Exception());
                    break;
            }
        }


    }
}
