package com.aspsine.multithreaddownload.service;

import android.os.Handler;
import android.os.Looper;

import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadManager {

    /**
     * singleton of DownloadManager
     */
    private static DownloadManager sDownloadManager;

    private static DataBaseManager mDBManager;

    /**
     * key: Tag
     * value:DownloadRequest
     */
    private Map<String, DownloadRequest> mDownloadRequestMap;

    private DownloadConfiguration mConfig;

    private ExecutorService mExecutorService;

    private DownloadStatusDelivery mDelivery;

    public static DownloadManager getInstance() {
        if (sDownloadManager == null) {
            synchronized (DownloadManager.class) {
                sDownloadManager = new DownloadManager();
            }
        }
        return sDownloadManager;
    }

    /**
     * private construction
     */
    private DownloadManager() {
        mDownloadRequestMap = new LinkedHashMap<>();
    }

    public void init(DownloadConfiguration configuration) {
        if (configuration == null) {
            throw new RuntimeException("configuration can not be null!");
        }
        this.mConfig = configuration;
        mExecutorService = Executors.newFixedThreadPool(configuration.maxThreadNum);
        mDelivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));

        mDBManager = DataBaseManager.getInstance(mConfig.context);
    }

    private void addRequest(String tag, DownloadRequest downloadRequest) {
        mDownloadRequestMap.put(tag, downloadRequest);
        downloadRequest.start();
    }

    private DownloadRequest getDownloadRequest(String url) {
        return mDownloadRequestMap.get(createTag(url));
    }

    /**
     * @param downloadInfo
     * @param callBack
     * @return tag
     */
    public void download(DownloadInfo downloadInfo, CallBack callBack) {
        if (mConfig == null) {
            throw new RuntimeException("Please config first!");
        }
        final DownloadRequest request = new DownloadRequest(downloadInfo, mConfig.downloadDir, mDBManager, mExecutorService, new DownloadStatus(callBack), mDelivery);
        String tag = createTag(downloadInfo.getUrl());
        addRequest(tag, request);
    }

    public void pause(DownloadInfo downloadInfo) {
        DownloadRequest request = mDownloadRequestMap.get(createTag(downloadInfo.getUrl()));
        request.pause();
    }

    public void cancel(DownloadInfo downloadInfo){
        DownloadRequest request = mDownloadRequestMap.get(createTag(downloadInfo.getUrl()));
        request.cancel();
    }

    public int getDownloadProgress(DownloadInfo downloadInfo) {
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(downloadInfo.getUrl());

        return 0;
    }

    private static String createTag(String url) {
        return String.valueOf(url.hashCode());
    }

}
