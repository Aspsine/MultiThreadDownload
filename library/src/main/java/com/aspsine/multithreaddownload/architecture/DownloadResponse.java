package com.aspsine.multithreaddownload.architecture;

/**
 * Created by Aspsine on 2015/10/28.
 */
public interface DownloadResponse {

    void onStarted();

    void onConnecting();

    void onConnected(long time, long length, boolean isAcceptRanges);

    void onConnectFailed(Exception e);

    void onConnectCanceled();

    void onDownloadProgress();

    void onDownloadCompleted();

    void onDownloadPaused();

    void onDownloadCanceled();

    void onDownloadFailed(Exception e);
}
