package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.AppInfoBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.AppUtils;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends FullScreenActivity {

    private String[] apkPackageArray = new String[]{Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_QIYIVIDEO,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_ANGRYBRID,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_ORGANIZED,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_BATHING,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_CHEF,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_NUMBER,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_BABYHOSPITAL};
    private int[] imgRes = new int[]{R.drawable.selector_scratchjr_bg, R.drawable.qiyi_video, R.drawable.app_angry_brid
            , R.drawable.babybus_oganized
            , R.drawable.babybus_bathing
            , R.drawable.babybus_chef
            , R.drawable.babybus_number
            , R.drawable.babybus_babyhospital};
    private int[] strRes = new int[]{R.string.intelligence_game, R.string.qiyi_video, R.string.angry_brid
            , R.string.babybus_oganized
            , R.string.babybus_bathing
            , R.string.babybus_chef
            , R.string.babybus_number
            , R.string.babybus_babyhospital};
    private ExecutorService loadApkService;
    private LinearLayout llContent;
    /**
     * 是否启动浏览器
     */
    private boolean isStartBrowser = false;

    /**
     * 是否运行益智游戏
     */
    private boolean isStartPuzzle = false;
    /**
     * 学习轨迹实体
     */
    private LearnTrajectoryBean trajectoryBean = new LearnTrajectoryBean();


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int position = msg.what;
            addViewForPosition(position);
            return false;
        }
    });

    private void addViewForPosition(final int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_game, (ViewGroup) getWindow().getDecorView(), false);
        ImageView img = (ImageView) view.findViewById(R.id.img_game);
        TextView textView = (TextView) view.findViewById(R.id.text_game);
        Log.e("----position-----", position + "");
        img.setImageDrawable(ContextCompat.getDrawable(this, imgRes[position]));
        textView.setText(strRes[position]);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActForPack(apkPackageArray[position]);
            }
        });
        llContent.addView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStartBrowser) {
            trajectoryBean.setEndTime(System.currentTimeMillis());
            LearnTrajectoryUtil.sendBro(this, trajectoryBean);
            isStartBrowser = false;
        } else if (isStartPuzzle) {
            trajectoryBean.setEndTime(System.currentTimeMillis());
            LearnTrajectoryUtil.sendBro(this, trajectoryBean);
            isStartPuzzle = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
    }

    private void init() {
        loadApkService = Executors.newSingleThreadExecutor();
        llContent = (LinearLayout) findViewById(R.id.ll_content);
//        View llActPuzzle = findViewById(R.id.ll_act_puzzle);
//        View llActQiyi = findViewById(R.id.ll_act_qiyi);

        loadApk();
//        displayIcon(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR, llActPuzzle);
//        displayIcon(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_QIYIVIDEO, llActQiyi);


    }

    /**
     * 加载apk
     */
    private void loadApk() {
        loadApkService.submit(new Runnable() {
            @Override
            public void run() {
                List<AppInfoBean> allAppInfo = AppUtils.getAllAppInfo(getApplicationContext());

                for (AppInfoBean bean : allAppInfo) {
                    for (int i = 0; i < apkPackageArray.length; i++) {

                        if (TextUtils.equals(bean.packageName, apkPackageArray[i])) {
                            Log.e("--bean.packageName--", bean.packageName + "");
                            Log.e("--i---", i + "");
                          /*  if (TextUtils.equals(Utils.getDeviceSerial(),"0016112910036")){//为指定机器才显示除了编程游戏之外的游戏
//                            if (TextUtils.equals(Utils.getDeviceSerial(),"0016112910056")){//test
                                mHandler.sendEmptyMessage(i);
                            }else if (i == 0){*/
                            mHandler.sendEmptyMessage(i);
//                            }
                        }
                    }
                }
            }
        });

    }

    private void displayIcon(final String apkPackage, final View view) {

    }

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.back_button_container:
                finish();
                break;
            case R.id.iv_return_home:
                returnHome();
                break;
            case R.id.ll_act_browser:
                //intent = getPackageManager().getLaunchIntentForPackage(Constant.APK_PACKAGENAME_ANDROID_SYSTEM_BROWSER);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                trajectoryBean.setName(getString(R.string.browser));
                trajectoryBean.setStartTime(System.currentTimeMillis());
                trajectoryBean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_BROWSER);
                if (intent.resolveActivity(this.getPackageManager()) != null) {
                    Uri content_url = Uri.parse(Constant.Config.DEFAULT_URL_FOR_BROWSER);
                    intent.setData(content_url);
                    if (intent.resolveActivity(this.getPackageManager()) != null) {
                        startActivity(intent);
                        isStartBrowser = true;
                    } else {
                        Intent intent2 = getPackageManager().getLaunchIntentForPackage(Constant.ThirdPartAppPackageName.APK_PACKAGENAME_ANDROID_SYSTEM_BROWSER);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (intent.resolveActivity(this.getPackageManager()) != null) {
                            startActivity(intent2);
                            isStartBrowser = true;
                        } else {
                            ToastUtils.showCustomToast(null, "Uninstall browser");
                        }
                    }
                } else {
                    ToastUtils.showCustomToast(null, "Uninstall browser");
                }

                break;

        }
    }

    private void returnHome() {
        ////////////// old code
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        ///////////////

        //////////
        finish();
        ///////////////
        //moveTaskToBack(true);
    }

    private void startActForPack(String apkName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(apkName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (TextUtils.equals(apkName, Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR)) {
                //记录学习轨迹
                isStartPuzzle = true;
                trajectoryBean.setName(getString(R.string.intelligence_game));
                trajectoryBean.setStartTime(System.currentTimeMillis());
                trajectoryBean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_PUZZLE);

            }
        } else {
            ToastUtils.showCustomToast(null, "please install the game of  :" + apkName);
        }
    }
}
