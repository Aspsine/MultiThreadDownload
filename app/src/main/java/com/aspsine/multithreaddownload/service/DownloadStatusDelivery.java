package com.aspsine.multithreaddownload.service;

/**
 * Created by Aspsine on 2015/7/15.
 */
public interface DownloadStatusDelivery {
    void postFinishInit(int length, DownloadStatus status);

    void postProgressUpdate(int finished, int total, DownloadStatus status);

    void postComplete(DownloadStatus status);

    void postFailure(Exception e, DownloadStatus status);
}
