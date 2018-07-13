package com.infomax.ibotncloudplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.service.IbotnCoreService;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.NetworkConnectivityUtils;
import com.infomax.ibotncloudplayer.utils.TtsControl;
import com.map.helper.baidu.BaiduMapHandler;
import com.ysx.qqcloud.QQCloudInitService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jy on 2017/3/31. <br/>
 * 1. to start QQCloudInitService ,IbotnCoreService<br/>
 * 2.开机，及网络连接后检查假连接。<br/>
 */
public class IbotnBroadcastReceiver extends BroadcastReceiver{
    private String TAG = IbotnBroadcastReceiver.class.getSimpleName();

    private long DELAY = 15 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null)
        {
            String action = intent.getAction();
            MyLog.d(TAG,">>>>onReceive>>>>>>action:" + action);
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)){
                context.startService(new Intent(context, QQCloudInitService.class));
                context.startService(new Intent(context, IbotnCoreService.class));

                ///////////init baidu map
                BaiduMapHandler.getInstance().init(context.getApplicationContext());
                //////////

                //开机启动后，15s后检查检查假连接；取消检查，由lancher中语音模块检查
                //checkUnusefulNetwork(DELAY);

            }else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){

                //commented out
                /*DevicePath devicePath = new DevicePath(MyApplication.getInstance());
                if (!TextUtils.isEmpty(devicePath.getSdStoragePath())){
                    Constant.IBOTN_CLOUD_SYSTEM_FILE_INIT_FINISH_FLAG_FOR_LAUNCHER = true;
                }*/

            }else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)){
            }else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected())
                {//网络连接后再延迟15s后检查假连接
                    //checkUnusefulNetwork(DELAY);
                }
            }
        }
    }

    /**
     * 检查假连接。<br/>
     * 该方法不在调用。【假连接播报，由lancher中speech模块赋值】
     * @param delay
     */
    private void checkUnusefulNetwork(long delay) {
        final String SERVER_ADDRESS_1 =  "www.ibotn.com";
        final String SERVER_ADDRESS_2 =  "www.baidu.com";
        final String[] SERVER_ADDRESSES = {SERVER_ADDRESS_1,SERVER_ADDRESS_2};
        Timer netCheckTimer = new Timer();
        TimerTask netCheckTask = new TimerTask() {
            @Override
            public void run() {
                boolean result1 = NetworkConnectivityUtils.checkUnusefulNetworkByPing(MyApplication.getInstance().getApplicationContext(),SERVER_ADDRESSES);
                boolean result2 = false;
                MyLog.d(TAG,TAG + ">>>checkUnusefulNetwork>>>>result:" + result1);
                if (result1 == false)
                {
                    TtsControl.startTtsSpeaker(MyApplication.getInstance().getApplicationContext(),MyApplication.getInstance().getString(R.string.network_can_not_use));
//                    result2 = NetworkConnectivityUtils.checkUnusefulNetworkByPing(MyApplication.getInstance().getApplicationContext(),SERVER_ADDRESS_2);
//                    MyLog.d(TAG,TAG + ">>>checkUnusefulNetwork>>>>result2:" + result2);
                }
            }
        };
        if(NetworkConnectivityUtils.isConnectedToNetwork(MyApplication.getInstance().getApplicationContext()))
        {
            netCheckTimer.schedule(netCheckTask,delay);
        }
    }
}
