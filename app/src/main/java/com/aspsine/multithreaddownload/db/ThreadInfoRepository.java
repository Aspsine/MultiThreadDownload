package com.aspsine.multithreaddownload.db;

import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public interface ThreadInfoRepository{
    public void insert(ThreadInfo threadInfo);

    public void delete(String url, int threadId);

    public void update(String url, int threadId, int finished);

    public List<ThreadInfo> getThreadInfos(String url);

    public boolean exists(String url, int threadId);
}
