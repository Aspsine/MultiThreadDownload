package com.aspsine.multithreaddownload.entity;

import java.io.Serializable;

/**
 * Created by aspsine on 15-4-19.
 */
public class DownloadInfo implements Serializable {
    private String name;
    private String url;
    private int progress;
    private int length;
    private int finished;

    public DownloadInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }

    public int getFinished() {
        return finished;
    }

    public synchronized void setFinished(int finished) {
        this.finished = finished;
    }
}
