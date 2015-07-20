package com.aspsine.multithreaddownload.service;

import android.util.Log;

import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.IOCloseUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private volatile int mFinished = 0;
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

    public DownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    public int getFinished() {
        return mFinished;
    }

    public static final int threadNum = 3;

    public void start() {
        if (!mDownloadDir.exists()) {
            if (mDownloadDir.mkdir()) {
                mDelivery.postFailure(new DownloadException("can't make dir!"), mDownloadStatus);
                return;
            }
        }
        mExecutorService.execute(new ConnectTask(mDownloadInfo));
    }

    private void download() {
        mIsPause = false;
        mCancel = false;

        // init threadInfo
        List<ThreadInfo> threadInfos = mDBManager.getThreadInfos(mDownloadInfo.getUrl());

        // calculate average
        if (threadInfos.size() == 0) {
            for (int i = 0; i < threadNum; i++) {
                int average = mDownloadInfo.getLength() / threadNum;
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

        // thread list
        mDownloadTasks = new ArrayList<>();
        for (ThreadInfo threadInfo : threadInfos) {
            DownloadTask downloadTask = new DownloadTask(threadInfo);
            mDownloadTasks.add(downloadTask);
        }

        // start
        for (DownloadTask downloadTask : mDownloadTasks) {
            mExecutorService.execute(downloadTask);
        }
    }

    public void pause() {
        mIsPause = true;
        mDelivery.postPause(mDownloadStatus);
    }

    public void cancel() {
        mCancel = true;
        File file = new File(mDownloadDir, mDownloadInfo.getName());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        mDelivery.postCancel(mDownloadStatus);
    }

    private void checkAllFinished() {
        boolean allFinished = true;
        for (DownloadTask downloadTask : mDownloadTasks) {
            if (!downloadTask.getDownloadFinished()) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            mDBManager.delete(mDownloadInfo.getUrl());
            mDelivery.postComplete(mDownloadStatus);
        }
    }

    /**
     * init thread
     */
    private class ConnectTask implements Runnable {
        DownloadInfo mDownloadInfo;

        private ConnectTask(DownloadInfo downloadInfo) {
            mDownloadInfo = downloadInfo;
        }

        @Override
        public void run() {
            Log.i("ThreadInfo", "InitThread = " + this.hashCode());
            HttpURLConnection httpConn = null;
            try {
                URL url = new URL(mDownloadInfo.getUrl());
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setConnectTimeout(10 * 1000);
                int length = -1;
                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = httpConn.getContentLength();
                }
                if (length <= 0) {
                    //TODO
                    throw new DownloadException("length<0 T-T~");
                } else {
                    mDownloadInfo.setLength(length);
                    mDelivery.postConnected(length, mDownloadStatus);

                }
            } catch (IOException e) {
                mDelivery.postFailure(new DownloadException(e), mDownloadStatus);
            } catch (DownloadException e) {
                mDelivery.postFailure(e, mDownloadStatus);
            } finally {
                httpConn.disconnect();
            }
            // start download
            download();
        }
    }

    /**
     * download thread
     */
    private class DownloadTask implements Runnable {
        private ThreadInfo mThreadInfo;
        private boolean mDownloadFinished = false;

        public DownloadTask(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        private boolean getDownloadFinished() {
            return mDownloadFinished;
        }

        @Override
        public void run() {
            if (!mDBManager.exists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDBManager.insert(mThreadInfo);
            }
            HttpURLConnection httpConn = null;
            InputStream inputStream = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setConnectTimeout(5 * 1000);
                httpConn.setRequestMethod("GET");

                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                int end = mThreadInfo.getEnd();
                httpConn.setRequestProperty("Range", "bytes=" + start + "-" + end);

                File file = new File(mDownloadDir, mDownloadInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                mFinished = mThreadInfo.getFinished();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    inputStream = new BufferedInputStream(httpConn.getInputStream());
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        synchronized (DownloadTask.class) {
                            mFinished += len;
                            if (mDownloadStatus.getCallBack() != null) {
                                Log.i("ThreadInfo", "DownloadTask = " + this.hashCode() + ";  mFinished = " + mFinished);
                                mDelivery.postProgressUpdate(mFinished, mDownloadInfo.getLength(), mDownloadStatus);
                            }
                        }
                        if (mIsPause) {
                            mDBManager.update(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }

                        if (mCancel) {
                            return;
                        }
                    }
                    mDownloadFinished = true;
                    checkAllFinished();
                }
            } catch (IOException e) {
                mDelivery.postFailure(new DownloadException(e), mDownloadStatus);
            } finally {
                httpConn.disconnect();
                try {
                    IOCloseUtils.close(inputStream);
                    IOCloseUtils.close(raf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
