package com.infomax.ibotncloudplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Xml;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.additional.AdditionalDataDealHandler;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DateUtils;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.IbotnFileDealUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.NetUtils;
import com.infomax.ibotncloudplayer.utils.NetworkConnectivityUtils;
import com.infomax.ibotncloudplayer.utils.NumberUtils;
import com.infomax.ibotncloudplayer.utils.PropertiesEnhanceUtils;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.onedriver.AppOneDriver;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;

/**
 * Created by jy on 2017/3/2 ;10:10.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 * 1.在该server中注册广播；接收【语音拍照】完成后的广播，实现该图片的后台自动上传。<br/>
 * 2.开启处理初始化oneDrive的任务<br/>
 * 3. 定时120s检查一次崩溃日志文件目录 <br/>
 */
public class IbotnCoreService extends Service{

    public static final String TAG = IbotnCoreService.class.getSimpleName();
    /**
     * 上传crash log url
     */
    private final String UPLOAD_CRASH_LOG_URL = "http://log.ibotn.com/upload";
    private Context mContext;
    /** true时才执行任务 */
    private boolean mExecuteTaskFlag;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>>>>>onBind()>>");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>>>>>onCreate()>>");

        mContext = this;

        //Begin jinlong.zou,remove onedirve
//        IntentFilter filter = new IntentFilter(Constant.MyIntentProperties.ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO);
//        registerReceiver(mBroadcastReceiver, filter);
        //End jinlong.zou

        mExecuteTaskFlag = true;

        mTimer.schedule(mTimerTask, 2000);

        /////////////////
        //testSendBroadcast();
        ////////////////

        ibotnSystemFileInitTimerForLauncher();

        checkCrashLogFilesTimer(Constant.Config.PERIOD_FOR_CHECK_CRASH_LOG_FILES_TIMER);

        //AdditionalDataDealHandler init
        AdditionalDataDealHandler.getInstance().init();

        //checkNetConnection(); //取消检查，由lancher中语音模块检查

        checkSDExtorageAndSaveLog();
    }

    /**
     * 开机20s后检查外置sd卡。1.如果不存在就上报应用的log。2.同时上报kernel的log<br/>
     * 1.<br/>
     */
    private void checkSDExtorageAndSaveLog() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                String sdStoragePath;
                sdStoragePath = DevicePath.getInstance().getSdStoragePath();
                MyLog.d(TAG, ">>>checSDExtorage()>>>>sdStoragePath:" + sdStoragePath );
                if (TextUtils.isEmpty(sdStoragePath))
                {
                    try {
                        // 创建日志文件
                        final String fileName = Utils.getDeviceSerial()
                                + "_log_"
                                + DateUtils.formatDate(new Date(),1)
                                + "_logForUnSdExtorage_app"
                                + ".txt";
                        final String kernelLogFileName = Utils.getDeviceSerial()
                                + "_log_"
                                + DateUtils.formatDate(new Date(),1)
                                + "_logForUnSdExtorage_kernel"
                                + ".txt";
                        final String dumpstateLogFileName = Utils.getDeviceSerial()
                                + "_log_"
                                + DateUtils.formatDate(new Date(),1)
                                + "_logForUnSdExtorage_dumpstate"
                                + ".txt";
                        final long beforeTime =  1 * 60 * 60 * 1000;//获取多久以前的log。
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                        {
                            String path  = Environment.getExternalStorageDirectory() + Constant.Config.SUB_PATH_FOR_CRASH_LOG ;
                            boolean createOrExistsDir = FileUtils.createOrExistsDir(path);
                            final String filePath = path + File.separator + fileName;
                            //final String kernelFilePath = path + File.separator + kernelLogFileName;
                            final String dumpstateFilePath = path + File.separator + dumpstateLogFileName;
                            if (createOrExistsDir){
                                //控制台log格式如下：06-14 13:49:35.444 18699-18740/com.infomax.ibotncloudplayer D/IbotnCoreService: >>>checSDExtorage()>>>>sdStoragePath:
                                //过滤日期格式要与控制台一致
                                DateFormat logcat_formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
                                Date logcat_date = new Date(Calendar.getInstance().getTimeInMillis() - beforeTime);
                                String logcat_time = logcat_formatter.format(logcat_date);
                                //String logcat_time = DateUtils.formatDate(logcat_date,2);
                                MyLog.d(TAG, ">>>checSDExtorage()>>>>logcat_time:" + logcat_time );
                                Process su = Runtime.getRuntime().exec("/system/xbin/su root");

                                //06-14 14:38:24.936 12870-12923/com.infomax.ibotncloudplayer D/IbotnCoreService: >>>checSDExtorage()>>>>cmd:logcat -v time -d -T "06-14 12:38:24.00" -f /storage/sdcard/crash//APPCRASH/001611290948E_log_20170614143824_logForUnSdExtorage.txt
                                //-t <count>      print only the most recent <count> lines (implies -d)。可以是整数。也可以是时间字符串：按时间过滤。
                                String cmd = "logcat -v time -d -T \"" + logcat_time + ".00\" -f " + filePath;
                                MyLog.d(TAG, ">>>checSDExtorage()>>>>cmd:" + cmd );
                                su.getOutputStream().write(cmd.getBytes("UTF-8"));
                                su.getOutputStream().flush();
                                su.getOutputStream().close();//要先关闭。下一个文件才可以写入内容。

                                //////////////////02  获取dumpstate的log/////////////
                                su = Runtime.getRuntime().exec("/system/xbin/su root");//
                                //String getKernelLogCmd = "dmesg > "  + kernelFilePath;
                                String getKernelLogCmd = "dumpstate > "  + dumpstateFilePath;
                                su.getOutputStream().write(getKernelLogCmd.getBytes("UTF-8"));
                                su.getOutputStream().flush();
                                su.getOutputStream().close();
                                //////////////////02  获取dumpstate的log///end//////////
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(timerTask,20 * 1000);
    }
    /**
     * 定时检查网络连接状态。
     */
    private void checkNetConnection() {
        final String SERVER_ADDRESS_1 =  "www.ibotn.com";
        final String SERVER_ADDRESS_2 =  "www.baidu.com";
        final String[] SERVER_ADDRESSES = {SERVER_ADDRESS_1,SERVER_ADDRESS_2};
        Timer netCheckTimer = new Timer();
        TimerTask netCheckTask = new TimerTask() {
            @Override
            public void run() {
                boolean result1 = NetworkConnectivityUtils.checkUnusefulNetworkByPing(mContext,SERVER_ADDRESSES);
                boolean result2 = false;
                MyLog.d(TAG,TAG + ">>>checkNetConnection>>>>result:" + result1);
                if (result1 == false)
                {
//                    result2 = NetworkConnectivityUtils.checkUnusefulNetworkByPing(mContext,SERVER_ADDRESSES);
//                    MyLog.d(TAG,TAG + ">>>checkNetConnection>>>>result2:" + result2);
                }
            }
        };
        netCheckTimer.schedule(netCheckTask,0,10000);
    }

    /**
     * 1.开机后，计时180s,防止读取不到sd卡上的文件。文件过多100g
     * 2.立马打开播放去，播放器加载视频文件不能显示图标<br/>
     * 3.二次优化处理 ，单在LocalVideoFragment中添加进度提示经测试是不够的(v1.1.5及之前的版本都是这样处理的)。<br/>
     * 因为此时用户开机立即点击，系统数据库没有加载完成所有视频等文件。<br/>
     * 使用开机ibotncloudplayer启动后添加计时器，计时时间180s。180s期间，用户点击播放器【音乐/视频】，以【系统文件初始化中....】提示给用户。如果此时用户语音播放【音乐/视频】；如果此时用户遥控播放【音乐/视频】--待添加中，也都给同样的提示。<br/>
     * 4. 计时结束后执行，【上传生成的音频文档】
     */
    private void ibotnSystemFileInitTimerForLauncher(){

        /**计数量*/
        final AtomicInteger aiCount = new AtomicInteger(0);

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                int count = aiCount.getAndAdd(1);

                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>>>>count:" + count);
                if (count >= Constant.Config.SIMULATION_SYSTEM_FILE_INIT_FOR_LAUNCHER_COUNT_TIME){

                    ////////// // TODO: 2017/4/2 使用开机后sd卡扫描完成的广播，可以直接设置该值为true
                    Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER = true;
                    ///////////

                    dealAudioVideoFileBackground();

                    timer.cancel();
                }

            }
        };

        timer.schedule(timerTask, 0, 1000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>>>>>onStartCommand()>>mExecuteTaskFlag:"+mExecuteTaskFlag);

        //testPlayUrl();
        return   START_STICKY_COMPATIBILITY;//自动启动，如系统资源紧缺；长按屏幕-某个键--关闭进程；通过系统关掉，该配置会重启service
    }

    /**
     * 定时60s检查一次崩溃日志文件目录，如果有文件就上传；上传完成后将本地该文件删除。
     */
    private void checkCrashLogFilesTimer(final long period){

        final Timer timer  = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                MyLog.d(TAG, ">>>>checkCrashLogFilesTimer()>>>isNetworkConnected>:" + NetUtils.isNetworkConnected(mContext));
                if (NetUtils.isNetworkConnected(mContext)){

                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    {
                        String path  = Environment.getExternalStorageDirectory() + Constant.Config.SUB_PATH_FOR_CRASH_LOG ;
                        boolean createOrExistsDir =  FileUtils.createOrExistsDir(path);
                        if (createOrExistsDir){
                            File file = new File(path);
                            File[] files = file.listFiles();
                            if (files != null){
                                for (int i=0 ; i< files.length;i++){
                                    uploadCrashFile(files[i]);
                                    SystemClock.sleep(2000);
                                }
                            }
                        }
                    }

                }
            }
        };

        timer.schedule(timerTask, 5000, period);//5s后执行，2s执行一次
    }

    /**
     * 测试使用本地播放器播放网络音乐。only for test
     */
    private void testPlayUrl(){
        /**
         * {
         "status": 200,
         "url": "http://log.ibotn.com/download/upload/ListDir/AUDIO/1-If you are happy.mp3"
         }
         */

       ThreadUtils.runOnUIThreadDelay(new Runnable() {
           @Override
           public void run() {

//               Intent intent = new Intent(Intent.ACTION_VIEW);
//               Uri uri = Uri.parse("http://log.ibotn.com/download/upload/ListDir/AUDIO/1-If you are happy.mp3");
//               intent.setDataAndType(uri, "audio/*");
//               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//               startActivity(intent);

               //video
//               Intent intent = new Intent(Intent.ACTION_VIEW);
//               Uri uri = Uri.parse("http://log.ibotn.com/download/upload/ListDir/AUDIO/1-If you are happy.mp3");
//               intent.setDataAndType(uri, "video/*");
//               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//               startActivity(intent);
           }
       }, 1000 * 10);
    }

    /**
     * jy
     * 测试发送，【语音拍照】完成后的广播 。only for myself test to invoke
     */
    private void testSendBroadcast(){

        /**
         * 测试图片路径
         * 03-02 15:54:01.049 6744-6744/? D/MGridViewAdapter: >>>>>>>>getView()>>>>>>>>>>path:/storage/sdcard/DCIM/Camera/IMG_20170227_120358169.jpg
             03-02 15:54:01.049 6744-6744/? D/MGridViewAdapter: onRequestImage() ----path:/storage/sdcard/DCIM/Camera/IMG_20170227_120358169.jpg
             03-02 15:54:01.055 6744-6744/? D/MGridViewAdapter: >>>>>>>>getView()>>>>>>>>>>path:/storage/sdcard/DCIM/Camera/IMG_20170227_104322752.jpg
         */

        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true){
                    SystemClock.sleep(20 * 1000);
                    Intent intent = new Intent(Constant.MyIntentProperties.ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO);
                    intent.putExtra(Constant.MyIntentProperties.EXTRA_PHOTO_PATH_FOR_VOICE_TAKE_PHOTO,"/storage/sdcard/DCIM/Camera/IMG_20170227_120358169.jpg");
                    sendBroadcast(intent);
                }
            }
        }.start();

    }

    /**
     * 开启处理初始化oneDrive的任务。
     */
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {

            while (mExecuteTaskFlag){//没有销毁就开始循环处理

                boolean isConnectedToNetwork = NetworkConnectivityUtils.isConnectedToNetwork(getApplicationContext());
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>isConnectedToNetwork:" + isConnectedToNetwork);
                        //1.先检查网络,5s循环一次
                        if (!isConnectedToNetwork)
                        {
                            SystemClock.sleep(5000);
                            continue;
                        }else {

                            //2.有网络后再，再【根据refresh_token来判断用户的ibotn上的播放器oneDriver是否登录或注册微软账号。】
                            String refresh_token = SharedPreferenceUtils.getSp(Constant.MySharedPreference.SP_NAME_ONEDRIVE_COM_MICROSOFT_LIVE).getString("refresh_token","");
                            MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>refresh_token:" + refresh_token
                                    + ",thread-name:" + Thread.currentThread().getName());
                            if (TextUtils.isEmpty(refresh_token))
                            {
                                //3.refresh_token为空，就继续循环处理

                                ////////////////
//                                boolean automaticUploadSwitchState = SharedPreferenceUtils.getSwitchStateForAutomaticPhoto(mContext);
//                                if (automaticUploadSwitchState)
//                                {
//                                    ThreadUtils.runOnUIThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            ToastUtils.showCustomToast(mContext, getString(R.string.network_error) +"," + getString(R.string.text_unauto_upload_photo_for_voice_photo));
//                                        }
//                                    });
//                                }
                                ///////////////

                                SystemClock.sleep(10000);
                                continue;

                            }else {
                                SystemClock.sleep(1000);
                                //4.refresh_token不为空，就初始化oneDriver。
                                AppOneDriver.getInstance().loadOnedrive(mContext);

                                mExecuteTaskFlag = false;
                                mTimer.cancel();
                            }
                        }
            }

        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null)
            {
                String action = intent.getAction();
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>onReceive()>>action:"+action);
                if (Constant.MyIntentProperties.ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO.equals(action))
                {
                    boolean automaticUploadSwitchState = SharedPreferenceUtils.getSwitchStateForAutomaticPhoto(mContext);

                    String photoPath = intent.getStringExtra(Constant.MyIntentProperties.EXTRA_PHOTO_PATH_FOR_VOICE_TAKE_PHOTO);
                    //1.先判断网络
                    boolean connectedToNetwork = NetUtils.isNetworkConnected(mContext);
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>>>>onReceive()>>photoPath:"+photoPath
                            + ",isFile:" + FileUtils.isFile(photoPath)
                            + ",Constant.PHOTO_FOLDER_ID_ONEDRIVER:" + Constant.PHOTO_FOLDER_ID_ONEDRIVER
                            + ",connectedToNetwork:" + connectedToNetwork
                            + ",automaticUploadSwitchState:" + automaticUploadSwitchState
                    );
                    if (automaticUploadSwitchState)
                    {//如果开启自动上传,   才执行下面逻辑，// TODO: 2017/3/2
                        if (!connectedToNetwork)
                        {
                            ToastUtils.showCustomToast(mContext, getString(R.string.network_error) + "," + getString(R.string.text_unauto_upload_photo_for_voice_photo));
                        } else if (TextUtils.isEmpty(Constant.PHOTO_FOLDER_ID_ONEDRIVER))
                        {
                            ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_to_main_view_login_onedrive) + ";" + getString(R.string.text_auto_upload_photo_for_voice_photo));
                        }else
                        {
                            if (FileUtils.isFile(photoPath))
                            {
                                Constant.SHOW_UI_TIP = true;
                                AppOneDriver.getInstance().uploadFile(photoPath
                                        , Constant.PHOTO_FOLDER_ID_ONEDRIVER
                                        ,true
                                );

                            }else {
                                ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_file_error_unupload));
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 生成音频文当。请参考【ibotncloudplayer相关通讯定义文档.docx】<br/>
     * 后台系统文件初始化完成后再调用该方法。<br/>
     */
    private void dealAudioVideoFileBackground(){

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>dealAudioVideoFileBackground()>>>>:");

        ThreadUtils.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                try {
                    uploadAudioFile();
                    uploadVideoFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void uploadVideoFile() {
        //step 1 获取文件夹，及所对应的文件
        LinkedList<EcVideoFolderBean> childFolders = IbotnFileDealUtils.getInstance().getAllFolders(new File(Constant.Config.Education_Content_Video_File_Root_Path));

        HashMap<String,ArrayList<LocalAudioBean>> hashMap = null;
        if(childFolders != null)
        {
            hashMap = IbotnFileDealUtils.getInstance().getLocalVideosWithRecursion(mContext,Constant.Config.Education_Content_Video_File_Root_Path);
        }

        if (hashMap != null)
        {
            ///////////properties类型的音乐文档不再维护
            //step 2，创建properties文件
            //genarateAudioProperties(childFolders, hashMap);
            ////////////

            //step 2.1，创建XML文件
            genarateXML(childFolders, hashMap,Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML);

            //step 3 ,上传文件
            if (NetUtils.isNetworkConnected(mContext))
            {
                /////////properties类型的音乐文档不再维护
                //uploadAudioFile(Constant.AUDIO_PROPS);
                /////////

                uploadVideoFile(Constant.VIDEO_XML);
            }else {
                uploadTimerWaitForNetConnected("VIDIO");
            }
        }
    }

    private void uploadAudioFile() {
        //step 1 获取文件夹，及所对应的文件
        LinkedList<EcVideoFolderBean> childFolders = IbotnFileDealUtils.getInstance().getAllFolders(new File(Constant.Config.Education_Content_Audio_File_Root_Path));

        HashMap<String,ArrayList<LocalAudioBean>> hashMap = null;
        if(childFolders != null)
        {
            hashMap = IbotnFileDealUtils.getInstance().getLocalAudio(mContext,Constant.Config.Education_Content_Audio_File_Root_Path,childFolders);
        }

        if (hashMap != null)
        {
            ///////////properties类型的音乐文档不再维护
            //step 2，创建properties文件
            //genarateAudioProperties(childFolders, hashMap);
            ////////////

            //step 2.1，创建XML文件
            genarateXML(childFolders, hashMap,Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH_WITH_XML);

            //step 3 ,上传文件
            if (NetUtils.isNetworkConnected(mContext))
            {
                /////////properties类型的音乐文档不再维护
                //uploadAudioFile(Constant.AUDIO_PROPS);
                /////////

                uploadAudioFile(Constant.AUDIO_XML);
            }else {
                uploadTimerWaitForNetConnected("AUDIO");
            }
        }
    }

    /**
     * properties创建完成后。没有网络时执行调用该方法，直到有网络时才真正上传文件。<br/>
     * 5s后执行，10s执行一次
     */
    private void uploadTimerWaitForNetConnected(final String type){

        final Timer timer  = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadTimerWaitForNetConnected()>>>isNetworkConnected>:" + NetUtils.isNetworkConnected(mContext));
                if (NetUtils.isNetworkConnected(mContext)){
                    SystemClock.sleep(2000);

                    if("AUDIO".equals(type))
                        uploadAudioFile();
                    else
                        uploadVideoFile();

                    timer.cancel();
                }

            }
        };

        timer.schedule(timerTask, 5000, 10000);//5s后执行，10s执行一次

    }

    /**
     *
     * @param folderType ，AUDIO_XML ，AUDIO_PROPS;
     * 上传文件
     */
    private void uploadAudioFile(String folderType){
        File file = new File(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH_WITH_XML);
        if (FileUtils.isFileExists(file))
        {
            //step 3,上传properties文件
            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadAudioFile()>>>"
                            + "\n deviceId:" + Utils.getDeviceSerial()
                            + "\n folderType:" + folderType
                            + "\n file.getName():" + file.getName()
            );

            OkHttpUtils.post()
                    .addFile("file", file.getName(), file)
                    .url("http://log.ibotn.com/upload")
                    .addParams("deviceId", Utils.getDeviceSerial())
                    .addParams("folderType", folderType)//指定上传目录。是服务器目录
                    .addParams("upfile_type", "3")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadAudioFile()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadAudioFile()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadAudioFile()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadAudioFile()>>JSONException:" + e.getMessage()
                                );
                            }
                        }
                    });
        }
    }

    /**
     *
     * @param folderType ，AUDIO_XML ，AUDIO_PROPS;
     * 上传文件
     */
    private void uploadVideoFile(String folderType){
        File file = new File(Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML);
        if (FileUtils.isFileExists(file))
        {
            //step 3,上传properties文件
            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadVideoFile()>>>"
                    + "\n deviceId:" + Utils.getDeviceSerial()
                    + "\n folderType:" + folderType
                    + "\n file.getName():" + file.getName()
            );

            OkHttpUtils.post()
                    .addFile("file", file.getName(), file)
                    .url("http://log.ibotn.com/upload")
                    .addParams("deviceId", Utils.getDeviceSerial())
                    .addParams("folderType", folderType)//指定上传目录。是服务器目录
                    .addParams("upfile_type", "3")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadVideoFile()>>onError>>>"
                                    + "\n id:" + id
                                    + "\n Exception:" + e.getMessage()
                            );

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadVideoFile()>>onResponse>>>"
                                    + "\n id:" + id
                                    + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadVideoFile()>>onResponse>>>"
                                            + "\n Message:" + jsonObject.get("Message")
                                            + "\n Status:" + jsonObject.getInt("Status")
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadVideoFile()>>JSONException:" + e.getMessage()
                                );
                            }
                        }
                    });
        }
    }

    /**
     * 上传崩溃日志文件 <br/>
     * 上传完成后将本地该文件删除。 <br/>
     */
    private void uploadCrashFile(final File file){
        MyLog.d(TAG, ">>>>uploadCrashFile()>>>");
        if (FileUtils.isFileExists(file))
        {
            //step 3,上传properties文件
            MyLog.d(TAG, ">>>>uploadCrashFile()>>>"
                            + "\n getAbsolutePath:" + file.getAbsolutePath()
                            + "\n file.getName():" + file.getName()
            );

            OkHttpUtils.post()
                    .addFile("files", file.getName(), file)
                    .url(UPLOAD_CRASH_LOG_URL)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(TAG, TAG + ">>>>uploadCrashFile()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(TAG, ">>>>uploadCrashFile()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(TAG, ">>>>uploadCrashFile()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                MyLog.d(TAG, ">>>>uploadCrashFile()>>JSONException:" + e.getMessage()
                                );
                            }

                            FileUtils.deleteFile(file);
                        }
            });
        }
    }

    /**
     *
     * @param childFolders
     * @param hashMap
     * 测试生成properties文件
     */
    private void genarateAudioProperties(LinkedList<EcVideoFolderBean> childFolders, HashMap<String, ArrayList<LocalAudioBean>> hashMap){

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateAudioProperties()>>>>:");

        if (FileUtils.isFileExists(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH))
        {
            boolean delete = FileUtils.deleteFile(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);
            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateAudioProperties()>>>>delete:"
                    + delete);
        }

        boolean createOrExistsFile = FileUtils.createOrExistsFile(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);
        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateAudioProperties()>>>>file:"
                + createOrExistsFile);

        Properties properties = PropertiesEnhanceUtils.loadProps(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);

        //////////////////单独存储文件夹 方式一：生成的文件properties是乱序的,造成手机端下载后与ibotn不一致。
        //
//        for (EcVideoFolderBean bean: childFolders) {
//            //存储文件夹 key,value
//            PropertiesEnhanceUtils.updateProperty(properties,
//                    Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
//                    Constant.KEY_PROPS_PART_PREFIX_FOLDER + bean.name,
//                    bean.name
//            );
//
//            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateAudioProperties()>>>> bean.name:"
//                    +  bean.name);
//
//            //存储当前文件夹下面的所有文件path。key,value
//            ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>)hashMap.get(bean.name);
//            if (arrayList != null && arrayList.size()>0 )
//            {
//                for(LocalAudioBean localAudioBean : arrayList){
//                    PropertiesEnhanceUtils.updateProperty(properties,
//                            Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
//                            bean.name + "_" + localAudioBean.getDisplayName(),
//                            localAudioBean.getPath()
//                    );
//
//                    ////////////
////                    Uri.parse(localAudioBean.getPath());
//                    ///////////
//                }
//            }
//        }
        /////////////////
        //////////////////单独存储文件夹 方式二：
        // 生成的文件properties是乱序的。所以： 保存时给value添加分类序号：使用数字 如果一共4位：0001,0002，...1000。最多考虑6位数，实际也就几百而已。
        for (int i = 0 ; i < childFolders.size() ; i++) {
            //存储文件夹 key,value
            EcVideoFolderBean bean = childFolders.get(i);
//            int maxLength = (childFolders.size() + "").length();//获取集合中元素最大值对应的位数。
            String sortPrefixForFolder = NumberUtils.completeString((i + ""),Constant.PROPERTIES_VALUE_SORT_PREFIX_MAX_LENGH);//生成位数相同的前缀，如001,002,，，099,100。
            String value = sortPrefixForFolder + bean.name;
            PropertiesEnhanceUtils.updateProperty(properties,
                    Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
                    Constant.KEY_PROPS_PART_PREFIX_FOLDER + bean.name,
                    value
            );

            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateAudioProperties()>>>> bean.name:"
                    +  bean.name);

            //存储当前文件夹下面的所有文件path。key,value
            ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>)hashMap.get(bean.name);
            if (arrayList != null && arrayList.size()>0 )
            {
                for(int j = 0 ; j < arrayList.size();j++ ){
                    LocalAudioBean localAudioBean = arrayList.get(j);

//                    int maxLengthForFile = (arrayList.size() + "").length();//获取集合中元素最大值对应的位数。
                    String sortPrefixForFile = NumberUtils.completeString((j + ""),Constant.PROPERTIES_VALUE_SORT_PREFIX_MAX_LENGH/*maxLengthForFile*/);//生成位数相同的前缀，如001,002,，，099,100。
                    String valueForFile = sortPrefixForFile + localAudioBean.getPath();

                    PropertiesEnhanceUtils.updateProperty(properties,
                            Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
                            /*sortPrefixForFolder +*/ bean.name + "_" + localAudioBean.getDisplayName(),
                            valueForFile
                    );
                }
            }
        }
        /////////////////

        //打印结果：：：获取文件夹key,value
        PropertiesEnhanceUtils.getAnalyzePropData( Constant.KEY_PROPS_PART_PREFIX_FOLDER,Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);

        for (EcVideoFolderBean bean: childFolders) {
            //打印结果：：：获取文件key,value
            ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>)hashMap.get(bean.name);
            if(arrayList != null)
            {
                for (LocalAudioBean localAudioBean : arrayList)
                {
                    PropertiesEnhanceUtils.getAnalyzePropData(bean.name + "_" /*+ localAudioBean.getDisplayName()*/,Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);
                }

            }
        }
    }
    /**
     *
     * @param childFolders
     * @param hashMap
     * 生成XML文件
     */
    private void genarateXML(LinkedList<EcVideoFolderBean> childFolders, HashMap<String, ArrayList<LocalAudioBean>> hashMap, String type){

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateXML()>>>>:");

        if (FileUtils.isFileExists(type))
        {
            boolean delete = FileUtils.deleteFile(type);
            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateXML()>>>>delete:"
                    + delete);
        }

        boolean createOrExistsFile = FileUtils.createOrExistsFile(type);
        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateXML()>>>>file:"
                + createOrExistsFile);
        FileOutputStream fos = null;
        try {
            XmlSerializer serializer = Xml.newSerializer();

            File file = new File(type);
            fos = new FileOutputStream(file);
            serializer.setOutput(fos, "utf-8");
            serializer.startDocument("utf-8", true);

            if(Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML.equals(type))
                serializer.startTag(null,"video");
            else
                serializer.startTag(null,"audio");


            for (int i = 0 ; i < childFolders.size() ; i++) {
                //存储文件夹 key,value
                EcVideoFolderBean bean = childFolders.get(i);

                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateXML()>>>> bean.name:"
                        +  bean.name);

                serializer.startTag(null, "folder");
                serializer.attribute(null,"folderName",bean.name);//文件夹以属性的方式写入xml标签中。

                //存储当前文件夹下面的所有文件path。key,value
                ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>)hashMap.get(bean.name);
                if (arrayList != null && arrayList.size()>0 )
                {
                    for(int j = 0 ; j < arrayList.size();j++ ){
                        LocalAudioBean localAudioBean = arrayList.get(j);
                        //写入当前文件夹下的所有文件。以属性的方式
                        serializer.startTag(null,"file");
                        serializer.attribute(null, "filePath", localAudioBean.getPath());
                        serializer.endTag(null,"file");
                    }
                }
                serializer.endTag(null,"folder");
            }
            if(Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML.equals(type))
                serializer.endTag(null,"video");
            else
                serializer.endTag(null,"audio");
            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fos != null)
            {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /////////////////
        if(Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML.equals(type))
            parseXML("video");
        else
            parseXML("audio");
    }



    /**
     * 解析xml个数的音乐文档
     */
    private void parseXML(String type) {
        //解析xml,打印结果：
        LinkedList<EcVideoFolderBean> childFoldersForParse = null;
        HashMap<String, ArrayList<LocalAudioBean>> hashMapForParse = null;
        ArrayList<LocalAudioBean> currentArrayListForParse = null;
        EcVideoFolderBean ecVideoFolderBeanForParse = null;
        LocalAudioBean localAudioBeanForParse = null;
        FileInputStream fis = null;
        try {
            //得到pullparser
            XmlPullParser parser = Xml.newPullParser();
            File file = new File(Constant.UPLOAD_VIDEO_FILE_ADSOLUTE_PATH_WITH_XML);

            if (FileUtils.isFileExists(file))
            {
                fis = new FileInputStream(file);

                parser.setInput(fis,"utf-8");
                // 得到事件类型
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    switch (eventType){
                        case XmlPullParser.START_TAG :
                            if (type.equals(parser.getName())){
                                childFoldersForParse = new LinkedList<EcVideoFolderBean>();
                                hashMapForParse = new HashMap<String, ArrayList<LocalAudioBean>>();
                            }else if ("folder".equals(parser.getName()))
                            {
                                ecVideoFolderBeanForParse = new EcVideoFolderBean(parser.getAttributeValue(null,"folderName"),false);
                                currentArrayListForParse = new ArrayList<LocalAudioBean>();
                            }else if ("file".equals(parser.getName()))
                            {
                                localAudioBeanForParse = new LocalAudioBean(parser.getAttributeValue(null,"filePath"));
                            }

                            break;
                        case XmlPullParser.END_TAG :
                            if ("folder".equals(parser.getName()))
                            {
                                childFoldersForParse.add(ecVideoFolderBeanForParse);
                                hashMapForParse.put(parser.getAttributeValue(null,"folderName"),currentArrayListForParse);
                            }else if ("file".equals(parser.getName()))
                            {
                                currentArrayListForParse.add(localAudioBeanForParse);
                            }

                            break;
                    }
                    eventType = parser.next();
                }
            }
            if (childFoldersForParse != null)
            {
                for(EcVideoFolderBean bean : childFoldersForParse){
                    MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>genarateXML()>>>>foldername:" + bean.name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fis != null)
            {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mExecuteTaskFlag = false;

        mTimer.cancel();

        //AdditionalDataDealHandler stop
        AdditionalDataDealHandler.getInstance().stop();

        unregisterReceiver(mBroadcastReceiver);
    }
}
