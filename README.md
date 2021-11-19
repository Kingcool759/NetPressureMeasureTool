## 网络压测工具

#### 实现思路

```
在Activity中绑定Service，并通过DownloadBinder类控制Service来实现下载功能，在Service类中通过DownloadListener实现监听。
```


#### 具体做法

- **下载文件**
  ```
  下载状态监听接口：DownloadListener
  用于下载的服务：DownloadService
  异步下载任务：DownLoadTask
  代理类管理Service：DownloadBinder
  可视化界面管理：DownloadActivity 
  ```
- **循环下载**
  ```
  [可作为压测工具]
  DownloadListener中在监听文件下载成功后，删除文件，通过DownloadBinder重新开启下载任务
  ```
- **支持断点续传**
  ```
  DownloadListener中在监听文件下载成功后，删除文件，通过DownloadBinder重新开启下载任务
  ```
- **可视化界面**
  ```
  WindowUtils.addPercentWindow()通过windowManager进行增加View，后台也可显示运行。
  ```
- **显示下载进度**
  ```
  通知管理任务栏：DownloadService.getNotification()
  ```
- **检测实时网速**
  ```
  实时网速测算：NetWorkSpeedUtils.class
  ```
