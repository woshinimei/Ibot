package com.infomax.ibotncloudplayer.utils;

import android.app.Activity;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by jy on 2017/3/20.
 * 下载工具类。
 */
public class OkHttpDownLoadUtils {
    private static String TAG = OkHttpDownLoadUtils.class.getSimpleName();
    /**
     * 使用OkHttpUtils 下载文件<br/>
     * @param url  下载url
     * @param destFileDir 存储目录
     * @param destFileName 存储文件名
     */
    public static void downloadFile(String url, final String destFileDir, final String destFileName){
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new FileCallBack(destFileDir, destFileName) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        MyLog.d(TAG, TAG + ">>>onBefore()>>>:");
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        super.inProgress(progress, total, id);

//                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>inProgress()>>>thread-name:" + Thread.currentThread().getName()
//                                        + "\n progress:" + progress
//                                        + "\n total:" + total
//                        );
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        //下载失败,或取消请求，要删除不完整的文件
                        FileUtils.deleteFile(destFileDir + File.separator + destFileName);
                    }

                    @Override
                    public void onResponse(File response, int id) {

                        MyLog.d(TAG, TAG + ">>>onResponse()>>>File:" + response.getAbsolutePath());
                    }
                });
    }
}
