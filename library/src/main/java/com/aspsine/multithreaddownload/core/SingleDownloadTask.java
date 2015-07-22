package com.aspsine.multithreaddownload.core;

import android.util.Log;

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
 * Created by Aspsine on 2015/7/22.
 */
public class SingleDownloadTask implements DownloadTask {
    private ThreadInfo mThreadInfo;
    private File mDownloadDir;
    private DownloadInfo mDownloadInfo;
    private OnDownloadListener mOnDownloadListener;

    private boolean mCancel;
    private boolean mPause;
    private boolean mFinished;

    public SingleDownloadTask(ThreadInfo threadInfo, DownloadInfo downloadInfo, File downloadDir, OnDownloadListener listener) {
        this.mThreadInfo = threadInfo;
        this.mDownloadInfo = downloadInfo;
        this.mDownloadDir = downloadDir;
        this.mOnDownloadListener = listener;
    }

    @Override
    public void cancel() {
        mCancel = true;
    }

    @Override
    public void pause() {
        mPause = true;
    }

    @Override
    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public void run() {
        this.mPause = false;
        this.mCancel = false;
        HttpURLConnection httpConn = null;
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        try {
            URL url = new URL(mThreadInfo.getUrl());
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(5 * 1000);
            httpConn.setRequestMethod("GET");

            File file = new File(mDownloadDir, mDownloadInfo.getName());
            raf = new RandomAccessFile(file, "rwd");
            raf.seek(0);
            final int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(httpConn.getInputStream());
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1 && !mCancel && !mPause) {
                    raf.write(buffer, 0, len);
                    synchronized (mOnDownloadListener) {
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        Log.i("SingleDownloadTask", "[Downloading] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished() + "; length = " + mDownloadInfo.getLength());
                        mOnDownloadListener.onProgress(mThreadInfo.getFinished(), mDownloadInfo.getLength());
                    }
                }
                if (mCancel) {
                    // cancel
                    Log.i("SingleDownloadTask", "[Cancel] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                } else if (mPause) {
                    // pause
                    Log.i("SingleDownloadTask", "[Pause] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                } else if (!mCancel && !mPause) {
                    // complete
                    Log.i("SingleDownloadTask", "[Complete] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    mFinished = true;
                    synchronized (mOnDownloadListener) {
                        mOnDownloadListener.onComplete();
                    }
                }
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

