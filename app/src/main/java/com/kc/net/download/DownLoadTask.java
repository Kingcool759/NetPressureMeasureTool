package com.kc.net.download;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 11:13 AM
 */

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 异步下载任务
 */
public class DownLoadTask extends AsyncTask<String, Integer, Integer> {
    //四个常量表示下载状态：分别为成功，失败，暂停，取消。
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCLED = 3;

    private DownloadListener listener;
    private boolean isPaused = false;
    private boolean isCancled = false;
    private boolean isSuccess = false;
    private int lastProgress;

    //构造方法中传入我们定义的接口，待会就可以把下载的结果通过这个参数进行回调
    public DownLoadTask(DownloadListener listener) {
        this.listener = listener;
    }

    /**
     * 后台任务开始执行之前调用，用于进行一些界面上的初始化操作，如显示进度条。
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * 后台任务：
     * 子线程中执行耗时操作。任务完成可以用return语句来返回任务的结果。
     * 如果需要更新UI，可以调用 publishProgress();
     *
     * @param params 这里的参数就是根据我们制指定的泛型来的
     * @return
     */
    @Override
    protected Integer doInBackground(String... params) {
        InputStream inputStream = null;
        RandomAccessFile savedFile = null;//RandomAccessFile 是随机访问文件(包括读/写)的类
        File file = null;
        try {
            long downloadLength = 0;//记录已下载的文件的长度(默认为0)
            String downloadUrl = params[0];
            //截取下载的URL的最后一个"/"后面的内容，作为下载文件的文件名
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //将文件下载到sd卡的根目录下
//          String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(directory + fileName);

            if (file.exists()) {//判断文件是否已经存在
                downloadLength = file.length();//如果文件已经存在，读取文件的字节数。（这样后面能开启断点续传）
            }
            long contentLength = getContentLength(downloadUrl); //获取待下载文件的总长度
            if (contentLength == 0) {
                return TYPE_FAILED;//待下载文件字节数为0，说明文件有问题，直接返回下载失败。
            }
            else if (downloadLength == contentLength) {
                return TYPE_SUCCESS;//待下载文件字节数=已下载文件字节数，说明文件已经下载过。
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点续传，指定从哪个文件开始下载
                    .addHeader("RANGE", "bytes=" + downloadLength + "-")
                    .url(downloadUrl)
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {//返回数据不为空，则使用java文件流的方式，不断把数据写入到本地
                inputStream = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadLength);//断点续传--跳过已经下载的字节
                int total = 0;//记录此次下载的字节数，方便计算下载进度
                byte[] b = new byte[1024];
                int len;
                while ((len = inputStream.read(b)) != -1) {
                    //下载是一个持续过程，用户随时可能暂停下载或取消下载
                    //所以把逻辑放在循环中，在整个下载过程中随时进行判断
                    if (isCancled) {
                        return TYPE_CANCLED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        //计算已经下载到的百分比
                        int progress = (int) ((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCancled && file != null) {
                    file.delete();//如果已经取消，并且文件不为空，则删掉下载的文件
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    /**
     * 当在后台任务中调用了publishProgress()后，onProgressUpdate很快就会被执行。
     *
     * @param values 参数就是在后台任务中传过来的，这个方法中可以更新UI。
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    /**
     * 当后台任务执行完毕并调用return返回时，这个方法很快会被调用。返回的数据会被作为参数传到这个方法中
     * 可根据返回数据更新UI。提醒任务结果，关闭进度条等。
     *
     * @param integer
     */
    @Override
    protected void onPostExecute(Integer integer) {
        //把下载结果通过接口回调传出去
        switch (integer) {
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFaild();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCLED:
                listener.onCancled();
                break;
            default:
                break;

        }

    }

    //暂停下载
    public void pausedDownload() {
        isPaused = true;
    }

    //取消下载
    public void cancledDownload() {
        isCancled = true;
    }

    /**
     * 获取待下载文件的字节数
     *
     * @param downloadUrl
     * @return
     * @throws IOException
     */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}
