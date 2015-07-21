package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatus {
    public static final int FLAG_CONNECTED = 0XF00;
    public static final int FLAG_PROGRESS = 0xF01;
    public static final int FLAG_COMPLETE = 0XF02;
    public static final int FLAG_PAUSE = 0XF03;
    public static final int FLAG_CANCEL = 0XF04;
    public static final int FLAG_FAILURE = 0XF05;

    private int flag;
    private int length;
    private int finished;
    private DownloadException exception;

    private CallBack callBack;

    public DownloadStatus(CallBack callBack) {
        this.callBack = callBack;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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
