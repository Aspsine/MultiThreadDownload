package com.aspsine.multithreaddownload.service;

/**
 * Created by Aspsine on 2015/7/20.
 */

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

/**
 * download thread
 */
public class DownloadTask implements Runnable {
    private ThreadInfo mThreadInfo;
    private DataBaseManager mDBManager;
    private File mDownloadDir;
    private DownloadInfo mDownloadInfo;
    private OnDownloadListener mOnDownloadListener;

    private boolean mCancel;
    private boolean mPause;
    private boolean mFinished;

    interface OnDownloadListener {
        void onProgress(int finished, int length);

        void onComplete();

        void onFail(DownloadException de);
    }

    public DownloadTask(ThreadInfo threadInfo, DownloadInfo downloadInfo, File downloadDir, DataBaseManager dbManager, OnDownloadListener listener) {
        this.mThreadInfo = threadInfo;
        this.mDownloadInfo = downloadInfo;
        this.mDownloadDir = downloadDir;
        this.mDBManager = dbManager;
        this.mOnDownloadListener = listener;
    }

    public void cancel(){
        mCancel = true;
    }

    public void pause(){
        mPause = true;
    }

    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public void run() {
        this.mPause = false;
        this.mCancel = false;
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

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                inputStream = new BufferedInputStream(httpConn.getInputStream());
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    if (mCancel) {
                        return;
                    }
                    if (mPause) {
                        mDBManager.update(mThreadInfo.getUrl(), mThreadInfo.getId(), mDownloadInfo.getFinished());
                        return;
                    }
                    raf.write(buffer, 0, len);
                    mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                    synchronized (mOnDownloadListener) {
                        mDownloadInfo.setFinished(mDownloadInfo.getFinished() + len);
                        mOnDownloadListener.onProgress(mDownloadInfo.getFinished(), mDownloadInfo.getLength());
                    }
                }
                mFinished = true;
                mOnDownloadListener.onComplete();
            }
        } catch (IOException e) {
            mOnDownloadListener.onFail(new DownloadException(e));
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