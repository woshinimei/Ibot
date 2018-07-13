package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by jack_zou on 2016/11/24.
 */

public class VideoEncryptUtils {
    private static final String  TAG = VideoEncryptUtils.class.getSimpleName();
    private static int ENCRYPTION_LENGTH = 4096;  //default 4k
    private static String decodeVersion;    //
    private static final String ENCRYPTION_MARK = "tag:ibotn";   // 标志位总工16个字节长度，判断是视频文件加密Tag只需获取前9个字节
    private static String FLAG = "X";  //是否加密 标志位
    private static int MARK_LENGTH = 9;
    private static int MARK_SUM_LENGTH = 16;
    private static byte decode[] = new byte[ENCRYPTION_LENGTH];
    private static byte src[] = new byte[ENCRYPTION_LENGTH];
    public static final int IS_MARK_FILE = 1;    // 为标记文件
    public static final int IS_MARK_AND_ENCRYPTION_FILE = 2;   //为标记加密文件
    public static final int IS_NORMAL_FILE = 3;  //为标记解密文件
    /**
     * return
     * true : video file is Mark file
     * false : video file is not Mark file
     */
    public static int obtainFileState(File file) {
        byte mark[] = new byte[MARK_LENGTH];
        byte flag[] =new byte[1];
        RandomAccessFile inputFile =null;
        try {
            inputFile = new RandomAccessFile(file, "r");
            int offset = (int) inputFile.length() - MARK_SUM_LENGTH;
            inputFile.seek(offset);
            inputFile.read(mark);
            String markIbotn = new String(mark, "UTF-8");

            Log.d(TAG,"######## obtainFileState markIbotn:"+markIbotn);
            if (markIbotn.equals(ENCRYPTION_MARK)) {
                inputFile.seek(offset+10);
                inputFile.read(flag);
                FLAG = new String(flag, "UTF-8");
                Log.d(TAG,"########  obtainFileState   FLAG:"+FLAG);
                if (FLAG.equals("e")){
                    inputFile.seek(0);
                    inputFile.read(src);
                    return IS_MARK_AND_ENCRYPTION_FILE;
                }
                inputFile.seek(0);
                inputFile.read(decode);
                return IS_MARK_FILE;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputFile !=null){
                try {
                    inputFile.close();
                    inputFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG,"########  isEncryption false");

        return IS_NORMAL_FILE;
    }

    public static void EncryptVideoFile(File file){
        RandomAccessFile randomAccessFile =null;
        try {
            Log.d(TAG,"########  EncryptVideoFile   ");

            randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(0);
            for (int i=0 ; i <ENCRYPTION_LENGTH; i++){
                decode[i]^= 0x08;
            }
            randomAccessFile.write(decode);
            randomAccessFile.seek(randomAccessFile.length()- 6);
            randomAccessFile.write("e".getBytes("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile !=null){
                try {
                    randomAccessFile.close();
                    randomAccessFile=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void DecodeVideoFile(File file){
        RandomAccessFile randomAccessFile =null;
        try {
            randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.seek(0);
            for (int i=0 ; i <ENCRYPTION_LENGTH; i++){
                src[i]^= 0x08;
            }
            Log.d(TAG,"########  DecodeVideoFile   ");

            randomAccessFile.write(src);
            randomAccessFile.seek(randomAccessFile.length()- 6);
            randomAccessFile.write("d".getBytes("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile !=null){
                try {
                    randomAccessFile.close();
                    randomAccessFile=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     *  播放前对加密视频文件的处理
     */
    public synchronized static void  processVideoEncryptFunction(Context context,String filePath){
        Log.i(TAG,"processVideoEncryptFunction path is "+filePath);
        File file = new File(filePath);
        int flag = VideoEncryptUtils.obtainFileState(file);
        if (flag == IS_MARK_AND_ENCRYPTION_FILE){
            VideoEncryptUtils.DecodeVideoFile(file);
            SharedPreferenceUtils.setVideoPath(context,filePath);
        }else if (flag == IS_MARK_FILE){
            SharedPreferenceUtils.setVideoPath(context,filePath);
        }
    }

    /**
     * 对解密后的视频文件加密处理
     */
    public synchronized static void  toEncryptVideoForDecryptedVideo(Context context,String filePath){
        Log.i(TAG,"toEncryptVideoForDecryptedVideo path is "+filePath);
        File file = new File(filePath);
        int flag = VideoEncryptUtils.obtainFileState(file);
        if (flag == IS_MARK_AND_ENCRYPTION_FILE){
            SharedPreferenceUtils.setVideoPath(context,filePath);
        }else if (flag == IS_MARK_FILE){
            EncryptVideoFile(file);
            SharedPreferenceUtils.setVideoPath(context, filePath);
        }else if (flag == IS_NORMAL_FILE){
            SharedPreferenceUtils.setVideoPath(context, filePath);
        }
    }
}