package com.kc.net.download;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 11:11 AM
 * 回调接口，对下载状态进行监听
 */
public interface DownloadListener {
    void onProgress(int progress);//通知当前下载进度
    void onSuccess();//下载成功
    void onFaild();//下载失败
    void onPaused();//暂停下载
    void onCancled();//取消下载
}
