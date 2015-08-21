package com.aspsine.multithreaddownload.entity;

/**
 * Created by aspsine on 15-4-19.
 */
public class ThreadInfo {
    private int id;
    private String url;
    private long start;
    private long end;
    private long finished;
    private int progress;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String url, long start, long end, long finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public ThreadInfo(int id, String url, int finished) {
        this.id = id;
        this.url = url;
        this.finished = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
