package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadException extends Exception {
    private String mErrorMessage;
    private String mErrorCode;

    public DownloadException(String detailMessage) {
        super(detailMessage);
        this.mErrorMessage = detailMessage;
    }

    public DownloadException(Throwable throwable) {
        super(throwable);
    }

    public DownloadException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.mErrorMessage = detailMessage;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(String errorCode) {
        this.mErrorCode = errorCode;
    }
}
