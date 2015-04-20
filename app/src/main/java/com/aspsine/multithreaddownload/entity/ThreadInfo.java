package com.aspsine.multithreaddownload.entity;

/**
 * Created by aspsine on 15-4-19.
 */
public class ThreadInfo {
    private int id;
    private String url;
    private int start;
    private int end;
    private int finshed;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String url, int start, int end, int finshed) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finshed = finshed;
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

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getFinshed() {
        return finshed;
    }

    public void setFinshed(int finshed) {
        this.finshed = finshed;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", finshed=" + finshed +
                '}';
    }
}
