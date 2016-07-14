package com.leo.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.util.List;

/**
 * 下载管理，下载功能入口
 */
public class DownloadManager {
    private static final String TAG = "Download";
    /**
     * 下载管理单例
     */
    private static DownloadManager sInstance;
    /**
     * 数据库单例
     */
    private DownloadDbHelper mDbHelper;
    /**
     * 下载任务
     */
    private DownloadTask mTask;

    /**
     * 构造方法
     *
     * @param context 上下文对象
     */
    private DownloadManager(Context context) {
        mDbHelper = DownloadDbHelper.getInstance(context);
        mTask = new DownloadTask();
    }

    /**
     * 获取单例
     *
     * @param context 上下文对象
     * @return 下载管理单例
     */
    public static DownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * 开始续传，会根据下载状态进行判断开始/暂停
     *
     * @param info     下载信息
     * @param listener 下载监听
     */
    private void enquene(DownloadInfo info, DownloadListener listener) {
        if (!isUrlValid(info.getUrl())) {
            return;
        }
        DownloadInfo localInfo = mDbHelper.queryFirst(DownloadDbHelper.COLUMN_URL, info.getUrl());
        if (localInfo == null) {
            deleteFile(info.getDir() + File.separator + info.getName());
            start(info, listener);
        } else {
            File file = new File(localInfo.getDir() + File.separator + localInfo.getName());
            if(localInfo.getTotalSize() == 0){
                restart(localInfo, listener);
                return;
            }
            if (file.length() >= localInfo.getTotalSize()) {
                new DownloadListenerWrapper(localInfo, listener).onComplete(localInfo.getId(),
                        localInfo.getDir(), localInfo.getName());
            } else {
                switch (localInfo.getStatus()) {
                    case DownloadConst.Status.START:
                        _pause(info.getUrl(), listener);
                        break;
                    case DownloadConst.Status.PAUSE:
                        restart(localInfo, listener);
                        break;
                    case DownloadConst.Status.FAIL:
                        restart(localInfo, listener);
                        break;
                    case DownloadConst.Status.FINISH:
                        new DownloadListenerWrapper(localInfo, listener).onComplete(localInfo.getId(),
                                localInfo.getDir(), localInfo.getName());
                        break;
                }
            }
        }
    }

    /**
     * 开始下载
     *
     * @param info     下载信息
     * @param listener 下载监听
     */
    private void start(DownloadInfo info, DownloadListener listener) {
        mDbHelper.insert(info);
        mTask.start(info.getUrl(), info.getDir() + File.separator + info.getName(),
                info.isRename(), new DownloadListenerWrapper(info, listener));
    }

    /**
     * 重新开始下载
     *
     * @param info     下载信息
     * @param listener 下载监听
     */
    private void restart(DownloadInfo info, DownloadListener listener) {
        mTask.start(info.getUrl(), info.getDir() + File.separator + info.getName(),
                info.isRename(), new DownloadListenerWrapper(info, listener));
    }

    /**
     * 暂停下载
     *
     * @param url      下载地址
     * @param listener 下载监听
     */
    private void _pause(String url, DownloadListener listener) {
        if (!isUrlValid(url)) {
            return;
        }
        DownloadInfo info = mDbHelper.queryFirst("url", url);
        if (info == null) {
            return;
        }
        mTask.pause(url, info.getDir() + File.separator + info.getName(), new DownloadListenerWrapper(info, listener));
    }

    /**
     * 取消下载
     *
     * @param url      下载地址
     * @param listener 下载监听
     */
    private void _cancel(String url, DownloadListener listener) {
        if (!isUrlValid(url)) {
            return;
        }
        DownloadInfo info = mDbHelper.queryFirst("url", url);
        if (info == null) {
            return;
        }
        mTask.cancel(url, new DownloadListenerWrapper(info, listener));
    }

    /**
     * 是否是有效的url
     *
     * @param url 要判断的url
     * @return 是否有效
     */
    private boolean isUrlValid(String url) {
        return !TextUtils.isEmpty(url) && URLUtil.isNetworkUrl(url);
    }

    /**
     * 下载监听的包装类，回调中先进行数据库操作，再回调到主线程
     */
    private class DownloadListenerWrapper implements DownloadListener {
        private DownloadInfo mDownloadInfo;
        private DownloadListener mListener;

        public DownloadListenerWrapper(DownloadInfo downloadInfo, DownloadListener listener) {
            mDownloadInfo = downloadInfo;
            mListener = listener;
        }

        @Override
        public void onStart(int id, long size) {
            log("Start->id:" + mDownloadInfo.getId() + " size:" + size);
            mDownloadInfo.setStatus(DownloadConst.Status.START);
            mDownloadInfo.setStartTime(System.currentTimeMillis());
            mDownloadInfo.setTotalSize(size);
            mDbHelper.update(mDownloadInfo);
            if (mListener != null)
                mListener.onStart(mDownloadInfo.getId(), size);
        }

        @Override
        public void onProgress(int id, long currSize, long totalSize) {
            log("Progress->id:" + mDownloadInfo.getId() + " curr:" + currSize + " total:" + totalSize);
            if (mListener != null)
                mListener.onProgress(mDownloadInfo.getId(), currSize, totalSize);
        }

        @Override
        public void onRestart(int id, long currSize, long totalSize) {
            log("Restart->id:" + mDownloadInfo.getId() + " curr:" + currSize + " total:" + totalSize);
            mDownloadInfo.setStatus(DownloadConst.Status.START);
            mDownloadInfo.setCurrSize(currSize);
            mDownloadInfo.setTotalSize(totalSize);
            mDbHelper.update(mDownloadInfo);
            if (mListener != null)
                mListener.onRestart(mDownloadInfo.getId(), currSize, totalSize);
        }

        @Override
        public void onPause(int id, long currSize) {
            log("Pause->id:" + mDownloadInfo.getId() + " curr:" + currSize);
            mDownloadInfo.setStatus(DownloadConst.Status.PAUSE);
            mDownloadInfo.setCurrSize(currSize);
            mDbHelper.update(mDownloadInfo);
            if (mListener != null)
                mListener.onPause(mDownloadInfo.getId(), currSize);
        }

        @Override
        public void onComplete(int id, String dir, String name) {
            log("Complete->id:" + mDownloadInfo.getId() + " dir:" + dir + " name:" + name);
            mDownloadInfo.setFinishTime(System.currentTimeMillis());
            mDownloadInfo.setStatus(DownloadConst.Status.FINISH);
            mDownloadInfo.setCurrSize(mDownloadInfo.getTotalSize());
            mDbHelper.update(mDownloadInfo);
            if (mListener != null)
                mListener.onComplete(mDownloadInfo.getId(), dir, name);
        }

        @Override
        public void onCancel(int id) {
            log("Cancel->id:" + mDownloadInfo.getId());
            mDbHelper.delete(mDownloadInfo.getId());
            deleteFile(mDownloadInfo.getDir() + File.separator + mDownloadInfo.getName());
            if (mListener != null)
                mListener.onCancel(mDownloadInfo.getId());
        }

        @Override
        public void onError(int id, DownloadError error) {
            log("Error->id:" + id + " error:" + error.getCode());
            mDownloadInfo.setStatus(DownloadConst.Error.FAIL);
            mDbHelper.update(mDownloadInfo);
            if (mListener != null)
                mListener.onError(mDownloadInfo.getId(), error);
        }
    }

    private void deleteFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists())
                return;
            boolean ret = file.delete();
            if (!ret) {
                Log.v("downloadmanager", "delete file failed");
            }
        }
    }

    private void log(String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.d(TAG, "-> " + message);
    }


    /**
     * 开始下载
     *
     * @param url      下载地址
     * @param path     保存地址
     * @param rename   是否重命名
     * @param listener 下载监听
     */
    public void start(String url, String path, boolean rename, DownloadListener listener) {
        _cancel(url, listener);//停止
        deleteFile(path);//删文件
        DownloadInfo localInfo = mDbHelper.queryFirst(DownloadDbHelper.COLUMN_URL, url);
        if (localInfo != null) {
           mDbHelper.delete(url);//删记录
        }
        DownloadInfo info = new DownloadInfo.Builder()
                .url(url)
                .path(path)
                .rename(rename)
                .build();//创建新对象
        start(info, listener);//下载
    }

    /**
     * 开始下载
     *
     * @param url      下载地址
     * @param path     保存地址
     * @param listener 下载监听
     */
    public void start(String url, String path, DownloadListener listener) {
        start(url, path, false, listener);
    }

    /**
     * 开始续传
     *
     * @param url      下载地址
     * @param path     保存地址
     * @param rename   是否重命名
     * @param listener 下载监听
     */
    public void enquene(String url, String path, boolean rename, DownloadListener listener) {
        DownloadInfo info = new DownloadInfo.Builder()
                .url(url)
                .path(path)
                .rename(rename)
                .build();
        enquene(info, listener);
    }

    /**
     * 开始续传
     *
     * @param url      下载地址
     * @param path     保存地址
     * @param listener 下载监听
     */
    public void enquene(String url, String path, DownloadListener listener) {
        enquene(url, path, false, listener);
    }

    /**
     * 暂停下载
     *
     * @param url      下载地址
     * @param listener 下载监听
     */
    public void pause(String url, DownloadListener listener) {
        _pause(url, listener);
    }

    /**
     * 取消下载
     *
     * @param url      下载地址
     * @param listener 下载监听
     */
    public void cancel(String url, DownloadListener listener) {
        _cancel(url, listener);
    }

    /**
     * 查询下载任务
     *
     * @param id 任务id
     * @return 下载信息
     */
    public DownloadInfo query(int id) {
        return mDbHelper.queryFirst(DownloadDbHelper.COLUMN_ID, String.valueOf(id));
    }

    /**
     * 查询下载任务
     *
     * @param url 下载地址
     * @return 下载信息
     */
    public DownloadInfo query(String url) {
        return mDbHelper.queryFirst(DownloadDbHelper.COLUMN_URL, url);
    }

    /**
     * 查询所有下载任务
     *
     * @return 下载任务列表
     */
    public List<DownloadInfo> queryAll() {
        return mDbHelper.queryAll();
    }

    /**
     * 删除任务记录
     *
     * @param url 下载地址
     */
    public void deleteRecord(String url) {
        mDbHelper.delete(url);
    }

    /**
     * 更新任务状态
     *
     * @param status 下载状态
     * @param url    下载地址
     */
    public void updateStatus(int status, String url) {
        mDbHelper.updateStatus(status, url);
    }

    public boolean isDownloading(String url) {
        DownloadInfo info = mDbHelper.queryFirst(DownloadDbHelper.COLUMN_URL, url);
        return info != null && info.getStatus() == DownloadConst.Status.START;
    }

}
