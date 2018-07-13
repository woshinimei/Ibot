package com.infomax.ibotncloudplayer.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by jy on 2017/3/20.
 */
public class SDUtils {
    private boolean existSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * 获取sd卡剩余空间
     * @return
     */
    public static long getSDFreeSize(){
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long freeBlocks = sf.getAvailableBlocks();
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * 获取指定目录下的剩余空间
     * @param rootPath
     * @return 单位MB
     */
    public static long getSDFreeSize(String rootPath){

        if (FileUtils.isDir(rootPath))
        {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(rootPath);
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            //return freeBlocks * blockSize;  //单位Byte
            //return (freeBlocks * blockSize)/1024;   //单位KB
            return (freeBlocks * blockSize)/1024 /1024; //单位MB
        }
        return 0;
    }

    /**
     * 获取sd卡总空间
     * @return 单位MB
     */
    public static long getSDAllSize(){
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long allBlocks = sf.getBlockCount();
        return (allBlocks * blockSize)/1024/1024;
    }

    /**
     *
     * @param rootPath
     * @return
     * 获取指定目录下的剩余空间
     */
    public static long getSDAllSize(String rootPath){
        if (FileUtils.isDir(rootPath)) {
            StatFs sf = new StatFs(rootPath);
            long blockSize = sf.getBlockSize();
            long allBlocks = sf.getBlockCount();
            return (allBlocks * blockSize) / 1024 / 1024;
        }
        return 0;
    }

    /**
     * sd卡是否挂载[是内置sd卡]
     * @return
     * Returns the current state of the primary "external" storage device.
     */
    public static boolean isSDMounted(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
