package com.infomax.ibotncloudplayer.utils;

import android.util.Log;

/**
 * Created by jy on 2016/10/17.
 */
public class MyLog {
    public static void d(String tag,String str){
        if (Constant.Config.DEBUG){
            Log.d(tag,str);
        }
    }
    public static void e(String tag,String str){
        if (Constant.Config.DEBUG){
            Log.e(tag,str);
        }
    }
}
