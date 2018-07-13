package com.wifidirect;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by admin on 2017/5/5.
 */
public class WifiDirectConstant {

    /**
     * extra file path
     */
    public static String EXTRAS_FILE_PATH = "file_url";
    /**  file path  **/
    public static String FILE_PATH;

    /** multiply files path list */
    public static LinkedList<String> FILE_PATHS = new LinkedList<>();

    /**
     * this application run on ibotn device,runOnIbotnDevice is true,the wifidirect as client  ;false ,run on mobile phone。
     */
    public static final boolean RUN_ON_IBOTN_DEVICE = true;
    /**
     * 群组Type
     */
    public static final String GROUP_TYPE = "GROUP_TYPE";
    /**
     * 服务端为接收端
     */
    public static final String GROUP_SERVER = "GROUP_SERVER";
    /**
     * 客户端为发送端
     */
    public static final String GROUP_CLIENT = "GROUP_CLIENT";
    /** false : client ,will send file ;true :server ,will receieve file  */
    public static boolean isReceiveServer = false;

    /** the port for socket connection 。note : client and server should use the them port */
    public static final int DEFAUT_PORT = 8988;

    /** default true;false can deal  */
    public static boolean isCancel = true;
    /** 正在wifidirect图片集合；如果有内容，长按图片不再执行操作 */
    public static ConcurrentLinkedQueue clqImageWifiDirect = new ConcurrentLinkedQueue();
    /** 正在wifidirect视频集合；如果有内容，长按视频不再执行操作 */
    public static ConcurrentLinkedQueue clqVideoWifiDirect = new ConcurrentLinkedQueue();
    public static class Config {

        /**
         * wifidirect，ROOT_PATH_OF_WIFIDIRECT_IMAGE_STORAGE
         */
        public static String ROOT_PATH_OF_WIFIDIRECT_IMAGE_STORAGE = File.separator + "IBOTN_DATA" + File.separator + "WIFIDIRECT" + File.separator + "IMAGE" + File.separator;
        /**
         * wifidirect，ROOT_PATH_OF_WIFIDIRECT_VIDEO_STORAGE
         */
        public static String ROOT_PATH_OF_WIFIDIRECT_VIDEO_STORAGE = File.separator + "IBOTN_DATA" + File.separator + "WIFIDIRECT" + File.separator + "VIDEO" + File.separator;

    }
}
