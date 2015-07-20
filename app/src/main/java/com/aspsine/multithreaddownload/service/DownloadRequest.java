package com.aspsine.multithreaddownload.service;

import android.util.Log;

import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadRequest {

    private final DownloadInfo mDownloadInfo;
    private final File mDownloadDir;
    private final DataBaseManager mDBManager;
    private final ExecutorService mExecutorService;
    private final DownloadStatus mDownloadStatus;
    private final DownloadStatusDelivery mDelivery;

    private List<DownloadTask> mDownloadTasks;

    private boolean mIsPause = false;
    private boolean mCancel = false;


    public DownloadRequest(DownloadInfo downloadInfo, File downloadDir, DataBaseManager dbManager, ExecutorService executorService, DownloadStatus downloadStatus, DownloadStatusDelivery delivery) {
        this.mDownloadInfo = downloadInfo;
        this.mDownloadDir = downloadDir;
        this.mExecutorService = executorService;
        this.mDownloadStatus = downloadStatus;
        this.mDelivery = delivery;
        this.mDBManager = dbManager;
    }

    public static final int threadNum = 3;

    public void start() {
        mIsPause = false;
        mCancel = false;
        ConnectTask connectTask = new ConnectTask(mDownloadInfo, new ConnectTask.OnConnectedListener() {
            @Override
            public void onConnected(DownloadInfo downloadInfo) {
                mDelivery.postConnected(downloadInfo.getLength(), mDownloadStatus);
                if (!mDownloadDir.exists()) {
                    if (mDownloadDir.mkdir()) {
                        mDelivery.postFailure(new DownloadException("can't make dir!"), mDownloadStatus);
                        return;
                    }
                }
                if (!(mIsPause || mCancel)) {
                    download(downloadInfo);
                }
            }

            @Override
            public void onFail(DownloadException de) {
                mDelivery.postFailure(de, mDownloadStatus);
            }
        });
        mExecutorService.execute(connectTask);
    }

    public void pause() {
        mIsPause = true;
        for (DownloadTask task : mDownloadTasks) {
            task.pause();
        }
        mDelivery.postPause(mDownloadStatus);
    }

    public void cancel() {
        mCancel = true;
        for (DownloadTask task : mDownloadTasks) {
            task.cancel();
        }
        File file = new File(mDownloadDir, mDownloadInfo.getName());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        mDelivery.postCancel(mDownloadStatus);
    }

    private void checkAllFinished() {
        boolean allFinished = true;
        for (DownloadTask downloadTask : mDownloadTasks) {
            if (!downloadTask.isFinished()) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            mDBManager.delete(mDownloadInfo.getUrl());
            mDelivery.postComplete(mDownloadStatus);
        }
    }

    private void download(DownloadInfo downloadInfo) {
        // init threadInfo
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(mDownloadInfo.getUrl());

        // calculate average
        if (threadInfos.size() == 0) {
            for (int i = 0; i < threadNum; i++) {
                final int average = mDownloadInfo.getLength() / threadNum;
                int end = 0;
                int start = average * i;
                if (i == threadNum - 1) {
                    end = mDownloadInfo.getLength();
                } else {
                    end = start + average - 1;
                }
                Log.i("ThreadInfo", i + ":" + "start=" + start + "; end=" + end);
                ThreadInfo threadInfo = new ThreadInfo(0, mDownloadInfo.getUrl(), start, end, 0);
                threadInfos.add(threadInfo);
            }
        }

        DownloadTask.OnDownloadListener onDownloadListener = new DownloadTask.OnDownloadListener() {
            @Override
            public void onProgress(int finished, int length) {
                mDelivery.postProgressUpdate(finished, length, mDownloadStatus);
            }

            @Override
            public void onComplete() {
                checkAllFinished();
            }

            @Override
            public void onFail(DownloadException de) {
                mDelivery.postFailure(de, mDownloadStatus);
            }
        };

        // thread list
        mDownloadTasks = new ArrayList<>();
        for (ThreadInfo threadInfo : threadInfos) {
            DownloadTask downloadTask = new DownloadTask(threadInfo, downloadInfo, mDownloadDir, mDBManager, onDownloadListener);
            mDownloadTasks.add(downloadTask);
        }
        // start
        for (DownloadTask downloadTask : mDownloadTasks) {
            mExecutorService.execute(downloadTask);
        }
    }


}
