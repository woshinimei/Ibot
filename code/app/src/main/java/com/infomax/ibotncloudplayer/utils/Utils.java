package com.infomax.ibotncloudplayer.utils;

import android.os.Build;
import android.os.SystemClock;

/**
 * Created by juying on 2016/11/12.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    /**
     * 获取本机 序列号 前13 位<br/>
     * @return
     * 1.真实开发逻辑代码 <br/>
     * 2.运行到手机上模拟序列号 001609240922B（测试ibotn账户）<br/>
     */
    public static String getDeviceSerial(){

        String tmp = Build.SERIAL;
        if (Constant.Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 0)
        {
            /////////////////////
            //运行到ibotn上面的真实开发逻辑代码
            if(tmp == null || tmp.length() < 13) {
                tmp = "01001010010400123456";
            }
            MyLog.d(TAG,"serial:"+tmp.substring(0, 13));
            return tmp.substring(0, 13);
            ////////////////////////

        }else  if (Constant.Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 1)
        {
            //////////////////
            //运行到手机上模拟序列号 001609240922B（相当于模拟测试ibotn账户001609240922B）。
            //最新使用号：0016112910030
            return "001609240922B";
            ///////////////////
        }

        return tmp;
    }

    private static long mLastCallTime = 0;
    /**
     * Preventing multiple clicks,run ,....
     * @return true 被阻止了
     */
    public static boolean preventMultipleRun(){

        long now = SystemClock.elapsedRealtime();
        if (now - mLastCallTime <= Constant.Config.INTERVAL) {
            return true;
        }
        mLastCallTime = now;
        return false;
    }
}