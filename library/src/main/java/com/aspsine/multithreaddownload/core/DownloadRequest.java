package com.aspsine.multithreaddownload.core;


import android.util.Log;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.FileUtils;
import com.aspsine.multithreaddownload.util.L;
import com.aspsine.multithreaddownload.util.ListUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadRequest implements ConnectTask.OnConnectListener, DownloadTask.OnDownloadListener {

    private static final int threadNum = 3;

    private final DownloadInfo mDownloadInfo;
    private final File mDownloadDir;
    private final DataBaseManager mDBManager;
    private final ExecutorService mExecutorService;
    private final DownloadStatus mDownloadStatus;
    private final DownloadStatusDelivery mDelivery;

    private List<DownloadTask> mDownloadTasks;

    private int mStatus = -1;

    public DownloadRequest(DownloadInfo downloadInfo, DataBaseManager dbManager, ExecutorService executorService, DownloadStatus downloadStatus, DownloadStatusDelivery delivery) {
        this.mDownloadInfo = downloadInfo;
        this.mExecutorService = executorService;
        this.mDownloadStatus = downloadStatus;
        this.mDelivery = delivery;
        this.mDBManager = dbManager;

        this.mDownloadDir = mDownloadInfo.getDir();
    }

    @Override
    public void onStart() {
        mStatus = DownloadStatus.STATUS_STAT;
        mDelivery.postStart(mDownloadStatus);
    }

    @Override
    public void onConnected(DownloadInfo downloadInfo) {
        mStatus = DownloadStatus.STATUS_CONNECTED;
        mDelivery.postConnected(downloadInfo.getLength(), downloadInfo.isSupportRange(), mDownloadStatus);
        if (!mDownloadDir.exists()) {
            if (FileUtils.isSDMounted()) {
                mDownloadDir.mkdir();
            } else {
                mDelivery.postFailure(new DownloadException("can't make dir!"), mDownloadStatus);
                return;
            }
        }

        download(downloadInfo);
    }

    @Override
    public void onConnectFail(DownloadException de) {
        mStatus = DownloadStatus.STATUS_FAILURE;
        mDelivery.postFailure(de, mDownloadStatus);
    }

    @Override
    public void onProgress(int finished, int length) {
        mStatus = DownloadStatus.STATUS_PROGRESS;
        mDelivery.postProgressUpdate(finished, length, mDownloadStatus);
    }

    @Override
    public void onComplete() {
        L.i("onComplete", "onComplete");
        if (isAllFinished()) {
            mDBManager.delete(mDownloadInfo.getUrl());
            mStatus = DownloadStatus.STATUS_COMPLETE;
            mDelivery.postComplete(mDownloadStatus);
        }
    }

    @Override
    public void onPause() {
        if (isAllPaused()) {
            mStatus = DownloadStatus.STATUS_PAUSE;
            mDelivery.postPause(mDownloadStatus);
        }
    }

    @Override
    public void onCancel() {
        if (isAllCanceled()) {
            mDBManager.delete(mDownloadInfo.getUrl());
            File file = new File(mDownloadDir, mDownloadInfo.getName());
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            mStatus = DownloadStatus.STATUS_CANCEL;
            mDelivery.postCancel(mDownloadStatus);
        }
    }

    @Override
    public void onFailure(DownloadException de) {
        if (isAllFailure()) {
            mStatus = DownloadStatus.STATUS_FAILURE;
            mDelivery.postFailure(de, mDownloadStatus);
        }
    }

    public void start(CallBack callBack) {
        mDownloadInfo.setFinished(0);
        mDownloadInfo.setLength(0);
        mDownloadStatus.setCallBack(callBack);
        ConnectTask connectTask = new ConnectTask(mDownloadInfo, this);
        mExecutorService.execute(connectTask);
    }

    public void pause() {
        if (ListUtils.isEmpty(mDownloadTasks)) {
            return;
        }
        for (DownloadTask task : mDownloadTasks) {
            task.pause();
        }
    }

    public void cancel() {
        if (ListUtils.isEmpty(mDownloadTasks)) {
            return;
        }
        for (DownloadTask task : mDownloadTasks) {
            task.cancel();
        }
    }

    public synchronized boolean isStarted() {
        return mStatus == DownloadStatus.STATUS_STAT
                || mStatus == DownloadStatus.STATUS_CONNECTED
                || mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    private boolean isAllPaused() {
        boolean allPaused = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isPaused()) {
                allPaused = false;
                break;
            }
        }
        Log.i("isAllPaused", "isAllPaused = " + allPaused);
        return allPaused;
    }

    private boolean isAllCanceled() {
        boolean allCanceled = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isCanceled()) {
                allCanceled = false;
                break;
            }
        }
        return allCanceled;
    }

    /**
     * check if all threads finished download
     *
     * @return
     */
    private boolean isAllFinished() {
        boolean allFinished = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isComplete()) {
                allFinished = false;
                break;
            }
        }
        return allFinished;
    }

    private boolean isAllFailure() {
        boolean allFailure = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isFailure()) {
                allFailure = false;
                break;
            }
        }
        return allFailure;
    }

    private void download(DownloadInfo downloadInfo) {
        mDownloadTasks = new LinkedList<>();
        if (downloadInfo.isSupportRange()) {
            //multi thread
            List<ThreadInfo> threadInfos = getMultiThreadInfos();
            // init finished
            int finished = 0;
            for (ThreadInfo threadInfo : threadInfos) {
                finished += threadInfo.getFinished();
            }
            mDownloadInfo.setFinished(finished);
            // init tasks
            for (ThreadInfo threadInfo : threadInfos) {
                DownloadTask task = new MultiDownloadTask(downloadInfo, threadInfo, mDBManager, this);
                mDownloadTasks.add(task);
            }
        } else {
            //single thread
            ThreadInfo threadInfo = getSingleThreadInfo();
            DownloadTask task = new SingleDownloadTask(downloadInfo, threadInfo, this);
            mDownloadTasks.add(task);
        }
        // start tasks
        for (DownloadTask downloadTask : mDownloadTasks) {
            mExecutorService.execute(downloadTask);
        }
    }

    private List<ThreadInfo> getMultiThreadInfos() {
        // init threadInfo from db
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(mDownloadInfo.getUrl());
        if (threadInfos.isEmpty()) {
            for (int i = 0; i < threadNum; i++) {
                // calculate average
                final int average = mDownloadInfo.getLength() / threadNum;
                int end = 0;
                int start = average * i;
                if (i == threadNum - 1) {
                    end = mDownloadInfo.getLength();
                } else {
                    end = start + average - 1;
                }
                L.i("ThreadInfo", i + ":" + "start=" + start + "; end=" + end);
                ThreadInfo threadInfo = new ThreadInfo(i, mDownloadInfo.getUrl(), start, end, 0);
                threadInfos.add(threadInfo);
            }
        }
        return threadInfos;
    }

    public ThreadInfo getSingleThreadInfo() {
        ThreadInfo threadInfo = new ThreadInfo(0, mDownloadInfo.getUrl(), 0);
        return threadInfo;
    }


}
