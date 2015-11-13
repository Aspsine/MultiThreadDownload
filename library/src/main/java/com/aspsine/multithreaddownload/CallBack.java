package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.core.ConnectTaskImpl;

import java.net.HttpURLConnection;

/**
 * CallBack of download status
 */
public interface CallBack {

    void onStarted();

    /**
     * <p> this will be the the first method called by
     * {@link ConnectTaskImpl}.
     */
    void onConnecting();

    /**
     * <p> if {@link ConnectTaskImpl} is successfully
     * connected with the http/https server this method will be invoke. If not method
     * {@link #onFailed(DownloadException)} will be invoke.
     *
     * @param total          The length of the file. See {@link HttpURLConnection#getContentLength()}
     * @param isRangeSupport indicate whether download can be resumed from pause.
     *                       See {@link ConnectTaskImpl#run()}. If the value of http header field
     *                       {@code Accept-Ranges} is {@code bytes} the value of  isRangeSupport is
     *                       {@code true} else {@code false}
     */
    void onConnected(long total, boolean isRangeSupport);

    /**
     * <p> progress callback.
     *
     * @param finished the downloaded length of the file
     * @param total    the total length of the file same value with method {@link }
     * @param progress the percent of progress (finished/total)*100
     */
    void onProgress(long finished, long total, int progress);

    /**
     * <p> download complete
     */
    void onCompleted();

    /**
     * <p> if you invoke {@link DownloadManager#pause(String)} or {@link DownloadManager#pauseAll()}
     * this method will be invoke if the downloading task is successfully paused.
     */
    void onDownloadPaused();

    /**
     * <p> if you invoke {@link DownloadManager#cancel(String)} or {@link DownloadManager#cancelAll()}
     * this method will be invoke if the downloading task is successfully canceled.
     */
    void onDownloadCanceled();

    /**
     * <p> download fail or exception callback
     *
     * @param e download exception
     */
    void onFailed(DownloadException e);
}
