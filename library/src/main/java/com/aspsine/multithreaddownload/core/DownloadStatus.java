package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.CallBack;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatus {
    public static final int STATUS_STAT = 100;
    public static final int STATUS_CONNECTED = 101;
    public static final int STATUS_PROGRESS = 102;
    public static final int STATUS_COMPLETE = 103;
    public static final int STATUS_PAUSE = 104;
    public static final int STATUS_CANCEL = 105;
    public static final int STATUS_FAILURE = 106;

    private int status;
    private int length;
    private int finished;
    private boolean isRangeSupport;
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

    public boolean isRangeSupport() {
        return isRangeSupport;
    }

    public void setIsRangeSupport(boolean isRangeSupport) {
        this.isRangeSupport = isRangeSupport;
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
