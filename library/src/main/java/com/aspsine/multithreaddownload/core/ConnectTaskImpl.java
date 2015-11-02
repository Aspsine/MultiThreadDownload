package com.aspsine.multithreaddownload.core;

import android.text.TextUtils;

import com.aspsine.multithreaddownload.Constants;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.util.L;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Aspsine on 2015/7/20.
 */
public class ConnectTaskImpl implements ConnectTask {
    private String mUri;
    private ConnectTask.OnConnectListener mOnConnectListener;

    private volatile int mStatus;

    private HttpURLConnection mHttpConn;


    public ConnectTaskImpl(String uri, ConnectTask.OnConnectListener listener) {
        this.mUri = uri;
        this.mOnConnectListener = listener;
    }

    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCELED;
        currentThread().interrupt();
        if (mHttpConn != null) {
            L.i("canceled" + mStatus);
            mHttpConn.disconnect();
        }
    }

    @Override
    public boolean isConnecting() {
        return mStatus == DownloadStatus.STATUS_START;
    }

    @Override
    public boolean isConnected() {
        return mStatus == DownloadStatus.STATUS_CONNECTED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        L.i("ThreadInfo", "InitThread = " + this.hashCode());
        mStatus = DownloadStatus.STATUS_START;
        mOnConnectListener.onConnecting();
        DownloadException exception = null;
        try {
            URL url = new URL(mUri);
            mHttpConn = (HttpURLConnection) url.openConnection();
            mHttpConn.setConnectTimeout(Constants.HTTP.CONNECT_TIME_OUT);
            mHttpConn.setReadTimeout(Constants.HTTP.READ_TIME_OUT);
            mHttpConn.setRequestMethod(Constants.HTTP.GET);
            long length = -1;
            boolean isAcceptRanges = false;
            long startTime = System.currentTimeMillis();
            if (mHttpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String headerLength = mHttpConn.getHeaderField("Content-Length");
                L.i("ConnectTask", "headerLength :" + headerLength);
                if (TextUtils.isEmpty(headerLength) || headerLength.equals("0") || headerLength.equals("-1")) {
                    length = mHttpConn.getContentLength();
                } else {
                    length = Long.parseLong(headerLength);
                }
                String acceptRanges = mHttpConn.getHeaderField("Accept-Ranges");
                L.i("ConnectTask", "Accept-Ranges:" + acceptRanges);
                if (!TextUtils.isEmpty(acceptRanges)) {
                    isAcceptRanges = acceptRanges.equals("bytes");
                }
                L.i("ConnectTask", "isAcceptRanges:" + isAcceptRanges);
            }
            if (length <= 0) {
                //Fail
                throw new DownloadException("length<=0 T-T~");
            } else {
                //Successful
                mStatus = DownloadStatus.STATUS_CONNECTED;
                mOnConnectListener.onConnected(System.currentTimeMillis() - startTime, length, isAcceptRanges);
            }
        } catch (IOException e) {
            if (isCanceled()) {
                // catch exception will clear interrupt status
                // we need reset interrupt status
                currentThread().interrupt();
                mOnConnectListener.onConnectCanceled();
                return;
            } else {
                exception = new DownloadException(e);
                mStatus = DownloadStatus.STATUS_FAILED;
            }
        } catch (DownloadException e) {
            exception = e;
            mStatus = DownloadStatus.STATUS_FAILED;
        } finally {
            mHttpConn.disconnect();
        }

        if (isFailed()) {
            mOnConnectListener.onConnectFailed(exception);
        }
    }

    private synchronized Thread currentThread() {
        return Thread.currentThread();
    }
}
