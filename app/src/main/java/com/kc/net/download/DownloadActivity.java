package com.kc.net.download;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kc.net.R;

import static android.content.Context.WINDOW_SERVICE;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 11:18 AM
 */
public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private DownloadService.DownloadBinder downloadBinder;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            Log.d(TAG,"## onServiceConnected ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"## onServiceDisconnected ");
        }
    };

    private static String TAG = "DownloadActivity";
    // 下载王者荣耀apk （模拟大文件下载）
    public static String url = "https://d76963ea34ae129f220fbd90fe4514e4.dlied1.cdntips.net/godlied4.myapp.com/myapp/1104466820/cos.release-40109/10040714_com.tencent.tmgp.sgame_a1687512_3.71.1.8_noAfhq.apk?mkey=6195cd46dcb52568&f=0000&cip=220.181.3.157&proto=https&access_type=";
    // 显示实时网速，1s刷新率
    private final Handler mHnadler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                Log.d(TAG,"## handleMessage");
                WindowUtils.setNetworkSpeed(msg.obj.toString());
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        findViewById(R.id.btnStartDownload).setOnClickListener(this);
        findViewById(R.id.btnPauseDownload).setOnClickListener(this);
        findViewById(R.id.btnCancleDownload).setOnClickListener(this);

        requestRunPermissions();
        initService();
        if (!WindowUtils.isAddedView) {
            // 如果之前已经有了网速View则不添加，没有才添加View。
            WindowUtils.addPercentWindow();
        }
        new NetWorkSpeedUtils(this,mHnadler).startShowNetSpeed();
    }

    /**
     * 申请运行时权限
     */
    private void requestRunPermissions() {
        Log.d(TAG,"## requestRunPermissions");
        //运行时权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.d(TAG,"## case 1 权限申请");
        } else {
            Log.d(TAG,"## case 2 权限已具备 ");
        }
    }

    private void initService(){
        Intent intent = new Intent(this, DownloadService.class);
        // 8.0以后需要加
        intent.setComponent(new ComponentName(this, "com.kc.net.download.DownloadService"));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);//启动服务
//        }
        startService(intent);//启动服务
        bindService(intent, connection, BIND_AUTO_CREATE);//绑定服务
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (downloadBinder == null) {
            return;
        }
        switch(v.getId()){
            case R.id.btnStartDownload:
                downloadBinder.startDownload(url);
                break;
            case R.id.btnPauseDownload:
                downloadBinder.pausedDownload();
                break;
            case R.id.btnCancleDownload:
                downloadBinder.cancledDownload();
                WindowUtils.removeView();
                break;
            default :
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(DownloadActivity.this, "拒绝权限将无法使用程序！", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.d(TAG,"## permission 已获取到,开启下载服务");
            }
        }
        Log.d(TAG,"## onRequestPermissionsResult , requestCode = " + requestCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}

