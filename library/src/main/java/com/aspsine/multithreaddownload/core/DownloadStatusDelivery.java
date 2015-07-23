package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/15.
 */
public interface DownloadStatusDelivery {
    void postConnected(int length, boolean isRangeSupport,DownloadStatus status);

    void postProgressUpdate(int finished, int total, DownloadStatus status);

    void postComplete(DownloadStatus status);

    void postPause(DownloadStatus status);

    void postCancel(DownloadStatus status);

    void postFailure(DownloadException e, DownloadStatus status);
}
