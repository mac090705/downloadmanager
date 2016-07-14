package com.leo.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载数据库操作
 */
public class DownloadDbHelper extends SQLiteOpenHelper {
    /**
     * 数据库工具实例
     */
    private static DownloadDbHelper sInstance;
    /**
     * 数据库名
     */
    private static final String DB_NAME = "download.db";
    /**
     * 数据库版本
     */
    private static final int DB_VERSION = 1;
    /**
     * 表名
     */
    private static final String TABLE_NAME = "downloads";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_URL = "url";
    private static final String COLUMN_DIR = "dir";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CURR = "curr_size";
    private static final String COLUMN_TOTAL = "total_size";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESC = "desc";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_START = "start_time";
    private static final String COLUMN_FINISH = "finish_time";
    private static final String COLUMN_RENAME = "rename";
    private static final String COLUMN_MIMETYPE = "mimetype";

    /**
     * 建表SQL
     */
    private static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER primary key autoincrement," +
            COLUMN_URL + " TEXT," +
            COLUMN_DIR + " TEXT," +
            COLUMN_NAME + " TEXT," +
            COLUMN_CURR + " INTEGER," +
            COLUMN_TOTAL + " INTEGER," +
            COLUMN_TITLE + " TEXT," +
            COLUMN_DESC + " TEXT," +
            COLUMN_STATUS + " INTEGER," +
            COLUMN_START + " INTEGER," +
            COLUMN_FINISH + " INTEGER," +
            COLUMN_RENAME + " INTEGER," +
            COLUMN_MIMETYPE + " TEXT" + ")";

    private DownloadDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 获取单例
     *
     * @param context 上下文对象
     * @return 数据库操作单例
     */
    public static DownloadDbHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadDbHelper.class) {
                if (sInstance == null) {
                    sInstance = new DownloadDbHelper(context, DB_NAME, null, DB_VERSION);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 查询下载任务
     *
     * @param key   字段名
     * @param value 字段值
     * @return 下载任务
     */
    public DownloadInfo queryFirst(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cur = db.query(TABLE_NAME, new String[]{"*"}, key + "=?", new String[]{value}, "", "", "");
        if (cur != null) {
            if (cur.moveToFirst()) {
                DownloadInfo info = convert(cur);
                cur.close();
                return info;
            }
        }
        return null;
    }

    /**
     * 查询所有下载任务
     *
     * @return 任务列表
     */
    public List<DownloadInfo> queryAll() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cur = db.query(TABLE_NAME, new String[]{"*"}, null, null, null, null, null);
        List<DownloadInfo> infos = new ArrayList<>();
        if (cur != null) {
            while (cur.moveToNext()) {
                DownloadInfo info = convert(cur);
                infos.add(info);
            }
            cur.close();
        }
        return infos;
    }

    /**
     * 插入任务
     *
     * @param info 任务信息
     * @return 主键id
     */
    public int insert(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        int id = (int) db.insert(TABLE_NAME, null, convert(info));
        info.setId(id);
        return id;
    }

    /**
     * 更新
     *
     * @param info 任务信息
     */
    public void update(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, convert(info), "id=?", new String[]{String.valueOf(info.getId())});
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * 删除
     *
     * @param url 下载地址
     */
    public void delete(String url) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "url=?", new String[]{url});
    }

    /**
     * 更新任务状态
     *
     * @param status 状态
     * @param url    下载地址
     */
    public void updateStatus(int status, String url) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, values, "url=?", new String[]{url});
    }

    /**
     * 将DownloadInfo转换为ContentValue
     *
     * @param info 下载任务
     * @return 可用于数据库操作的ContentValue
     */
    private ContentValues convert(DownloadInfo info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, info.getUrl());
        values.put(COLUMN_DIR, info.getDir());
        values.put(COLUMN_NAME, info.getName());
        values.put(COLUMN_CURR, info.getCurrSize());
        values.put(COLUMN_TOTAL, info.getTotalSize());
        values.put(COLUMN_TITLE, info.getTitle());
        values.put(COLUMN_DESC, info.getDescription());
        values.put(COLUMN_STATUS, info.getStatus());
        values.put(COLUMN_START, info.getStartTime());
        values.put(COLUMN_FINISH, info.getFinishTime());
        values.put(COLUMN_RENAME, info.isRename());
        values.put(COLUMN_MIMETYPE, info.getMimetype());
        return values;
    }

    /**
     * 从游标中取出任务信息
     *
     * @param cur 数据库查询游标
     * @return 任务信息
     */
    private DownloadInfo convert(Cursor cur) {
        DownloadInfo info = new DownloadInfo();
        info.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
        info.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
        info.setDir(cur.getString(cur.getColumnIndex(COLUMN_DIR)));
        info.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
        info.setCurrSize(cur.getLong(cur.getColumnIndex(COLUMN_CURR)));
        info.setTotalSize(cur.getLong(cur.getColumnIndex(COLUMN_TOTAL)));
        info.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
        info.setDescription(cur.getString(cur.getColumnIndex(COLUMN_DESC)));
        info.setStatus(cur.getInt(cur.getColumnIndex(COLUMN_STATUS)));
        info.setStartTime(cur.getLong(cur.getColumnIndex(COLUMN_START)));
        info.setFinishTime(cur.getLong(cur.getColumnIndex(COLUMN_FINISH)));
        info.setRename(cur.getInt(cur.getColumnIndex(COLUMN_RENAME)) == 1);
        info.setMimetype(cur.getString(cur.getColumnIndex(COLUMN_MIMETYPE)));
        return info;
    }
}
