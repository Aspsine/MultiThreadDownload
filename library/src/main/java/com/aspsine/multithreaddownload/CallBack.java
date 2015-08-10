package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.core.ConnectTask;
import com.aspsine.multithreaddownload.core.DownloadException;

import java.net.HttpURLConnection;

/**
 * CallBack of download status
 */
public interface CallBack {

    /**
     * <p> {@link #onDownloadStart()}
     * <p> this will be the the first method called by
     * {@link com.aspsine.multithreaddownload.core.ConnectTask}.
     */
    void onDownloadStart();

    /**
     * <p> {@link #onConnected(int, boolean)}
     * <p> if {@link com.aspsine.multithreaddownload.core.ConnectTask} is successfully
     * connected with the http/https server this method will be invoke. If not method
     * {@link #onFailure(DownloadException)} will be invoke.
     *
     * @param total          The length of the file. See {@link HttpURLConnection#getContentLength()}
     * @param isRangeSupport indicate whether download can be resumed from pause.
     *                       See {@link ConnectTask#run()}. If the value of http header field
     *                       {@code Accept-Ranges} is {@code bytes} the value of  isRangeSupport is
     *                       {@code true} else {@code false}
     */
    void onConnected(int total, boolean isRangeSupport);

    /**
     * <p> {@link #onProgress(int, int, int)}
     * <p> progress callback.
     *
     * @param finished the downloaded length of the file
     * @param total    the total length of the file same value with method {@link }
     * @param progress the percent of progress (finished/total)*100
     */
    void onProgress(int finished, int total, int progress);

    /**
     * <p>{@link #onComplete()}
     * <p> download complete
     */
    void onComplete();

    /**
     * <p>{@link #onDownloadPause()}
     * <p> if you invoke {@link DownloadManager#pause(String)} or {@link DownloadManager#pauseAll()}
     * this method will be invoke if the downloading task is successfully paused.
     */
    void onDownloadPause();

    /**
     * <p>{@link #onDownloadCancel()}
     * <p> if you invoke {@link DownloadManager#cancel(String)} or {@link DownloadManager#cancelAll()}
     * this method will be invoke if the downloading task is successfully canceled.
     */
    void onDownloadCancel();

    /**
     * <p>{@link #onDownloadCancel()}
     * <p> download fail or exception callback
     *
     * @param e
     */
    void onFailure(DownloadException e);
}
