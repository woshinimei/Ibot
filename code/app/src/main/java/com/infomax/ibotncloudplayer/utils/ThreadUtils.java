package com.infomax.ibotncloudplayer.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by jy on 2017/3/2 ;15:32.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 */
public class ThreadUtils {
    /**
     * 在子线程运行方法
     * @param runnable
     */
    public static void runOnBackThread(Runnable runnable){
        new Thread(runnable).start();
    }

    /**
     * @param runnable
     */
    public static void runOnUIThread(Runnable runnable){
        mHandler.post(runnable);
    }
    /**
     *
     * @param runnable
     * @param delay
     */
    public static void runOnUIThreadDelay(Runnable runnable, long delay){
        mHandler.postDelayed(runnable,delay);
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper());
}