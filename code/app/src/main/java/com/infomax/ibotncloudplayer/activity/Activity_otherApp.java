package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import static com.infomax.ibotncloudplayer.R.id.tv_brower;

/**
 * Created by hushaokun on 2018/6/12.
 */

public class Activity_otherApp extends FullScreenActivity implements View.OnClickListener {
    TextView tvBack;
    TextView tvBrower;
    TextView tvExtralCard;
    TextView tvHelp;
    /**
     * 是否启动浏览器
     */
    private boolean isStartBrowser = false;
    /**
     * 学习轨迹实体
     */
    private LearnTrajectoryBean trajectoryBean = new LearnTrajectoryBean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otherapp);
        initView();
        setOnclickLisenter();
    }

    private void setOnclickLisenter() {
        tvBack.setOnClickListener(this);
        tvHelp.setOnClickListener(this);
        tvExtralCard.setOnClickListener(this);
        tvBrower.setOnClickListener(this);
    }

    private void initView() {
        tvBack = (TextView) findViewById(R.id.tv_back);
        tvBrower = (TextView) findViewById(R.id.tv_brower);
        tvExtralCard = (TextView) findViewById(R.id.tv_extralcard);
        tvHelp = (TextView) findViewById(R.id.tv_help);
    }

    /**
     * check usb device [要根据当前定制的Android设备而定]
     */
    private Boolean checkExtalCard() {
        DevicePath device = new DevicePath(this);
        //根据是否有U盘，显示控件
        if (TextUtils.isEmpty(device.getUsbStoragePath())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_brower:
                initBrower();
                break;
            case R.id.tv_help:
                Toast.makeText(getBaseContext(), "尚未开通，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_extralcard:
                if (checkExtalCard()) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_FILE_EXPLORER);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Constant.EXTRA_START_APP_FLAG, Constant.START_BY_IBOTNCLOUDPLAYER);//启动FileExplorer,FileExployer根据这个标志设置可以操作的控件
                        // TODO: 2017/4/1
                        startActivity(intent);
                    } else {
                        ToastUtils.showCustomToast(null, "Please install FileExplorer");
                    }
                } else {
                    Toast.makeText(getBaseContext(), "未检测到有外置卡", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initBrower() {

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
    }
}
