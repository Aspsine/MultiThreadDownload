package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.CallBack;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatus {
    public static final int STATUS_CONNECTED = 0XF00;
    public static final int STATUS_PROGRESS = 0xF01;
    public static final int STATUS_COMPLETE = 0XF02;
    public static final int STATUS_PAUSE = 0XF03;
    public static final int STATUS_CANCEL = 0XF04;
    public static final int STATUS_FAILURE = 0XF05;

    private int status;
    private int length;
    private int finished;
    private DownloadException exception;

    private CallBack callBack;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public DownloadException getException() {
        return exception;
    }

    public void setException(DownloadException exception) {
        this.exception = exception;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
