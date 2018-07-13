package com.infomax.ibotncloudplayer.fragment;

import android.content.*;
import android.database.Cursor;
import android.media.MediaScannerConnection;
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
import com.infomax.ibotncloudplayer.activity.VideoActivity;
import com.infomax.ibotncloudplayer.adapter.EcLocalVideoFolderAdapter;
import com.infomax.ibotncloudplayer.adapter.LocalVideoGVAdapter;
import com.infomax.ibotncloudplayer.adapter.LocalVideoLVAdapter;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.*;
import com.infomax.ibotncloudplayer.view.LoadingDialog;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jy on 2016/10/18.<br/>
 * 教育内容-本地视频 fragment <br/>
 */
public class LocalVideoFragment extends Fragment {
    final String  TAG = LocalVideoFragment.class.getSimpleName();

//    private final String filePath = File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY";
//    /**
//     * 教育内容-本地视频文件视频路径
//     */
//    private final String filePath = Constant.Config.Education_Content_Video_File_Root_Path;
    private LayoutInflater myInflater;
    private Context ctx;
    /**
     * 默认集合，当前目录下没有文件夹时，使用该集合
     */
    ArrayList<LocalVideoBean> defaultArrayLists = new ArrayList<LocalVideoBean>();
    private GridView gv_act_local_video;
    /**
     * 左侧文件夹列表
     */
    private ListView lv_act_local_video;
    private ListView lv_list;//右侧listview，列表展示

    private LinearLayout ll_act_local_video;
    private ImageView iv_act_local_video;
    private TextView tv_additional;
    /**
     * VIDEO文件夹下面的所有一级文件夹集合
     */
    private LinkedList<EcVideoFolderBean> childFolders = new LinkedList<EcVideoFolderBean>();
    /**
     * VIDEO文件夹下面的所有一级文件夹集合,内容和childFolders第一复制后的一样；只是以后不会变化，而childFolders根据需要随时变更
     */
    private LinkedList<EcVideoFolderBean> unChangeChildFolders = new LinkedList<EcVideoFolderBean>();

    /**
     * VIDEO文件夹下面的所有一级文件夹，记录sd卡下面真实的个数
     */
    private int childFoldersRealSizeFormSd = 0;

    /**
     * 文件夹名称作为key,其下面的视频集合作为value。
     */
    private HashMap<String,ArrayList<LocalVideoBean>> hashMap = new HashMap<String,ArrayList<LocalVideoBean>>();

    /** 文件夹列表适配器 */
    private EcLocalVideoFolderAdapter ecLocalVideoFolderAdapter;

    private LocalVideoGVAdapter localVideoGVAdapter ;

    private LocalVideoLVAdapter localVideoLVAdapter;

    private VideoActivity mVideoActivity;

    /** 依附的activity的btn*/
    private Button btn_list_from_activity;

    private RelativeLayout  rl_nodata;

    /** 是否显示列表状态 默认false 为九宫格*/
    private boolean isStateList;

    /**从SharedPreference得到的临时集合*/
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

    /**可以加载文件的标志；当fragment销毁，就重置该值为false*/
    private  boolean canLoadingData = false;

    /**
     * 实际访问到的文件个数
     */
    private int fileNumber;

    private ScanSdReceiver scanSdReceiver;

    /**
     *手机测试视频文件路径
     */
    private final String pathShouJiTest = "/storage/emulated/0/DCIM/Camera/";

    /**
     * preInitData()执行次数
     */
    private AtomicInteger preInitDataInvokeCount = new AtomicInteger(0);

    /**
     * 包含语音传递的文件夹状态
     */
    private boolean containVoiceFolderFlag = false;
    @Override
    public void onResume() {
        super.onResume();
        if (mVideoActivity != null) {
            mVideoActivity.actEndLearn();
        }
    }@Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myInflater = inflater;

        ctx = getActivity();

        loadingDialog = new LoadingDialog(ctx);

        mVideoActivity = (VideoActivity) getActivity();

        isStateList = false;

//      View view = inflater.inflate(R.layout.fragment_local_video,container,false);
        View view = View.inflate(getActivity(),R.layout.fragment_local_video,null);
        initViews(view);

        setAdapter();

        registListener();

        registerReceiver();

        preInitData();

        return view;
    }

    private void setAdapter() {

    }

    private void initViews(View view) {
        view.findViewById(R.id.rl_act_local_video).setOnClickListener(mFunctionOnClickListener);

        gv_act_local_video = (GridView) view.findViewById(R.id.gv_act_local_video);
        lv_act_local_video = (ListView) view.findViewById(R.id.lv_act_local_video);
        lv_list = (ListView) view.findViewById(R.id.lv_list);
        ll_act_local_video = (LinearLayout) view.findViewById(R.id.ll_act_local_video);
        iv_act_local_video = (ImageView) view.findViewById(R.id.iv_act_local_video);
        rl_nodata = (RelativeLayout) view.findViewById(R.id.rl_nodata);
        tv_additional = (TextView) view.findViewById(R.id.tv_additional);

        btn_list_from_activity = (Button) mVideoActivity.getView();

        changeViewGvOrLv(isStateList);

        ecLocalVideoFolderAdapter = new EcLocalVideoFolderAdapter(ctx,childFolders,R.layout.item_lv_common);
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

                ecLocalVideoFolderAdapter.setData(childFolders);

                ArrayList<LocalVideoBean> arrayList = (ArrayList<LocalVideoBean>) hashMap.get(childFolders.get(position).name);
//                localVideoGVAdapter = new LocalVideoGVAdapter(ctx, arrayList, gv_act_local_video, mOnRequestVideoListener);
//                gv_act_local_video.setAdapter(localVideoGVAdapter);

                if (localVideoGVAdapter != null) {
                    localVideoGVAdapter.setData(arrayList);
                }

                if (localVideoLVAdapter != null) {
                    localVideoLVAdapter.setData(arrayList);
                }

                if (arrayList.size() <= 0) {
                    changeViewGvAndLv(false);
                    changeRelativeLayoutState(true);
                } else {
                    changeViewGvOrLv(isStateList);
                    changeRelativeLayoutState(false);
                }
            }
//            }
        });

        //切换list
        if (btn_list_from_activity != null){

            btn_list_from_activity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isStateList = !isStateList;
                    changeViewGvOrLv(isStateList);
                }
            });
        }

        //listview 事件
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocalVideoBean bean = (LocalVideoBean) localVideoLVAdapter.getItem(position);

                VideoEncryptUtils.processVideoEncryptFunction(ctx, bean.getPath());

                //播放video
                Intent intent = new Intent(Intent.ACTION_VIEW);

                //////////////////////////// 旧的播放视频方式
//                String tempDisplayName = bean.getDisplayName().toLowerCase();
//                if (tempDisplayName.endsWith("mp4"))
//                {
////                    intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/*");//commented out 该方式会调用视频及flash播放器
//                        intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/mp4");
//                }else if (tempDisplayName.endsWith("swf"))
//                {
//                        intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "application/x-shockwave-flash");
//                }else {
//                    intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/*");
//                }
                /////////////////////////////////
                /////////////////////////////////
                //新增swf文件等判断-根据MIME type指定播放器播放
                File file = new File(bean.getPath());
                String tempFileName = bean.getDisplayName().toLowerCase();

                String MIMEType = FileMIMEUtils.getMIMEType(file);
                MyLog.d(TAG,">>>>>>>>>setOnItemClickListener()>>>getDisplayName:" + bean.getDisplayName()
                        +",MIMEType:" +  MIMEType );
                intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), MIMEType);

                if (tempFileName.endsWith(".mp4")) {
                    intent.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code101);
                        saveWatchHitoryToSp(bean);
                        addTrajectoryOfAct(bean.getDisplayName(),System.currentTimeMillis());

                    } else {
                        ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                    }
                }else {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code101);
                        saveWatchHitoryToSp(bean);
                        addTrajectoryOfAct(bean.getDisplayName(),System.currentTimeMillis());


                    } else {
                        if (tempFileName.endsWith(".swf"))
                        {
                            ToastUtils.showCustomToast(getString(R.string.tip_swf_player_disable));
                        }else {
                            ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                        }
                    }
                }
            }
        });
        gv_act_local_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocalVideoBean bean = (LocalVideoBean) localVideoGVAdapter.getItem(position);
                VideoEncryptUtils.processVideoEncryptFunction(ctx, bean.getPath());

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
                    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code102);
                        saveWatchHitoryToSp(bean);
                        addTrajectoryOfAct(bean.getDisplayName(),System.currentTimeMillis());

                    } else {
                        ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
                    }
                }else {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                        startActivityForResult(intent, Request_Code102);
                        saveWatchHitoryToSp(bean);
                        addTrajectoryOfAct(bean.getDisplayName(),System.currentTimeMillis());

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

    /**
     * 用act的记录轨迹holder记录下轨迹
     * @param displayName
     * @param currentTimeMillis
     */
    private void addTrajectoryOfAct(String displayName, long currentTimeMillis) {
        LearnTrajectoryBean bean = new LearnTrajectoryBean();
        bean.setName(displayName);
        bean.setStartTime(currentTimeMillis);
        bean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO);
        mVideoActivity.actStartLearn(bean);
    }

    /**
     * playVideoByVoiceWithBean 语音自动播放视频时 调用
     * @param bean
     */
    private void playVideoByVoiceWithBean(LocalVideoBean bean){
        VideoEncryptUtils.processVideoEncryptFunction(ctx, bean.getPath());

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
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                startActivityForResult(intent, Request_Code102);
                saveWatchHitoryToSp(bean);
            } else {
                ToastUtils.showCustomToast(getString(R.string.tip_video_player_disable));
            }
        }else {
            //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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

    private void saveWatchHitoryToSp(LocalVideoBean bean){
        //保存bean到sp
        SharedPreferences sp = ctx.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);

        String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");
        LocalVideoBean currentBean = bean/*mItems.run(position)*/;

        MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>local-->>:" + localList);

        if (TextUtils.isEmpty(localList))
        {
            LinkedList<LocalVideoBean> tempList = new LinkedList<LocalVideoBean>();
            tempList.addFirst(currentBean);

            try {
                String string = SharedPreferenceUtils.object2String(tempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,string).commit();

            }catch (Exception e)
            {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

            }
            tempList = null;

        }else
        {
            try {

                myTempList = (LinkedList<LocalVideoBean>) SharedPreferenceUtils.string2Object(localList);

                if (myTempList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
                {
                    myTempList.removeLast();
                }

                if (myTempList.contains(currentBean))
                {
                    MyLog.d(TAG,"----contain---currentBean:"+currentBean.getDisplayName());
                    myTempList.remove(currentBean);
                    myTempList.addFirst(currentBean);
                }else
                {
                    MyLog.d(TAG,"----uncontain---currentBean:"+currentBean.getDisplayName());
                    myTempList.addFirst(currentBean);
                }

                String tempStr = SharedPreferenceUtils.object2String(myTempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,tempStr).commit();
            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
            }
        }

        MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>myTempList-->>:" + (myTempList == null ? 0 : myTempList.size()));
    }

    /** true为列表，false 显示九宫格*/
    private  void changeViewGvOrLv(boolean flag){
        if (flag){
            btn_list_from_activity.setText(getString(R.string.text_palace));
            gv_act_local_video.setVisibility(View.GONE);
                lv_list.setVisibility(View.VISIBLE);
        }else {
            btn_list_from_activity.setText(getString(R.string.text_list));
                gv_act_local_video.setVisibility(View.VISIBLE);
            lv_list.setVisibility(View.GONE);
        }

    }
    /** true listview,gridview 都显示 */
    private  void changeViewGvAndLv(boolean show){
        if (show){
            gv_act_local_video.setVisibility(View.VISIBLE);
            lv_list.setVisibility(View.VISIBLE);
        }else {
            gv_act_local_video.setVisibility(View.GONE);
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
     * 根标志是否显示文件夹 true 显示。false,隐藏
     * @param flag
     */
    private  void changeLvFolder(final boolean flag){

//        Message msg = Message.obtain();
//        msg.what = MSG_WHAT_FOLDER_LOAD_DATA;
//        msg.obj = flag;
//
//        mHandler.sendMessage(msg);

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
    /////////////////
//    /**
//     * 初始化数据钱的准备工作
//     */
//    public void preInitData() {
//        Log.d(TAG, "->>>preInitData:");
//        loadingDialog.showLoadingDialog(loadingDialog);
//        canLoadingData = true;
//
////        scanDirAsync(ctx,Constant.Config.Education_Content_Video_File_Root_Path);
//
////        scanDirByApi(ctx,null);
//
//        initData();
//    }
    ////////////////////

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
                initData();

            }
        }
    };

    /**
     * 立马打开播放去，播放器加载视频文件不能显示图标<br/>
     * 二次优化处理 ，单在LocalVideoFragment中添加进度提示经测试是不够的(v1.1.5及之前的版本都是这样处理的)。<br/>
     * 因为此时用户开机立即点击，系统数据库没有加载完成所有视频等文件。<br/>
     * 使用开机ibotncloudplayer启动后添加计时器，计时时间180s。180s期间，用户点击播放器【音乐/视频】，以【系统文件初始化中....】提示给用户。如果此时用户语音播放【音乐/视频】；如果此时用户遥控播放【音乐/视频】--待添加中，也都给同样的提示。<br/>
     */
    public void preInitData(){

        MyLog.d(TAG, ">>>>>preInitData()>>>>preInitDataInvokeCount:" + preInitDataInvokeCount.get()) ;

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
                if (preInitDataInvokeCount.getAndAdd(1) == 0)
                {
                    mTimer.schedule(mTimerTask, 1000, 1000);
                }
            }else {
                initData();
            }*/

            initData();
        }else {//外置sd卡是否可用

            initData();
        }
    }
    ////////////////////////////////////// 新方式加载数据--根据server中计时器处理/////////end

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

        new Thread(){
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

                if (!canLoadingData){
                    return;
                }

                dealRelationDataAfterRecursion();

            }
        }.start();
    }


    /**
     *注册广播
     */
    public void registerReceiver(){
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentfilter.addDataScheme("file");
        if (scanSdReceiver == null){
            scanSdReceiver = new ScanSdReceiver();
        }
        ctx.registerReceiver(scanSdReceiver, intentfilter);
    }
    /**
     *反注册广播
     */
    public void unRegisterReceiver(){
        if (scanSdReceiver != null)
        {
            ctx.unregisterReceiver(scanSdReceiver);
        }
    }

    /**
     * 发送扫描指定目录的广播<br/>
     * TODO 此广播对ibton系统 不起作用
     * @param ctx
     * @param scanDirectory
     */
    public void scanDirAsync(Context ctx, String scanDirectory) {
        try {
//            Intent scanIntent = new Intent(Constant.Config.ACTION_MEDIA_SCANNER_SCAN_DIR);
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(new File(scanDirectory)));
            ctx.sendBroadcast(scanIntent);
        }catch (Exception e)
        {
            MyLog.e(TAG,e.getMessage());
        }
    }

    /**
     * 通过MediaScanner提供的API接口，扫描媒体文件。
     这种扫描媒体文件的方式是同步的，扫描工作将会阻塞当前的程序进程。当扫描少量文件，且要求立即获取扫描结果的情况下，适合使用该扫描方式。
     在扫描媒体文件前，程序应该根据终端当前的语言环境正确设置MediaScanner的语言环境设置, 避免产生编解码的错误:
     * @param ctx
     * @param scanDirectory
     */
    public void scanDirByApi(Context ctx, String scanDirectory) {
        try {
            android.media.MediaScannerConnection scanner = new android.media.MediaScannerConnection(ctx,new MediaScannerNotifier() );
            Locale locale = ctx.getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            String country = locale.getCountry();
        }catch (Exception e)
        {
            MyLog.e(TAG,e.getMessage());
        }
    }
    public class MediaScannerNotifier implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mConnection;
        private int mScannedFilesInProgress = 0;

        public MediaScannerNotifier() {
            mConnection = new MediaScannerConnection(ctx, this);
            mConnection.connect();
        }

        @Override
        public void onMediaScannerConnected() {

            MyLog.d(TAG, "onMediaScannerConnected -> ");
            if(mConnection.isConnected())
            {
                mConnection.scanFile(Constant.Config.Education_Content_Video_File_Root_Path, null);
                mScannedFilesInProgress++;
            }

        }

        @Override
        public void onScanCompleted(String text, Uri uri) {

            //ibotn 主机运行程序 可以回调，onScanCompleted -> content://media/external/file/702
            //手机测试，该方法不能回调。

            MyLog.d(TAG, "onScanCompleted -> " + uri.toString());

            mScannedFilesInProgress--;
            mConnection.disconnect();

        }

    }

    /**
     *发送扫描SD卡文件的广播<br/>
     * 扫描指定文件 <br/>
     */
    public void sendBroadcast(){
        try {
            ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" +/* Constant.Config.Education_Content_Video_File_Root_Path*/Environment.getExternalStorageDirectory())));

        }catch (Exception e)
        {
            MyLog.e(TAG,e.getMessage());
        }
    }

    /**
     * 扫描sd卡完成后的广播接受者
     */
    public class ScanSdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            MyLog.d(TAG, "action>>>>>>>>>>>>>" + intent.getAction());
            if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())){
                MyLog.d(TAG,"scanner_started>>>>>>>>>>>>>");
            }else if (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE.equals(intent.getAction()))
            {
                MyLog.d(TAG,"scanner_scan_file.... ing>>>>>>>>>>>>>");
            }else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction()))
            {
                initData();
            }
        }
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
     * 1.根据用户级别过滤文件夹；<br/>
     * 2.add 2016-12-9. OTA升级后，之前的机器，播放器文件夹都不显示了。<br/>
     * 最后解决方案: 不改变旧的sd卡
     * 新增级别文件夹配置文件expand_authorityfolder.properties
     *且都不带清华二字。如果通过authorityfolder.properties得到的文件夹集合比对后，只含有一个ibotn文件夹。
     *  就再次读取authorityfolder_expand.properties，并比较文件夹集合。
     */
    private void filterFolderWithLevel(){

//        Constant.Video_Folder_Authority_Level = SharedPreferenceUtils.getUserLevel(ctx);
        formConfigLevelFolderList = PropertiesUtils.get(Constant.Video_Folder_Authority_Level,Constant.Config.AUTHORITYFOLDER_PATH);

         LinkedList<EcVideoFolderBean> tempFolderBeans = new LinkedList<>();//用来复制childFolders到临时集合中
        for (EcVideoFolderBean bean : childFolders)
        {
            tempFolderBeans.add(bean);
        }

        //此时先清空集合
        childFolders.clear();

        for (EcVideoFolderBean bean : tempFolderBeans)
        {

            for (String name : formConfigLevelFolderList)
            {
                if (bean.name != null && bean.name.equals(name))
                {
                    childFolders.add(bean);//如果formConfigLevelFolderList中的文件夹，与复制的临时文件夹集合中的某个文件夹同名，就将该文件夹添加进来
                }
            }

        }
        MyLog.e(TAG, "filterFolderWithLevel()>>>>>folder num>>>>：" + childFolders.size()
                +",childFoldersRealSizeFormSd:"
                +childFoldersRealSizeFormSd
                +",unChangeChildFolders:"+unChangeChildFolders.size()
                +",Video_Folder_Authority_Level:"+Constant.Video_Folder_Authority_Level
                );

        //////修改新增日期 2016-12-9.。OTA升级后，之前的机器，播放器文件夹都不显示了.最后解决方案://////start//////

        if (childFolders.size() == 1 && childFoldersRealSizeFormSd >1)
        {
        //如果childFolders.size() == 1，根据现有情况判断(同时考虑新旧sd卡视频目录只有一个文件夹情况【只有一个就不在执行】)：是没有清华二字的旧的sd卡
            /////////////////////////与上面的代码类似/////start//////////
            formConfigLevelFolderList = PropertiesUtils.get(Constant.Video_Folder_Authority_Level,Constant.Config.EXPAND_AUTHORITYFOLDER_PATH);

            tempFolderBeans = new LinkedList<>();//用来复制childFolders到临时集合中
            for (EcVideoFolderBean bean : /*childFolders*/unChangeChildFolders)
            {
                tempFolderBeans.add(bean);
            }

            //此时先清空集合
            childFolders.clear();

            for (EcVideoFolderBean bean : tempFolderBeans)
            {
                for (String name : formConfigLevelFolderList)
                {
                    if (bean.name != null && bean.name.equals(name))
                    {
                        childFolders.add(bean);//如果formConfigLevelFolderList中的文件夹，与复制的临时文件夹集合中的某个文件夹同名，就将该文件夹添加进来
                    }
                }

            }
            MyLog.e(TAG, "filterFolderWithLevel()>>>>>childFolders>>>>："+childFolders.size() );
            /////////////////////////与上面的代码类似//////end///////////////

        }

        //////修改新增日期2016-12-9 。 OTA升级后，之前的机器，播放器文件夹都不显示了.最后解决方案://end//////////////////////////

        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);

    }
    /**
     * 新的方式根据用户级别过滤文件夹；<br/>
     * 1.根据用户权限如果为1（默认都为1），显示文件夹由【原来的固定十个】调整为【总文件夹的一半】;如果文件总个数 <=5。将全部显示，就不做一半处理。
     *  这样的好处：就不怕sd卡文件夹名称修改了。<br/>
     * 2、如果权限为2，就显示文件夹，由【原来的固定二十七个】调整为【总文件夹】<br/>
     */
    private void filterFolderWithLevelEnhance(){

//        Constant.Video_Folder_Authority_Level = SharedPreferenceUtils.getUserLevel(ctx);
        LinkedList<EcVideoFolderBean> tempFolderBeans = new LinkedList<>();//用来复制childFolders到临时集合中

        for (EcVideoFolderBean bean : childFolders)
        {
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
                        +",childFoldersRealSizeFormSd:"
                        +childFoldersRealSizeFormSd
                        +",unChangeChildFolders:"+unChangeChildFolders.size()
                        +",Video_Folder_Authority_Level:"+Constant.Video_Folder_Authority_Level
        );

        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
    }

    /**
     *
     * @param folderRootPath
     * 使用ContentResolver方式加载文件，该方式，需要收到sd卡扫描完成的广播后才能获取到内容。
     */
    private void getLocalVideosWithContentResolver(String folderRootPath){

        MyLog.d(TAG, ">>>getLocalVideosWithContentResolver>>>>>mPath：" + folderRootPath );

        /**文件夹是否有对应视频文件，false为没有*/
//        boolean flagCurrentFolderHasVideo = false;

        long time = SystemClock.currentThreadTimeMillis();

        defaultArrayLists.clear();

        StringBuilder selection = new StringBuilder();
        //查询条件是视频文件。对于swf文件是无效的---。
        selection.append("(" + MediaStore.Video.Media.DATA + " LIKE '" + folderRootPath +File.separator+ "%')");
        MyLog.d(TAG, ">>>>>>>>>>>>" + selection.toString());
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = ctx.getContentResolver();
            cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext() && canLoadingData) {

                    fileNumber++ ;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    ////////////////
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
//                String album = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
//                String artist = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
//                String mimeType = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
//                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

//                    Log.d(TAG, "->>>cursor:" + path);
                    //////////
                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName))
                    {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith(".mp4") )//过滤mp4文件
                        {
                            LocalVideoBean bean = new LocalVideoBean(id,date,path,displayName,size);

                            if (tempDisplayName.endsWith(".mp4"))
                            {//给bean的ThumbnailPath赋值  [只对mp4]
                                FileForBitmapUtils.createPngForMP4(bean);
                            }

//                            Log.d(TAG, "->>>bean.getThumbnailPath():" + bean.getThumbnailPath());
//                            MyLog.d(TAG, ">>getLocalVideosWithContentResolver()>>>>getPath:" + bean.getPath());

                            defaultArrayLists.add(bean);

                            //分文件夹遍历文件，存入对应集合中
                            for(EcVideoFolderBean folderBean : childFolders){

                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4

                                int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                String tempPath = bean.getPath().substring(0,i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
//                                MyLog.d(TAG, ">>>getLocalVideosWithContentResolver()>>>>tempPath():" + tempPath
//                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
//                                    flagCurrentFolderHasVideo = true;
                                    //将当前bean添加到hashMap中key为folderName
                                    hashMap.get(folderBean.name).add(bean);
                                }

                                String tempFolderName = childFolders.get(0).name;

                                if(tempFolderName.toLowerCase().startsWith("ibotn"))
                                {
                                    String fileName =  bean.getDisplayName().toLowerCase();
                                    if (fileName.startsWith("ibotn")){
                                        MyLog.d(TAG, ">>>>>>>>ibotn:" + bean.getPath());
                                        SharedPreferenceUtils.setIbotnFile(ctx,bean.getPath());
                                        FileEnhancedUtils.dealFileForLevel(ctx, SharedPreferenceUtils.getIbotnFile(ctx));
                                    }
                                }
                            }
                        }
                    }

                }
            }else{
                MyLog.e(TAG, "Exception--->>cursor is null:");
            }

        }catch (Exception e){
            e.printStackTrace();

            MyLog.e(TAG, "Exception--->>:" + e.getMessage());

//            flagCurrentFolderHasVideo = false;
            defaultArrayLists.clear();
        }finally {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        //退出activity时flagLoadingFileData,上面的cursor遍历就停止。下面代码立即执行
        if (!canLoadingData){
            return;
        }

       /* if (!flagCurrentFolderHasVideo){
            if (childFolders.size() > 0){
                if (hashMap.get(childFolders.get(0).name) != null){
                    hashMap.get(childFolders.get(0).name).clear();
                    hashMap.get(childFolders.get(0).name).addAll(defaultArrayLists);//第一个文件夹添加数据
                }
            }
        }*/

        MyLog.e(TAG, "file fileNumber>>>>>>>>>>>>>>>>>>>:"+ fileNumber);

        time = SystemClock.currentThreadTimeMillis() - time;

        MyLog.e(TAG, "elapsedRealtime(ms)>>>>>>>>>>>>>>>>>>>:" + (time));

        if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 0)
        {//此情况下再根据用户级别过滤文件夹

            /////////
//            filterFolderWithLevel();
            /////////

            ////////使用新的方式过了文件夹。
            filterFolderWithLevelEnhance();
            /////////

        }else if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1) {
            mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
        }

       /* if(Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1){
//        获取swf文件// TODO: 2017/2/24 新增过滤添加swf文件
            addSwfFiles(new File(Constant.Config.Education_Content_Video_File_Root_Path));
        }*/
        //所有版本都添加swf文件
        addSwfFiles(new File(Constant.Config.Education_Content_Video_File_Root_Path));

        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_GV_LV_DATA);

    }

    /**
     *
     * @param folderRootPath
     * 使用递归方式直接遍历文件夹。
     */
    private synchronized void getLocalVideosWithRecursion(String folderRootPath){

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
                        getLocalVideosWithRecursion(tempFile.getAbsolutePath());

                    }else {
                        String displayName = tempFile.getName();
                        if (!TextUtils.isEmpty(displayName)){
                            int id = 0;
                            long date = 0;
                            String path = tempFile.getAbsolutePath();
                            String tempDisplayName = displayName.toLowerCase();
                            long size = tempFile.length();
                            boolean existFileType = false;
                            for (String fileType : Constant.CONFIG_LOAD_VIDEO_TYPES)
                            {
                                if (tempDisplayName.endsWith(fileType))
                                {
                                    existFileType = true;
                                }
                            }
                            if (existFileType)
                            {
                                LocalVideoBean bean = new LocalVideoBean(id,date,path,displayName,size);

                                /////////close this way to generate png
                                if (tempDisplayName.endsWith(".mp4")){//mp4文件才创建缩略图
                                    FileForBitmapUtils.createPngForMP4(bean);
                                }
                                ////////

                                defaultArrayLists.add(bean);

                                for(EcVideoFolderBean folderBean : childFolders){

                                    int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                    String tempPath = bean.getPath().substring(0,i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
//                                MyLog.d(TAG, ">>>getLocalVideosWithContentResolver()>>>>tempPath():" + tempPath
//                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                    if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
                                        hashMap.get(folderBean.name).add(bean);
                                    }

                                    String tempFolderName = childFolders.get(0).name;

                                    if(tempFolderName.toLowerCase().startsWith("ibotn"))
                                    {
                                        String fileName =  bean.getDisplayName().toLowerCase();
                                        if (fileName.startsWith("ibotn")){
                                            MyLog.d(TAG, ">>>>>>>>ibotn:" + bean.getPath());
                                            SharedPreferenceUtils.setIbotnFile(ctx,bean.getPath());
                                            FileEnhancedUtils.dealFileForLevel(ctx, SharedPreferenceUtils.getIbotnFile(ctx));
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
    private void dealRelationDataAfterRecursion(){

        //////////////调整时间 TODO 20170613 原定制版的不分权限来显示视频文件夹个数。依然不变：【只需：tf卡 ibotn简介文件夹放置不同权限的视频文件】根据权限过滤文件夹
        if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 0)
        {//此情况下再根据用户级别过滤文件夹

            /////////
            //filterFolderWithLevel();
            /////////

            ////////使用新的方式过了文件夹。
            filterFolderWithLevelEnhance();
            /////////

        }else if (Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1) {
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
     * 递归遍历指定目录-下面的所有文件。此时只是为了得到swf文件。【flash】文件。<br/>
     * 1.该方法从20170613起不再使用。
     * @param file
     */
    private void addSwfFiles(File file) {
        MyLog.d(TAG, ">>>>addSwfFiles>>>");
        File flist[] = file.listFiles();
        if (flist == null || flist.length == 0) {
            return ;
        }
        for (File f : flist) {
            if (f.isDirectory()) {

                MyLog.d(TAG, ">>>>isDirectory>>>" + f.getAbsolutePath());
                addSwfFiles(f);
            } else
            {
                MyLog.d(TAG, "" + f.getAbsolutePath());

                ///////////////////分文件夹遍历文件，存入对应集合中
                //获取swf文件
                LocalVideoBean bean = new LocalVideoBean(0,0,f.getAbsolutePath(),f.getName(),f.length());
                String tempDisplayNameLower = bean.getDisplayName().toLowerCase();
                if (tempDisplayNameLower.endsWith(".swf"))
                {
                    for(EcVideoFolderBean folderBean : childFolders){

                        //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4 ；only for test// TODO: 2017/2/24
                        int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                        String tempPath = bean.getPath().substring(0,i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
                        if (tempPath.endsWith(folderBean.name)){//只要文件路径（不包含文件名称）包含文件夹集合中的一项
                            //将当前bean添加到hashMap中key为folderName
                            hashMap.get(folderBean.name).add(bean);
                        }
                    }
                }
                ///////////////////分文件夹遍历文件，存入对应集合中
            }
        }
    }

    private  void showGvLvData(){
        if (childFolders.size() > 0)
        {//有文件夹
            ArrayList<LocalVideoBean> arrayList = null;
            if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE)
            {//获取语音传递的文件夹，命令词即【播放 + 文件夹】
                //注意：：原有的【播放音乐】命令要保持不变。即此时没有文件夹
                if (TextUtils.isEmpty(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE))
                {

                    //////////////// 随机切换文件夹，
                    final int lenth = childFolders.size();
                    int randomIndex = (int)(Math.random()  * lenth);
                    if (lenth == 0){
                        randomIndex = 0;

                    }else if (randomIndex >= lenth){
                        randomIndex = lenth -1;
                    }
                    final  String randomFolderName = childFolders.get(randomIndex).name;
                    arrayList = (ArrayList<LocalVideoBean>) hashMap.get(randomFolderName);
                    int moveToItemPosition = 0;
                    for (int i = 0 ; i<childFolders.size() ;i++)
                    {
                        EcVideoFolderBean bean  = childFolders.get(i);
                        if(bean.name.equals(randomFolderName))
                        {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            moveToItemPosition = i;

                            MyLog.d(TAG, "showGvLvData>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:"+bean.name);
                        }else {
                            bean.selected = false;
                        }
                    }
                    ecLocalVideoFolderAdapter.setData(childFolders);
                    lv_act_local_video.setSelection(moveToItemPosition);
                    moveToItemPosition = 0;
                    //////////////////
                }else{
                    arrayList = (ArrayList<LocalVideoBean>) hashMap.get(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);
                }
            }else {
                ////不是语音时，默认加载第一个文件夹下的视频
                arrayList = (ArrayList<LocalVideoBean>) hashMap.get(childFolders.get(0).name);
            }
            MyLog.d(TAG,">>>>showGvLvData()>>START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE:" + Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE
                    + ",FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE:" + Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE
                    + ",arrayList:" + (arrayList == null ? "null": arrayList.size()));

            if (localVideoGVAdapter == null) {
                localVideoGVAdapter = new LocalVideoGVAdapter(ctx, arrayList, gv_act_local_video, mOnRequestVideoListener);
                gv_act_local_video.setAdapter(localVideoGVAdapter);
            }else {
                localVideoGVAdapter.setData(arrayList);
            }

            //lsitview set adapter
            if (localVideoLVAdapter == null) {
                localVideoLVAdapter = new LocalVideoLVAdapter(ctx, arrayList, mLvOnRequestVideoListener);
                lv_list.setAdapter(localVideoLVAdapter);
            }else {
                localVideoLVAdapter.setData(arrayList);
            }
            if (arrayList != null){
                //更新控件
                if (arrayList.size() <= 0) {
                    changeViewGvAndLv(false);
                    changeRelativeLayoutState(true);
                } else {
                    //根据用户是否点击‘列表，九宫格’而显示lv,gv
                    changeViewGvOrLv(isStateList);
                    changeRelativeLayoutState(false);
                    if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE ){
                        ////////////////修改：语音【播放音乐】，调整为随机播放当前文件夹下面的文件。
                        final int lenth = arrayList.size();
                        int randomIndex = (int)(Math.random()  * lenth);
                        if (lenth == 0)
                        {
                            randomIndex = 0;
                        }else if (randomIndex >= lenth){
                            randomIndex = lenth -1;
                        }
                        LocalVideoBean bean = arrayList.get(randomIndex);
                        playVideoByVoiceWithBean(bean);

                        MyLog.e(TAG, "showGvLvData()>>>>>getPath:" + bean.getPath());
                    }
                }
            }
        } else
        {//没有文件夹
            if (localVideoGVAdapter == null) {
                localVideoGVAdapter = new LocalVideoGVAdapter(ctx, defaultArrayLists, gv_act_local_video, mOnRequestVideoListener);
                gv_act_local_video.setAdapter(localVideoGVAdapter);
            }

            if (localVideoLVAdapter == null) {
                localVideoLVAdapter = new LocalVideoLVAdapter(ctx, defaultArrayLists, mLvOnRequestVideoListener);
                lv_list.setAdapter(localVideoLVAdapter);
            }
            //更新控件
            if (defaultArrayLists.size() <= 0) {
                changeViewGvAndLv(false);
                changeRelativeLayoutState(true);
            } else {
                //根据用户是否点击‘列表，九宫格’而显示lv,gv
                changeViewGvOrLv(isStateList);
                changeRelativeLayoutState(false);
                //语音随机播放
                if (Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE ){

                    ////////////////修改：语音【播放】，调整为随机播放文件。

                    final int lenth = defaultArrayLists.size();
                    int randomIndex = (int)(Math.random()  * 10);
                    if (randomIndex >= lenth){
                        randomIndex = lenth -1;
                    }
                    LocalVideoBean bean = defaultArrayLists.get(randomIndex);
                    playVideoByVoiceWithBean(bean);

                    MyLog.e(TAG, "showGvLvData()>>>>>getPath:" + bean.getPath());

                    ////////////////
                }
            }
        }
        Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE= false;
        Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "";
    }

    private  Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == MSG_WHAT_FOLDER_LOAD_DATA)
            {
                if (childFolders.size() > 0 )
                {
                    changeLvFolder(true);
                    int moveToItemPosition = 0;
                    for (int i = 0 ; i<childFolders.size() ;i++)
                    {
                        EcVideoFolderBean bean  = childFolders.get(i);
                        if(bean.name.equals(Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE))
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
                    }
                    ecLocalVideoFolderAdapter.setData(childFolders);
                    lv_act_local_video.setSelection(moveToItemPosition);
                    moveToItemPosition = 0;

                    containVoiceFolderFlag = false;//此时可以重置该值
                }else{
                    changeLvFolder(false);
                }
            }else if (what == MSG_WHAT_SHOW_GV_LV_DATA)
            {
                Log.d(TAG, ">>>initData()>>>MSG_WHAT_SHOW_GV_LV_DATA:" + MSG_WHAT_SHOW_GV_LV_DATA);

                if (loadingDialog != null)
                {
                    loadingDialog.dismissLoadingDialog(loadingDialog);
                }
                showGvLvData();
            }
        }
    };

    /**
     * 供VideoActivity的onNewIntent调用；
     * 功能需求：：如果已经在【EcAudioActivity】界面，也要定位到语音文件夹
     */
    public void updateFoldersForVoice(){
        MyLog.d(TAG,">>>>>updateFoldersForVoice>>>>>>");
        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
    }
    /**
     * 供VideoActivity的onNewIntent调用；
     * 功能需求：：如果已经在【VideoActivity】界面，也要定位到语音文件夹,随机播放该文件夹下面的文件
     */
    public void updateRadomPlayFileUnderFolderForVoice(){

        MyLog.d(TAG,">>>>>updateRadomPlayFileUnderFolderForVoice>>>>>>");
        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_GV_LV_DATA);
    }

    /**
     *请求视频缩略图回调，对应gridview
     */
    private LocalVideoGVAdapter.OnRequestVideoListener mOnRequestVideoListener = new LocalVideoGVAdapter.OnRequestVideoListener() {

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
            GlideUtils.load(ctx, imgPath, iv);
//            GlideUtils.loadWithListener(ctx,imgPath,iv);
        }

    };
    /**
     *请求视频缩略图回调,对应listview
     */
    private LocalVideoLVAdapter.OnRequestVideoListener mLvOnRequestVideoListener = new LocalVideoLVAdapter.OnRequestVideoListener() {

        @Override
        public void OnRequestImage(String imgPath, ImageView iv) {
            ///////////
//            Glide.with(ctx).load(Uri.fromFile(new File(imgPath))).transform(new GlideRoundTransform(ctx,20)).into(iv);
            //////////
            GlideUtils.load(ctx, imgPath, iv);
        }

    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MyLog.e(TAG, "onHiddenChanged()--->>>>>canLoadingData:" + canLoadingData
                + ",hidden:" + hidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.e(TAG, "onDestroyView()--->>>>>canLoadingData:" + canLoadingData);

        preInitDataInvokeCount.set(0);
        mTimer.cancel();
        Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "";
        canLoadingData = false;

        unRegisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MyLog.e(TAG,"onDestroy()--->>>>>canLoadingData:"+ canLoadingData);
    }
}
