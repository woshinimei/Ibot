package com.infomax.ibotncloudplayer.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.utils.VideoEncryptUtils;

import java.io.File;

/**
 * Created by jack_zou on 2016/11/25.<br/>
 * 因在视频单曲循环播放时，会收到 media.player.stoped 广播而对视频进行加密，之后在下次播放时导致视频无法播放，所以不在收到停止广播后对视频进行加密
 */
@Deprecated
public class ChangeVideoEncryptService extends Service {
    private ProgressDialog pDialog;
    private  final String TAG = ChangeVideoEncryptService.class.getSimpleName();

    BroadcastReceiver broadcastReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLog.d(TAG,"intent.getAction()>>>" + intent.getAction());
            if (intent.getAction().equals("media.player.stoped")){
                Log.d("########","################");


                String path = SharedPreferenceUtils.getVideoPatch(ChangeVideoEncryptService.this);
                Log.d(TAG,"Path is :"+path);
                if (!TextUtils.isEmpty(path)){
                    File file =new File(path);
                    Log.d("########","########1111#######");

                    if (VideoEncryptUtils.obtainFileState(file) == VideoEncryptUtils.IS_MARK_FILE){
                        showProgressDialog(context);
                        Log.d("########","########222########");

                        VideoEncryptUtils.EncryptVideoFile(file);
                        SharedPreferenceUtils.setVideoPath(ChangeVideoEncryptService.this,"");
                        if (pDialog !=null) {
                            pDialog.hide();
                        }
                    }
                }
            }
            stopSelf();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("########","########"+"   @@@@@");
        IntentFilter intentFilter =new IntentFilter("media.player.stoped");
        registerReceiver(broadcastReceiver,intentFilter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showProgressDialog(Context context){
        pDialog = new ProgressDialog(context);
        pDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Processing");
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }
}
