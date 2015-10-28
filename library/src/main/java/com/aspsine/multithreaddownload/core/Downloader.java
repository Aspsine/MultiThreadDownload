package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.CallBack;

import java.util.List;

/**
 * Created by Aspsine on 2015/10/28.
 */
public class Downloader {

    private DownloadRequest mRequest;

    private List<DownloadTask> mDownloadTasks;
    private boolean running;

    public Downloader(DownloadRequest request) {
        mRequest = request;
    }

    public void start(CallBack callBack) {

    }

    public void pause() {

    }

    public void cancel() {

    }

    public boolean isRunning() {
        return running;
    }
}
