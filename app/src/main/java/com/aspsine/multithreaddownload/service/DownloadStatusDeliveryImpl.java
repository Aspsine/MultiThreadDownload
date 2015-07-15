package com.aspsine.multithreaddownload.service;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatusDeliveryImpl implements DownloadStatusDelivery {
    private Executor mDownloadStatusPoster;

    public DownloadStatusDeliveryImpl(final Handler handler) {
        mDownloadStatusPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void postFinishInit(int length, DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_FINISH_INIT);
        status.setLength(length);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postProgressUpdate(int finished, int total, DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_PROGRESS_UPDATE);
        status.setLength(total);
        status.setFinished(finished);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postComplete(DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_COMPLETE);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postFailure(Exception e, DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_FAILURE);
        status.setException(e);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    private class DownloadStatusDeliveryRunnable implements Runnable {
        private final DownloadStatus mDownloadStatus;
        private final CallBack mCallBack;

        public DownloadStatusDeliveryRunnable(DownloadStatus downloadStatus) {
            this.mDownloadStatus = downloadStatus;
            this.mCallBack = mDownloadStatus.getCallBack();
        }

        @Override
        public void run() {
            switch (mDownloadStatus.getFlag()) {
                case DownloadStatus.FLAG_FINISH_INIT:
                    mCallBack.onFinishInit(mDownloadStatus.getLength());
                    break;
                case DownloadStatus.FLAG_PROGRESS_UPDATE:
                    mCallBack.onProgressUpdate(mDownloadStatus.getFinished(), mDownloadStatus.getLength());
                    break;
                case DownloadStatus.FLAG_COMPLETE:
                    mCallBack.onComplete();
                    break;
                case DownloadStatus.FLAG_FAILURE:
                    mCallBack.onFailure(mDownloadStatus.getException());
                    break;
            }
        }
    }


}
