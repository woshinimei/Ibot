package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.base.FullScreenFragmentActivity;
import com.infomax.ibotncloudplayer.fragment.AudioWatchHistoryFragment;
import com.infomax.ibotncloudplayer.fragment.LocalAudioFragment;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.MyLog;

/**
 * Created by juying on 2016-12-12  <br/>
 * 1.教育内容-本地音频 Activity     <br/>
 * 1.包含： 本地音频，及其历史记录 <br/>
 */
public class EcAudioActivity extends /*BaseFragmentActiviy*/ FullScreenFragmentActivity {
    private final String  TAG = EcAudioActivity.class.getSimpleName();

    private RadioGroup radio_group;

    private LocalAudioFragment localAudioFragment;
    private AudioWatchHistoryFragment audioWatchHistoryFragment;

    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ec_audio);

        Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = getIntent().getBooleanExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE, false);
        Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = getIntent().getStringExtra(Constant.MyIntentProperties.EXTRA_FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);
        if ( Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE)
        {
//            Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "国学";//test
        }

        MyLog.d(TAG, "-->>>>>onCreate:START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE：" + Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE
                + ",FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE:"+ Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);

        initViews();

        registListener();

        ///////////
        //testCrash();
        //////////
    }

    /**
     * only for test
     */
    private void testCrash() {

        int a = 10/0;

    }

    private void initViews() {
        radio_group = (RadioGroup) findViewById(R.id.radio_group);

        //默认选中第一个
        radio_group.check(R.id.rb_local);

        //默认选中第一个localAudioFragment
        setFragmentTabSelection(R.id.rb_local);

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

    /**
     * 该activity已经打开，用户通过语音，再次唤起该activity。【根据lancher中而定，lancher中是使用startActivity这种方式】
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /////////// donot use this code
       /* if (!Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER){
            return;
        }*/
        /////////

        /**
         *   当调用到onNewIntent(intent)的时候，需要在onNewIntent() 中使用setIntent(intent)赋值给Activity的Intent.
         *   否则，后续的getIntent()都是得到老的Intent。
         *
         */
        if (intent != null)
        {
            //如果当前在【播放记录】界面，语音播放音乐时应该切换到【音乐】界面
            setFragmentTabSelection(R.id.rb_local);

            Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = intent.getBooleanExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE, false);
            Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = intent.getStringExtra(Constant.MyIntentProperties.EXTRA_FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);
            if ( Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE)
            {
                //Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "谜语";//only for test
            }

            MyLog.d(TAG, ">>>>>onNewIntent:START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE：" + Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE
                    + ",FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE:"+ Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);

            if (localAudioFragment != null)
            {
                //////////////////
                //localAudioFragment.initData(); // TODO: 2017/3/9
                /////////////////

                localAudioFragment.updateFoldersForVoice();
                localAudioFragment.updateRadomPlayFileUnderFolderForVoice();
            }
        }

        setIntent(intent);

        MyLog.d(TAG, ">>>>>onNewIntent>>>>>>>START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE:" + Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (trajectoryHolder != null) {
            trajectoryHolder.endLearn();
        }
        MyLog.d(TAG, ">>>>>onResume>>>>>>");
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.d(TAG, ">>>>>onStart>>>>>>");
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.d(TAG, ">>>>>onStop>>>>>>");
    }

    View.OnClickListener mFunctionOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.back_button_container:
                    EcAudioActivity.this.finish();
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
            if (localAudioFragment == null){
                localAudioFragment = new LocalAudioFragment();

                ft.add(R.id.fl_content,localAudioFragment);
            }
            else {
                ft.show(localAudioFragment);
            }
            radio_group.check(R.id.rb_local);

        }else if (tabSelection == R.id.rb_history){
            if (audioWatchHistoryFragment == null){
                audioWatchHistoryFragment = new AudioWatchHistoryFragment();
                ft.add(R.id.fl_content, audioWatchHistoryFragment);
            }else {
                ft.show(audioWatchHistoryFragment);
                MyLog.d(TAG, "-->>audioWatchHistoryFragment.initData()");
                //加载历史记录数据
                audioWatchHistoryFragment.initData();
            }
        }

        ft.commit();
    }

    private void hideFragments(FragmentTransaction ft) {
        if (localAudioFragment != null){
            ft.hide(localAudioFragment);
        }
        if (audioWatchHistoryFragment != null){
            ft.hide(audioWatchHistoryFragment);
        }
    }

    /**
     * 获取act的TrajectoryHolder实体
     * @return
     */
    public LearnTrajectoryUtil.LearnTrajectoryHolder getTrajectoryHolder() {
        return trajectoryHolder;
    }

    @Override
    protected void onDestroy() {
        LearnTrajectoryUtil.sendBro(this, trajectoryHolder);
        super.onDestroy();

//        if (localAudioFragment == null){
//            //向下传递
//            localAudioFragment.onDestroy();
//
//        }
        Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = false;
        MyLog.e(TAG,"onDestroy()-->>>>>START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE>>>>>>>>:"+Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE);
    }

    /**
     *  Android的Fragment中onActivityResult不被调用的解决方案
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (localAudioFragment == null){
            //调用Fragment的onActivityResult
            localAudioFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
