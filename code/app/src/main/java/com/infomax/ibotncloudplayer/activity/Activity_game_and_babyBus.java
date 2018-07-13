package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.adapter.IconGvAdater;
import com.infomax.ibotncloudplayer.bean.AppInfoBean;
import com.infomax.ibotncloudplayer.bean.Icon;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.AppUtils;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by hushaokun on 2018/6/7.
 */

public class Activity_game_and_babyBus extends FullScreenActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemClickListener {

    TextView tvBack;
    GridView gv;
    RadioGroup rg;
    List<Icon> applist = new ArrayList<>();//所有的应用
    List<Icon> rblist = new ArrayList<>();//适合各周岁的应用


    IconGvAdater gvAdater;
    private String[] apkPackageArray = new String[]{
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_QIYIVIDEO,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_ANGRYBRID,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_ORGANIZED,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_BATHING,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_CHEF,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_NUMBER,
            Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_BABYHOSPITAL};

    //    R.drawable.selector_scratchjr_bg,//编程游戏icon  Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR,
    private int[] imgRes = new int[]{
            R.drawable.qiyi_video,
            R.drawable.app_angry_brid
            , R.drawable.babybus_oganized
            , R.drawable.babybus_bathing
            , R.drawable.babybus_chef
            , R.drawable.babybus_number
            , R.drawable.babybus_babyhospital};
    private int[] strRes = new int[]{
            R.string.qiyi_video,
            R.string.angry_brid
            , R.string.babybus_oganized
            , R.string.babybus_bathing
            , R.string.babybus_chef
            , R.string.babybus_number
            , R.string.babybus_babyhospital};
    private ExecutorService loadApkService;
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


    private void addViewForPosition(final int position) {
        int imgRe = imgRes[position];
        int str = strRes[position];
        applist.add(new Icon(getString(str), imgRe));
    }

    private void initApk() {
        applist.clear();
        Icon iconBrowser = new Icon(getString(R.string.browser), R.drawable.selector_browser_bg);//浏览器
        applist.add(iconBrowser);
        loadApkService = Executors.newSingleThreadExecutor();
        loadApk();
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
                            addViewForPosition(i);
                        }
                    }
                }
                mHandler.sendEmptyMessage(1);
            }
        });

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                rblist.clear();
                rblist.addAll(applist.subList(0, applist.size() - 2));
                gvAdater = new IconGvAdater(rblist, getBaseContext());
                gv.setAdapter(gvAdater);
//                Log.e("-----app---", applist.size() + "");
            }
        }
    };

    //根据包名跳转到应用
    private void startActForPack(String apkName) {
        String packUrl = "";
        for (int i = 0; i < strRes.length; i++) {
            String name = getString(strRes[i]);
            if (name.equals(apkName)) {
                packUrl = apkPackageArray[i];
                break;
            }
        }
//        Log.e("---packUrl--", packUrl + "--");
        Intent intent = getPackageManager().getLaunchIntentForPackage(packUrl);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_and_babybus);
        initView();
        initClickListener();
        initApk();
    }


    private void initClickListener() {
        rg.setOnCheckedChangeListener(this);
        tvBack.setOnClickListener(this);
    }

    private void initView() {
        tvBack = (TextView) findViewById(R.id.tv_back);
        gv = (GridView) findViewById(R.id.gv_content);
        rg = (RadioGroup) findViewById(R.id.rg);
        rg.check(R.id.rb0);
        gv.setOnItemClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        switch (i) {
            case R.id.rb0:
                rblist.clear();
                rblist.addAll(applist.subList(0, applist.size() - 2));
                gvAdater.notifyDataSetChanged();
                break;
            case R.id.rb1:
                rblist.clear();
                rblist.add(applist.get(applist.size() - 1));
                gvAdater.notifyDataSetChanged();
                break;
            case R.id.rb2:
                rblist.clear();
                gvAdater.notifyDataSetChanged();
                break;
            case R.id.rb3:
                rblist.clear();
                gvAdater.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String name = rblist.get(i).getName();
        Log.e("--name---", name + "");
        if (name.equals(getString(R.string.browser))) {//打开浏览器
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
        } else {//打开其他apk
            startActForPack(name);
        }
    }
}
