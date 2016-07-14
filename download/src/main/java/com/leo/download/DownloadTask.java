package com.leo.download;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载任务
 */
public class DownloadTask {

    /**
     * 内部存储路径
     */
    private static final String INTERNAL_DIR = "/data/data/";
    /**
     * 所有已开始的任务
     */
    private Map<String, Call> mCallMap;
    /**
     * 保证回调到主线程的Handler
     */
    private Handler mHandler;

    /**
     * 保证在主线程中实例化
     */
    public DownloadTask() {
        mCallMap = new HashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 开始任务
     *
     * @param url      下载地址
     * @param path     保存路径
     * @param rename   是否重命名
     * @param listener 下载监听
     */
    public void start(final String url, final String path, final boolean rename, final DownloadListener listener) {
        OkHttpClient client = new OkHttpClient();
        final File localFile = new File(path);
        final long range = localFile.length();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.tag(url);
        builder.addHeader("Connection", "Keep-Alive");
        if (range > 0) {
            builder.addHeader("Range", "bytes=" + range + "-");
        }
        Request request = builder.build();
        Call call = client.newCall(request);
        mCallMap.put(url, call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onError(-1, new DownloadError(DownloadConst.Error.FAIL, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null) {
                    sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.RESP_NULL, "empty reponse"));
                    return;
                }
                if (!response.isSuccessful() && !response.isRedirect()) {
                    sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.FAIL, response.message()));
                    return;
                }

                long fileLength = localFile.length();
                long totalLength = 0L;
                if (response.header("Content-Length") != null) {
                    totalLength = Long.parseLong(response.header("Content-Length"));
                }
                if (fileLength == 0) {
                    sendStartCallback(listener, -1, totalLength);
                } else {
                    if (isSupportRanges(response)) {
                        totalLength += fileLength;
                    }
                    sendRestartCallback(listener, -1, fileLength, totalLength);
                }
                if (path.startsWith(INTERNAL_DIR)) {
                    if (getAvailableInternalMemorySize() <= totalLength - fileLength) {
                        sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.INSUFFICIENT_SPACE, path));
                        return;
                    }
                } else {
                    if (getAvailableExternalMemorySize() <= totalLength - fileLength) {
                        sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.INSUFFICIENT_SPACE, path));
                        return;
                    }
                }

                try {
                    InputStream in;
                    RandomAccessFile out;
                    byte[] bytes = new byte[2048];
                    int len;
                    in = new BufferedInputStream(response.body().byteStream());
                    out = new RandomAccessFile(path, "rwd");
                    if (isSupportRanges(response)) {
                        out.seek(range);
                    } else {
                        out.seek(0);
                    }
                    while ((len = in.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                        sendProgressCallback(listener, -1, localFile.length(), totalLength);
                    }
                    if (totalLength == 0) {
//                        if (localFile.length() != 0) {
//                            sendCompleteCallback(listener, -1, localFile.getParent(), localFile.getName());
//                        } else {
                            sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.FILE_INCOMPLETE, "file is incomplete"));
//                        }
                    } else {
                        if (localFile.length() != 0 && localFile.length() == totalLength) {
                            File resultFile = localFile;
                            if (rename) {
                                resultFile = renameFile(localFile, response);
                            }
                            sendCompleteCallback(listener, -1, resultFile.getParent(), resultFile.getName());
                        } else {
                            sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.FILE_INCOMPLETE, "file is incomplete"));
                        }
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                    Log.d("DownloadTask", "Socket closed");
//                    sendErrorCallback(listener, -1, new DownloadError(DownloadConst.Error.IO_EXCEPTION, response.message()));
                }
            }
        });
    }

    /**
     * 主线程调用onStart
     *
     * @param listener 监听
     * @param id       任务id
     * @param size     文件大小
     */
    private void sendStartCallback(final DownloadListener listener, final int id, final long size) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onStart(id, size);
                }
            }
        });
    }

    /**
     * 主线程调用onRestart
     *
     * @param listener  监听
     * @param id        任务id
     * @param currSize  当前大小
     * @param totalSize 总大小
     */
    private void sendRestartCallback(final DownloadListener listener, final int id, final long currSize, final long totalSize) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onRestart(id, currSize, totalSize);
                }
            }
        });
    }

    /**
     * 主线程调用onProgress
     *
     * @param listener  监听
     * @param id        任务id
     * @param currSize  当前大小
     * @param totalSize 总大小
     */
    private void sendProgressCallback(final DownloadListener listener, final int id, final long currSize, final long totalSize) {
        if(totalSize == 0){
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onProgress(id, currSize, totalSize);
                }
            }
        });
    }

    /**
     * 主线程调用onComplete
     *
     * @param listener 监听
     * @param id       任务id
     * @param dir      文件目录
     * @param name     文件名
     */
    private void sendCompleteCallback(final DownloadListener listener, final int id, final String dir, final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onComplete(id, dir, name);
                }
            }
        });
    }

    /**
     * 主线程调用onError
     *
     * @param listener      监听
     * @param id            任务id
     * @param downloadError 错误信息
     */
    private void sendErrorCallback(final DownloadListener listener, final int id, final DownloadError downloadError) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onError(id, downloadError);
                }
            }
        });
    }

    /**
     * 主线程调用onPause
     *
     * @param listener 监听
     * @param id       任务id
     * @param currSize 当前大小
     */
    private void sendPauseCallback(final DownloadListener listener, final int id, final long currSize) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onPause(id, currSize);
                }
            }
        });
    }

    /**
     * 主线程调用onCancel
     *
     * @param listener 监听
     * @param id       任务id
     */
    private void sendCancelCallback(final DownloadListener listener, final int id) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onCancel(id);
                }
            }
        });
    }

    /**
     * 判断是否支持断点续传
     *
     * @param resp 响应体
     * @return 是否支持
     */
    private boolean isSupportRanges(Response resp) {
        if (resp == null) {
            return false;
        } else {
            String header = resp.header("Accept-Ranges");
            if (!TextUtils.isEmpty(header)) {
                return "bytes".equals(header);
            } else {
                header = resp.header("Content-Range");
                return !TextUtils.isEmpty(header) && header.startsWith("bytes");
            }
        }
    }

    /**
     * 重命名文件
     *
     * @param localFile 本地文件
     * @param response  响应体
     * @return 重命名后的文件
     */
    private File renameFile(File localFile, Response response) {
        String disposition = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(disposition)) {
            int startIndex = disposition.indexOf("filename=");
            if (startIndex > 0) {
                startIndex += "filename=".length();
                int endIndex = disposition.indexOf(";", startIndex);
                if (endIndex < 0) {
                    endIndex = disposition.length();
                }
                if (endIndex > startIndex) {
                    try {
                        String newFileName = URLDecoder.decode(disposition.substring(startIndex, endIndex), "UTF-8");
                        File newFile = new File(localFile.getParent(), newFileName);
                        return localFile.renameTo(newFile) ? newFile : localFile;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return localFile;
    }

    /**
     * 暂停
     *
     * @param url      下载地址
     * @param path     文件路径
     * @param listener 监听
     */
    public void pause(String url, String path, DownloadListener listener) {
        if (mCallMap != null && mCallMap.containsKey(url)) {
            Call call = mCallMap.get(url);
            if (call != null) {
                call.cancel();
            }
        }
        sendPauseCallback(listener, -1, new File(path).length());
    }

    /**
     * 取消
     *
     * @param url      下载地址
     * @param listener 监听
     */
    public void cancel(String url, DownloadListener listener) {
        if (mCallMap != null && mCallMap.containsKey(url)) {
            Call call = mCallMap.get(url);
            if (call != null) {
                call.cancel();
            }
        }
        sendCancelCallback(listener, -1);
    }

    /**
     * 剩余内部存储空间大小
     *
     * @return 空间大小
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        } else {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        }
        return availableBlocks * blockSize;
    }

    /**
     * 剩余外部存储空间大小
     *
     * @return 空间大小
     */
    public static long getAvailableExternalMemorySize() {
        if (hasSDCard()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize;
            long availableBlocks;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            } else {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            }
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 是否装载SD卡
     *
     * @return 是否装载
     */
    public static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
