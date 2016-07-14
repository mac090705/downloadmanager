package com.leo.download;

/**
 * Created by mal on 2016/7/14.
 */
public abstract class DownloadListenerAdapter implements DownloadListener {
    @Override
    public void onStart(int id, long size) {

    }

    @Override
    public void onProgress(int id, long currSize, long totalSize) {

    }

    @Override
    public void onRestart(int id, long currSize, long totalSize) {

    }

    @Override
    public void onPause(int id, long currSize) {

    }

    @Override
    public void onComplete(int id, String dir, String name) {

    }

    @Override
    public void onCancel(int id) {

    }

    @Override
    public void onError(int id, DownloadError error) {

    }
}
