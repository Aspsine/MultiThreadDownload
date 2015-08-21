package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/15.
 */
public interface DownloadStatusDelivery {
    void postStart(DownloadStatus status);

    void postConnected(long length, boolean isRangeSupport, DownloadStatus status);

    void postProgressUpdate(long finished, long total, DownloadStatus status);

    void postComplete(DownloadStatus status);

    void postPause(DownloadStatus status);

    void postCancel(DownloadStatus status);

    void postFailure(DownloadException e, DownloadStatus status);
}
