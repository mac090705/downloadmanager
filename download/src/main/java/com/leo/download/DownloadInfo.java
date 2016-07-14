package com.leo.download;

import android.text.TextUtils;

import java.io.Serializable;
import java.net.URLConnection;

/**
 * 任务信息
 */
public class DownloadInfo implements Serializable{
    /**
     * 任务自增id
     */
    private int id;
    /**
     * 下载地址
     */
    private String url;
    /**
     * 文件保存目录
     */
    private String dir;
    /**
     * 文件名
     */
    private String name;
    /**
     * 已下载大小
     */
    private long currSize;
    /**
     * 文件总大小
     */
    private long totalSize;
    /**
     * 下载标题
     */
    private String title;
    /**
     * 下载描述
     */
    private String description;
    /**
     * 是否重命名
     */
    private boolean rename = false;
    /**
     * 文件MIME
     */
    private String mimetype;
    /**
     * 开始时间戳
     */
    private long startTime;
    /**
     * 结束时间戳
     */
    private long finishTime;
    /**
     * 下载状态
     */
    private int status;

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

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCurrSize() {
        return currSize;
    }

    public void setCurrSize(long currSize) {
        this.currSize = currSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRename() {
        return rename;
    }

    public void setRename(boolean rename) {
        this.rename = rename;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DownloadInfo() {
    }

    public DownloadInfo(Builder builder) {
        setUrl(builder.url);
        String path = builder.path;
        int index = path.lastIndexOf("/");
        setDir(path.substring(0, index));
        setName(path.substring(index + 1, path.length()));
        setRename(builder.rename);
        setTitle(builder.title);
        setDescription(builder.description);
        setMimetype(getMimetype(getName()));
    }

    private String getMimetype(String name){
        String mimetype = URLConnection.getFileNameMap().getContentTypeFor(name);
        if(TextUtils.isEmpty(mimetype)){
            mimetype = "application/octet-stream";
        }
        return mimetype;
    }

    /**
     * 下载任务Builder
     */
    public static class Builder{
        /**
         * 下载地址
         */
        private String url;
        /**
         * 文件保存路径
         */
        private String path;
        /**
         * 是否重命名
         */
        private boolean rename = false;
        /**
         * 下载标题
         */
        private String title;
        /**
         * 下载描述
         */
        private String description;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder rename(boolean rename) {
            this.rename = rename;
            return this;
        }

        public Builder title(String title){
            this.title = title;
            return this;
        }

        public Builder description(String description){
            this.description = description;
            return this;
        }

        public DownloadInfo build(){
            return new DownloadInfo(this);
        }
    }
}
