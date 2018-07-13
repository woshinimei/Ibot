package com.infomax.ibotncloudplayer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.base.BaseActivity;
import com.infomax.ibotncloudplayer.utils.AppUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.view.WiperSwitch;

/**
 * Created by jy on 2017/3/3 ;10:06.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 * 设置activity
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG = SettingActivity.class.getSimpleName();

    private LinearLayout back_button_container;
    private WiperSwitch wiperSwitch_act_setting;
    private TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews() {
        tv_version = (TextView) findViewById(R.id.tv_version);
        wiperSwitch_act_setting = (WiperSwitch) findViewById(R.id.wiperSwitch_act_setting);
        back_button_container = (LinearLayout) findViewById(R.id.back_button_container);
    }

    @Override
    protected void registerLinstener() {
        super.registerLinstener();

        back_button_container.setOnClickListener(this);

        wiperSwitch_act_setting.setOnChangedListener(new WiperSwitch.OnChangedListener() {
            @Override
            public void OnChanged(WiperSwitch wiperSwitch, boolean checkState) {

               SharedPreferenceUtils.setSwitchStateForAutomaticPhoto(mContext,checkState);

            }
        });
    }

    @Override
    protected void initData() {

        /////////////////加载版本号
        tv_version.setText(AppUtils.getAppVersionName(mContext));

        ////
        wiperSwitch_act_setting.setChecked(SharedPreferenceUtils.getSwitchStateForAutomaticPhoto(mContext));
        ////
    }

    @Override
    public void onClick(View v) {

//        if (v.getId() == back_button_container.getId())
//        {
//            finish();
//        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG,">>>>>>onDestroy()>>>");
        overridePendingTransition(0, R.anim.zoom_out);
    }
}
