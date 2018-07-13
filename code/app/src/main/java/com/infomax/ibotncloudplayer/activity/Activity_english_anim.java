package com.infomax.ibotncloudplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.base.FullScreenFragmentActivity;
import com.infomax.ibotncloudplayer.adapter.Chinese_anim_gvAdapter;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.FileEnhancedUtils;
import com.infomax.ibotncloudplayer.utils.FileForBitmapUtils;
import com.infomax.ibotncloudplayer.utils.FileMIMEUtils;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.GlideUtils;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.utils.VideoEncryptUtils;
import com.infomax.ibotncloudplayer.view.GridviewScrollBar;
import com.infomax.ibotncloudplayer.view.LoadingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hushaokun on 2018/6/4.
 */

public class Activity_english_anim extends FullScreenActivity implements View.OnClickListener {
    TextView tvBack;
    GridView gv;
    GridviewScrollBar scrollBar;
    TextView tvNone;
    /**
     * preInitData()执行次数
     */
    private AtomicInteger preInitDataInvokeCount = new AtomicInteger(0);
    /**
     * VIDEO文件夹下面的所有一级文件夹集合
     */
    private LinkedList<EcVideoFolderBean> childFolders = new LinkedList<EcVideoFolderBean>();
    /**
     * VIDEO文件夹下面的所有一级文件夹集合,内容和childFolders第一复制后的一样；只是以后不会变化，而childFolders根据需要随时变更
     */
    private LinkedList<EcVideoFolderBean> unChangeChildFolders = new LinkedList<EcVideoFolderBean>();
    Chinese_anim_gvAdapter gvAdapter;
    public static final String TAG = Activity_english_anim.class.getSimpleName();
    /**
     * VIDEO文件夹下面的所有一级文件夹，记录sd卡下面真实的个数
     */
    private int childFoldersRealSizeFormSd = 0;

    /**
     * 文件夹名称作为key,其下面的视频集合作为value。
     */
    private HashMap<String, ArrayList<LocalVideoBean>> hashMap = new HashMap<String, ArrayList<LocalVideoBean>>();
    /**
     * 默认集合，当前目录下没有文件夹时，使用该集合
     */
    ArrayList<LocalVideoBean> defaultArrayLists = new ArrayList<LocalVideoBean>();
    /**
     * /**
     * 实际访问到的文件个数
     */
    private int fileNumber;
    /**
     * 可以加载文件的标志；当activity销毁，就重置该值为false
     */
    private boolean canLoadingData = false;

    /**
     * 从SharedPreference得到的临时集合
     */
    private LinkedList<LocalVideoBean> myTempList;

    private final int Request_Code101 = 101;
    private final int Request_Code102 = 102;
    /**
     * 文件夹列表对应的数据加载 msg what;
     */
    private final int MSG_WHAT_FOLDER_LOAD_DATA = 101;
    /**
     * 展示listview，GridView，对应的 msg what;
     */
    private final int MSG_WHAT_SHOW_GV_LV_DATA = 102;

    private LoadingDialog loadingDialog;

    /**
     * 读取级别文件夹配置文件的到的，文件夹集合
     */
    private List<String> formConfigLevelFolderList = new LinkedList<String>();

    /**
     * 包含语音传递的文件夹状态
     */
    private boolean containVoiceFolderFlag = false;
    /**
     * 学习轨迹holder
     */
    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();
    private ScanSdReceiver scanSdReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_anim);
        initViews();
        registerReceiver();
        preInitData();
    }


    private void initViews() {
        tvBack = (TextView) findViewById(R.id.tv_back);
        gv = (GridView) findViewById(R.id.gv_content);
        scrollBar = (GridviewScrollBar) findViewById(R.id.myscrollbar);
        tvNone = (TextView) findViewById(R.id.tv_none);
        loadingDialog = new LoadingDialog(this);
        tvBack.setOnClickListener(this);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocalVideoBean bean = (LocalVideoBean) gvAdapter.getItem(position);
                VideoEncryptUtils.processVideoEncryptFunction(getBaseContext(), bean.getPath());

                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(bean.getPath());
                String tempFileName = bean.getDisplayName().toLowerCase();
                ////////////////////////////////////////
                //新增swf文件等判断-指定播放器播放
                String MIMEType = FileMIMEUtils.getMIMEType(file);
                MyLog.d(TAG, ">>>>>>>>>onItemClick()>>>getDisplayName:" + bean.getDisplayName()
                        + ",MIMEType:" + MIMEType);
                intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), MIMEType);

                /////////////////////////////////////////
                if (tempFileName.endsWith(".mp4")) {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    intent.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code102);
//                        saveWatchHitoryToSp(bean);
//                        addTrajectoryOfAct(bean.getDisplayName(), System.currentTimeMillis());

                    } else {
                        ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                    }
                } else {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code102);
//                        saveWatchHitoryToSp(bean);
//                        addTrajectoryOfAct(bean.getDisplayName(), System.currentTimeMillis());

                    } else {
                        if (tempFileName.endsWith(".swf")) {
                            ToastUtils.showCustomToast(getString(R.string.tip_swf_player_disable));
                        } else {
                            ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    /**
     * 立马打开播放去，播放器加载视频文件不能显示图标<br/>
     * 二次优化处理 ，单在LocalVideoFragment中添加进度提示经测试是不够的(v1.1.5及之前的版本都是这样处理的)。<br/>
     * 因为此时用户开机立即点击，系统数据库没有加载完成所有视频等文件。<br/>
     * 使用开机ibotncloudplayer启动后添加计时器，计时时间180s。180s期间，用户点击播放器【音乐/视频】，以【系统文件初始化中....】提示给用户。如果此时用户语音播放【音乐/视频】；如果此时用户遥控播放【音乐/视频】--待添加中，也都给同样的提示。<br/>
     */
    public void preInitData() {

        MyLog.d(TAG, ">>>>>preInitData()>>>>preInitDataInvokeCount:" + preInitDataInvokeCount.get());

        //step 1 检查外置sd卡是否可用
        DevicePath devicePath = new DevicePath(this);
        if (TextUtils.isEmpty(devicePath.getSdStoragePath())) {
            /*if (!Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER){

                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.setContent(R.string.text_system_file_initing);
                        loadingDialog.showLoadingDialog(loadingDialog);
                    }
                });
                if (preInitDataInvokeCount.getAndAdd(1) == 0)
                {
                    mTimer.schedule(mTimerTask, 1000, 1000);
                }
            }else {
                initData();
            }*/

            initData();
        } else {//外置sd卡是否可用

            initData();
        }
    }

    /**
     * 初始化数据
     */
    public void initData() {
        Log.d(TAG, ">>>initData()>>>path:" + Environment.getExternalStorageDirectory().getAbsolutePath() +
                "\n" + Environment.getRootDirectory().getAbsolutePath());
        ///////////////test
        ///storage/sd-ext/STUDY/VIDEO
        //File file = new File(File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY"+File.separator+"VIDEO");
        // ->>>cursor:/storage/sd-ext/STUDY/VIDEO/0a-Unit1 Hello.mp4
        //File file = new File("/storage/emulated/0/DCIM/Camera/");
        //File file = new File("/storage/emulated/0/DCIM/");
        //w我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
        //storage/sd-ext/STUDY/VIDEO    //视频文件路径
        ////////////////
        canLoadingData = true;
        ThreadUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.setContent(R.string.text_loading);
                loadingDialog.showLoadingDialog(loadingDialog);
            }
        });

        new Thread() {
            @Override
            public void run() {
                super.run();

                childFolders.clear();
                unChangeChildFolders.clear();
                defaultArrayLists.clear();
                hashMap.clear();
                fileNumber = 0;
                childFoldersRealSizeFormSd = 0;

                File file = new File(Constant.Config.Education_Content_Video_File_Root_Path);
                getAllFolders(file);

                ///////// close
//                getLocalVideosWithContentResolver(file.getAbsolutePath());
                ////////

                getLocalVideosWithRecursion(file.getAbsolutePath());

                if (!canLoadingData) {
                    return;
                }

                dealRelationDataAfterRecursion();

            }
        }.start();
    }


    /**
     * 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
     * 然后将该路径下面的所有文件夹列出来
     */
    private void getAllFolders(File root) {

        String tempName = "";

        childFolders.clear();
        ArrayList<String> tempFolders = new ArrayList<>();

        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    MyLog.d(TAG, "folder>>>>：" + f.getName());

                    /*EcVideoFolderBean bean = new EcVideoFolderBean(f.getName(), false);
                    //为了把ibotn文件夹放到文件夹列表首位 考虑大小写情况----start。
                    tempName = f.getName();
                    tempName.toLowerCase();
                    if (tempName.startsWith("ibotn")) {

                        childFolders.addFirst(bean);
                    } else {
                        childFolders.add(bean);
                    }*/

                    ///////
                    tempFolders.add(f.getName());
                    ///////

                } else {
//                    Log.d(TAG, "文件名称》》》》：" + f.getName());
                }
            }
        }

        //对音乐文件根据前三位序号排序，num001,num002,num003，...;如果是旧的sd卡，没有num***,存储时就不去掉前六位
        Collections.sort(tempFolders);
        for (String name : tempFolders) {
            if (name.startsWith("num")) {
                name = name.substring(6);
            }
            EcVideoFolderBean bean = new EcVideoFolderBean(name, false);
            childFolders.add(bean);
//            Log.e("---childFolders------", childFolders.size() + "");
        }

        childFoldersRealSizeFormSd = childFolders.size();

        //文件夹名称作为hashMap的key
        hashMap.clear();

        if (childFolders.size() == 0) {//当前目录下没有任何文件夹，就模拟一个文件夹
//            childFolders.add(new EcVideoFolderBean(getString(R.string.text_folder),true));
        }

        //----------------下面代码抽离到filterFolderWithLevel()-----------------//
        //根据级别过滤文件夹，即datas
//        LinkedList<EcVideoFolderBean> tempFolderBeans = new LinkedList<>();
//        for (EcVideoFolderBean bean : childFolders)
//        {
//            tempFolderBeans.add(bean);
//        }
//
//        childFolders.clear();
//        for (EcVideoFolderBean bean : tempFolderBeans)
//        {
//
//            for (String name : formConfigLevelFolderList)
//            {
//                if (bean.name != null && bean.name.equals(name))
//                {
//                    childFolders.add(bean);
//                }
//            }
//
//        }
//----------------下面代码抽离到filterFolderWithLevel()---end--------------//
        for (EcVideoFolderBean bean : childFolders) {
            ArrayList<LocalVideoBean> temp = new ArrayList<LocalVideoBean>();
            hashMap.put(bean.name, temp);

            //        unChangeChildFolders = childFolders;//不能直接这样赋值，unChangeChildFolders会持有 childFolders的引用，并随其变化而变化，最后结果和childFolders一样
            unChangeChildFolders.add(bean);//要遍历集合重新单独赋值。
        }


        MyLog.e(TAG, "getAllFolders()>>>>>folder num>>>>：" + childFolders.size());

        //----------------下面代码抽离到filterFolderWithLevel()-----------------//
//        if (childFolders.size() > 0 ){
//            changeLvFolder(true);
//            //首次更新lv，第一个文件夹默认选中
//            childFolders.get(0).selected = true;
//            ecLocalVideoFolderAdapter.setData(childFolders);
//
//        }else{
//            changeLvFolder(false);
//        }
        //----------------下面代码抽离到filterFolderWithLevel()----end-------------//

    }

    /**
     * @param folderRootPath 使用递归方式直接遍历文件夹。
     */
    private synchronized void getLocalVideosWithRecursion(String folderRootPath) {

        MyLog.d(TAG, ">>>getLocalVideosWithContentResolver>>>>>mPath：" + folderRootPath);

        if (FileUtils.isFileExists(folderRootPath)) {

            File file = new File(folderRootPath);
            File[] files = file.listFiles();
            if (files != null) {
                for (File tempFile : files) {

                    if (!canLoadingData) {
                        return;
                    }

                    if (FileUtils.isDir(tempFile)) {
                        getLocalVideosWithRecursion(tempFile.getAbsolutePath());

                    } else {
                        String displayName = tempFile.getName();
                        if (!TextUtils.isEmpty(displayName)) {
                            int id = 0;
                            long date = 0;
                            String path = tempFile.getAbsolutePath();
                            String tempDisplayName = displayName.toLowerCase();
                            long size = tempFile.length();
                            boolean existFileType = false;
                            for (String fileType : Constant.CONFIG_LOAD_VIDEO_TYPES) {
                                if (tempDisplayName.endsWith(fileType)) {
                                    existFileType = true;
                                }
                            }
                            if (existFileType) {
                                LocalVideoBean bean = new LocalVideoBean(id, date, path, displayName, size);

                                /////////close this way to generate png
                                if (tempDisplayName.endsWith(".mp4")) {//mp4文件才创建缩略图
                                    FileForBitmapUtils.createPngForMP4(bean);
                                }
                                ////////

                                defaultArrayLists.add(bean);

                                for (EcVideoFolderBean folderBean : childFolders) {

                                    int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                    String tempPath = bean.getPath().substring(0, i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
//                                MyLog.d(TAG, ">>>getLocalVideosWithContentResolver()>>>>tempPath():" + tempPath
//                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                    if (tempPath.endsWith(folderBean.name)) {//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
                                        String name = folderBean.name;
                                        Log.e("--name-", name + "");
                                        hashMap.get(folderBean.name).add(bean);
                                    }

                                    String tempFolderName = childFolders.get(0).name;
//                                    Log.e("----tempFolderName---", tempFolderName + "");
                                    if (tempFolderName.toLowerCase().startsWith("ibotn")) {
                                        String fileName = bean.getDisplayName().toLowerCase();
                                        if (fileName.startsWith("ibotn")) {
                                            MyLog.d(TAG, ">>>>>>>>ibotn:" + bean.getPath());
                                            SharedPreferenceUtils.setIbotnFile(getBaseContext(), bean.getPath());
                                            FileEnhancedUtils.dealFileForLevel(getBaseContext(), SharedPreferenceUtils.getIbotnFile(getBaseContext()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 文件遍历完成后再调用该方法，过滤文件，展示数据。
     */
    private void dealRelationDataAfterRecursion() {

        //////////////调整时间 TODO 20170613 原定制版的不分权限来显示视频文件夹个数。依然不变：【只需：tf卡 ibotn简介文件夹放置不同权限的视频文件】根据权限过滤文件夹
        if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 0) {//此情况下再根据用户级别过滤文件夹

            /////////
            //filterFolderWithLevel();
            /////////

            ////////使用新的方式过了文件夹。
            filterFolderWithLevelEnhance();
            /////////

        } else if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1) {
            mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
        }
        ///////////

        ///////
        //需求变更为：定制版/正常版本,都添加swf文件
        //addSwfFiles(new File(Constant.Config.Education_Content_Video_File_Root_Path));
        //////
        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_GV_LV_DATA);
    }

    /**
     * 新的方式根据用户级别过滤文件夹；<br/>
     * 1.根据用户权限如果为1（默认都为1），显示文件夹由【原来的固定十个】调整为【总文件夹的一半】;如果文件总个数 <=5。将全部显示，就不做一半处理。
     * 这样的好处：就不怕sd卡文件夹名称修改了。<br/>
     * 2、如果权限为2，就显示文件夹，由【原来的固定二十七个】调整为【总文件夹】<br/>
     */
    private void filterFolderWithLevelEnhance() {

//        Constant.Video_Folder_Authority_Level = SharedPreferenceUtils.getUserLevel(ctx);
        LinkedList<EcVideoFolderBean> tempFolderBeans = new LinkedList<>();//用来复制childFolders到临时集合中

        for (EcVideoFolderBean bean : childFolders) {
            tempFolderBeans.add(bean);
        }
     /*     int size = tempFolderBeans.size();
      if (Constant.Video_Folder_Authority_Level == 1)//去除vip设定 20180515 phc
        {
            if (size >= 6){//如果文件总个数 <=5。将全部显示，就不做一半处理。
                int division = size / 2 ;
                for (int i=0 ; i <size ; i++)
                {
                    if (i >= division -1){//删除一半以后的
                        EcVideoFolderBean bean = tempFolderBeans.get(i);
                        childFolders.remove(bean);
                    }
                }
            }
        }else if (Constant.Video_Folder_Authority_Level == 2)
        {
            //默认即全部文件夹
        }*/

        MyLog.e(TAG, "filterFolderWithLevelEnhance()>>>>>childFolders num>>>>：" + childFolders.size()
                + ",childFoldersRealSizeFormSd:"
                + childFoldersRealSizeFormSd
                + ",unChangeChildFolders:" + unChangeChildFolders.size()
                + ",Video_Folder_Authority_Level:" + Constant.Video_Folder_Authority_Level
        );

        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == MSG_WHAT_FOLDER_LOAD_DATA) {
                if (childFolders.size() > 0) {

                    int moveToItemPosition = 0;
                    for (int i = 0; i < childFolders.size(); i++) {
                        EcVideoFolderBean bean = childFolders.get(i);
                        if (bean.name.equals(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE)) {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            moveToItemPosition = i;

                            MyLog.d(TAG, "mHandler>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:" + bean.name);
                        } else {
                            bean.selected = false;
                        }
                    }
                    if (!containVoiceFolderFlag) {//没有包含语音传递的文件夹，更新lv，第一个文件夹默认选中----------
                        moveToItemPosition = 0;
                        childFolders.get(0).selected = true;
                    }

                    moveToItemPosition = 0;

                    containVoiceFolderFlag = false;//此时可以重置该值
                } else {

                }
            } else if (what == MSG_WHAT_SHOW_GV_LV_DATA) {
                Log.d(TAG, ">>>initData()>>>MSG_WHAT_SHOW_GV_LV_DATA:" + MSG_WHAT_SHOW_GV_LV_DATA);

                if (loadingDialog != null) {
                    loadingDialog.dismissLoadingDialog(loadingDialog);
                }
                showGvLvData();
            }
        }
    };

    private void showGvLvData() {
        if (childFolders.size() > 0) {//有文件夹
            ArrayList<LocalVideoBean> arrayList = null;
            if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE) {//获取语音传递的文件夹，命令词即【播放 + 文件夹】
                //注意：：原有的【播放音乐】命令要保持不变。即此时没有文件夹
                if (TextUtils.isEmpty(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE)) {

                    //////////////// 随机切换文件夹，
                    final int lenth = childFolders.size();
                    int randomIndex = (int) (Math.random() * lenth);
                    if (lenth == 0) {
                        randomIndex = 0;

                    } else if (randomIndex >= lenth) {
                        randomIndex = lenth - 1;
                    }

                    final String randomFolderName = childFolders.get(randomIndex).name;
                    arrayList = (ArrayList<LocalVideoBean>) hashMap.get(randomFolderName);
                    int moveToItemPosition = 0;
                    for (int i = 0; i < childFolders.size(); i++) {
                        EcVideoFolderBean bean = childFolders.get(i);
                        if (bean.name.equals(randomFolderName)) {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            moveToItemPosition = i;

                            MyLog.d(TAG, "showGvLvData>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:" + bean.name);
                        } else {
                            bean.selected = false;
                        }
                    }

                    moveToItemPosition = 0;
                    //////////////////
                } else {
                    arrayList = (ArrayList<LocalVideoBean>) hashMap.get(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);
                }
            } else {
                ////不是语音时，默认加载第一个文件夹下的视频
//                Log.e("----hashMap----", hashMap.size() + "");
//                for (EcVideoFolderBean folderBean : childFolders) {
//                    arrayList = new ArrayList<>();
//                    arrayList.addAll(hashMap.get(folderBean.name));
//                }
                arrayList = new ArrayList<>();
                //获取英文文件
                for (EcVideoFolderBean folder : childFolders) {
                    String name = folder.name;
                    boolean b = isContainChinese(name);
                    if (!b) {
                        arrayList.addAll(hashMap.get(folder.name));
                    }
                }

//                arrayList = (ArrayList<LocalVideoBean>) hashMap.get(childFolders.get(2).name);
            }
            MyLog.d(TAG, ">>>>showGvLvData()>>START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE:" + Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE
                    + ",FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE:" + Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE
                    + ",arrayList:" + (arrayList == null ? "null" : arrayList.size()));

            if (gvAdapter == null) {
//                Log.e("-arrayList--", arrayList.size() + "");
                gvAdapter = new Chinese_anim_gvAdapter(getBaseContext(), arrayList, gv, mOnRequestVideoListener);
                gv.setAdapter(gvAdapter);
                if (arrayList.size() > 0) {
                    gv.setVisibility(View.VISIBLE);
                    scrollBar.setGridView(gv);
                    scrollBar.setVisibility(View.VISIBLE);
                    tvNone.setVisibility(View.GONE);
                } else {
                    gv.setVisibility(View.GONE);
                    scrollBar.setVisibility(View.GONE);
                    tvNone.setVisibility(View.VISIBLE);
                }
            } else {
                gvAdapter.setData(arrayList);

            }


            if (arrayList != null) {
                //更新控件
                if (arrayList.size() <= 0) {
                    tvNone.setVisibility(View.VISIBLE);

                } else {
                    tvNone.setVisibility(View.GONE);
                    //根据用户是否点击‘列表，九宫格’而显示lv,gv
                    if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE) {
                        ////////////////修改：语音【播放音乐】，调整为随机播放当前文件夹下面的文件。
                        final int lenth = arrayList.size();
                        int randomIndex = (int) (Math.random() * lenth);
                        if (lenth == 0) {
                            randomIndex = 0;
                        } else if (randomIndex >= lenth) {
                            randomIndex = lenth - 1;
                        }
                        LocalVideoBean bean = arrayList.get(randomIndex);
                        playVideoByVoiceWithBean(bean);

                        MyLog.e(TAG, "showGvLvData()>>>>>getPath:" + bean.getPath());
                    }
                }
            }
        } else {//没有文件夹
            if (gvAdapter == null) {

                gvAdapter = new Chinese_anim_gvAdapter(getBaseContext(), defaultArrayLists, gv, mOnRequestVideoListener);
                gv.setAdapter(gvAdapter);
                if (defaultArrayLists.size() == 0) {
                    tvNone.setVisibility(View.VISIBLE);
                } else {
                    tvNone.setVisibility(View.GONE);
                }
            }


            //更新控件
            if (defaultArrayLists.size() <= 0) {

            } else {
                //根据用户是否点击‘列表，九宫格’而显示lv,gv

                //语音随机播放
                if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE) {

                    ////////////////修改：语音【播放】，调整为随机播放文件。

                    final int lenth = defaultArrayLists.size();
                    int randomIndex = (int) (Math.random() * 10);
                    if (randomIndex >= lenth) {
                        randomIndex = lenth - 1;
                    }
                    LocalVideoBean bean = defaultArrayLists.get(randomIndex);
                    playVideoByVoiceWithBean(bean);

                    MyLog.e(TAG, "showGvLvData()>>>>>getPath:" + bean.getPath());

                    ////////////////
                }
            }
        }
        Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE = false;
        Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "";
    }

    //判断是否包含中文
    private boolean isContainChinese(String name) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(name);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * playVideoByVoiceWithBean 语音自动播放视频时 调用
     *
     * @param bean
     */
    private void playVideoByVoiceWithBean(LocalVideoBean bean) {
        VideoEncryptUtils.processVideoEncryptFunction(getBaseContext(), bean.getPath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(bean.getPath());
        String tempFileName = bean.getDisplayName().toLowerCase();
        ////////////////////////////////////////
        //新增swf文件等判断-指定播放器播放
        String MIMEType = FileMIMEUtils.getMIMEType(file);
        MyLog.d(TAG, ">>>>>>>>>playVideoByVoiceWithBean()>>>getDisplayName:" + bean.getDisplayName()
                + ",MIMEType:" + MIMEType);
        intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), MIMEType);

        /////////////////////////////////////////
        if (tempFileName.endsWith(".mp4")) {
            //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
            intent.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, Request_Code102);
                saveWatchHitoryToSp(bean);
            } else {
                ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
            }
        } else {
            //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, Request_Code102);
                saveWatchHitoryToSp(bean);
            } else {
                if (tempFileName.endsWith(".swf")) {
                    ToastUtils.showCustomToast(getString(R.string.tip_swf_player_disable));
                } else {
                    ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                }
            }
        }
    }

    private void saveWatchHitoryToSp(LocalVideoBean bean) {
        //保存bean到sp
        SharedPreferences sp = getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);

        String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");
        LocalVideoBean currentBean = bean/*mItems.run(position)*/;

        MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>local-->>:" + localList);

        if (TextUtils.isEmpty(localList)) {
            LinkedList<LocalVideoBean> tempList = new LinkedList<LocalVideoBean>();
            tempList.addFirst(currentBean);

            try {
                String string = SharedPreferenceUtils.object2String(tempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, string).commit();

            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

            }
            tempList = null;

        } else {
            try {

                myTempList = (LinkedList<LocalVideoBean>) SharedPreferenceUtils.string2Object(localList);

                if (myTempList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE) {
                    myTempList.removeLast();
                }

                if (myTempList.contains(currentBean)) {
                    MyLog.d(TAG, "----contain---currentBean:" + currentBean.getDisplayName());
                    myTempList.remove(currentBean);
                    myTempList.addFirst(currentBean);
                } else {
                    MyLog.d(TAG, "----uncontain---currentBean:" + currentBean.getDisplayName());
                    myTempList.addFirst(currentBean);
                }

                String tempStr = SharedPreferenceUtils.object2String(myTempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, tempStr).commit();
            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
            }
        }

        MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>myTempList-->>:" + (myTempList == null ? 0 : myTempList.size()));
    }

    /**
     * 请求视频缩略图回调，对应gridview
     */
    private Chinese_anim_gvAdapter.OnRequestVideoListener mOnRequestVideoListener = new Chinese_anim_gvAdapter.OnRequestVideoListener() {

        @Override
        public void OnRequestImage(String imgPath, ImageView iv) {
//            Bitmap bmp = getBmpFromCache(imgPath);
//            if (bmp != null){
//                iv.setImageBitmap(bmp);
//            }
            /**
             * 使用glide加载视频-缩略图(圆角)，glide不需要iv设置tag。glide内部处理了
             */
//            Glide.with(ctx).load(Uri.fromFile(new File(imgPath))).transform(new GlideRoundTransform(ctx,20)).into(iv);
            GlideUtils.load(getBaseContext(), imgPath, iv);
//            GlideUtils.loadWithListener(ctx,imgPath,iv);
        }

    };

    /**
     * 注册广播
     */
    public void registerReceiver() {
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentfilter.addDataScheme("file");
        if (scanSdReceiver == null) {
            scanSdReceiver = new ScanSdReceiver();
        }
        registerReceiver(scanSdReceiver, intentfilter);
    }

    /**
     * 反注册广播
     */
    public void unRegisterReceiver() {
        if (scanSdReceiver != null) {
            unregisterReceiver(scanSdReceiver);
        }
    }

    /**
     * 扫描sd卡完成后的广播接受者
     */
    public class ScanSdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            MyLog.d(TAG, "action>>>>>>>>>>>>>" + intent.getAction());
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
                MyLog.d(TAG, "scanner_started>>>>>>>>>>>>>");
            } else if (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE.equals(intent.getAction())) {
                MyLog.d(TAG, "scanner_scan_file.... ing>>>>>>>>>>>>>");
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
                initData();
            }
        }
    }

    /**
     * 用act的记录轨迹holder记录下轨迹
     *
     * @param displayName
     * @param currentTimeMillis
     */
    private void addTrajectoryOfAct(String displayName, long currentTimeMillis) {
        LearnTrajectoryBean bean = new LearnTrajectoryBean();
        bean.setName(displayName);
        bean.setStartTime(currentTimeMillis);
        bean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO);
        actStartLearn(bean);
    }

    public void actStartLearn(LearnTrajectoryBean bean) {
        if (trajectoryHolder != null) {
            trajectoryHolder.startLearn(bean);
        }
    }

    public void actEndLearn() {
        if (trajectoryHolder != null) {
            trajectoryHolder.endLearn();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actEndLearn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }
}
