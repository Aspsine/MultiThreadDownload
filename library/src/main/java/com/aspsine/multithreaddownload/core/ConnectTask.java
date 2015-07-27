package com.aspsine.multithreaddownload.core;

import android.text.TextUtils;

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

    public interface OnConnectListener {
        void onStart();

        void onConnected(DownloadInfo downloadInfo);

        void onConnectFail(DownloadException de);
    }

    public ConnectTask(DownloadInfo downloadInfo, OnConnectListener listener) {
        this.mDownloadInfo = downloadInfo;
        this.mOnConnectListener = listener;
    }

    @Override
    public void run() {
        L.i("ThreadInfo", "InitThread = " + this.hashCode());
        mOnConnectListener.onStart();
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(mDownloadInfo.getUrl());
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(10 * 1000);
            int length = -1;
            boolean isSupportRange = false;
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                length = httpConn.getContentLength();
                String acceptRanges = httpConn.getHeaderField("Accept-Ranges");
                L.i("ConnectTask", "Accept-Ranges:" + acceptRanges);
                if (!TextUtils.isEmpty(acceptRanges)){
                    isSupportRange = acceptRanges.equals("bytes");
                }
                L.i("ConnectTask", "isSupportRange:" + isSupportRange);
            }
            if (length <= 0) {
                //TODO
                throw new DownloadException("length<0 T-T~");
            } else {
                mDownloadInfo.setLength(length);
                mDownloadInfo.setIsSupportRange(isSupportRange);
                mOnConnectListener.onConnected(mDownloadInfo);
            }
        } catch (IOException e) {
            mOnConnectListener.onConnectFail(new DownloadException(e));
        } catch (DownloadException e) {
            mOnConnectListener.onConnectFail(new DownloadException(e));
        } finally {
            httpConn.disconnect();
        }
    }
}
