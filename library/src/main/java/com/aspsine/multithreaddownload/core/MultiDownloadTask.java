package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/20.
 */

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

/**
 * download thread
 */
public class MultiDownloadTask implements DownloadTask {
    private ThreadInfo mThreadInfo;
    private DataBaseManager mDBManager;
    private File mDownloadDir;
    private DownloadInfo mDownloadInfo;
    private OnDownloadListener mOnDownloadListener;

    private boolean mCancel;
    private boolean mPause;
    private boolean mFinished;

    public MultiDownloadTask(ThreadInfo threadInfo, DownloadInfo downloadInfo, File downloadDir, DataBaseManager dbManager, OnDownloadListener listener) {
        this.mThreadInfo = threadInfo;
        this.mDownloadInfo = downloadInfo;
        this.mDownloadDir = downloadDir;
        this.mDBManager = dbManager;
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
        synchronized (mDBManager) {
            if (!mDBManager.exists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDBManager.insert(mThreadInfo);
            }
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
            final int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                inputStream = new BufferedInputStream(httpConn.getInputStream());
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1 && !mCancel && !mPause) {
                    raf.write(buffer, 0, len);
                    mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                    Log.i("MultiDownloadTask", "[Downloading] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    synchronized (mOnDownloadListener) {
                        mDownloadInfo.setFinished(mDownloadInfo.getFinished() + len);
                        mOnDownloadListener.onProgress(mDownloadInfo.getFinished(), mDownloadInfo.getLength());
                    }
                }
                if (mCancel) {
                    // cancel
                    Log.i("MultiDownloadTask", "[Cancel] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                } else if (mPause) {
                    // pause
                    Log.i("MultiDownloadTask", "[Pause] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    synchronized (mOnDownloadListener) {
                        mDBManager.update(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                    }
                } else if (!mCancel && !mPause) {
                    // complete
                    Log.i("MultiDownloadTask", "[Complete] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    mFinished = true;
                    synchronized (mOnDownloadListener) {
                        mOnDownloadListener.onComplete();
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                throw new DownloadException("Don't support range download");
            }
        } catch (IOException e) {
            mOnDownloadListener.onFail(new DownloadException(e));
        } catch (DownloadException e) {
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