package com.aspsine.multithreaddownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aspsine.multithreaddownload.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class ThreadInfoDao extends AbstractDao<ThreadInfo> {

    private static final String TABLE_NAME = ThreadInfo.class.getSimpleName();

    public ThreadInfoDao(Context context) {
        super(context);
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(_id integer primary key autoincrement, id integer, url text, start integer, end integer, finished integer)");
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public synchronized void insert(ThreadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into "
                        + TABLE_NAME
                        + "(id, url, start, end, finished) values(?, ?, ?, ?, ?)",
                new Object[]{info.getId(), info.getUrl(), info.getStart(), info.getEnd(), info.getFinished()});
    }

    public synchronized void delete(String url) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "
                        + TABLE_NAME
                        + " where url = ?",
                new Object[]{url});
    }

    public synchronized void update(String url, int threadId, int finished) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update "
                        + TABLE_NAME
                        + " set finished = ?"
                        + " where url = ? and id = ? ",
                new Object[]{finished, url, threadId});
    }

    public List<ThreadInfo> getThreadInfos(String url) {
        List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where url = ?",
                new String[]{url});
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("id")));
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            info.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            info.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public synchronized boolean exists(String url, int threadId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where url = ? and id = ?",
                new String[]{url, threadId + ""});
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }

}
