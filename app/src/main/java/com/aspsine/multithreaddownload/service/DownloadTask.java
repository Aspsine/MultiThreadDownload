package com.aspsine.multithreaddownload.service;

import android.content.Context;
import android.content.Intent;

import com.aspsine.multithreaddownload.App;
import com.aspsine.multithreaddownload.db.ThreadInfoRepository;
import com.aspsine.multithreaddownload.db.ThreadInfoRepositoryImpl;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadTask {
    private final Context mContext;
    private final DownloadInfo mDownloadInfo;
    private final ThreadInfoRepository mRepository;

    private List<DownloadThread> mDownloadThreads;

    private int mFinished = 0;
    private boolean mIsPause = false;
    private boolean mCancel = false;

    public DownloadTask(Context context, DownloadInfo downloadInfo) {
        this.mContext = context;
        this.mDownloadInfo = downloadInfo;
        mRepository = App.getThreadInfoRepository();
    }


    final int threadNum = 5;

    public void download() {
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
                    end = start + average;
                }
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
            downloadThread.start();
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
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        private Intent mIntent;
        private boolean mDownloadFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
            this.mIntent = new Intent(DownloadService.ACTION_UPDATE);
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

                File file = new File(FileUtils.getDownloadDir(mContext), mDownloadInfo.getName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                mFinished = mThreadInfo.getFinished();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    inputStream = new BufferedInputStream(httpConn.getInputStream());
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        int progress = Integer.valueOf(mFinished * 100 / mDownloadInfo.getLength());
                        if (System.currentTimeMillis() - time > 500 || progress == 100) {
                            time = System.currentTimeMillis();
                            mIntent.putExtra(DownloadService.EXTRA_FINISHED, progress);
                            mContext.sendBroadcast(mIntent);
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
                e.printStackTrace();
            } finally {
                httpConn.disconnect();
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static interface ProgressCallBacks {
        public void onProgress(int progress);
    }
}
