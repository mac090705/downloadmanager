package com.leo.download;

/**
 * 下载错误
 */
public class DownloadError {
    /**
     * 错误码
     */
    private int code;
    /**
     * 错误信息
     */
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DownloadError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[" + code + "_" + message + "]";
    }
}
