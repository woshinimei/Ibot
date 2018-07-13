package com.infomax.ibotncloudplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.AppInfoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jy on 2017/3/3 ;12:19.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 */
public class AppUtils {

    static String TAG = AppUtils.class.getSimpleName();
    /**
    *1. @return VersionName
    */
    public static String getAppVersionName(Context context) {
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }
    /**
     *1. @return VersionCode
     */
    public static int getAppVersionCode(Context context) {
        int version = 0;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     *1. 获取版本号
     *2. @return 当前应用的版本号
     */
    public static String getAppPackageName(Context context) {
        String packageName = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            packageName = info.packageName;
            return packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }

    /**
     * 获取所有应用的信息
     * @param ctx
     * @return
     */
    public static List<AppInfoBean> getAllAppInfo(Context ctx) {
        List<AppInfoBean> list = new ArrayList<AppInfoBean>();

        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> installedApplications = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : installedApplications) {

            AppInfoBean bean = new AppInfoBean();
            list.add(bean);
            bean.appName = (String) appInfo.loadLabel(pm);

            /**
             * 包名
             */
            bean.packageName = appInfo.packageName;
            bean.appIcon = appInfo.loadIcon(pm);

            /**
             * apk文件路径
             * 1.  /data开头
             * 2.  /mnt/开头
             * 2.  /system/开头  肯定是系统应用
             */
            String apkPath = appInfo.sourceDir;//apk文件路径

            File file = new File(apkPath);
			MyLog.d(TAG,TAG + ">>>bean.appName:" + bean.appName+":"+apkPath+">>>packageName:"+bean.packageName);

            bean.appSize = file.length();

            if(apkPath.startsWith("/system")){//系统应用

//				System.out.println("根据 路径  判断，当前应用是系统应用：："+bean.appName+apkPath);
                bean.isSys = true;
            }else{//用户应用

                bean.isSys = false;
//				System.out.println("根据 路径  判断，当前应用是用户应用：："+bean.appName+apkPath);
            }

			/*
			 * flags 是应用的信息标记值
			 * ApplicationInfo.FLAG_EXTERNAL_STORAGE 是否安装在sk卡中
			 * 二者按位相与，如果不等于0，说明匹配成功，当前应就拥有，标记位所表示的属性
			 */
            if((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) !=0){
                bean.isInSd = true;
            }else{
                bean.isInSd = false;
            }
        }

        return list;
    }


    /**
     * 通过Class跳转界面
     **/
    public static void startActivity(Activity activity,Class<?> cls) {
        startActivity(activity,cls, null);
    }

    /**
     * 含有Bundle通过Class跳转界面,带有右进右出动画
     **/
    public static void startActivity(Activity activity,Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(activity, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.zoom_in, 0);
    }

    /**
     * 通过Action跳转界面
     **/
    public static void startActivity(Activity activity,String action) {
        startActivity(activity,action, null);
    }

    /**
     * 含有Bundle通过Action跳转界面，带有右进右出动画
     **/
    public static void startActivity(Activity activity,String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.zoom_in, 0);
    }

    /**
     * 含有Bundle通过Class打开编辑界面，带有右进右出动画
     **/
    public static void startActivityForResult(Activity activity,Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.zoom_in,0);
    }


}
