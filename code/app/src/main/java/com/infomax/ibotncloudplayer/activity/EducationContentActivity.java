package com.infomax.ibotncloudplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.MediaManager;
import com.infomax.ibotncloudplayer.MultimediaListActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.AppInfoBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.*;
import com.yuncunchu.YunCunChuUtils;

import java.util.List;

/**
 * Created by jy on 2016/10/14.<br/>
 * 1.包含本地视频，可扩展<br/>
 * 2.add 本地音乐 功能 2016-12-12<br/>
 */
public class EducationContentActivity extends /*Activity*/FullScreenActivity {

    final String  TAG = EducationContentActivity.class.getSimpleName();

    private LinearLayout ll_act_ec_game_wawalu;

    private Activity mActivity;
    /**
     * 娃娃路轨迹记录bean
     */
    private LearnTrajectoryBean wawaluTraBean;
    /**
     * 是否启动娃娃路
     */
    private boolean isStartWawaLu = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educationcontent);
        Log.d(TAG, "onCreate");

        mActivity = this;

        initViews();
        initData();
    }

    private void initViews() {
        Log.d(TAG, "initViews");
        findViewById(R.id.ll_audio).setOnClickListener(mFunctionOnClickListener);//本地视频
        findViewById(R.id.ll_video).setOnClickListener(mFunctionOnClickListener);//视频
        findViewById(R.id.ll_act_more_cloud_video_container).setOnClickListener(mFunctionOnClickListener);//在线视频
        findViewById(R.id.back_button_container).setOnClickListener(mFunctionOnClickListener);
        findViewById(R.id.iv_return_home).setOnClickListener(mFunctionOnClickListener);
        ll_act_ec_game_wawalu = (LinearLayout) findViewById(R.id.ll_act_ec_game_wawalu);

    }

    private void initData() {

        //////////旧的方式
        //如果是定制版，显示游戏模块
//        if(Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1){
//            ll_act_ec_game_wawalu.setVisibility(View.VISIBLE);
//            ll_act_ec_game_wawalu.setOnClickListener(mFunctionOnClickListener);//娃娃路游戏
//        }else {
//            ll_act_ec_game_wawalu.setVisibility(View.GONE);
//        }
        /////////

        //根据包名显示应用入口 ；新的方式
        ThreadUtils.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                List<AppInfoBean> allAppInfo = AppUtils.getAllAppInfo(getApplicationContext());
                for (AppInfoBean bean: allAppInfo) {

                    if (mActivity.isDestroyed()){
                        break;
                    }

                    if (Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_WAWALU.equals(bean.packageName)){
                        ThreadUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ll_act_ec_game_wawalu.setVisibility(View.VISIBLE);
                                ll_act_ec_game_wawalu.setOnClickListener(mFunctionOnClickListener);//娃娃路游戏
                            }
                        });
                        break;
                    }
                }
            }
        });
        ////////////////
//        testYcc();
        /////////////////
        /////////////////测试上传
//        NetUtils.uploadGenerateProps();
        /////////////////
    }

    View.OnClickListener mFunctionOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId())
            {
                case R.id.ll_audio: //音频
                    intent = new Intent(EducationContentActivity.this,EcAudioActivity.class);
//                    intent.putExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE,true);//TODO 测试语音播放音乐
                    startActivity(intent);
                    break;
                case R.id.ll_video://视频
                        intent = new Intent(EducationContentActivity.this,Activity_icon.class);
//                        intent = new Intent(EducationContentActivity.this,VideoActivity.class);
                        startActivity(intent);
                    break;
                case R.id.ll_act_more_cloud_video_container:
                    openCloudMedia();//打开在线视频
                    break;
                case R.id.back_button_container:
                    returnHome();
                    break;
                case R.id.iv_return_home:
                    returnHome();
                    break;
                case R.id.ll_act_ec_game_wawalu:
                    intent = getPackageManager().getLaunchIntentForPackage(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_WAWALU);
                    if (intent != null)
                    {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //记录进入娃娃路轨迹
//                        startActivityForResult(intent, LearnTrajectoryUtil.Constant.REQUEST_CODE_WAWALU);
                        isStartWawaLu = true;
                        if (wawaluTraBean == null) {
                            wawaluTraBean = new LearnTrajectoryBean();
                        }
                        wawaluTraBean.setStartTime(System.currentTimeMillis());
                        wawaluTraBean.setName(getString(R.string.text_game_wawalu));
                        wawaluTraBean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_EDU_WAWALU);
                    }else {
                        ToastUtils.showCustomToast(null, getString(R.string.text_tip_install_wawalu));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void openCloudMedia() {
        Intent intent = new Intent(EducationContentActivity.this, MultimediaListActivity.class);
        intent.putExtra(MediaManager.KEY_MEDIA_TYPE, MediaManager.MEDIA_TYPE_CLOUD_VIDEO);
        startActivity(intent);
    }
    private void returnHome()
    {
        finish();
    }

    private void testYcc(){
        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU,TAG+">>>>testYcc()>>>");
        YunCunChuUtils.loginAuthorization(2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStartWawaLu) {
            wawaluTraBean.setEndTime(System.currentTimeMillis());
            LearnTrajectoryUtil.sendBro(this, wawaluTraBean);
            isStartWawaLu = false;
        }
        MyLog.d(TAG, ">>>>onResume()>>>");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.d(TAG, ">>>>onNewIntent()>>>");
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.d(TAG, ">>>>onStop()>>>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG, ">>>>onDestroy()>>>");
    }
}
