package com.infomax.ibotncloudplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.infomax.ibotncloudplayer.activity.EducationContentActivity;
import com.infomax.ibotncloudplayer.activity.GameActivity;
import com.infomax.ibotncloudplayer.activity.SettingActivity;
import com.infomax.ibotncloudplayer.growthalbum.GrowthAlbumHomeActivity;
import com.infomax.ibotncloudplayer.service.IbotnCoreService;
import com.infomax.ibotncloudplayer.utils.AppUtils;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.NetUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.view.BottonEditTextCommonDialog;
import com.map.helper.baidu.BaiduMapHandler;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedriver.AppOneDriver;
import com.onedriver.DefaultCallback;
import com.ysx.qqcloud.QQCloudInitService;

/**
 * 主界面 activity
 */
public class MainActivity extends FullScreenActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    /**
     * 改按钮使用oneDriver才显示
     */
    private Button btn_login_onedriver;

    private LinearLayout ll_act_main_upan;
    private LinearLayout ll_act_main_game;
    //Begin Ibotn jinlong.zou,for add qrScan Icon
    private LinearLayout ll_act_main_qr_scan;
    //End Ibotn jinlong.zou,for for add qrScan Icon

    private Context mContext;

    private Activity mActivity;
    OnClickListener mFunctionOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = null;

            switch (v.getId()) {
                case R.id.local_photo_container:
//                    openLocalMedia(false);
                    startActivity(new Intent(MainActivity.this, GrowthAlbumHomeActivity.class));

                    break;
                case R.id.local_video_container:
                    openLocalMedia(true);
                    break;

                case R.id.education_content_container://教育内容
                    openEducationContent(true);
                    break;
                case R.id.back_button_container:
                    returnHome();
                    break;
                case R.id.iv_return_home:
                    returnHome();
                    break;
                case R.id.iv_setting://设置

                    if (Constant.Toggle.TOGGLE_SHOW_DIALOG_ANSWER_QUESTION_FOR_ENTER_SETTING) {
                        final BottonEditTextCommonDialog dialog = new BottonEditTextCommonDialog(mContext);
                        dialog.setTitle(R.string.text_input_password_with_question);
                        dialog.showDialog(dialog);
                        dialog.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (v.getId() == R.id.btn_left) {

                                    dialog.dismissDialog(dialog);
                                } else if (v.getId() == R.id.btn_right) {

                                    String password = dialog.getEditTextPassword().getText().toString();

                                    if (Constant.DEFAUT_ENTER_SETTING_PASSWORD.equals(password)) {
                                        AppUtils.startActivity(mActivity, SettingActivity.class);
                                        dialog.dismissDialog(dialog);
                                    } else {
                                        ToastUtils.showCustomToast(getString(R.string.text_input_password_with_question));
                                    }
                                }
                            }
                        });
                    } else {
                        AppUtils.startActivity(mActivity, SettingActivity.class);
                    }

                    break;

                case R.id.btn_login_onedriver://login_onedriver

                    //1.先判断网络
                    boolean connectedToNetwork = NetUtils.isNetworkConnected(mContext);
                    if (!connectedToNetwork) {
                        ToastUtils.showCustomToast(mContext, getString(R.string.network_error));
                        return;
                    }

                    /**
                     * 	1.运行ibotncloudplayer，点击oneDriver注册时，此时出现oneDriver注册登录界面（真正注册，或登录），此时退出界面。/data/data/com.infomax.ibotncloudplayer文件夹下面的数据
                     sp文件/shared_prefs/com.microsoft.live.xml 中就有数据了
                     <map>
                     <string name="cookies">wlidperf,MSPRe</string>
                     <string name="refresh_token">9xVAQ$$9xVAQ$$9xVAQ$$9xVAQ$$</string>
                     </map>
                     2.分析结果：可以根据refresh_token来判断用户的ibotn上的播放器oneDriver是否登录或注册微软账号。
                     3.根据refresh_token，判断用户是否登录onedriver;手动登录不用检查refresh_token 。自动登录前才检查，如果为空就提示【先手动登录】。
                     */
                    loadOnedrive();

                    break;
                case R.id.ll_act_main_upan:

                    intent = getPackageManager().getLaunchIntentForPackage(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_FILE_EXPLORER);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Constant.EXTRA_START_APP_FLAG, Constant.START_BY_IBOTNCLOUDPLAYER);//启动FileExplorer,FileExployer根据这个标志设置可以操作的控件
                        // TODO: 2017/4/1
                        startActivity(intent);
                    } else {
                        ToastUtils.showCustomToast(null, "Please install FileExplorer");
                    }

                    break;
                case R.id.ll_act_main_game:

					/*intent = getPackageManager().getLaunchIntentForPackage(Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR);
					if (intent != null)
					{
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}else {
						ToastUtils.showCustomToast(null, "please install the game of (scratchjr.apk) ");
					}*/
                    startActivity(new Intent(MainActivity.this, GameActivity.class));
                    break;
                //
                //Begin Ibotn jinlong.zou,for add qrScan Icon
                case R.id.ll_act_main_qr_scan:
                    Intent qrScanIntent = getPackageManager().getLaunchIntentForPackage("com.ibotn.ibotnqrscan");
                    qrScanIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(qrScanIntent);
                    break;
                //End Ibotn jinlong.zou,for add qrScan Icon
                default:
                    break;
            }
        }

    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();

                MyLog.d(TAG, ">>>onReceive()>>>>>>>onReceive:" + action);

//				ToastUtils.showCustomToast(null,action);
                checkUSBDevice();

            }
        }
    };

//	public static Activity getInstance(){
//		return mActivity;
//	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mActivity = this;

        Constant.mainActivity = this;

        registerBroadcastReceiver();

        // jy 原来 LauncherBoot TODO: 2016/11/21
        //lancher 中程序已经修改，lancher启动不开启ibotncloudplayer.
        boolean mIsLauncherBoot = getIntent().getBooleanExtra("LauncherBoot", false);
        Log.d(TAG, "->>>>MainActivity->>>onCreate(),mIsLauncherBoot:" + mIsLauncherBoot);
        if (mIsLauncherBoot) {//launch已启动
            Log.d(TAG, "onCreate(): LauncherBoot");
            Intent intent = new Intent(this, QQCloudInitService.class);
            startService(intent);

            //////////////////////
            Intent intent2 = new Intent(this, IbotnCoreService.class);
            startService(intent2);
            //////////////////////

            ////////////////////这是旧的方式直接finish掉
            finish();
            ///////////////////

            ///////////////////这是新的方式直接退到后台，因为oneDriver自动登录会使用该MainActivity (使用static修饰)// TODO: 2017/3/3
            //第一次开机退到后台，用户第一次开启播放器后，点击返回，及home，界面中所有的点击都无效。
            //moveTaskToBack(true);
            ///////////////////

            return;
        }

        initViewsAndRegisterListener();

        // 模拟加载就启动服务，为了使用ibotn序列号
        Intent intent = new Intent(MainActivity.this, QQCloudInitService.class);
        startService(intent);

        //////////////////////
        Intent intent2 = new Intent(this, IbotnCoreService.class);
        startService(intent2);
        //////////////////////

        initData();

        ///////////init baidu map
        BaiduMapHandler.getInstance().init(getApplicationContext());
        //////////

    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.d(TAG, ">>>onResume:");

        //FileEnhancedUtils.run(this);
    }

    private void initViewsAndRegisterListener() {
        findViewById(R.id.local_photo_container).setOnClickListener(mFunctionOnClickListener);
        findViewById(R.id.local_video_container).setOnClickListener(mFunctionOnClickListener);
        findViewById(R.id.education_content_container).setOnClickListener(mFunctionOnClickListener);//教育内容
        findViewById(R.id.back_button_container).setOnClickListener(mFunctionOnClickListener);
        findViewById(R.id.iv_return_home).setOnClickListener(mFunctionOnClickListener);
        findViewById(R.id.iv_setting).setOnClickListener(mFunctionOnClickListener);

        ll_act_main_upan = (LinearLayout) findViewById(R.id.ll_act_main_upan);
        ll_act_main_game = (LinearLayout) findViewById(R.id.ll_act_main_game);
        //Begin Ibotn jinlong.zou,for add qrScan Icon
        ll_act_main_qr_scan = (LinearLayout) findViewById(R.id.ll_act_main_qr_scan);
        //End Ibotn jinlong.zou,for add qrScan Icon

        ll_act_main_upan.setOnClickListener(mFunctionOnClickListener);
        ll_act_main_game.setOnClickListener(mFunctionOnClickListener);
        //Begin Ibotn jinlong.zou,for add qrScan Icon
        ll_act_main_qr_scan.setOnClickListener(mFunctionOnClickListener);
        //End Ibotn jinlong.zou,for add qrScan Icon

        btn_login_onedriver = (Button) findViewById(R.id.btn_login_onedriver);
        toggleButtonState(true);

        btn_login_onedriver.setOnClickListener(mFunctionOnClickListener);
    }

    private void initData() {


        //////////旧的方式
        //如果是定制版，显示游戏模块
//		if(Constant.Toggle.TOGGLE_CUSTOME_VERSION_TYPE == 1){
//			ll_act_main_game.setVisibility(View.VISIBLE);
//		}else {
//			ll_act_main_game.setVisibility(View.GONE);
//		}
        //////////

        //根据包名显示应用入口 ；新的方式

		/*ThreadUtils.runOnBackThread(new Runnable() {
            @Override
			public void run() {
				List<AppInfoBean> allAppInfo = AppUtils.getAllAppInfo(getApplicationContext());
				for (AppInfoBean bean: allAppInfo) {

					if (mActivity.isDestroyed()){
						break;
					}

					if (Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR.equals(bean.packageName)){
						ThreadUtils.runOnUIThread(new Runnable() {
							@Override
							public void run() {
								ll_act_main_game.setVisibility(View.VISIBLE);
								ll_act_main_game.setOnClickListener(mFunctionOnClickListener);
							}
						});
						break;
					}
				}
			}
		});*/

        checkUSBDevice();
    }

    /**
     * check usb device [要根据当前定制的Android设备而定]
     */
    private void checkUSBDevice() {
        DevicePath device = new DevicePath(this);

        //ToastUtils.showCustomToast("checkusb:" + device.getUsbStoragePath());

        //根据是否有U盘，显示控件
        if (TextUtils.isEmpty(device.getUsbStoragePath())) {
            //Begin Ibotn jinlong.zou,for ui
            ll_act_main_upan.setVisibility(View.INVISIBLE);
            //End Ibotn jinlong.zou,for ui
        } else {
            ll_act_main_upan.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载oneDriver ，如果没有登录就登录appOneDriver.createOneDriveClientForLogin(MainActivity.this, serviceCreated);<br/>
     * 如果已经登录过了就提示并调用appOneDriver.checkOrCreateFolders
     */
    private void loadOnedrive() {

        toggleButtonState(false);
        final String mItemRootId = "root";
        final AppOneDriver appOneDriver = AppOneDriver.getInstance();
        final ICallback<Void> serviceCreated = new DefaultCallback<Void>(mContext) {
            @Override
            public void success(final Void result) {
                toggleButtonState(true);
                ToastUtils.showCustomToast(mContext, "Connect successfully to OneDrive.");
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>success()>>>>>>:");
                appOneDriver.checkOrCreateFolders(mContext, mItemRootId);
            }

            @Override
            public void failure(ClientException error) {
                super.failure(error);
                toggleButtonState(true);
                ToastUtils.showCustomToast(mContext, "Connect to oneDrive failed");
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>failure()>>>>>>:" + error.getMessage());
            }
        };
        try {
            IOneDriveClient iOneDriveClient = appOneDriver.getOneDriveClient();

            if (iOneDriveClient != null) {//此条件说明是登录过了
                ToastUtils.showCustomToast(null, getString(R.string.tip_logined_onedrive));
            }

            appOneDriver.checkOrCreateFolders(mContext, mItemRootId);
            toggleButtonState(true);
        } catch (final UnsupportedOperationException ignored) {
            MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>UnsupportedOperationException>>>>>>serviceCreated:" + serviceCreated);
            appOneDriver.createOneDriveClientForLogin(MainActivity.this, serviceCreated);
        }
    }

    /**
     * onedriver用户手动开关
     *
     * @param show true 显示。
     */
    private void toggleButtonState(boolean show) {

        if (Constant.Toggle.TOGGLE_UPLOAD_TYPE_THIRD_PATY_TYPE == 1) {
            btn_login_onedriver.setVisibility(View.GONE);
            return;
        } else if (Constant.Toggle.TOGGLE_UPLOAD_TYPE_THIRD_PATY_TYPE == 2) {
            btn_login_onedriver.setVisibility(View.VISIBLE);
        }
        btn_login_onedriver.setEnabled(show);
        if (show) {
            btn_login_onedriver.setBackgroundResource(R.drawable.selector_button_blue_bg_circle_rect_reverse);
        } else {
            btn_login_onedriver.setBackgroundResource(R.drawable.shape_button_bg_normal);
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

    /**
     * @param isVideo false 是照片。
     */
    private void openLocalMedia(boolean isVideo) {
        Intent intent = new Intent(MainActivity.this, MultimediaListActivity.class);
        if (isVideo) {
            intent.putExtra(MediaManager.KEY_MEDIA_TYPE, MediaManager.MEDIA_TYPE_LOCAL_VIDEO);
        } else {
            intent.putExtra(MediaManager.KEY_MEDIA_TYPE, MediaManager.MEDIA_TYPE_LOCAL_PHOTO);
        }

        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean mIsLauncherBoot = getIntent().getBooleanExtra("LauncherBoot", false);
        Log.d(TAG, "->>>>MainActivity->>>onNewIntent(),mIsLauncherBoot:" + mIsLauncherBoot);
        if (mIsLauncherBoot) {//launch已启动
            Intent i = new Intent(this, QQCloudInitService.class);
            startService(i);
            return;
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        registerReceiver(broadcastReceiver, filter);
    }

    private void openEducationContent(boolean isVideo) {
        Intent intent = new Intent(MainActivity.this, EducationContentActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy():");
        unregisterReceiver(broadcastReceiver);
    }
}
