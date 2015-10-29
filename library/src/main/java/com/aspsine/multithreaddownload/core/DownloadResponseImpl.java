package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadStatusDelivery;

/**
 * Created by Aspsine on 2015/10/29.
 */
public class DownloadResponseImpl implements DownloadResponse {
    private DownloadStatusDelivery mDelivery;

    private DownloadStatus mDownloadStatus;

    public DownloadResponseImpl(DownloadStatusDelivery delivery, CallBack callBack) {
        mDelivery = delivery;
        mDownloadStatus = new DownloadStatus();
        mDownloadStatus.setCallBack(callBack);
    }

    @Override
    public void onConnecting() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CONNECTING);
    }

    @Override
    public void onConnectFailed(Exception e) {
        mDownloadStatus.setException(e);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_FAILED);
    }

    @Override
    public void onConnectCanceled() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CANCELED);
    }

    @Override
    public void onDownloadProgress() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PROGRESS);
    }

    @Override
    public void onDownloadCompleted() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_COMPLETED);
    }

    @Override
    public void onDownloadPaused() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PAUSED);
    }

    @Override
    public void onDownloadCanceled() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CANCELED);
    }

    @Override
    public void onDownloadFailed(Exception e) {
        mDownloadStatus.setException(e);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_FAILED);
    }
}
