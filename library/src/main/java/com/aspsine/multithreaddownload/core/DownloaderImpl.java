package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadTask;
import com.aspsine.multithreaddownload.architecture.Downloader;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.DownloadInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Aspsine on 2015/10/28.
 */
public class DownloaderImpl implements Downloader, ConnectTask.OnConnectListener, DownloadTask.OnDownloadListener {

    private DownloadRequest mRequest;

    private DownloadResponse mResponse;

    private Executor mExecutor;

    private DataBaseManager mDBManager;

    private String mTag;

    private OnDownloaderDestroyedListener mListener;

    private ConnectTask mConnectTask;

    private List<DownloadTask> mDownloadTasks;

    public DownloaderImpl(DownloadRequest request, DownloadResponse response, Executor executor, DataBaseManager dbManager, String key, OnDownloaderDestroyedListener listener) {
        mRequest = request;
        mResponse = response;
        mExecutor = executor;
        mDBManager = dbManager;
        mTag = key;
        mListener = listener;
    }

    private void init() {
        //TODO restore last downloadInfo
        mDownloadTasks = new LinkedList<>();

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start() {
        connect();
    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void onDestroy() {
        // trigger the onDestroy callback tell download manager
        mListener.onDestroyed(mTag, this);
    }

    @Override
    public void onConnecting() {
        mResponse.onConnecting();
    }

    @Override
    public void onConnected(long time, long length, boolean isAcceptRanges) {
        download(isAcceptRanges);
    }

    @Override
    public void onConnectFailed(DownloadException de) {
        mResponse.onConnectFailed(de);
    }

    @Override
    public void onConnectCanceled() {
        mResponse.onConnectCanceled();
    }

    @Override
    public void onDownloadConnecting() {
    }

    @Override
    public void onDownloadProgress(long finished, long length) {
        mResponse.onDownloadProgress();
    }

    @Override
    public void onDownloadCompleted() {
        mResponse.onDownloadCompleted();
    }

    @Override
    public void onDownloadPaused() {
        mResponse.onDownloadPaused();
    }

    @Override
    public void onDownloadCanceled() {
        mResponse.onDownloadCanceled();
    }

    @Override
    public void onDownloadFailed(DownloadException de) {
        mResponse.onDownloadFailed(de);
    }

    private void connect() {
        mConnectTask = new ConnectTaskImpl(mRequest.getUri().toString(), this);
        mExecutor.execute(mConnectTask);
    }

    private void download(boolean isAcceptRanges) {

    }


}
