package com.leo.download;

/**
 * 下载相关常量
 */
public class DownloadConst {
    /**
     * 下载错误常量
     */
    public static class Error{
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 0;
        /**
         * 失败
         */
        public static final int FAIL = 1;
        /**
         * 无响应
         */
        public static final int RESP_NULL = 2;
        /**
         * 空间不足
         */
        public static final int INSUFFICIENT_SPACE = 3;
        /**
         * 文件不完整
         */
        public static final int FILE_INCOMPLETE = 4;
        /**
         * IO异常
         */
        public static final int IO_EXCEPTION = 5;
    }

    /**
     * 下载状态常量
     */
    public static class Status{
        /**
         * 开始
         */
        public static final int START = 1;
        /**
         * 暂停
         */
        public static final int PAUSE = 2;
        /**
         * 结束
         */
        public static final int FINISH = 3;
        /**
         * 失败
         */
        public static final int FAIL = 4;
    }
}
