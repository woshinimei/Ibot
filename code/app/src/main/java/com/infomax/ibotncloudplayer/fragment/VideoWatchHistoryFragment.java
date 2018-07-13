package com.infomax.ibotncloudplayer.fragment;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.infomax.ibotncloudplayer.QQCloudPlayerActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.VideoActivity;
import com.infomax.ibotncloudplayer.adapter.LocalVideoLVAdapter;
import com.infomax.ibotncloudplayer.adapter.QQCloudLvAdapter;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.*;
import com.infomax.ibotncloudplayer.view.LoadingDialog;
import com.ysx.qqcloud.QQCloudFileInfo;
import com.ysx.qqcloud.QQCloudObject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *1. Created by jy on 2016/10/20.<br/>
 *2. 视频播放历史记录 fragment;<br/>
 *3. 2018/3/13 【暂停使用云端腾讯后台视频】 隐藏按钮选择 。
 */
public class VideoWatchHistoryFragment extends Fragment {

    private final String TAG = VideoWatchHistoryFragment.class.getSimpleName();

    private RadioGroup radio_group;
    private VideoActivity mVideoActivity;
    private ListView lv_local;
    private ListView lv_cloud;

    private RelativeLayout rl_nodata;

    private SharedPreferences sp;
//    private LocalVideoHistoryLVAdapter localAdapter;
    private LocalVideoLVAdapter localAdapter;
    private QQCloudLvAdapter qqCloudLvAdapter;

    private int localHistorySize;
    private int cloudHistorySize;
    private QQCloudObject qqObject;

    private LoadingDialog loadingDialog;
    /**从SharedPreference得到的临时集合*/
    private LinkedList<LocalVideoBean> myTempList;
    /**
     * 本地locallist的点击监听
     */
    private AdapterView.OnItemClickListener localClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            List<LocalVideoBean> mItems = localAdapter.getData();
            if (position < mItems.size()) {
                LocalVideoBean bean = (LocalVideoBean) mItems.get(position);

                //check file
                if (!FileUtils.isFileExists(bean.getPath())) {

                    ToastUtils.showCustomToast(null, mVideoActivity.getString(R.string.text_tip_can_not_file_file));
                    return;
                }

                VideoEncryptUtils.processVideoEncryptFunction(mVideoActivity, mItems.get(position).getPath());

                Intent intent = new Intent(Intent.ACTION_VIEW);

                /////////////////////////////////
                //新增swf文件等判断-指定播放器播放
                File file = new File(bean.getPath());
                String tempFileName = bean.getDisplayName().toLowerCase();
                String MIMEType = FileMIMEUtils.getMIMEType(file);
                MyLog.d(TAG, ">>>>>>>>>onItemClick()>>>getDisplayName:" + bean.getDisplayName()
                        + ",MIMEType:" + MIMEType);
                intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), MIMEType);
                if (tempFileName.endsWith(".mp4")) {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    intent.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);
                    if (intent.resolveActivity(mVideoActivity.getPackageManager()) != null) {
                        mVideoActivity.startActivity(intent);
                        saveLocalWatchHitoryToSp(bean);

                        mVideoActivity.actStartLearn(new LearnTrajectoryBean(System.currentTimeMillis(),tempFileName,
                                LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO));
                    } else {
                        ToastUtils.showCustomToast(mVideoActivity.getString(R.string.tip_video_player_disable));
                    }
                } else {
                    //当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
                    if (intent.resolveActivity(mVideoActivity.getPackageManager()) != null) {
                        mVideoActivity.startActivity(intent);
                        saveLocalWatchHitoryToSp(bean);
                        mVideoActivity.actStartLearn(new LearnTrajectoryBean(System.currentTimeMillis(),tempFileName,
                                LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO));
                    } else {
                        if (tempFileName.endsWith(".swf")) {
                            ToastUtils.showCustomToast(mVideoActivity.getString(R.string.tip_swf_player_disable));
                        } else {
                            ToastUtils.showCustomToast(mVideoActivity.getString(R.string.tip_video_player_disable));
                        }
                    }
                }

            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mVideoActivity = (VideoActivity) getActivity();

        View view = View.inflate(mVideoActivity, R.layout.fragment_watch_historyl_video, null);

        initViews(view);

        registListener();

        initData();

        initFromMediaType();

        return view;
    }

    private void initViews(View view) {

        radio_group = (RadioGroup) view.findViewById(R.id.radio_group);
        //默认选中第一个
        radio_group.check(R.id.rb_local);
        lv_local = (ListView) view.findViewById(R.id.lv_list_local);
        lv_cloud = (ListView) view.findViewById(R.id.lv_list_cloud);

        rl_nodata = (RelativeLayout) view.findViewById(R.id.rl_nodata);

        loadingDialog = new LoadingDialog(mVideoActivity);
    }



    private void registListener() {
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeLvState(localHistorySize,cloudHistorySize,checkedId);
            }
        });

//        lv_local.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
        lv_cloud.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                boolean preventMultipleRun = Utils.preventMultipleRun();
                if (preventMultipleRun)
                {
                    ToastUtils.showToast(mVideoActivity, getString(R.string.click_too_fast));
                    return;
                }

                showLoadingDialog();

//              qqObject.getQQCloudGetVideoUrl(position, true);

                saveCloudWatchHitoryToSp(position);

                QQCloudFileInfo info = (QQCloudFileInfo)qqCloudLvAdapter.getItem(position);
                ToastUtils.showToastDebug(getActivity(), "" + info.fileName);

                MyLog.d(TAG, info.fileName+",info.fileId:"+info.fileId);
                File file20 = new File(info.localVideoURL20);
                File file30 = new File(info.localVideoURL30);
                if (file20.exists() /*&& file20.isFile() && info.fileSize.equals(file20.length())*/)
                {
                    dismissLoadingDialog();

                    MyLog.d(TAG, info.fileName + ",file:" + file20.getAbsolutePath());
                    playVideoDownloadOk(file20);
                    return;
                }else if (file30.exists() /*&& file30.isFile() && info.fileSize.equals(file30.length())*/){
                    dismissLoadingDialog();

                    MyLog.d(TAG, info.fileName + ",file:" + file30.getAbsolutePath());
                    playVideoDownloadOk(file30);

                }else {//不存在下载好的，就重新访问网络

                    qqObject.getQQCloudGetVideoUrl(info,true);
                }
            }
        });
    }

    public void initData() {

        localHistorySize = 0;
        cloudHistorySize = 0;

        /************本地历史记录********************/
        //保存bean到sp
        sp = mVideoActivity.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);
        //sp.edit().clear().commit();

        String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");

        MyLog.d(TAG, "---->>initData--->>local-->>:"+localList);
        if (TextUtils.isEmpty(localList))
        {
        }else
        {
            try {

                LinkedList<LocalVideoBean> tempList = (LinkedList<LocalVideoBean>) SharedPreferenceUtils.string2Object(localList);
                if (localAdapter == null)
                {
                    //localAdapter = new LocalVideoHistoryLVAdapter(mVideoActivity,tempList,lv_local,mOnRequestVideoListener);
                    localAdapter = new LocalVideoLVAdapter(mVideoActivity, tempList,  mOnRequestVideoListener);
                    //2017/7/20 将onclik移至fragment中 phc
                    lv_local.setOnItemClickListener(localClickListener);
                    lv_local.setAdapter(localAdapter);
                }else
                {
                    localAdapter.setData(tempList);
                }

                MyLog.d(TAG, "---->>local--->>tempList.size():"+tempList.size());

                localHistorySize = tempList.size();

            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
                localHistorySize = 0;
            }
        }

        /*******************云端历史记录 start *********************/

        String cloudList = sp.getString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST, "");

        MyLog.d(TAG, "---->>initData--->>cloudList-->>:" + cloudList);
        if (TextUtils.isEmpty(cloudList))
        {
        }else
        {
            try {

                LinkedList<QQCloudFileInfo> tempList = (LinkedList<QQCloudFileInfo>) SharedPreferenceUtils.string2Object(cloudList);
                if (qqCloudLvAdapter == null)
                {
                    qqCloudLvAdapter = new QQCloudLvAdapter(mVideoActivity,tempList);
                    lv_cloud.setAdapter(qqCloudLvAdapter);
                }else
                {
                    qqCloudLvAdapter.setList(tempList);
                }

                MyLog.d(TAG, "---->>cloud--->>tempList.size():"+tempList.size());

                cloudHistorySize = tempList.size();

            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
                cloudHistorySize = 0;
            }
        }

        changeLvState(localHistorySize, cloudHistorySize,/*R.id.rb_local*/radio_group.getCheckedRadioButtonId());

    }

//    private LocalVideoHistoryLVAdapter.OnRequestVideoListener  mOnRequestVideoListener = new LocalVideoHistoryLVAdapter.OnRequestVideoListener() {
//        @Override
//        public void onRequestImage(String imgPath, ImageView iv) {
//            GlideUtils.load(mVideoActivity, imgPath, iv);
//        }
//    };
    private LocalVideoLVAdapter.OnRequestVideoListener mOnRequestVideoListener = new LocalVideoLVAdapter.OnRequestVideoListener() {
        @Override
        public void OnRequestImage(String imgPath, ImageView iv) {
            GlideUtils.load(mVideoActivity, imgPath, iv);

        }
    };

//    private void changeLvState(int checkedId){
//        if (checkedId == R.id.rb_local)
//        {
//            lv_local.setVisibility(View.VISIBLE);
//            lv_cloud.setVisibility(View.GONE);
//        }else if (checkedId == R.id.rb_cloud)
//        {
//            lv_local.setVisibility(View.GONE);
//            lv_cloud.setVisibility(View.VISIBLE);
//        }
//
//    }
    private void changeLvState(int localHistorySize,int cloudHistorySize,int checkedId)
    {

        if (checkedId == R.id.rb_local)
        {
            if (localHistorySize > 0 )
            {
                lv_local.setVisibility(View.VISIBLE);
                lv_cloud.setVisibility(View.GONE);
                rl_nodata.setVisibility(View.GONE);
            }else
            {
                lv_local.setVisibility(View.GONE);
                lv_cloud.setVisibility(View.GONE);
                rl_nodata.setVisibility(View.VISIBLE);
            }
        }
        if (checkedId == R.id.rb_cloud)
        {
            if (cloudHistorySize > 0 )
            {
                lv_local.setVisibility(View.GONE);
                lv_cloud.setVisibility(View.VISIBLE);
                rl_nodata.setVisibility(View.GONE);
            }else
            {
                lv_local.setVisibility(View.GONE);
                lv_cloud.setVisibility(View.GONE);
                rl_nodata.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initFromMediaType()
    {
        registerBroadCastReceiver();

        qqObject = QQCloudObject.sharedInstance();
        if (qqObject == null)
        {
            qqObject = new QQCloudObject(mVideoActivity);
        }

        System.gc();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.d(TAG, "---->>>>onResume:");
    }

    /**保存 云端 播放历史 到sp*/
    private void saveCloudWatchHitoryToSp(int position){

        SharedPreferences sp = mVideoActivity.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);

        LinkedList<QQCloudFileInfo> tempList = null;

        String cloudlList = sp.getString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST, "");
        if (qqCloudLvAdapter == null) {// 2017-10-12 phc 预防空指针异常
            return;
        }
        QQCloudFileInfo currentBean = (QQCloudFileInfo)qqCloudLvAdapter.getItem(position);

        MyLog.d(TAG, "---->>mOnItemClickListener--->>cloud-->>:" + cloudlList);

        if (TextUtils.isEmpty(cloudlList))
        {
            LinkedList<QQCloudFileInfo> tempList1 = new LinkedList<QQCloudFileInfo>();
            tempList1.addFirst(currentBean);

            try {
                String string = SharedPreferenceUtils.object2String(tempList1);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST,string).commit();

            }catch (Exception e)
            {
                MyLog.d(TAG, "---->>cloud--->>Exception:" + e.getMessage());
            }
            tempList1 = null;

        }else
        {
            try {

                tempList = (LinkedList<QQCloudFileInfo>) SharedPreferenceUtils.string2Object(cloudlList);

                if (tempList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
                {
                    tempList.removeLast();
                }

                if (tempList.contains(currentBean))
                {
                    MyLog.d(TAG,"----包含--cloud-currentBean:"+currentBean.fileName);
                    tempList.remove(currentBean);
                    tempList.addFirst(currentBean);
                }else
                {
                    MyLog.d(TAG,"----不包含-cloud--currentBean:"+currentBean.fileName);
                    tempList.addFirst(currentBean);
                }

                String tempStr = SharedPreferenceUtils.object2String(tempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST,tempStr).commit();
            } catch (Exception e) {
                MyLog.d(TAG, "---->cloud--->>>Exception:" + e.getMessage());
            }
        }

        MyLog.d(TAG, "---->>cloud--->>tempList-->>:" + (tempList == null ? 0 : tempList.size()));
    }
    /**保存本地视频播放历史到sp*/
    private void saveLocalWatchHitoryToSp(LocalVideoBean bean){
        //保存bean到sp
        SharedPreferences sp = mVideoActivity.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);

        String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");
        LocalVideoBean currentBean = bean/*mItems.run(position)*/;

        MyLog.d(TAG, "---->>saveLocalWatchHitoryToSp--->>local-->>:" + localList);

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

        MyLog.d(TAG, "---->>saveLocalWatchHitoryToSp--->>myTempList-->>:" + (myTempList == null ? 0 : myTempList.size()));
    }
    private void playVideoDownloadOk(File file){
        //播放video
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
        if (intent.resolveActivity(mVideoActivity.getPackageManager()) != null) {
            mVideoActivity.startActivity(intent);
            mVideoActivity.actStartLearn(new LearnTrajectoryBean(System.currentTimeMillis(),
                    file.getName(),
                    LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO));
        } else {
            ToastUtils.showToast(mVideoActivity, mVideoActivity.getString(R.string.tip_video_player_disable));
        }
    }

    private void registerBroadCastReceiver(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_VIDEOURL);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_ERROR);
        mVideoActivity.registerReceiver(updateReceiver, intentFilter);
    }

    private void unregister() {
        mVideoActivity.unregisterReceiver(updateReceiver);
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "--->>>>>onReceive---->>>action:" + action);

            if (action.equals(QQCloudObject.MSG_QQCLOUD_VIDEOURL)){

                MyLog.d(TAG,"---收到广播了>>>MSG_QQCLOUD_VIDEOURL>>>:");

                dismissLoadingDialog();

                boolean isDownload = intent.getBooleanExtra("isDownload", false);
//                int index = intent.getIntExtra("videoIndex", -1);
//                if (index == -1)
//                {
//                    return;
//                }

//                Bundle myBundle = intent.getBundleExtra(Constant.MyIntentProperties.NAME_KEY_01);
                Bundle myBundle = intent.getExtras();
                if (myBundle != null)
                {
                    if (isDownload) {
                        Intent intentPlayer = new Intent();
                        intentPlayer.setClass(mVideoActivity, QQCloudPlayerActivity.class);
//                    intentPlayer.putExtra("videoIndex", index);
//                    intentPlayer.putExtra(Constant.MyIntentProperties.NAME_KEY_01,myBundle);//intentPlaye 写成 intent了
                        intentPlayer.putExtras(myBundle);
                        try {
                            startActivity(intentPlayer);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                    }

                }else
                {
                }
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_ERROR)) {

                MyLog.d(TAG, "---收到广播了>>>MSG_QQCLOUD_ERROR>>>:");

                dismissLoadingDialog();

                int errorCode = intent.getIntExtra("errorCode", 0);
                String errorMsg = intent.getStringExtra("errorMsg");

                ToastUtils.showToast(mVideoActivity, R.string.text_server_bus);

            }
        }};

    private void showLoadingDialog() {
        if (null != loadingDialog && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog.setOnDismissListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 是否启动video
     */
    private boolean isStartVideo;
    /**
     * 创建记录轨迹holder
     */
    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();



}
