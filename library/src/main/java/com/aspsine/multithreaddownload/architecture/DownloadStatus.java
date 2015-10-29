package com.aspsine.multithreaddownload.architecture;

import com.aspsine.multithreaddownload.CallBack;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatus {
    public static final int STATUS_START = 100;
    public static final int STATUS_WAITING = 101;
    public static final int STATUS_CONNECTING = 102;
    public static final int STATUS_CONNECTED = 103;
    public static final int STATUS_PROGRESS = 104;
    public static final int STATUS_COMPLETED = 105;
    public static final int STATUS_PAUSED = 106;
    public static final int STATUS_CANCELED = 107;
    public static final int STATUS_FAILED = 108;

    private int status;
    private long length;
    private long finished;
    private boolean isRangeSupport;
    private Exception exception;

    private CallBack callBack;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public boolean isRangeSupport() {
        return isRangeSupport;
    }

    public void setIsRangeSupport(boolean isRangeSupport) {
        this.isRangeSupport = isRangeSupport;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
