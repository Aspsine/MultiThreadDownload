package com.aspsine.multithreaddownload.service;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatus{
    public static final int FLAG_FINISH_INIT = 0XF00;
    public static final int FLAG_PROGRESS_UPDATE = 0xF01;
    public static final int FLAG_COMPLETE = 0XF02;
    public static final int FLAG_FAILURE = 0XF03;

    private int flag;
    private int length;
    private int finished;
    private Exception exception;

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
