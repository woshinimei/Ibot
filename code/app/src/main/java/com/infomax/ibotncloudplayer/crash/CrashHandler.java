package com.infomax.ibotncloudplayer.crash;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.utils.AppUtils;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DateUtils;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by jy on 2017/3/25.<br/>
 * 崩溃日志收集操作类。【上传动作由IbotnCoreService完成】定时120s检查一次崩溃日志文件目录
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler instance;

    private Context mContext;

    private CrashHandler(){}

    public static CrashHandler getInstance(){
        if (instance == null)
        {
            synchronized (CrashHandler.class){
                if (instance == null){
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }
    /**
     * 初始化
     * @param context
     */
    public void init(final Context context) {
        mContext = context;

        // 设置该 CrashHandler 为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        ThreadUtils.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, ">>>uncaughtException()>>>>system crash ,i catch it" );
                try {
                    // 创建日志文件
                    final String fileName = Utils.getDeviceSerial()
                            + "_error_"
                            + DateUtils.formatDate(new Date(),1)
                            + "_ibotncloudplayer"
                            + ".txt";
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    {
                        String path  = Environment.getExternalStorageDirectory() + Constant.Config.SUB_PATH_FOR_CRASH_LOG ;
                        boolean createOrExistsDir = FileUtils.createOrExistsDir(path);

                        if (createOrExistsDir){
                            PrintStream printStream = new PrintStream(new FileOutputStream(path + File.separator + fileName));

                            // 使用反射的方式，将 android.os.Build 类中所有的成员变量，及值，写入出错日志
                            Class clazz = Class.forName(Build.class.getName());
                            Field[] fields = clazz.getFields();
                            for (Field field : fields) {
                                String name = field.getName();
                                Object value = field.get(null);
                                printStream.println(name+":"+value);
                            }
                            printStream.println("versionCode:" + AppUtils.getAppVersionCode(MyApplication.getInstance()));
                            printStream.println("versionName:" + AppUtils.getAppVersionName(MyApplication.getInstance()));
                            printStream.println("========division============");
                            ex.printStackTrace(printStream);
                            //MyApplication.getInstance().startService(new Intent(MyApplication.getInstance(),IbotnCoreService.class));
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
