package com.aspsine.multithreaddownload.core;

import android.text.TextUtils;

import com.aspsine.multithreaddownload.Constants;
import com.aspsine.multithreaddownload.entity.DownloadInfo;
import com.aspsine.multithreaddownload.util.L;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Aspsine on 2015/7/20.
 */
public class ConnectTask implements Runnable {
    private DownloadInfo mDownloadInfo;
    private OnConnectListener mOnConnectListener;

    private volatile int mStatus;

    private HttpURLConnection mHttpConn;

    public interface OnConnectListener {
        void onStart();

        void onConnected(DownloadInfo downloadInfo);

        void onConnectCanceled();

        void onConnectFail(DownloadException de);
    }

    public ConnectTask(DownloadInfo downloadInfo, OnConnectListener listener) {
        this.mDownloadInfo = downloadInfo;
        this.mOnConnectListener = listener;
    }

    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCEL;
        currentThread().interrupt();
        if (mHttpConn != null) {
            L.i("canceled" + mStatus);
            mHttpConn.disconnect();
        }
    }

    public boolean isStart() {
        L.i("mStatus" + mStatus);
        return mStatus == DownloadStatus.STATUS_STAT;
    }

    public boolean isConnected() {
        return mStatus == DownloadStatus.STATUS_CONNECTED;
    }

    public boolean isCancel() {
        return mStatus == DownloadStatus.STATUS_CANCEL;
    }

    public boolean isFailure() {
        return mStatus == DownloadStatus.STATUS_FAILURE;
    }

    @Override
    public void run() {
        L.i("ThreadInfo", "InitThread = " + this.hashCode());
        mStatus = DownloadStatus.STATUS_STAT;
        mOnConnectListener.onStart();
        DownloadException exception = null;
        try {
            URL url = new URL(mDownloadInfo.getUrl());
            mHttpConn = (HttpURLConnection) url.openConnection();
            mHttpConn.setConnectTimeout(Constants.HTTP.CONNECT_TIME_OUT);
            mHttpConn.setReadTimeout(Constants.HTTP.READ_TIME_OUT);
            mHttpConn.setRequestMethod(Constants.HTTP.GET);
            int length = -1;
            boolean isSupportRange = false;
            if (mHttpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                length = mHttpConn.getContentLength();
                String acceptRanges = mHttpConn.getHeaderField("Accept-Ranges");
                L.i("ConnectTask", "Accept-Ranges:" + acceptRanges);
                if (!TextUtils.isEmpty(acceptRanges)) {
                    isSupportRange = acceptRanges.equals("bytes");
                }
                L.i("ConnectTask", "isSupportRange:" + isSupportRange);
            }
            if (length <= 0) {
                //Fail
                throw new DownloadException("length<=0 T-T~");
            } else {
                //Successful
                mDownloadInfo.setLength(length);
                mDownloadInfo.setIsSupportRange(isSupportRange);
                mStatus = DownloadStatus.STATUS_CONNECTED;
                mOnConnectListener.onConnected(mDownloadInfo);
            }
        } catch (IOException e) {
            if (isCancel()) {
                // catch exception will clear interrupt status
                // we need reset interrupt status
                currentThread().interrupt();
                mOnConnectListener.onConnectCanceled();
                return;
            } else {
                exception = new DownloadException(e);
                mStatus = DownloadStatus.STATUS_FAILURE;
            }
        } catch (DownloadException e) {
            exception = e;
            mStatus = DownloadStatus.STATUS_FAILURE;
        } finally {
            mHttpConn.disconnect();
        }

        if (isFailure()) {
            mOnConnectListener.onConnectFail(exception);
        }
    }

    private synchronized Thread currentThread() {
        return Thread.currentThread();
    }
}
