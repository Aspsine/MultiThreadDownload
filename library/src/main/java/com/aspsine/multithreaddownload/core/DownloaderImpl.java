package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadTask;
import com.aspsine.multithreaddownload.architecture.Downloader;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.db.ThreadInfo;

import java.io.File;
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

    private DownloadConfiguration mConfig;

    private OnDownloaderDestroyedListener mListener;

    private int mStatus;

    private DownloadInfo mDownloadInfo;

    private ConnectTask mConnectTask;

    private List<DownloadTask> mDownloadTasks;

    public DownloaderImpl(DownloadRequest request, DownloadResponse response, Executor executor, DataBaseManager dbManager, String key, DownloadConfiguration config, OnDownloaderDestroyedListener listener) {
        mRequest = request;
        mResponse = response;
        mExecutor = executor;
        mDBManager = dbManager;
        mTag = key;
        mConfig = config;
        mListener = listener;

        init();
    }

    private void init() {
        mDownloadInfo = new DownloadInfo(mRequest.getName().toString(), mRequest.getUri(), mRequest.getFolder());
        mDownloadTasks = new LinkedList<>();
    }

    @Override
    public boolean isRunning() {
        return mStatus == DownloadStatus.STATUS_STARTED
                || mStatus == DownloadStatus.STATUS_CONNECTING
                || mStatus == DownloadStatus.STATUS_CONNECTED
                || mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public void start() {
        mStatus = DownloadStatus.STATUS_STARTED;
        mResponse.onStarted();
        connect();
    }

    @Override
    public void pause() {
        if (mConnectTask != null) {
            mConnectTask.pause();
        }
        for (DownloadTask task : mDownloadTasks) {
            task.pause();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadPaused();
        }
    }

    @Override
    public void cancel() {
        if (mConnectTask != null) {
            mConnectTask.cancel();
        }
        for (DownloadTask task : mDownloadTasks) {
            task.cancel();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadCanceled();
        }
    }

    @Override
    public void onDestroy() {
        // trigger the onDestroy callback tell download manager
        mListener.onDestroyed(mTag, this);
    }

    @Override
    public void onConnecting() {
        mStatus = DownloadStatus.STATUS_CONNECTING;
        mResponse.onConnecting();
    }

    @Override
    public void onConnected(long time, long length, boolean isAcceptRanges) {
        if (mConnectTask.isCanceled()) {
            // despite connection is finished, the entire downloader is canceled
            onConnectCanceled();
        } else {
            mStatus = DownloadStatus.STATUS_CONNECTED;
            mResponse.onConnected(time, length, isAcceptRanges);

            mDownloadInfo.setAcceptRanges(isAcceptRanges);
            mDownloadInfo.setLength(length);
            download(length, isAcceptRanges);
        }
    }

    @Override
    public void onConnectPaused() {
        onDownloadPaused();
    }

    @Override
    public void onConnectCanceled() {
        deleteFromDB();
        deleteFile();
        mStatus = DownloadStatus.STATUS_CANCELED;
        mResponse.onConnectCanceled();
        onDestroy();
    }

    @Override
    public void onConnectFailed(DownloadException de) {
        if (mConnectTask.isCanceled()) {
            // despite connection is failed, the entire downloader is canceled
            onConnectCanceled();
        } else if (mConnectTask.isPaused()) {
            // despite connection is failed, the entire downloader is paused
            onDownloadPaused();
        } else {
            mStatus = DownloadStatus.STATUS_FAILED;
            mResponse.onConnectFailed(de);
            onDestroy();
        }
    }

    @Override
    public void onDownloadConnecting() {
    }

    @Override
    public void onDownloadProgress(long finished, long length) {
        // calculate percent
        final int percent = (int) (finished * 100 / length);
        mResponse.onDownloadProgress(finished, length, percent);
    }

    @Override
    public void onDownloadCompleted() {
        if (isAllComplete()) {
            deleteFromDB();
            mStatus = DownloadStatus.STATUS_COMPLETED;
            mResponse.onDownloadCompleted();
            onDestroy();
        }
    }

    @Override
    public void onDownloadPaused() {
        if (isAllPaused()) {
            mStatus = DownloadStatus.STATUS_PAUSED;
            mResponse.onDownloadPaused();
            onDestroy();
        }
    }

    @Override
    public void onDownloadCanceled() {
        if (isAllCanceled()) {
            deleteFromDB();
            deleteFile();
            mStatus = DownloadStatus.STATUS_CANCELED;
            mResponse.onDownloadCanceled();
            onDestroy();
        }
    }

    @Override
    public void onDownloadFailed(DownloadException de) {
        if (isAllFailed()) {
            mStatus = DownloadStatus.STATUS_FAILED;
            mResponse.onDownloadFailed(de);
            onDestroy();
        }
    }

    private void connect() {
        mConnectTask = new ConnectTaskImpl(mRequest.getUri(), this);
        mExecutor.execute(mConnectTask);
    }

    private void download(long length, boolean acceptRanges) {
        mStatus = DownloadStatus.STATUS_PROGRESS;
        initDownloadTasks(length, acceptRanges);
        // start tasks
        for (DownloadTask downloadTask : mDownloadTasks) {
            mExecutor.execute(downloadTask);
        }
    }

    //TODO
    private void initDownloadTasks(long length, boolean acceptRanges) {
        mDownloadTasks.clear();
        if (acceptRanges) {
            List<ThreadInfo> threadInfos = getMultiThreadInfos(length);
            // init finished
            int finished = 0;
            for (ThreadInfo threadInfo : threadInfos) {
                finished += threadInfo.getFinished();
            }
            mDownloadInfo.setFinished(finished);
            for (ThreadInfo info : threadInfos) {
                mDownloadTasks.add(new MultiDownloadTask(mDownloadInfo, info, mDBManager, this));
            }
        } else {
            ThreadInfo info = getSingleThreadInfo();
            mDownloadTasks.add(new SingleDownloadTask(mDownloadInfo, info, this));
        }
    }

    //TODO
    private List<ThreadInfo> getMultiThreadInfos(long length) {
        // init threadInfo from db
        final List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(mTag);
        if (threadInfos.isEmpty()) {
            final int threadNum = mConfig.getThreadNum();
            for (int i = 0; i < threadNum; i++) {
                // calculate average
                final long average = length / threadNum;
                final long start = average * i;
                final long end;
                if (i == threadNum - 1) {
                    end = length;
                } else {
                    end = start + average - 1;
                }
                ThreadInfo threadInfo = new ThreadInfo(i, mTag, mRequest.getUri(), start, end, 0);
                threadInfos.add(threadInfo);
            }
        }
        return threadInfos;
    }

    //TODO
    private ThreadInfo getSingleThreadInfo() {
        ThreadInfo threadInfo = new ThreadInfo(0, mTag, mRequest.getUri(), 0);
        return threadInfo;
    }

    private boolean isAllComplete() {
        boolean allFinished = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isComplete()) {
                allFinished = false;
                break;
            }
        }
        return allFinished;
    }

    private boolean isAllFailed() {
        boolean allFailed = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allFailed = false;
                break;
            }
        }
        return allFailed;
    }

    private boolean isAllPaused() {
        boolean allPaused = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allPaused = false;
                break;
            }
        }
        return allPaused;
    }

    private boolean isAllCanceled() {
        boolean allCanceled = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allCanceled = false;
                break;
            }
        }
        return allCanceled;
    }

    private void deleteFromDB() {
        mDBManager.delete(mTag);
    }

    private void deleteFile() {
        File file = new File(mDownloadInfo.getDir(), mDownloadInfo.getName());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
