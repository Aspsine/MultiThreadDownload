package com.aspsine.multithreaddownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class ThreadInfoRepositoryImpl implements ThreadInfoRepository{

    private final ThreadInfoDao dao;

    public ThreadInfoRepositoryImpl(Context context) {
        dao = new ThreadInfoDao(context);
    }

    @Override
    public void insert(ThreadInfo threadInfo) {
        dao.insert(threadInfo);
    }

    @Override
    public void delete(String url, int threadId) {
        dao.delete(url, threadId);
    }

    @Override
    public void update(String url, int threadId, int finished) {
        dao.update(url, threadId, finished);
    }

    @Override
    public List<ThreadInfo> getThreadInfos(String url) {
        return dao.getThreadInfos(url);
    }

    @Override
    public boolean exists(String url, int threadId) {
        return dao.exists(url, threadId);
    }
}
