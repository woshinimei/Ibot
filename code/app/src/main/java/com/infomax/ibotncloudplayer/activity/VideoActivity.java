package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.base.FullScreenFragmentActivity;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.fragment.CloudVideoFragment;
import com.infomax.ibotncloudplayer.fragment.LocalVideoFragment;
import com.infomax.ibotncloudplayer.fragment.VideoWatchHistoryFragment;
import com.infomax.ibotncloudplayer.service.ChangeVideoEncryptService;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.MyLog;

/**
 * 1.Created by jy on 2016/10/18.<br/>
 * 2.包含 本地 ，云端，历史记录 <br/>
 * 3.2018/3/13 【暂停使用云端腾讯后台视频】 隐藏按钮选择。<br/>
 */
public class VideoActivity extends /*BaseFragmentActiviy*/ FullScreenFragmentActivity {
    private final String  TAG = "VideoActivity";

    private RadioGroup radio_group;

    private LocalVideoFragment localVideoFragment;
    private CloudVideoFragment cloudVideoFragment;
    private VideoWatchHistoryFragment videoWatchHistoryFragment;

    /**local对应的 本地 切换按钮*/
    private Button btn_list_local;

    /**local对应的 云端 切换按钮*/
    private Button btn_list_for_cloud;

    /**
     * 学习轨迹holder
     */
    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyLog.d(TAG, ">>>>>savedInstanceState:" + savedInstanceState);

        setContentView(R.layout.activity_video);

        Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE= getIntent().getBooleanExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE, false);
        Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = getIntent().getStringExtra(Constant.MyIntentProperties.EXTRA_FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);
        if ( Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE)
        {
            //Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "国学";//test
        }

        MyLog.d(TAG, ">>>>>onCreate:START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE：" + Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE
                + ",FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE:"+ Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);

        initViews();
//        startVideoEncryptService();
        registListener();
    }
    private void startVideoEncryptService(){
        MyLog.d(TAG,"######## startVideoEncryptService:");
        Intent intent = new Intent(VideoActivity.this, ChangeVideoEncryptService.class);
        startService(intent);
    }
    private void initViews() {
        radio_group = (RadioGroup) findViewById(R.id.radio_group);

        //默认选中第一个
        radio_group.check(R.id.rb_local);

        //默认选中第一个localVideoFragment
        setFragmentTabSelection(R.id.rb_local);

        btn_list_local = (Button) findViewById(R.id.btn_list);
        btn_list_for_cloud = (Button) findViewById(R.id.btn_list_for_cloud);

        changeButton(R.id.btn_list,true);
        changeButton(R.id.btn_list_for_cloud,false);
    }

    public TextView getView(){
        return btn_list_local;
    }
    public TextView getViewForCloud(){
        return btn_list_for_cloud;
    }

    private void changeButton(int resId,boolean flag){
        if (resId == R.id.btn_list){
            if (btn_list_local != null){
                if (flag){
                    btn_list_local.setVisibility(View.VISIBLE);
                }else {
                    btn_list_local.setVisibility(View.GONE);
                }
            }

        } else if (resId == R.id.btn_list_for_cloud){
            if (btn_list_for_cloud != null){
                if (flag){
                    btn_list_for_cloud.setVisibility(View.VISIBLE);
                }else {
                    btn_list_for_cloud.setVisibility(View.GONE);
                }
            }
        }
    }

    private void registListener() {
        findViewById(R.id.back_button_container).setOnClickListener(mFunctionOnClickListener);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                setFragmentTabSelection(checkedId);
            }
        });

    }
    View.OnClickListener mFunctionOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.back_button_container:
                    VideoActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     *
     * @param tabSelection id resource id
     */
    private void setFragmentTabSelection(int tabSelection){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft =  fm.beginTransaction();

        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(ft);

        if (tabSelection == R.id.rb_local){//local
            if (localVideoFragment == null){
                localVideoFragment = new LocalVideoFragment();

                ft.add(R.id.fl_content,localVideoFragment);
            }
            else {
                ft.show(localVideoFragment);
            }

            changeButton(R.id.btn_list,true);
            changeButton(R.id.btn_list_for_cloud,false);
            radio_group.check(R.id.rb_local);
        }else if (tabSelection == R.id.rb_cloud){//cloud  //2018/3/13 【暂停使用云端腾讯后台视频】 隐藏按钮选择。
            if (cloudVideoFragment == null){
                cloudVideoFragment = new CloudVideoFragment();

                ft.add(R.id.fl_content,cloudVideoFragment);
            }else {
                ft.show(cloudVideoFragment);

                //切换CloudVideoFragment时也调用，重新加载 commented out
                //cloudVideoFragment.initData(); //commented out
            }

            changeButton(R.id.btn_list,false);
            changeButton(R.id.btn_list_for_cloud,true);
        }else if (tabSelection == R.id.rb_history){//
            if (videoWatchHistoryFragment == null){
                videoWatchHistoryFragment = new VideoWatchHistoryFragment();
                ft.add(R.id.fl_content, videoWatchHistoryFragment);
            }else {
                ft.show(videoWatchHistoryFragment);
                MyLog.d(TAG,"---->>EducationContentActivity--->>videoWatchHistoryFragment.initData()");
                //重新加载历史记录
                videoWatchHistoryFragment.initData();
            }

            changeButton(R.id.btn_list,false);
            changeButton(R.id.btn_list_for_cloud,false);
        }

        ft.commit();
    }

    private void hideFragments(FragmentTransaction ft) {
        if (localVideoFragment != null){
            ft.hide(localVideoFragment);
        }
        if (cloudVideoFragment != null){
            ft.hide(cloudVideoFragment);
        }
        if (videoWatchHistoryFragment != null){
            ft.hide(videoWatchHistoryFragment);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.e(TAG, "onNewIntent()-->>>>>>>>>>>>>:");

        if(intent != null){
            //成长中心--教育视频--视频--云端--语音指令：播放视频；应跳传到本地视频界面去；
            setFragmentTabSelection(R.id.rb_local);

            Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE= intent.getBooleanExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE, false);
            Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = intent.getStringExtra(Constant.MyIntentProperties.EXTRA_FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);
            if ( Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE)
            {
                //Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE = "国学";//test
            }

            MyLog.d(TAG, ">>>>>onNewIntent:START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE：" + Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE
                    + ",FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE:"+ Constant.FOLDER_START_CLOUDPLAYER_VIDEO_BY_VOICE);
            if (localVideoFragment != null)
            {
                localVideoFragment.updateFoldersForVoice();
                localVideoFragment.updateRadomPlayFileUnderFolderForVoice();
            }
        }
    }

    @Override
    protected void onDestroy() {
        LearnTrajectoryUtil.sendBro(this, trajectoryHolder);
        super.onDestroy();
        MyLog.e(TAG,"onDestroy()>>>>>>>>>>>>>>>:");

        ///////
//        if (localVideoFragment == null){
//            //向下传递
//            localVideoFragment.onDestroy();
//
//            MyLog.e(TAG,"onDestroy()-->>>>>>>>>>>>>:");
//        }
        //////////

        Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE = false;
        MyLog.e(TAG,"onDestroy()-->>>>>START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE>>>>>>>>:"+Constant.MyIntentProperties.START_CLOUDPLAYER_VIDEO_BY_VOICE_VALUE);

    }

    /**
     *  Android的Fragment中onActivityResult不被调用的解决方案
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (localVideoFragment == null){
            //调用Fragment的onActivityResult
            localVideoFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void actStartLearn(LearnTrajectoryBean bean){
        if (trajectoryHolder != null) {
            trajectoryHolder.startLearn(bean);
        }
    }

    public void actEndLearn(){
        if (trajectoryHolder != null) {
            trajectoryHolder.endLearn();
        }
    }
}
