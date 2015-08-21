package com.aspsine.multithreaddownload.entity;

import java.io.File;
import java.io.Serializable;

/**
 * Created by aspsine on 15-4-19.
 */
public class DownloadInfo implements Serializable {
    private String name;
    private String url;
    private File dir;
    private int progress;
    private long length;
    private long finished;
    private boolean isSupportRange;

    public DownloadInfo(String name, String url, File dir) {
        this.name = name;
        this.url = url;
        this.dir = dir;
    }

    public DownloadInfo(){

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

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getProgress() {
        return progress;
    }

    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }

    public long getFinished() {
        return finished;
    }

    public synchronized void setFinished(long finished) {
        this.finished = finished;
    }

    public boolean isSupportRange() {
        return isSupportRange;
    }

    public void setIsSupportRange(boolean isSupportRange) {
        this.isSupportRange = isSupportRange;
    }
}
