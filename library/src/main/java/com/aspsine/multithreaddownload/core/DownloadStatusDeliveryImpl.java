package com.aspsine.multithreaddownload.core;

import android.os.Handler;

import com.aspsine.multithreaddownload.CallBack;

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
    public void postConnected(int length, DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_CONNECTED);
        status.setLength(length);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postProgressUpdate(int finished, int total, DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_PROGRESS);
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
    public void postPause(DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_PAUSE);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postCancel(DownloadStatus status) {
        status.setFlag(DownloadStatus.FLAG_CANCEL);
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(status));
    }

    @Override
    public void postFailure(DownloadException e, DownloadStatus status) {
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
                case DownloadStatus.FLAG_CONNECTED:
                    mCallBack.onConnected(mDownloadStatus.getLength());
                    break;
                case DownloadStatus.FLAG_PROGRESS:
                    int finished = mDownloadStatus.getFinished();
                    int length = mDownloadStatus.getLength();
                    mCallBack.onProgress(finished, length, finished * 100 / length);
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
