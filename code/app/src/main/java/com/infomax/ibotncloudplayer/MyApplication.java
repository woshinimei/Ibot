package com.infomax.ibotncloudplayer;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.infomax.ibotncloudplayer.crash.CrashHandler;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by jy on 2016/12/30.
 * Application
 */
public class MyApplication extends Application{

    private  final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.d(TAG, ">>onCreate()>>>>: ");
        instance = this;

        ////////////设置一个未捕获的异常的，统一处理
        CrashHandler.getInstance().init(this);
        ////////////

        ///////////init Gaode map ,now commented out
        //GaodeMapHandler.getInstance().init(this);
        //////////

        ////////add LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        /////////
        Fresco.initialize(this);
    }

    public  static MyApplication getInstance() {
        return instance;
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        MyLog.d(TAG, ">>onTerminate()>>>>: ");
    }
}

