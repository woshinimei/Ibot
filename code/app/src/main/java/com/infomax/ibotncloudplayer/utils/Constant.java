package com.infomax.ibotncloudplayer.utils;

import com.infomax.ibotncloudplayer.MainActivity;

import java.io.File;

/**
 * Created by jy on 2016/10/25.<br/>
 * 该类配置了开关及用到的常量 <br/>
 */
public class Constant {

    public static class Config{

        /*****************************************/

        /**true为调试模式：比如可以控制打印log等；false 非调试模式*/
        public static final boolean DEBUG = true;

        /********************************************/

        /**触摸加载进度对话框外部可以取消该对话框**/
        public static final boolean CANCEL_DIALOG_PROGRESS = true;

        /**时间间隔 ms*/
        public static final  int INTERVAL = 1500;

        /**模拟系统文件加载时长,测试使用30s,发布的时候180s*/
        public static final  int SIMULATION_SYSTEM_FILE_INIT_FOR_LAUNCHER_COUNT_TIME = 60 * 2;
        /**
         * 教育内容-本地视频文件视频根路径。根据设配类型开关而定
         */
        public static final String Education_Content_Video_File_Root_Path =
                (Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 0 ?
                        (File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY"+File.separator+"VIDEO")
                        :(File.separator+"storage"+File.separator+"emulated"+File.separator+"0"+File.separator+"DCIM"+File.separator+"TEST" +File.separator+"VIDEO" )
                );
        /**
         * 教育内容-本地音频文件根路径。根据设配类型开关而定
         * '/storage/sd-ext/STUDY/AUDIO/%'
         */
        public static final String Education_Content_Audio_File_Root_Path =
                (Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 0 ?
                 (File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY"+File.separator+"AUDIO")
                 :(File.separator+"storage"+File.separator+"emulated"+File.separator+"0"+File.separator+"DCIM"+File.separator+"TEST" +File.separator+"AUDIO" )
                );

        /**
         * ibotn宣传图片文件夹路径,暂停使用 TODO
         */
        public static final String Ibotn_Img_Root_Path = File.separator+"storage"+File.separator+"sd-ext"+
                                                                        File.separator+"STUDY"+
                                                                        File.separator+"VIDEO"+
                                                                        File.separator+"ibotn简介";
        /**
         * 根目录是sd-ext的外置sd卡根目录
         */
        public static final String ROOT_PATH_FIRST_EXTERNAL_SD_SD_EXT = File.separator+"storage"+File.separator+"sd-ext";

        /** 写入sd卡时，大于该值时才可以写入。单位Mb */
        public static final long MAX_WRITE_SPACE_FOR_SD_MB = 1024 * 2;

        /**
         * 扫描指定目录,暂停使用 TODO
         */
        public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
        /**
         * 级别文件夹配置文件expand_authorityfolder.properties路径
         * "/assets/expand_authorityfolder.properties"
         */
        public static final String EXPAND_AUTHORITYFOLDER_PATH = File.separator+"assets"+File.separator+"expand_authorityfolder.properties";
        /**
         * 级别文件夹配置文件authorityfolder.properties 路径
         * "/assets/authorityfolder.properties"
         */
        public static final String AUTHORITYFOLDER_PATH = File.separator + "assets" + File.separator + "authorityfolder.properties";

        /**
         * 崩溃日志文件根目录 /storage/sd-ext/crash/APPCRASH/ <br/>
         * unused <br/>
         */
        public static final String ROOT_PATH_FOR_CRASH_LOG = File.separator+"storage"+File.separator+"sd-ext"
                                                                + File.separator+ "crash" + File.separator
                                                                + File.separator+ "APPCRASH";
        /** 崩溃日志文件根目录 /storage/sdcard/crash/APPCRASH/ */
        public static final String SUB_PATH_FOR_CRASH_LOG =  File.separator+ "crash" + File.separator
                + File.separator+ "APPCRASH";
        /**wifidirect，接受文件下载根目录*/
        public static String ROOT_PATH_FOR_WIFIDIRECT_STORAGE =
                (Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 0 ?
                        (File.separator+"storage" + File.separator + "sd-ext" + File.separator + "IBOTN" + File.separator + "WIFIDIRECT" + File.separator)
                        :(File.separator+"storage"+File.separator+"emulated"+File.separator+"0"+File.separator+"IBOTN_DATA" +File.separator+"WIFIDIRECT" + File.separator)
                );


        /**定时检查崩溃日志文件目录（ROOT_PATH_FOR_CRASH_LOG） 的时间间隔 测试可用10s。发布版本2m*/
        public static final long PERIOD_FOR_CHECK_CRASH_LOG_FILES_TIMER = 2* 60 * 1000;

        /**
         * 默认打开浏览器，加载地址
         */
        public static final String DEFAULT_URL_FOR_BROWSER = "http://www.ibotn.com/";
        public static final String DEFAULT_URL_FOR_BROWSER_BAIDU = "http://www.baidu.com";
    }

    /**
     * 腾讯云相关配置类
     */
    public static  class TengXunYun{

        /**  0存储到内置sd卡，1外部sd卡 */
        public static int PATH_STORAGE_TYPE_EXTERNALSD_OR_INTERNALDS = 1;

        /**腾讯云视频,图片，下载根目录根目录*/
        public static String ROOT_PATH_FOR_TENGXUYUN_VIDEO_STORAGE =
        (Toggle.TOGGLE_RUN_ON_DEVICE_TYPE == 0 ?
                (File.separator+"storage" + File.separator + "sd-ext" + File.separator + "IBOTN" + File.separator + "TENGXUNYUN")
                :(File.separator+"storage"+File.separator+"emulated"+File.separator+"0"+File.separator+"IBOTN_DATA"+File.separator+"IBOTNCLOUDPLAYER" +File.separator+"TENGXUNYUN"+File.separator+"VIDEO" )
        );

    }

    /**
     * 第三方包名类 PackageName
     */
    public static class ThirdPartAppPackageName {

        /**
         * 猫头鹰播放器应用包名
         */
        public static final String PACKAGE_NAME_MAOTOUYING = "com.actions.owlplayer";
//        public static final String PACKAGE_NAME_MAOTOUYING = "com.android.gallery3d";
        /** 益智游戏apk包名 */
        public static final String OTHER_APK_PACKAGENAME_SRCATCHJR = "org.scratchjr.android";
        /** 爱奇艺动画屋apk包名 */
        public static final String OTHER_APK_PACKAGENAME_QIYIVIDEO = "com.qiyi.video.child";
        /** 爱奇艺愤怒的小鸟apk包名 */
        public static final String OTHER_APK_PACKAGENAME_ANGRYBRID = "com.rovio.angrybirdsstarwarshd.premium.iap";
        /** 娃娃路apk包名 */
        public static final String OTHER_APK_PACKAGENAME_WAWALU = "com.zhitong.wawalooo.android.phone";
        /** FileExplorer apk包名com.actions.explorer */
        public static final String OTHER_APK_PACKAGENAME_FILE_EXPLORER = "com.actions.explorer";

        public static final String OTHER_APK_PACKAGENAME_ORGANIZED = "com.sinyee.babybus.organized";

        public static final String OTHER_APK_PACKAGENAME_BATHING = "com.sinyee.babybus.bathing";

        public static final String OTHER_APK_PACKAGENAME_CHEF = "com.sinyee.babybus.chef";

        public static final String OTHER_APK_PACKAGENAME_NUMBER = "com.sinyee.babybus.number";

        public static final String OTHER_APK_PACKAGENAME_BABYHOSPITAL = "com.sinyee.babybus.babyhospital";
        /**
         * android system browser package name
         */
        public static final String APK_PACKAGENAME_ANDROID_SYSTEM_BROWSER = "com.android.browser";
    }

    public static class MySharedPreference
    {
        public static final String SP_NAME = "sp_name_watch_list";

        /**
         * 用户使用onedrive注册或登录后，/data/data/com.infomax.ibotncloudplayer下面会有com.microsoft.live.xml sp文件
         */
        public static final String SP_NAME_ONEDRIVE_COM_MICROSOFT_LIVE = "com.microsoft.live";
        /**本地播放历史 sp key */
        public static final String SP_KEY_LOCAL_LIST = "history_local_list";
        /**本地音频播放历史 sp key */
        public static final String SP_KEY_LOCAL_AUDIO_LIST = "sp_key_local_audio_list";
        /**云端播放历史 sp key*/
        public static final String SP_KEY_CLOUD_LIST = "history_cloud_list";

        /**视频/音频播放历史保存的最大条数 sp size */
        public static final int SAVE_WATCH_HISTORY_SIZE = 5000;

        /**key。用户级别*/
        public static final String Ibotn_User_level = "ibotn_user_level";
        /**key。ibton 视频文件*/
        public static final String Ibotn_Video_File = "ibotn_video_file";

        /**key。语音拍照完成后自动上传开关*/
        public static final String TOGGLE_UPLOAD_PHOTO = "TOGGLE_UPLOAD_PHOTO";
    }

    public static class MyIntentProperties
    {
        /**云端播放历史 传递的 bundle的name*/
        public static final String NAME_KEY_01 = "com.infomax.ibotncloudplayer.myBundle";
        /**语音自动播放音乐 通过intent传递值 ，获取对应值为true*/
        public static final String START_CLOUDPLAYER_AUDIO_BY_VOICE = "START_CLOUDPLAYER_AUDIO_BY_VOICE";
        /**语音自动播放音乐 通过intent传递值 ，EXTRA对应文件夹*/
        public static final String EXTRA_FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "EXTRA_FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE";
        /**语音自动播放音乐时,获取对应extr后的值为true **/
        public static  boolean START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = false;

        /**语音自动播放视频 通过intent传递值 ，获取对应值为true*/
        public static final String START_CLOUDPLAYER_VIDEO_BY_VOICE = "START_CLOUDPLAYER_VIDEO_BY_VOICE";
        /**语音自动播放视频 通过intent传递值 ，EXTRA对应文件夹*/
        public static final String EXTRA_FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "EXTRA_FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE";
        /**语音自动播放视频时,获取对应extr后的值为true ,**/
        public static  boolean START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE = false;
        /**接收【语音拍照】完成后的广播  action**/
        public static  String ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO = "ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO";
        /**接收【语音拍照】完成后的广播,extra 该图片绝对路径**/
        public static  String EXTRA_PHOTO_PATH_FOR_VOICE_TAKE_PHOTO = "EXTRA_PHOTO_PATH_FOR_VOICE_TAKE_PHOTO";
    }
    /**
     * 开关工具类，配置各种开关选项
     */
    public static class Toggle{
        /**
         * 进入设置界面时是否需要回答问题才可进入，false不用回答问题直接进入，true需要回答问题才可进入。需手动修改该值
         */
        public static boolean TOGGLE_SHOW_DIALOG_ANSWER_QUESTION_FOR_ENTER_SETTING = false;

        /**
         * 第三方上传类型，（需手动修改该值）。1是使用qqcloud ;2 是使用 OneDriver；
         */
        public static  int TOGGLE_UPLOAD_TYPE_THIRD_PATY_TYPE = 2;

        /** 运行到设备类型类型，默认0为ibotn上面，1为我的手机上面。 不同设备，音频、视频、图片等路径不同哦*/
        public static final int TOGGLE_RUN_ON_DEVICE_TYPE = 0;

        /**
         * 正常版本/定制版本类型开关。0为正常版本（非定制,有用户级别、有文件夹个数等的限制）；<br/>
         * 1为定制版本(带flash播放，swf文件，此时没有用户级别、没有文件夹个数等的限制。添加游戏模块) <br/>
         * 注意：改变量在发布版本时，根据版本类型设置对应值即可。<BR/>
         * 2.定制版与普通版，版本优化。现合并为同一个版本；TOGGLE_CUSTOME_VERSION_TYPE始终设置为0；
         * 3.调整时间 TODO 20170613 原定制版的不分权限来显示视频文件夹个数。依然不变：【只需：tf卡 ibotn简介文件夹放置不同权限的视频文件】根据权限过滤文件夹
         */
        public static final int TOGGLE_CUSTOME_VERSION_TYPE = 0;
    }

    /**
     *广播相关配置类
     */
    public static class MyBroadCast{
        /** TODO: inform iBotn to stop voice command. */
        public  static final String ACTION_STOP_IFLYTEK_VOICE = "com.ibotn.ibotnvoice.STOP_IFLYTEK_VOICE";

        /**  TODO: inform iBotn to restart voice command. */
        public  static final String ACTION_START_IFLYTEK_VOICE = "com.ibotn.ibotnvoice.START_IFLYTEK_VOICE";

        /**
         * 发送当前城市位置广播
         */
        public  static final String ACTION_SEND_LOCATION_DATA = "ACTION_SEND_LOCATION_DATA";

    }

    /**
     * 音频文件类型
     */
    public static String[] AUDIO_TYPES = new String[]{".mp3",".wav"};
    /**
     * 1.加载视频文件类型;<br/>
     * 2.新添加swf文件类型;<br/>
     */
    public static String[] CONFIG_LOAD_VIDEO_TYPES = new String[]{".mp4",".swf"};

    /**语音自动播放音乐 通过intent传递值 ，获取EXTRA值对应文件夹*/
    public static  String FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "";
    /**语音自动播放视频 通过intent传递值 ，获取EXTRA值对应文件夹*/
    public static  String FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "";
    /**视频文件加密或已被破坏，此时情况--如果没有缩略图，全部以ibotn默认图标显示。现以""空字符串作为标记*/
    public static final  String Thumbnail_Empty = "";

    /**level =1时只显示默认前11个，level=2时显示后27个全部*/
    public static  int Video_Folder_Authority_Level = 1;

    //////////////////////////////////////使用onedriver 时自己创建的constant////start//////////
        /** 公用 ONE_DRIVER tag，便于链式打印log*/
        public static final String TAG_COMMON_ONE_DRIVER = "one_driver";
        /**
         * ONEDRIVER 根目录root下面创建【Ibotn文件夹】，【Ibotn文件夹】下面创建【PHOTO】，【VIDEO】
         */
        public static final String IBOTN_FOLDER_ONEDRIVER = "IBOTN";
        /**
         * ONEDRIVER 【IBOTN文件夹】 id
         */
        public static  String IBOTN_FOLDER_ID_ONEDRIVER = "";
        /**
         * ONEDRIVER 中IBOTN下面VIDEO文件夹
         */
        public static final String VIDEO_FOLDER_ONEDRIVER = "VIDEO";
        /**
         *  ONEDRIVER 中IBOTN下面VIDEO文件夹 id
         */
        public static  String VIDEO_FOLDER_ID_ONEDRIVER = "";
        /**
         * ONEDRIVER 中IBOTN下面PHOTO文件夹
         */
        public static final String PHOTO_FOLDER_ONEDRIVER = "PHOTO";
        /**
         * ONEDRIVER 中IBOTN下面PHOTO文件夹 id
         */
        public static  String PHOTO_FOLDER_ID_ONEDRIVER = "";

        /**
         * 1. 开机启动 ，文件初始化完成 。计时180s,防止读取不到sd卡上的文件。文件过多100g。180s后置为true,进行实际加载文件 <br/>
         * 2. 使用开机后sd卡扫描完成的广播，可以直接设置该值为true
         * 3. 要注意：单独异常停止后，重新开启应用。就没有sd卡相关的广播。该常量就为false,就需要使用服务了。
         * 4.
         */
        public static  boolean IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER = false;

        /**
         * 上传照片、视频，离开当前界面，不显示相关提示。重新进入该界面后再显示。使用改变量控制。
         * onResume时为true，onPause时为false
         */
        public static boolean SHOW_UI_TIP = false;
    //////////////////////////////////////使用onedriver 时自己创建的constant////end//////////

    ////////////云存储start////////////////////////////
        public static final String TAG_COMMON_YUNCUNCHU = "yuncunchu";

        public static String YUN_CUN_CHU_TOKEN = "";
        /**
         * 云存储视频文件夹
         */
        public static final String VIDEO_FOLDER_YUNCUNCHU = "VIDEO";
        public static final String AUDIO_FOLDER_YUNCUNCHU = "AUDIO";
    ////////////云存储end////////////////////////////

    /**  进入设置项的密码，密码默认132456，且不可修改 */
    public static final String DEFAUT_ENTER_SETTING_PASSWORD = "123456";
    //////////////手机端遥控播放音乐相关start//
    /** 将【现有的sd中】音乐文件，以及所属的第一级父文件夹以文档的形式保存，保存位置为【该sd卡音乐文件夹的下面】。作为上传的音频文件  */
    public static final String UPLOAD_AUDIO_FILE_ADSOLUTE_PATH = Config.Education_Content_Audio_File_Root_Path
            + File.separator
            + Utils.getDeviceSerial()
            + "_AUDIO.properties";
    /** 将【现有的sd中】音乐文件，以及所属的第一级父文件夹以文档的形式保存，保存位置为【该sd卡音乐文件夹的下面】。作为上传的音频文件  */
    public static final String UPLOAD_AUDIO_FILE_ADSOLUTE_PATH_WITH_XML = Config.Education_Content_Audio_File_Root_Path
            + File.separator
            + Utils.getDeviceSerial()
            + "_AUDIO.xml";

    /** 将【现有的sd中】音乐文件，以及所属的第一级父文件夹以文档的形式保存，保存位置为【该sd卡音乐文件夹的下面】。作为上传的音频文件  */
    public static final String UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML = Config.Education_Content_Video_File_Root_Path
            + File.separator
            + Utils.getDeviceSerial()
            + "_VIDEO.xml";

    /**properties 中文件夹key，部分前缀。完整key = "folder_" + "文件夹名称"  */
    public static final String KEY_PROPS_PART_PREFIX_FOLDER = "folder_";
    //////////////手机端遥控播放音乐相关end//

    /**
     * 链式Tag
     */
    public static class TrackTag{
        /**远程遥控播放音频,视频tag */
        public static final String TAG_REMOTE_CONTROL_PALY_FILE = "tag_remote_control_paly_file";
    }
    public static final String VIDEO = "VIDEO";
    public static final String AUDIO = "AUDIO";
    /**properties格式的音乐文档上传到服务器时所指定的目录*/
    public static final String AUDIO_PROPS = "AUDIO_PROPS";

    /**properties格式的音乐文档上传到服务器时所指定的目录*/
    public static final String VIDEO_PROPS = "VIDEO_PROPS";

    /**xml格式的音乐文档上传到服务器时所指定的目录*/
    public static final String AUDIO_XML = "AUDIO_XML";

    /**xml格式的音乐文档上传到服务器时所指定的目录*/
    public static final String VIDEO_XML = "VIDEO_XML";

    /** 远程遥控播放音频,视频。所上传的生成的properties,value前面添加排序序号 。*/
    public static final int PROPERTIES_VALUE_SORT_PREFIX_MAX_LENGH = 5;

    /////////////
    /**
     * android system browser package name
     */
    public static final String APK_PACKAGENAME_ANDROID_SYSTEM_BROWSER = "com.android.browser";

    /**extra intent 开启第三方app*/
    public static final String EXTRA_START_APP_FLAG = "EXTRA_START_APP_FLAG";
    /**启动FileExplorer,FileExployer根据这个标志设置可以操作的控件*/
    public static final String START_BY_IBOTNCLOUDPLAYER = "IBOTNCLOUDPLAYER";

    //////////
    /** MainActivity 静态引用。因为oneDriver自动登录会使用该MainActivity (使用static修饰)，必须使用activity，而非context*/
    public static MainActivity mainActivity;
}
