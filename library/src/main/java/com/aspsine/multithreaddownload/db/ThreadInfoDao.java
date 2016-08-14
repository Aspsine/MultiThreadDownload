package com.aspsine.multithreaddownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        db.execSQL("create table " + TABLE_NAME + "(_id integer primary key autoincrement, id integer, tag text, uri text, start long, end long, finished long)");
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public void insert(ThreadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into "
                        + TABLE_NAME
                        + "(id, tag, uri, start, end, finished) values(?, ?, ?, ?, ?, ?)",
                new Object[]{info.getId(), info.getTag(), info.getUri(), info.getStart(), info.getEnd(), info.getFinished()});
    }

    public void delete(String tag) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "
                        + TABLE_NAME
                        + " where tag = ?",
                new Object[]{tag});
    }

    public void update(String tag, int threadId, long finished) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update "
                        + TABLE_NAME
                        + " set finished = ?"
                        + " where tag = ? and id = ? ",
                new Object[]{finished, tag, threadId});
    }

    public List<ThreadInfo> getThreadInfos(String tag) {
        List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where tag = ?",
                new String[]{tag});
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("id")));
            info.setTag(cursor.getString(cursor.getColumnIndex("tag")));
            info.setUri(cursor.getString(cursor.getColumnIndex("uri")));
            info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public List<ThreadInfo> getThreadInfos() {
        List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                , null);
        while (cursor.moveToNext()) {
            ThreadInfo info = new ThreadInfo();
            info.setId(cursor.getInt(cursor.getColumnIndex("id")));
            info.setTag(cursor.getString(cursor.getColumnIndex("tag")));
            info.setUri(cursor.getString(cursor.getColumnIndex("uri")));
            info.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            info.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            info.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public boolean exists(String tag, int threadId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "
                        + TABLE_NAME
                        + " where tag = ? and id = ?",
                new String[]{tag, threadId + ""});
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }

}
