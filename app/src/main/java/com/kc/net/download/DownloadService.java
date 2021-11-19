package com.kc.net.download;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 11:15 AM
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.kc.net.R;

import java.io.File;

/**
 * 用于下载的Service
 * Created by lmy on 2017/4/27.
 */

public class DownloadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private DownloadBinder mBinder=new DownloadBinder();
    private DownLoadTask downLoadTask;//要通过服务来下载，当然要在服务中创建下载任务并执行。
    private String downloadUrl;

    //创建一个下载的监听
    private DownloadListener listener = new DownloadListener() {
        //通知进度
        @Override
        public void onProgress(int progress) {
            //下载过程中不停更新进度
            getNotificationManager().notify(1, getNotification("正在下载...", progress));
        }

        //下载成功
        @Override
        public void onSuccess() {
            downLoadTask = null;
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载成功！", -1));

            // 下载成功后提示并删除文件重新下载，压测使用
            deleteFileAndReDownload();
        }

        //下载失败
        @Override
        public void onFaild() {
            downLoadTask = null;
            //下载失败时将前台服务通知关闭，并创建一个下载成功的通知
            getNotificationManager().notify(1, getNotification("下载失败！", -1));
        }

        //暂停下载
        @Override
        public void onPaused() {
            downLoadTask=null;
        }

        //取消下载
        @Override
        public void onCancled() {
            downLoadTask=null;
            stopForeground(true);
        }
    };

    /**
     * 代理对象：在这里面添加三个方法：
     * 开始下载，暂停下载，取消下载
     * 就可以在Activity中绑定Service，并控制Service来实现下载功能
     */
    class DownloadBinder extends Binder {
        //开始下载，在Activity中提供下载的地址
        public void startDownload(String url) {
            if (downLoadTask == null) {
                downLoadTask = new DownLoadTask(listener);
                downloadUrl = url;
                downLoadTask.execute(downloadUrl);
                startForeground(1, getNotification("正在下载...", 0));//开启前台通知
            }
        }

        //暂停下载
        public void pausedDownload() {
            if (downLoadTask != null) {
                downLoadTask.pausedDownload();
            }
        }

        //取消下载
        public void cancledDownload() {
            if (downLoadTask != null) {
                downLoadTask.cancledDownload();
            } else {
                if (downloadUrl != null) {
                    //取消下载时需要将下载的文件删除  并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getParent();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                }
            }
            WindowUtils.setPercentNum("已取消");
        }
    }

    /**
     * 删除文件再次下载，人造循环，压测使用
     */
    private void deleteFileAndReDownload(){
        Toast.makeText(App.getContext(),"下载已完成，将删除文件，进行重新下载……",Toast.LENGTH_LONG).show();
        mBinder.cancledDownload();
        mBinder.startDownload(DownloadActivity.url);
    }

    private Notification getNotification(String title, int progress) {
        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        // sdk版本 > 26 时，需要加channelid否则报错：android.app.RemoteServiceException: Bad notification for startForeground
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.huge);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.huge));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setChannelId(CHANNEL_ONE_ID); // sdk版本 > 26 时，需要加channelid，否则报错
        if (progress >= 0) {
            builder.setContentText(progress + "%");
            WindowUtils.setPercentNum(progress + "%");
            builder.setProgress(100, progress, false);//最大进度。当前进度。是否使用模糊进度
        }
        return builder.build();
    }

    //获取通知管理器
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
}
