package com.leo.download;

/**
 * 下载监听
 */
public interface DownloadListener {
    /**
     * 开始接收文件
     * @param id    任务id
     * @param size  文件大小
     */
    void onStart(int id, long size);

    /**
     * 下载进度
     * @param id    任务id
     * @param currSize  文件当前大小
     * @param totalSize 文件总大小
     */
    void onProgress(int id, long currSize, long totalSize);

    /**
     * 重新开始
     * @param id    任务id
     * @param currSize  文件当前大小
     * @param totalSize 文件总大小
     */
    void onRestart(int id, long currSize, long totalSize);

    /**
     * 任务暂停
     * @param id    任务id
     * @param currSize  当前大小
     */
    void onPause(int id, long currSize);

    /**
     * 下载完成
     * @param id    任务id
     * @param dir   文件目录
     * @param name  文件名
     */
    void onComplete(int id, String dir, String name);

    /**
     * 取消
     * @param id    任务id
     */
    void onCancel(int id);

    /**
     * 下载出错
     * @param id    任务id
     * @param error 错误信息
     */
    void onError(int id, DownloadError error);
}
