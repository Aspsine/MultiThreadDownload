package com.aspsine.multithreaddownload.entity;

import java.io.Serializable;

/**
 * Created by aspsine on 15-4-19.
 */
public class FileInfo implements Serializable{
    private String name;
    private String url;
    private int length;

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

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", length=" + length +
                '}';
    }
}
