# DownloadManager
基于OkHttp实现的断点下载工具。使用SQLite来记录断点。

##截图
![](https://github.com/mac090705/downloadmanager/blob/master/screenshots/device-2016-07-14-163020.png)

![](https://github.com/mac090705/downloadmanager/blob/master/screenshots/device-2016-07-14-163048.png)

## 使用
###普通下载
```java
DownloadManager.getInstance(context).start(url, path, new DownloadListenerAdapter() {

            @Override
            public void onProgress(int id, long currSize, long totalSize) {
            }

            @Override
            public void onComplete(int id, String dir, String name) {
            }
        });
```
###断点续传
```java
DownloadManager.getInstance(context).enquene(url, path, new DownloadListener() {
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
            public void onComplete(int id, final String dir, final String name) {
            }

            @Override
            public void onCancel(int id) {
            }

            @Override
            public void onError(int id, DownloadError error) {
            }
        });
```
###暂停下载
```java
DownloadManager.getInstance(context).pause(url, new DownloadListenerAdapter(){

			@Override
            public void onPause(int id, long currSize) {
            }
})
```
###取消下载
```java
DownloadManager.getInstance(context).cancel(url, new DownloadListenerAdapter(){

			@Override
            public void onCancel(int id, long currSize) {
            }
})
```
###查看下载任务
```java
DownloadManager.getInstance(context).queryAll()
```
###下载回调
可以使用DownloadListener实现全部回调，如果觉得方法太多，也可以使用DownloadListenerAdapter这个抽象类选择要实现的回调。