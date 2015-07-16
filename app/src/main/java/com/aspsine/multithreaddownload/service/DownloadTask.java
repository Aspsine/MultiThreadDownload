package com.aspsine.multithreaddownload.service;

import android.util.Log;

import com.aspsine.multithreaddownload.App;
import com.aspsine.multithreaddownload.db.ThreadInfoRepository;
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
public class DownloadTask {

    private final DownloadInfo mDownloadInfo;
    private final File mDownloadDir;
    private final ExecutorService mExecutorService;
    private final DownloadStatus mDownloadStatus;
    private final DownloadStatusDelivery mDelivery;

    private final ThreadInfoRepository mRepository;


    private CallBack mCallBack;

    private List<DownloadThread> mDownloadThreads;

    private int mFinished = 0;
    private boolean mIsPause = false;
    private boolean mCancel = false;

    public DownloadTask(DownloadInfo downloadInfo, File downloadDir, ExecutorService executorService, DownloadStatus downloadStatus, DownloadStatusDelivery delivery) {
        this.mDownloadInfo = downloadInfo;
        this.mDownloadDir = downloadDir;
        this.mExecutorService = executorService;
        this.mDownloadStatus = downloadStatus;
        this.mDelivery = delivery;

        this.mRepository = App.getThreadInfoRepository();
    }

    public DownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    public int getFinished() {
        return mFinished;
    }

    public static final int threadNum = 3;

    public void start() {
        mExecutorService.execute(new InitThread(mDownloadInfo));
    }

    private void download() {
        //
        mIsPause = false;
        mCancel = false;

        // init threadInfo
        List<ThreadInfo> threadInfos = mRepository.getThreadInfos(mDownloadInfo.getUrl());

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
        mDownloadThreads = new ArrayList<>();
        for (ThreadInfo threadInfo : threadInfos) {
            DownloadThread downloadThread = new DownloadThread(threadInfo);
            mDownloadThreads.add(downloadThread);
        }

        // start
        for (DownloadThread downloadThread : mDownloadThreads) {
            mExecutorService.execute(downloadThread);
        }
    }

    public void pause() {
        mIsPause = true;
    }


    private void checkAllFinished() {
        boolean allFinished = true;
        for (DownloadThread downloadThread : mDownloadThreads) {
            if (!downloadThread.getDownloadFinished()) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            mRepository.delete(mDownloadInfo.getUrl());
            mDelivery.postComplete(mDownloadStatus);
        }
    }

    /**
     * init thread
     */
    class InitThread extends Thread {
        DownloadInfo mDownloadInfo;

        private InitThread(DownloadInfo downloadInfo) {
            mDownloadInfo = downloadInfo;
        }

        @Override
        public void run() {
            Log.i("ThreadInfo", "InitThread = " + this.hashCode());
            HttpURLConnection httpConn = null;
            RandomAccessFile raf = null;
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
                    throw new DownloadException("");
                } else {
                    mDownloadInfo.setLength(length);
                    mDelivery.postFinishInit(length, mDownloadStatus);
                    if (!mDownloadDir.exists()) {
                        mDownloadDir.mkdir();
                    }
                    File file = new File(mDownloadDir, mDownloadInfo.getName());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                }
            } catch (IOException e) {
                mDelivery.postFailure(e, mDownloadStatus);
            } catch (DownloadException e) {
                mDelivery.postFailure(e, mDownloadStatus);
            } finally {
                httpConn.disconnect();
                try {
                    IOCloseUtils.close(raf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // start download
            download();
        }
    }

    /**
     * download thread
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        private boolean mDownloadFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        private boolean getDownloadFinished() {
            return mDownloadFinished;
        }

        @Override
        public void run() {
            if (!mRepository.exists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mRepository.insert(mThreadInfo);
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
                        Log.i("ThreadInfo", "DownloadThread = " + this.hashCode());
                        raf.write(buffer, 0, len);
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        mFinished += len;
                        if (mDownloadStatus.getCallBack() != null) {
                            mDelivery.postProgressUpdate(mFinished, mDownloadInfo.getLength(), mDownloadStatus);
                        }
                        if (mIsPause) {
                            mRepository.update(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    mDownloadFinished = true;
                    checkAllFinished();
                }
            } catch (IOException e) {
                mDelivery.postFailure(e, mDownloadStatus);
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
