package com.aspsine.multithreaddownload.core;


import android.text.TextUtils;

import com.aspsine.multithreaddownload.Constants.HTTP;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.entity.ThreadInfo;
import com.aspsine.multithreaddownload.util.IOCloseUtils;
import com.aspsine.multithreaddownload.util.L;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by Aspsine on 2015/7/27.
 */
public abstract class AbsDownloadTask implements DownloadTask {

    private String mTag;

    private final DownloadInfo mDownloadInfo;
    private final ThreadInfo mThreadInfo;
    private final OnDownloadListener mOnDownloadListener;

    private HttpURLConnection mHttpConn;

    private volatile int mStatus;

    public AbsDownloadTask(DownloadInfo mDownloadInfo, ThreadInfo mThreadInfo, OnDownloadListener mOnDownloadListener) {
        this.mDownloadInfo = mDownloadInfo;
        this.mThreadInfo = mThreadInfo;
        this.mOnDownloadListener = mOnDownloadListener;

        this.mTag = getTag();
        if (TextUtils.isEmpty(mTag)) {
            mTag = this.getClass().getSimpleName();
        }
    }

    protected void setStatus(int status) {
        this.mStatus = status;
    }

    protected int getStatus() {
        return mStatus;
    }

    @Override
    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCEL;
        currentThread().interrupt();
        if (mHttpConn != null) {
            mHttpConn.disconnect();
        }
    }

    @Override
    public void pause() {
        mStatus = DownloadStatus.STATUS_PAUSE;
        currentThread().interrupt();
        if (mHttpConn != null) {
            mHttpConn.disconnect();
        }
    }

    @Override
    public boolean isDownloading() {
        return mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public boolean isComplete() {
        return mStatus == DownloadStatus.STATUS_COMPLETE;
    }

    @Override
    public boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSE;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCEL;
    }

    @Override
    public boolean isFailure() {
        return mStatus == DownloadStatus.STATUS_FAILURE;
    }

    @Override
    public void run() {
        insertIntoDB(mThreadInfo);
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        DownloadException exception = null;
        try {
            URL url = new URL(mThreadInfo.getUrl());
            mHttpConn = (HttpURLConnection) url.openConnection();
            mHttpConn.setConnectTimeout(HTTP.CONNECT_TIME_OUT);
            mHttpConn.setRequestMethod(HTTP.GET);
            setHttpHeader(getHttpHeaders(mThreadInfo), mHttpConn);
            raf = getFile(mThreadInfo, mDownloadInfo);
            final int responseCode = mHttpConn.getResponseCode();
            if (responseCode == getResponseCode()) {
                inputStream = new BufferedInputStream(mHttpConn.getInputStream());
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                    L.i(mTag, "[Downloading] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    synchronized (mOnDownloadListener) {
                        mDownloadInfo.setFinished(mDownloadInfo.getFinished() + len);
                        mOnDownloadListener.onProgress(mDownloadInfo.getFinished(), mDownloadInfo.getLength());
                    }
                }
                if (!(isPaused() || isCanceled())) {
                    // complete
                    L.i(mTag, "[Complete] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    mStatus = DownloadStatus.STATUS_COMPLETE;
                    synchronized (mOnDownloadListener) {
                        mOnDownloadListener.onComplete();
                    }
                    return;
                }
            } else {
                throw new DownloadException("unSupported response code:" + responseCode);
            }
        } catch (IOException e) {
            if (isCanceled() || isPaused()) {
                // catch exception will clear interrupt status
                // we need reset interrupt status
                currentThread().interrupt();
                if (isCanceled()) {
                    // cancel
                    L.i(mTag, "[Cancel] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    synchronized (mOnDownloadListener) {
                        mOnDownloadListener.onCancel();
                    }
                    return;
                } else if (isPaused()) {
                    // pause
                    L.i(mTag, "[Pause] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
                    updateDBProgress(mThreadInfo);
                    synchronized (mOnDownloadListener) {
                        mOnDownloadListener.onPause();
                    }
                    return;
                }
            } else {
                mStatus = DownloadStatus.STATUS_FAILURE;
                exception = new DownloadException(e);
            }
        } catch (DownloadException e) {
            mStatus = DownloadStatus.STATUS_FAILURE;
            exception = new DownloadException(e);
        } finally {
            mHttpConn.disconnect();
            try {
                IOCloseUtils.close(inputStream);
                IOCloseUtils.close(raf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isFailure()) {
            // failure
            L.i(mTag, "[Failure] " + " hashcode = " + this.hashCode() + "; ThreadId = " + mThreadInfo.getId() + "; finished = " + mThreadInfo.getFinished());
            synchronized (mOnDownloadListener) {
                mOnDownloadListener.onFailure(exception);
            }
            return;
        }
    }

    private void setHttpHeader(Map<String, String> headers, URLConnection connection) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    private synchronized Thread currentThread() {
        return Thread.currentThread();
    }

    protected abstract void insertIntoDB(ThreadInfo info);

    protected abstract int getResponseCode();

    protected abstract void updateDBProgress(ThreadInfo info);

    protected abstract Map<String, String> getHttpHeaders(ThreadInfo info);

    protected abstract RandomAccessFile getFile(ThreadInfo threadInfo, DownloadInfo downloadInfo) throws IOException;

    protected abstract String getTag();
}