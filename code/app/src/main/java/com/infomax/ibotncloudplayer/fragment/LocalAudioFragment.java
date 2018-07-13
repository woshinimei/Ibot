package com.infomax.ibotncloudplayer.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.EcAudioActivity;
import com.infomax.ibotncloudplayer.adapter.EcLocalVideoFolderAdapter;
import com.infomax.ibotncloudplayer.adapter.LocalAudioLVAdapter;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.utils.*;
import com.infomax.ibotncloudplayer.view.LoadingDialog;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jy on 2016/12/12.<br/>
 * 1.教育内容-本地音频 fragment <br/>
 * 2.【新增】【添加语音播放音乐指定文件夹-传递的文件夹】。命令词即【播放 + 文件夹】 // TODO: 2017/2/24
 */
public class LocalAudioFragment extends Fragment {
    final String  TAG = LocalAudioFragment.class.getSimpleName();

    private LayoutInflater myInflater;
    private EcAudioActivity mActivity;
    /**
     * 默认集合，当前目录下没有文件夹时，使用该集合
     */
    ArrayList<LocalAudioBean> defaultArrayLists = new ArrayList<LocalAudioBean>();
    /**
     * 左侧文件夹列表
     */
    private ListView lv_act_local_video;
    private ListView lv_list;//右侧listview，列表展示

    private TextView tv_additional;

    private LinearLayout ll_act_local_video;
    private ImageView iv_act_local_video;
    /**
     * AUDIO文件夹下面的所有一级文件夹集合
     */
    private LinkedList<EcVideoFolderBean> childFolders = new LinkedList<EcVideoFolderBean>();
    /**
     * AUDIO文件夹下面的所有一级文件夹集合,内容和childFolders第一复制后的一样；只是以后不会变化，而childFolders根据需要随时变更
     */
    private LinkedList<EcVideoFolderBean> unChangeChildFolders = new LinkedList<EcVideoFolderBean>();

    /**
     * AUDIO文件夹下面的所有一级文件夹，记录sd卡下面真实的个数
     */
    private int childFoldersRealSizeFormSd = 0;

    /**
     * 音乐文件的HassMap,key文件夹，value为该文件夹下面的所有音频(mp3)文件
     */
    private HashMap<String,ArrayList<LocalAudioBean>> hashMap = new HashMap<String,ArrayList<LocalAudioBean>>();

    /** 文件夹列表适配器 */
    private EcLocalVideoFolderAdapter ecLocalVideoFolderAdapter;

    private LocalAudioLVAdapter localAudioLVAdapter;

    /**没有数据的布局*/
    private RelativeLayout  rl_nodata;

    private LinkedList<LocalAudioBean> tempList;

    private final int Request_Code101 = 101;
    private final int Request_Code102 = 102;

    /**
     * 文件夹列表对应的数据加载 msg what;
     */
    private final int MSG_WHAT_FOLDER_LOAD_DATA = 101;
    /**
     * 展示listview，对应的 msg what;
     */
    private final int MSG_WHAT_SHOW_LV_DATA = 102;

    private LoadingDialog loadingDialog;

    /**
     * 读取级别文件夹配置文件的到的，文件夹集合
     */
    //private List<String> formConfigLevelFolderList = new LinkedList<String>();

    /**可以加载文件的标志；当fragment销毁，就重置该值为false*/
    private  boolean canLoadingData = false;

    private int number;

    /**
     * 音频文件带专辑图片
     */
    private boolean flagLocalVideosWithAlbum;
    /**
     * 包含语音传递的文件夹状态
     */
    private boolean containVoiceFolderFlag = false;
    /**
     * 最近点击的文件夹名称
     */
    private String currentFoldersName = "";


    @Override
    public void onResume() {
        super.onResume();
        mActivity.getTrajectoryHolder().endLearn();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myInflater = inflater;

        mActivity = (EcAudioActivity) getActivity();

        loadingDialog = new LoadingDialog(mActivity);

        //View view = inflater.inflate(R.layout.fragment_local_video,container,false);
        View view = View.inflate(getActivity(),R.layout.fragment_local_audio,null);
        initViews(view);

        registListener();

        ///////////
        //initData();
        //////////

        ////////// 新方式加载数据
        preInitData(true);
        //////////

        ///////////
        am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        //////////

        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.rl_act_local_video).setOnClickListener(mFunctionOnClickListener);

        lv_act_local_video = (ListView) view.findViewById(R.id.lv_act_local_video);
        lv_list = (ListView) view.findViewById(R.id.lv_list);
        ll_act_local_video = (LinearLayout) view.findViewById(R.id.ll_act_local_video);
        iv_act_local_video = (ImageView) view.findViewById(R.id.iv_act_local_video);
        rl_nodata = (RelativeLayout) view.findViewById(R.id.rl_nodata);
        tv_additional = (TextView) view.findViewById(R.id.tv_additional);

        //设置adapter
        ecLocalVideoFolderAdapter = new EcLocalVideoFolderAdapter(mActivity,childFolders,R.layout.item_lv_common);
        lv_act_local_video.setAdapter(ecLocalVideoFolderAdapter);
    }

    private void registListener() {
        lv_act_local_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < childFolders.size(); i++) {
                    if (i == position) {
                        childFolders.get(i).selected = true;

                    } else {
                        childFolders.get(i).selected = false;
                    }
                }
                Log.d(TAG, ">>>>>onItemClick>>>>getSelectedItemPosition:" + lv_act_local_video.getSelectedItemPosition()
                        + ",getSelectedItemId:" + lv_act_local_video.getSelectedItemId());

                ecLocalVideoFolderAdapter.notifyDataSetChanged();

                //更新gridview
                ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>) hashMap.get(childFolders.get(position).name);
                currentFoldersName = childFolders.get(position).name;
                Log.d(TAG, "" + childFolders.get(position).name);

                //更新listview
                if (localAudioLVAdapter != null) {
                    localAudioLVAdapter.setData(arrayList);
                }
                //更新控件
                if (arrayList.size() <= 0) {
                    changeViewLv(false);
                    changeRelativeLayoutState(true);
                } else {
                    changeViewLv(true);
                    changeRelativeLayoutState(false);
                }
            }
        });

        //listview 事件
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocalAudioBean bean = (LocalAudioBean) localAudioLVAdapter.getItem(position);
                //播放audio
                Intent intent = new Intent(Intent.ACTION_VIEW);
                ///////////////
                //intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "audio/*");//"audio/*"
                ///////////////

                File file = new File(bean.getPath());

                String MIMEType = FileMIMEUtils.getMIMEType(file);
                MyLog.d(TAG, ">>>>>>>>>setOnItemClickListener()>>>getDisplayName:" + bean.getDisplayName()
                        + ",MIMEType:" + MIMEType);
                intent.setDataAndType(Uri.fromFile(file), MIMEType);
                //初始化学习轨迹实体
                LearnTrajectoryBean trajectoryBean = new LearnTrajectoryBean();
                trajectoryBean.setName(currentFoldersName+":"+bean.getDisplayName());
                trajectoryBean.setStartTime(System.currentTimeMillis());
                trajectoryBean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_EDU_AUDIO);
                //当前所有的音乐播放器都被手动强行停止后或者【没有音乐播放器时】。点击播放音乐文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                if (intent.resolveActivity(mActivity.getPackageManager()) != null) {

                    ////////获取音频焦点后再播放
                    startActivityForResult(intent, Request_Code101);

                    //testRequestAudioFocus(intent);
                    saveWatchHitoryToSp(bean);
                    mActivity.getTrajectoryHolder().startLearn(trajectoryBean);
                } else {
                    //没有音乐播放器，
                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                    intent2.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/*");//"video/*"
                    if (intent2.resolveActivity(mActivity.getPackageManager()) != null) {

                        ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_audio_player_disable));
                        startActivityForResult(intent2, Request_Code101);
                        saveWatchHitoryToSp(bean);
                        mActivity.getTrajectoryHolder().startLearn(trajectoryBean);

                    } else {
                        ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_player_disable));
                    }
                }

                ////////////云存储--上传测试////////////////////////////
                /*MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onItemClick()>>>"
                                + "\n id:" + id
                                + "\n  bean.getPath():" + bean.getPath()
                );

                YunCunCh就使用视频播放器代替。uUtils.upload(new File(bean.getPath()),3);
                YunCunChuUtils.uploadFileWithHttpurlconnection(new File(bean.getPath()));*/
                ////////////云存储////////////////////////////
            }
        });

    }

    AudioManager   am = null;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
            {

            }else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
            {

            }
        }
    };
    private void testRequestAudioFocus(final Intent intent){

        int result = am.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        MyLog.d(TAG, ">>>>>>>>>testRequestAudioFocus()>>>result:" + result);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            ////////获取音频焦点后再播放
            startActivityForResult(intent, Request_Code101);
        }

    }

    /**
     * playAudioByVoiceWithBean 语音自动播放音乐时 调用
     * @param bean
     */
    private void playAudioByVoiceWithBean(LocalAudioBean bean){

        if (bean == null){

            ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_no_audio));
            return;
        }
        if (TextUtils.isEmpty(bean.getPath()))
        {
            ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_no_audio));
            return;
        }

        //播放audio
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "audio/*");//"audio/*"
        //当前所有的音乐播放器都被手动强行停止后或者【没有音乐播放器时】。点击播放音乐文件就会异常停止。应该弹出是否使用【视频播放器来播放】
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivityForResult(intent, Request_Code101);
        } else {
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/*");//"video/*"
            if (intent2.resolveActivity(mActivity.getPackageManager()) != null) {

                ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_audio_player_disable));
                startActivityForResult(intent2, Request_Code101);

            } else {
                ToastUtils.showToast(mActivity, mActivity.getString(R.string.tip_player_disable));
            }
        }
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data){

        MyLog.e(TAG, "onActivityResult--requestCode:" + requestCode + ",data:" + data);

        if (requestCode == Request_Code101){

        }else if (requestCode == Request_Code102){

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
      * 得到根Fragment
      *
      * @return
      */
    private Fragment getRootFragment() {

         Fragment fragment = getParentFragment();

        MyLog.e(TAG, "getRootFragment:" + fragment);

         while (fragment != null && fragment.getParentFragment() != null) {
          fragment = fragment.getParentFragment();
         }
         return fragment;

    }

    /**
     * saveWatchHitoryToSp <br/>
     * current file of playing will be added to the top of history record in the local sp
     * @param bean
     */
    private void saveWatchHitoryToSp(LocalAudioBean bean){
        LinkedList<LocalAudioBean> currentList = null;
        try {
            currentList = (LinkedList<LocalAudioBean>) SharedPreferenceUtils.getLocalAudioHistoryList(mActivity);
        } catch (Exception e) {
            e.printStackTrace();
            currentList = null;
        }

        LocalAudioBean currentBean = bean;

        MyLog.d(TAG, "---->>mOnItemClickListener--->>local-->>:" + currentList);

        if (currentList == null)
        {
            currentList = new LinkedList<LocalAudioBean>();
            currentList.addFirst(currentBean);

            try {
                SharedPreferenceUtils.setLocalAudioHistoryList(mActivity,currentList);

            }catch (Exception e)
            {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

            }
            currentList = null;

        }else
        {
            try {

                if (currentList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
                {
                    currentList.removeLast();//删除最后一个
                }

                if (currentList.contains(currentBean))
                {
                    MyLog.d(TAG,"----contain---currentBean:"+currentBean.getDisplayName());
                    currentList.remove(currentBean);
                    currentList.addFirst(currentBean);
                }else
                {
                    MyLog.d(TAG,"----uncontain---currentBean:"+currentBean.getDisplayName());
                    currentList.addFirst(currentBean);
                }

                SharedPreferenceUtils.setLocalAudioHistoryList(mActivity,currentList);
            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
            }
        }

        MyLog.d(TAG, "---->>mOnItemClickListener--->>currentList-->>:" + (currentList == null ? 0 : currentList.size()));
    }

    /** true listview显示 */
    private  void changeViewLv(boolean show){
        if (show){
            lv_list.setVisibility(View.VISIBLE);
        }else {
            lv_list.setVisibility(View.GONE);
        }
    }

    /**
     * 是否显示没有数据提示
     * @param noData true 显示-没有数据
     */
    private  void changeRelativeLayoutState(boolean noData){
        if (noData){
            rl_nodata.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(DevicePath.getInstance().getSdStoragePath()))
            {
                tv_additional.setText(getString(R.string.text_no_data) + "," + getString(R.string.text_tip_check_sd));
            }else {
                tv_additional.setText(getString(R.string.text_no_data));
            }
        }else {
            rl_nodata.setVisibility(View.GONE);
        }
    }

    /**
     * 根标志是否显示文件夹 true 显示。false,隐藏；
     * 因需求代码变更，子线程更新UI问题；实际更新通过handler发送执行。
     * @param flag
     */
    private  void changeLvFolder(final boolean flag){

        if (flag){
            ll_act_local_video.setVisibility(View.VISIBLE);
        }else {
            ll_act_local_video.setVisibility(View.GONE);
        }

    }

    View.OnClickListener mFunctionOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.rl_act_local_video:

                    if (lv_act_local_video.getVisibility() == View.VISIBLE) {
                        lv_act_local_video.setVisibility(View.GONE);
                        iv_act_local_video.setBackgroundResource(R.drawable.selector_iv_arrow_right);
                    }else {
                        lv_act_local_video.setVisibility(View.VISIBLE);
                        iv_act_local_video.setBackgroundResource(R.drawable.selector_iv_arrow_left);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    ////////////////////////////////////// 新方式加载数据--根据server中计时器处理//////////start
    /**计数量*/
    private AtomicInteger aiCount = new AtomicInteger(0);
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            MyLog.d(TAG, ">>>>>mTimerTask>>run>>Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER：" + Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER);
            if (Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER){
                mTimer.cancel();

                initData(true);
            }
        }
    };

    /**
     * 立马打开播放去，播放器加载视频文件不能显示图标<br/>
     * 二次优化处理 ，单在LocalVideoFragment中添加进度提示经测试是不够的(v1.1.5及之前的版本都是这样处理的)。<br/>
     * 因为此时用户开机立即点击，系统数据库没有加载完成所有视频等文件。<br/>
     * 使用开机ibotncloudplayer启动后添加计时器，计时时间180s。180s期间，用户点击播放器【音乐/视频】，以【系统文件初始化中....】提示给用户。如果此时用户语音播放【音乐/视频】；如果此时用户遥控播放【音乐/视频】--待添加中，也都给同样的提示。<br/>
     * @param isShowLoading 是否显示加载进度，true 为显示
     */
    private void preInitData(boolean isShowLoading){

        MyLog.d(TAG, ">>>>>preInitData()>>>>") ;
        //step 1 检查外置sd卡是否可用
        DevicePath devicePath = new DevicePath(getContext());
        if (TextUtils.isEmpty(devicePath.getSdStoragePath())){
            /*if (!Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER){
                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.setContent(R.string.text_system_file_initing);
                        loadingDialog.showLoadingDialog(loadingDialog);
                    }
                });
                if (aiCount.getAndAdd(1) == 0){
                    mTimer.schedule(mTimerTask, 1000, 1000);
                }
            }else {
                initData();
            }*/
            initData(isShowLoading);

        }else {//外置sd卡是否可用
            initData(isShowLoading);
        }
        ///////////
//        initData();// TODO: 2017/3/9
        ///////////
    }
    ////////////////////////////////////// 新方式加载数据--根据server中计时器处理/////////end
    /**
     * @param isShowLoading
     */
    private void initData(boolean isShowLoading) {
        Log.d(TAG, ">>>>initData()>>>path:" + Environment.getExternalStorageDirectory().getAbsolutePath() +
                "\n" + Environment.getRootDirectory().getAbsolutePath());
        /////////////////test path//////////
        ///storage/sd-ext/STUDY/AUDIO
//        File file = new File(File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY"+File.separator+"AUDIO");
        // ->>>cursor:/storage/sd-ext/STUDY/AUDIO/0a-Unit1 Hello.mp4
//        File file = new File("/storage/emulated/0/DCIM/Camera/");
//        File file = new File("/storage/emulated/0/DCIM/");
        //w我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
        //storage/sd-ext/STUDY/AUDIO    //音频文件路径
        /////////////////test path////end//////////

        if (isShowLoading)
        {
            ThreadUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.setContent(R.string.text_loading);
                    loadingDialog.showLoadingDialog(loadingDialog);
                }
            });
        }

        canLoadingData = true;
        new Thread(){
            @Override
            public void run() {
                super.run();
                ////////////// close
//                Constant.Video_Folder_Authority_Level = SharedPreferenceUtils.getUserLevel(mActivity);
//                formConfigLevelFolderList = PropertiesUtils.get(Constant.Video_Folder_Authority_Level);
                /////////////

                File file = new File(Constant.Config.Education_Content_Audio_File_Root_Path);
                getAllFolders(file);

                ///////////close  getLocalAudiosContentResolver(.)
//                getLocalAudiosContentResolver(file.getAbsolutePath());
                /////////
                getLocalAudiosWithRecursion(file.getAbsolutePath());

                if (!canLoadingData){
                    return;
                }

                dealRelationDataAfterRecursion();

                ////////////
                // TODO: 2017/2/23 不加载专辑图片了
//                SystemClock.sleep(5000);
//                //10秒后再加载带专辑图的音频文件，重复执行getAllFiles(file); getLocalVideosWithAlbum(file.getAbsolutePath());
//                getAllFolders(file);
//                getLocalVideosWithAlbum(file.getAbsolutePath());
                ////////////

            }
        }.start();
    }

    /**
     * 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
     * 然后将该路径下面的所有文件夹列出来
     */
    private void getAllFolders(File root) {

        childFolders.clear();
        ArrayList<String> tempFolders = new ArrayList<>();

        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    MyLog.d(TAG, "folder>>>>：" + f.getName());

                    //////旧的方式，直接添加实体bean到集合中。
//                    EcVideoFolderBean bean = new EcVideoFolderBean(f.getName(), false);
                    //为了把ibotn文件夹放到文件夹列表首位 考虑大小写情况----start。
//                    tempName = f.getName();
//                    tempName.toLowerCase();

                   /* if (tempName.startsWith("ibotn")) { //对于音乐文件夹，没有ibotn

                        childFolders.addFirst(bean);
                    } else {
                        childFolders.add(bean);
                    }*/

//                    childFolders.add(bean);
                    ////////

                    ///////
                    tempFolders.add(f.getName());
                    ///////
                } else {
                    Log.d(TAG, "文件名称>>>：" + f.getName());
                }
            }
        }
        //对音乐文件根据前三位序号排序，num001,num002,num003，...;如果是旧的sd卡，没有num,存储时就不去掉前六位
        Collections.sort(tempFolders);
        for (String name : tempFolders)
        {
            if (name.startsWith("num")){
                name = name.substring(6);
            }
            EcVideoFolderBean bean = new EcVideoFolderBean(name, false);
            childFolders.add(bean);
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
        for (EcVideoFolderBean bean : childFolders){
            ArrayList<LocalAudioBean> temp = new ArrayList<LocalAudioBean>();
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
     * 1.根据用户级别过滤文件夹； <br/>
     * 2.add 2016-12-9. OTA升级后，之前的机器，播放器文件夹都不显示了。<br/>
     * 最后解决方案: 不改变旧的sd卡<br/>
     * 新增级别文件夹配置文件expand_authorityfolder.properties<br/>
     * 且都不带清华二字。如果通过authorityfolder.properties得到的文件夹集合比对后，只含有一个ibotn文件夹。<br/>
     * 就再次读取authorityfolder_expand.properties，并比较文件夹集合。<br/>
     * 3.对于音乐不需要过滤<br/>
     *
     */
    private void filterFolderWithLevel(){
         LinkedList<EcVideoFolderBean> tempFolderBeans = new LinkedList<>();//用来复制childFolders到临时集合中
        for (EcVideoFolderBean bean : childFolders)
        {
            tempFolderBeans.add(bean);
        }

        MyLog.e(TAG, "filterFolderWithLevel()>>>>>folder num>>>>：" + childFolders.size()
                + ",childFoldersRealSizeFormSd:"
                + childFoldersRealSizeFormSd
                + ",unChangeChildFolders:" + unChangeChildFolders.size());


        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
    }

    /**
     * 不加载专辑图片 ，默认album 复制 ""
     * @param folderRootPath
     * 使用ContentResolver方式加载文件，该方式，需要收到sd卡扫描完成的广播后才能获取到内容。
     */
    private void getLocalAudiosContentResolver(String folderRootPath){

        flagLocalVideosWithAlbum = false;

        /**文件夹是否有对应音频文件，false为没有*/
        boolean flagCurrentFolderHasVideo = false;

        long time = SystemClock.currentThreadTimeMillis();

        defaultArrayLists.clear();

        StringBuilder selection = new StringBuilder();
        //音频
        selection.append("(" + MediaStore.Audio.Media.DATA + " LIKE '" + folderRootPath +File.separator+ "%')");
        Log.d(TAG, "-->>>>>>>>" + selection.toString());

        try {
            ContentResolver contentResolver = mActivity.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext() && canLoadingData) {

                    number ++ ;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                    String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲文件的路径
                    //音频文件对应的专辑图片的album_id
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑id
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//文件的大小
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));//添加时间
                    //-------
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//                String album = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
//                String artist = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//                String mimeType = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
//                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

//                    Log.d(TAG, "->>>cursor:" + path+",album_id:"+album_id);//// TODO: 2017/2/23
                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName))
                    {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith(".mp3"))//过滤mp3
                        {
                            LocalAudioBean bean = new LocalAudioBean(id,date,path,displayName,size);

//                            String albumArtPath = getAlbumArtPath(album_id);//获取专辑图片，暂时关闭 // TODO: 2017/3/28
                            String albumArtPath = "";
                            bean.setAlbumArtPath(albumArtPath);

//                            Log.d(TAG, "->>>bean.setAlbumArtPath():" + bean.getAlbumArtPath());// TODO: 2017/2/23  

                            defaultArrayLists.add(bean);

                            //分文件夹遍历文件，存入对应集合中
                            for(EcVideoFolderBean folderBean : childFolders){

                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
                                int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                String tempPath = bean.getPath().substring(0,i-1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
                                Log.d(TAG, ">>>getLocalAudiosContentResolver()>>>>tempPath():" + tempPath
                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                if (tempPath.endsWith(folderBean.name)){//
//                                    flagCurrentFolderHasVideo = true;
                                    //将当前bean添加到hashMap中key为folderName
                                    hashMap.get(folderBean.name).add(bean);
                                }

                                ///////
//                                String tempFolderName = childFolders.get(0).name;
                                //过滤ibotn开头的文件
//                                if(tempFolderName.toLowerCase().startsWith("ibotn"))
//                                {
//                                    if (bean.getDisplayName().startsWith("ibotn")){
//                                        Log.d(TAG, "->>>ibotn:" + bean.getPath());
//                                        SharedPreferenceUtils.setIbotnFile(mActivity,bean.getPath());
//                                        FileEnhancedUtils.dealFileForLevel(mActivity, SharedPreferenceUtils.getIbotnFile(mActivity));
//                                    }
//                                }
                                /////////

                            }
                        }
                    }

                }
            }

            cursor.close();
        }catch (Exception e){
            e.printStackTrace();

            MyLog.e(TAG, "Exception--->>:" + e.getMessage());

//            flagCurrentFolderHasVideo = false;
            defaultArrayLists.clear();
        }

        if (!canLoadingData){//退出activity时就返回
            return;
        }

        /*if (!flagCurrentFolderHasVideo){
            if (childFolders.size() > 0){
                if (hashMap.get(childFolders.get(0).name) != null){
                    hashMap.get(childFolders.get(0).name).clear();
                    hashMap.get(childFolders.get(0).name).addAll(defaultArrayLists);//第一个文件夹添加数据
                }
            }
        }*/

        MyLog.e(TAG, "number>>>>>>>>>>>>>>>>>>>:"+ number);

        time = SystemClock.currentThreadTimeMillis() - time;

        MyLog.e(TAG, "elapsedRealtime>>>>>>>>>>>>>>>>>>>:" + (time));

//        filterFolderWithLevel();
        //对于音乐文件不需要过滤，直接展示
        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);

        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);

        ////////////////////测试手机遥控播放音乐 // TODO: 2017/3/9
//        testAudioProperties();
        ////////////////////测试多次播放文件 TODO: 2017/3/9
//        testPlayFile();
        ////////////////////

    }

    /**
     * 不加载专辑图片 ，默认album 复制 ""
     * @param folderRootPath
     * 使用递归方式直接遍历文件夹。
     */
    private void getLocalAudiosWithRecursion(String folderRootPath){
        MyLog.d(TAG, ">>>getLocalVideosWithContentResolver>>>>>mPath：" + folderRootPath );

        if (FileUtils.isFileExists(folderRootPath)){

            File file = new File(folderRootPath);
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File tempFile : files)  {

                    if (!canLoadingData){
                        return;
                    }

                    if (FileUtils.isDir(tempFile)){
                        getLocalAudiosWithRecursion(tempFile.getAbsolutePath());

                    }else {
                        String displayName = tempFile.getName();
                        if (!TextUtils.isEmpty(displayName)){
                            int id = 0;
                            long date = 0;
                            String path = tempFile.getAbsolutePath();
                            String tempDisplayName = displayName.toLowerCase();
                            long size = tempFile.length();
                            boolean existFileType = false;
                            for (String fileType : Constant.AUDIO_TYPES)
                            {
                                if (tempDisplayName.endsWith(fileType))
                                {
                                    existFileType = true;
                                }
                            }

                            if (existFileType)
                            {
                                LocalAudioBean bean = new LocalAudioBean(id,date,path,displayName,size);

                                defaultArrayLists.add(bean);

                                for(EcVideoFolderBean folderBean : childFolders){

                                    int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                    String tempPath = bean.getPath().substring(0,i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
//                                MyLog.d(TAG, ">>>getLocalVideosWithContentResolver()>>>>tempPath():" + tempPath
//                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                    if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
                                        hashMap.get(folderBean.name).add(bean);
                                    }

                                    /*String tempFolderName = childFolders.get(0).name;
                                    if(tempFolderName.toLowerCase().startsWith("ibotn"))
                                    {
                                        String fileName =  bean.getDisplayName().toLowerCase();
                                        if (fileName.startsWith("ibotn")){
                                            MyLog.d(TAG, ">>>>>>>>ibotn:" + bean.getPath());
                                            SharedPreferenceUtils.setIbotnFile(mActivity,bean.getPath());
                                            FileEnhancedUtils.dealFileForLevel(mActivity, SharedPreferenceUtils.getIbotnFile(mActivity));
                                        }
                                    }*/
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
    private void dealRelationDataAfterRecursion(){


//        filterFolderWithLevel();
        //对于音乐文件不需要过滤，直接展示
        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);

        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);

        ////////////////////测试手机遥控播放音乐 // TODO: 2017/3/9
//        testAudioProperties();
        ////////////////////测试多次播放文件 TODO: 2017/3/9
//        testPlayFile();
        ////////////////////

    }

    /**
     * 测试生成properties文件
     */
    private void testAudioProperties(){

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testProperties()>>>>:");

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testProperties()>>>>file:"
                + FileUtils.createOrExistsFile(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH));

        Properties properties = PropertiesEnhanceUtils.loadProps(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);

        //直接存储集合
//        PropertiesEnhanceUtils.updateProperty(properties,
//                Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
//                Constant.KEY_FOLDER_LIST,
//                FileEnhancedUtils.object2String(childFolders)
//                );
//        PropertiesEnhanceUtils.updateProperty(properties,
//                Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
//                Constant.KEY_FILE_HASHMAP,
//                FileEnhancedUtils.object2String(hashMap)
//        );
//
        /**
         * 03-08 22:22:54.685 6362-6593/com.infomax.ibotncloudplayer D/tag_remote_control_paly_audio: LocalAudioFragment>>>>testProperties()>>>>test_list:null,strFolderList:??  sr  java.util.LinkedList)S]J`?"    xpw      sr  3com.infomax.ibotncloudplayer.bean.EcVideoFolderBean????@!    xr  0com.infomax.ibotncloudplayer.bean.BaseFolderBean                Z  selectedL  namet  Ljava/lang/String;xp  t  ????sq  ~    t  ????x
         */
//
//        String  strFolderList = PropertiesEnhanceUtils.getString(PropertiesEnhanceUtils.loadProps(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH)
//                , Constant.KEY_FOLDER_LIST);
//        LinkedList<EcVideoFolderBean> test_list = FileEnhancedUtils.string2Object(strFolderList);
//        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testProperties()>>>>test_list:" + (test_list == null ? "null" : test_list.size())
//                + ",strFolderList:" + strFolderList);
//        if (test_list != null)
//        {
//            for (EcVideoFolderBean bean :test_list) {
//                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testProperties()>>>>name:" + bean.name);
//            }
//        }

        //单独存储文件夹
        for (EcVideoFolderBean bean: childFolders) {
            //存储文件夹 key,value
            PropertiesEnhanceUtils.updateProperty(properties,
                    Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
                    Constant.KEY_PROPS_PART_PREFIX_FOLDER + bean.name,
                    bean.name
            );

            //存储当前文件夹下面的所有文件path。key,value
            ArrayList<LocalAudioBean> arrayList = (ArrayList<LocalAudioBean>)hashMap.get(bean.name);
            if (arrayList != null && arrayList.size()>0 )
            {
                for(LocalAudioBean localAudioBean : arrayList){
                    PropertiesEnhanceUtils.updateProperty(properties,
                            Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH,
                            bean.name + "_" + localAudioBean.getDisplayName(),
                            localAudioBean.getPath()
                    );

//                    Uri.parse(localAudioBean.getPath());
                }
            }
        }

        //获取文件key,value
        PropertiesEnhanceUtils.getAnalyzePropData("folder_",Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);


//        for (EcVideoFolderBean bean: childFolders) {
//            properties = PropertiesEnhanceUtils.loadProps(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);
//
//            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testProperties()>>>>get>>name:" + PropertiesEnhanceUtils.getString(properties),bean);
//        }

    }

    /**
     * 测试多次播放文件，验证上一个音乐正在播放的是否停止，新的音乐是否自动播放。<br/>
     * 测试ok。
     */
    private void testPlayFile(){

        MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testPlayFile()>>>>");

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER)
                {
                    final int position  = (int)(Math.random() * 10 + 1);
                    LocalAudioBean bean = (LocalAudioBean) localAudioLVAdapter.getItem(position);
                    MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>testPlayFile()>>>>position:" + position
                                + ",getDisplayName:" + bean.getDisplayName());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File file = new File(bean.getPath());
                    String MIMEType = FileMIMEUtils.getMIMEType(file);
                    MyLog.d(TAG, ">>>>>>>>>setOnItemClickListener()>>>getDisplayName:" + bean.getDisplayName()
                            + ",MIMEType:" + MIMEType);
                    intent.setDataAndType(Uri.fromFile(file), MIMEType);

                    //当前所有的音乐播放器都被手动强行停止后或者【没有音乐播放器时】。点击播放音乐文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code101);
                    }
                }

            }
        };
        timer.schedule(timerTask,0,10 * 1000);

    }

    /**
     * 音频文件，加载对应专辑图片
     * @param mPath
     */
    private void getLocalVideosWithAlbum(String mPath){

        flagLocalVideosWithAlbum = true;

        /**文件夹是否有对应音频文件，false为没有*/
        boolean flagCurrentFolderHasVideo = false;

        long time = SystemClock.currentThreadTimeMillis();

        defaultArrayLists.clear();

        StringBuilder selection = new StringBuilder();
        //音频
        selection.append("(" + MediaStore.Audio.Media.DATA + " LIKE '" + mPath +File.separator+ "%')");
        Log.d(TAG, "-->>>>>>>>" + selection.toString());

        try {
            ContentResolver contentResolver = mActivity.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext() && canLoadingData) {

                    number ++ ;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                    String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲文件的路径
                    //音频文件对应的专辑图片的album_id
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑id
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//文件的大小
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));//添加时间
                    //-------
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//                String album = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
//                String artist = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//                String mimeType = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
//                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    Log.d(TAG, "->>>cursor:" + path+",album_id:"+album_id);
                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName))
                    {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith("mp3"))//过滤mp3
                        {
                            LocalAudioBean bean = new LocalAudioBean(id,date,path,displayName,size);

                            String albumArtPath = getAlbumArtPath(album_id);
                            bean.setAlbumArtPath(albumArtPath);
                            Log.d(TAG, "->>>bean.setAlbumArtPath():" + bean.getAlbumArtPath());
                            defaultArrayLists.add(bean);

                            //分文件夹遍历文件，存入对应集合中
                            for(EcVideoFolderBean folderBean : childFolders){

                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4

                                int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                String tempPath = bean.getPath().substring(0,i);//去掉文件名的路径
                                if (tempPath.contains(folderBean.name)){//只有文件路径（不包含文件名称）包含文件夹集合中的一项
//                                    flagCurrentFolderHasVideo = true;
                                    //将当前bean添加到hashMap中key为folderName
                                    hashMap.get(folderBean.name).add(bean);
                                }

                                String tempFolderName = childFolders.get(0).name;
                                //过滤ibotn开头的文件
//                                if(tempFolderName.toLowerCase().startsWith("ibotn"))
//                                {
//                                    if (bean.getDisplayName().startsWith("ibotn")){
//                                        Log.d(TAG, "->>>ibotn:" + bean.getPath());
//                                        SharedPreferenceUtils.setIbotnFile(mActivity,bean.getPath());
//                                        FileEnhancedUtils.dealFileForLevel(mActivity, SharedPreferenceUtils.getIbotnFile(mActivity));
//                                    }
//                                }

                            }
                        }
                    }
                }
            }

            cursor.close();
        }catch (Exception e){
            e.printStackTrace();

            MyLog.e(TAG, "Exception--->>:" + e.getMessage());

//            flagCurrentFolderHasVideo = false;
            defaultArrayLists.clear();
        }

        if (!canLoadingData){//退出activity时就返回
            return;
        }

        /*if (!flagCurrentFolderHasVideo){
            if (childFolders.size() > 0){
                if (hashMap.get(childFolders.get(0).name) != null){
                    hashMap.get(childFolders.get(0).name).clear();
                    hashMap.get(childFolders.get(0).name).addAll(defaultArrayLists);//第一个文件夹添加数据
                }
            }
        }*/

        MyLog.e(TAG, "number>>>>>>>>>>>>>>>>>>>:"+number);

        time = SystemClock.currentThreadTimeMillis() - time;

        MyLog.e(TAG, "elapsedRealtime>>>>>>>>>>>>>>>>>>>:"+(time));

        filterFolderWithLevel();

        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);

    }

    /**
     * 根据album_id，取得专辑图片的绝对路径<br/>
     * @param album_id  <br/>
     * @return 专辑图片的绝对路径    <br/>
     * 获取专辑图片主要是通过album_id进行查询，因此首先获取album_id   <br/>
     */
    private String getAlbumArtPath(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cursor = null;
        String album_art = "";
        try{
            cursor = mActivity.getContentResolver().query(
                    Uri.parse(mUriAlbums + File.separator + Integer.toString(album_id)),
                                projection, null, null, null);
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                cursor.moveToNext();
                album_art = cursor.getString(0);
            }
        }catch (Exception e){
            e.printStackTrace();
            MyLog.e(TAG, "getAlbumArt()>>>>>>>Exception>>:"+e.getMessage());
        }finally {
            if (cursor != null)
            {
                try {
                    cursor.close();

                }catch (Exception e){
                    e.printStackTrace();
                }
                cursor = null;
            }
        }
        return album_art;
    }

    /**
     * 展示listview数据
     */
    private  void showLvData(){
        if (childFolders.size() > 0)
        {//有文件夹

            ArrayList<LocalAudioBean> arrayList = null;
            if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE)
            {//获取语音传递的文件夹，命令词即【播放 + 文件夹】
                //注意：：原有的【播放音乐】命令要保持不变。即此时没有文件夹
                if (TextUtils.isEmpty(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE))
                {
                    ///////////////
                    //默认第一个文件夹
                    //arrayList = (ArrayList<LocalAudioBean>) hashMap.get(childFolders.get(0).name);
                    //////////////

                    //////////////// 随机切换文件夹，
                    final int lenth = childFolders.size();
                    int randomIndex = (int)(Math.random()  * lenth);
                    if (lenth == 0){
                        randomIndex = 0;

                    }else if (randomIndex >= lenth){
                        randomIndex = lenth -1;
                    }
                    final  String randomFolderName = childFolders.get(randomIndex).name;
                    arrayList = (ArrayList<LocalAudioBean>) hashMap.get(randomFolderName);
                    int moveToItemPosition = 0;
                    for (int i = 0 ; i<childFolders.size() ;i++)
                    {
                        EcVideoFolderBean bean  = childFolders.get(i);
                        if(bean.name.equals(randomFolderName))
                        {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            moveToItemPosition = i;

                            MyLog.d(TAG, "showLvData>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:"+bean.name);
                        }else {
                            bean.selected = false;
                        }
                    }
                    ecLocalVideoFolderAdapter.setData(childFolders);
                    lv_act_local_video.setSelection(moveToItemPosition);
                    moveToItemPosition = 0;
                    //////////////////


                }else {
                    arrayList = (ArrayList<LocalAudioBean>) hashMap.get(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);
                }
            }else
            {//不是语音时，默认加载第一个文件夹下的音频列表
                arrayList = (ArrayList<LocalAudioBean>) hashMap.get(childFolders.get(0).name);
            }
            MyLog.d(TAG,">>>>showLvData()>>START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE:" + Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE
                    + ",FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE:" + Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE
                    + ",arrayList:" + (arrayList == null ? "null": arrayList.size()));
            //lsitview set adapter
            if (localAudioLVAdapter == null) {
                localAudioLVAdapter = new LocalAudioLVAdapter(mActivity, arrayList, null, mLvOnRequestVideoListener,mActivity.getTrajectoryHolder());
                lv_list.setAdapter(localAudioLVAdapter);
            }else {
                localAudioLVAdapter.setData(arrayList);
            }

            if (arrayList != null){

                //更新控件
                if (arrayList.size() <= 0) {
                    changeViewLv(false);
                    changeRelativeLayoutState(true);
                } else {
                    changeViewLv(true);
                    changeRelativeLayoutState(false);

                    if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE ){
                        ////////////////
                        //语音播放--语音传递的文件夹下面的第一首音乐
                        // playAudioByVoiceWithBean(arrayList.get(0));
                        /////////////////

                        ////////////////修改：语音【播放音乐】，调整为随机播放当前文件夹下面的文件。

                        final int lenth = arrayList.size();
                        int randomIndex = (int)(Math.random()  * lenth);
                        if (lenth == 0)
                        {
                            randomIndex = 0;
                        }else if (randomIndex >= lenth){
                            randomIndex = lenth -1;
                        }
                        LocalAudioBean localAudioBean = arrayList.get(randomIndex);
                        playAudioByVoiceWithBean(localAudioBean);
                        saveWatchHitoryToSp(localAudioBean);

                        MyLog.e(TAG, "showLvData()>>>>>getPath:" + localAudioBean.getPath());

                        ////////////////
                    }
                }
            }

        } else
        {//没有文件夹

            if (localAudioLVAdapter == null) {
                localAudioLVAdapter = new LocalAudioLVAdapter(mActivity, defaultArrayLists, null, mLvOnRequestVideoListener,mActivity.getTrajectoryHolder());
                lv_list.setAdapter(localAudioLVAdapter);
            }else {
                localAudioLVAdapter.setData(defaultArrayLists);
            }
            //更新控件
            if (defaultArrayLists.size() <= 0) {
                changeViewLv(false);
                changeRelativeLayoutState(true);
            } else {
                changeViewLv(false);
                changeRelativeLayoutState(false);

                //语音随机播放音乐
                if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE ){

                    //////////////////语音播放--语音传递的文件夹下面的第一首音乐
                    //playAudioByVoiceWithBean(defaultArrayLists.get(0));
                    ///////////////

                    ////////////////修改：语音【播放音乐】，调整为随机播放文件。

                    final int lenth = defaultArrayLists.size();
                    int randomIndex = (int)(Math.random()  * 10);
                    if (randomIndex >= lenth){
                        randomIndex = lenth -1;
                    }
                    LocalAudioBean localAudioBean = defaultArrayLists.get(randomIndex);
                    playAudioByVoiceWithBean(localAudioBean);
                    saveWatchHitoryToSp(localAudioBean);

                    MyLog.e(TAG, "showLvData()>>>>>getPath:" + localAudioBean.getPath());

                    ////////////////
                }
            }
        }

        Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = false;
        Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "";
    }

    private  Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == MSG_WHAT_FOLDER_LOAD_DATA)
            {
                MyLog.d(TAG,"mHandler>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>");
                if (childFolders.size() > 0 )
                {
                    changeLvFolder(true);
                    ///////////////
                    //childFolders.get(0).selected = true;
                    //ecLocalVideoFolderAdapter.setData(childFolders);
                    ///////////////

                    int moveToItemPosition = 0;
                    for (int i = 0 ; i<childFolders.size() ;i++)
                    {
                        EcVideoFolderBean bean  = childFolders.get(i);
                        if(bean.name.equals(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE))
                        {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            moveToItemPosition = i;

                            MyLog.d(TAG, "mHandler>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:"+bean.name);
                        }else {
                            bean.selected = false;
                        }
                    }
                    if (!containVoiceFolderFlag){//没有包含语音传递的文件夹，更新lv，第一个文件夹默认选中----------
                        moveToItemPosition = 0;
                        childFolders.get(0).selected = true;
                        currentFoldersName = childFolders.get(0).name;
                    }
                    ecLocalVideoFolderAdapter.setData(childFolders);
                    lv_act_local_video.setSelection(moveToItemPosition);
                    moveToItemPosition = 0;

                    containVoiceFolderFlag = false;//此时可以重置该值

                }else{
                    changeLvFolder(false);
                }

            }else if (what == MSG_WHAT_SHOW_LV_DATA)
            {
                MyLog.d(TAG,"mHandler>>>>>MSG_WHAT_SHOW_LV_DATA>>>");
                if(loadingDialog != null)
                {
                    loadingDialog.dismissLoadingDialog(loadingDialog);
                }

                showLvData();
            }
        }
    };

    /**
     * 供EcAudioActivity的onNewIntent调用；
     * 功能需求：：如果已经在【EcAudioActivity】界面，也要定位到语音文件夹
     */
    public void updateFoldersForVoice(){
        MyLog.d(TAG,">>>>>updateFoldersForVoice>>>>>>");
        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
    }
    /**
     * 供EcAudioActivity的onNewIntent调用；
     * 功能需求：：如果已经在【EcAudioActivity】界面，也要定位到语音文件夹,随机播放该文件夹下面的文件
     */
    public void updateRadomPlayFileUnderFolderForVoice(){

        MyLog.d(TAG,">>>>>updateRadomPlayFileUnderFolderForVoice>>>>>>");
        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);
    }

    /**
     *请求音频缩略图回调,对应listview
     */
    private LocalAudioLVAdapter.OnRequestVideoListener mLvOnRequestVideoListener = new LocalAudioLVAdapter.OnRequestVideoListener() {

        @Override
        public void onRequestImage(String imgPath, ImageView iv, int tagId) {
//            Glide.with(mActivity).load(Uri.fromFile(new File(imgPath))).transform(new GlideRoundTransform(mActivity,20)).into(iv);
            GlideUtils.load(mActivity, imgPath, iv);
        }

    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MyLog.e(TAG, "onHiddenChanged()--->>>>>hidden:" + hidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        MyLog.e(TAG, "onDestroyView()--->>>>>canLoadingData:" + canLoadingData);

        aiCount.set(0);

        mTimer.cancel();

        Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "";

        canLoadingData = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.e(TAG, "onDestroy()--->>>>>canLoadingData:" + canLoadingData);
    }
}
