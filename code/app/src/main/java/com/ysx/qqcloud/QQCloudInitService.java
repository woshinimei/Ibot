package com.ysx.qqcloud;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.qcloud.Module.Vod;
import com.qcloud.QcloudApiModuleCenter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.TreeMap;

import static com.ysx.qqcloud.QQCloudObject.secretId;
import static com.ysx.qqcloud.QQCloudObject.secretKey;

/**
 * Created by tom on 2016/9/30.
 * 由 Ibotn 系统 主动调用的，用来创建文件
 */
public class QQCloudInitService extends Service {
    private static final String TAG = QQCloudInitService.class.getSimpleName();
    private static final String PREF_INIT_DONE = "INIT_DONE";
    private static final String PREF_PUBLIC_VIDEO = "PUBLIC_VIDEO";
    private static final String PREF_PUBLIC_AUDIO = "PUBLIC_AUDIO";
    private static final String PREF_PUBLIC_PHOTO = "PUBLIC_PHOTO";
    private static final String PREF_PRIVATE_VIDEO = "PRIVATE_VIDEO";
    private static final String PREF_PRIVATE_AUDIO = "PRIVATE_AUDIO";
    private static final String PREF_PRIVATE_PHOTO = "PRIVATE_PHOTO";
    private static final String VIDEO_NAME = "VIDEO";
    private static final String AUDIO_NAME = "AUDIO";
    private static final String PHOTO_NAME = "PHOTO";
    private SharedPreferences mPrefs;
    private QcloudApiModuleCenter module = null;
    private TreeMap<String, Object> config = null;
    private TreeMap<String, Object> params = null;

    private String mDeviceName = null;
    private String mPrivateRootId = null;
    private String mPrivateUserId = null;

    private String mPublicVideoId = null;
    private String mPublicAudioId = null;
    private String mPublicPhotoId = null;
    private String mPrivateVideoId = null;
    private String mPrivateAudioId = null;
    private String mPrivatePhotoId = null;

    private ConnectivityManager mConnectivityManager;
    private boolean mNetworkConnected = false;

    private boolean flagLoadingFileData = false;
    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate()");
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mIntentActionReceiver, filter);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info != null) {
            mNetworkConnected = info.isConnected();
        } else {
            mNetworkConnected = false;
        }

        if (config == null) {
            config = new TreeMap<String, Object>();
            config.put("SecretId", secretId);
            config.put("SecretKey", secretKey);
            config.put("RequestMethod", "GET");
            config.put("DefaultRegion", "gz");
        }

        if (module == null) {
            module = new QcloudApiModuleCenter(new Vod(), config);
            params = new TreeMap<String, Object>();
        }
        getDeviceName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "onStartCommand()");

        //mPrivateId = getClassIdPrivate();

        mPrefs = getSharedPreferences("qqCloud", MODE_PRIVATE);
        mPublicVideoId = mPrefs.getString(PREF_PUBLIC_VIDEO, null);
        mPublicAudioId = mPrefs.getString(PREF_PUBLIC_AUDIO, null);
        mPublicPhotoId = mPrefs.getString(PREF_PUBLIC_PHOTO, null);

        mPrivateVideoId = mPrefs.getString(PREF_PRIVATE_VIDEO, null);
        mPrivateAudioId = mPrefs.getString(PREF_PRIVATE_AUDIO, null);
        mPrivatePhotoId = mPrefs.getString(PREF_PRIVATE_PHOTO, null);

        if (mPrefs.getBoolean(PREF_INIT_DONE, false)) {
            Log.d(TAG, "mPublicVideoId: " + mPublicVideoId + ", mPublicAudioId: " + mPublicAudioId + ", mPublicPhotoId: " + mPublicPhotoId);
            Log.d(TAG, "mPrivateVideoId: " + mPrivateVideoId + ", mPrivateAudioId: " + mPrivateAudioId + ", mPrivatePhotoId: " + mPrivatePhotoId);

            QQCloudInitService.this.stopSelf();

        } else {
            mPrefs.edit().putBoolean(PREF_INIT_DONE, false).apply();
            if (mNetworkConnected)
            {
                qqClassInit();
            }
        }
        ///////// for test
        //getLocalVideos();
        /////////

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 新增条件判断，防止Build.SERIAL< 13的情况
     * @return
     */
    public String getDeviceName() {
        if (Build.SERIAL != null && Build.SERIAL.length() >=13 )
        {
            mDeviceName = Build.SERIAL.substring(0, 13);
            //固定写入序列号 001609261304B  TODO
    //        mDeviceName = "001609261304B";
            Log.d(TAG, "getDeviceName()>>>>>>>>>>>>myDeviceName: " + mDeviceName);
            return mDeviceName;

        }else {
            Log.d(TAG," Build.SERIAL: " +  Build.SERIAL);
            return Build.SERIAL == null ? "default_serial" : Build.SERIAL;
        }
    }

    /**
     * 模拟扫描sd卡,服务启动后就在后台扫描
     * 对应LocalVideoFragment中 getLocalVideos
     * @param
     */
    private void getLocalVideos(){

        new Thread(){
            @Override
            public void run() {
                super.run();
                flagLoadingFileData = true;

                File file = new File(Constant.Config.Education_Content_Video_File_Root_Path);
                String mPath = file.getAbsolutePath();

                StringBuilder selection = new StringBuilder();
                selection.append("(" + MediaStore.Video.Media.DATA + " LIKE '" + mPath +File.separator+ "%')");
                Log.d(TAG, "-->>>>>>>>" + selection.toString());
                Cursor cursor = null;
                try {
                    ContentResolver contentResolver = getContentResolver();
                    cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
                    if (cursor != null) {
                        while (cursor.moveToNext() && flagLoadingFileData) {
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (cursor != null)
                    {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

    }

    public String getPrivateTypeId(JSONObject jobject, String privateName) {
        Log.d(TAG, "=========getPrivateTypeId===========");
        String userId = null;
        try {
            int i, j, k;
            JSONObject jo1, jo2, jo3, joInfo1, joInfo2, joInfo3;
            JSONArray ja1, ja2;
            JSONArray jarray = jobject.getJSONArray("data");
            for (i = 0; i < jarray.length(); i++) {
                jo1 = jarray.getJSONObject(i);
                joInfo1 = jo1.getJSONObject("info");
                Log.d(TAG, "info->name: " + joInfo1.getString("name"));

                if ("PRIVATE".equals(joInfo1.getString("name"))) {
                    Log.d(TAG, "PRIVATE id: " + joInfo1.getString("id"));
                    mPrivateRootId = joInfo1.getString("id");

                    ja1 = jo1.getJSONArray("subclass");
                    for (j = 0; j < ja1.length(); j++) {
                        jo2 = ja1.getJSONObject(j);
                        joInfo2 = jo2.getJSONObject("info");
                        if (privateName.equals(joInfo2.getString("name"))) {//当前账号文件夹
                            userId = joInfo2.getString("id");  //private user id  ；modify 2016-12-01 juyng TODO 此时返回 userId 如果没有当前账号下没有子文件夹就不会创建
                            Log.d(TAG, privateName + " userId: " + userId);

                            ja2 = jo2.getJSONArray("subclass");
                            for (k = 0; k < ja2.length(); k++) {//当前账号文件夹下没有子文件夹 VIDEO, PHOTO ,AUDIO

//                                userId = joInfo2.getString("id");//add  2016-12-01 jy
//                                Log.d(TAG, privateName + " userId: " + userId);//add 2016-12-01 jy

                                jo3 = ja2.getJSONObject(k);
                                joInfo3 = jo3.getJSONObject("info");
                                String name = joInfo3.getString("name");
                                Log.d(TAG, name + " id: " + joInfo3.getString("id"));

                                if (VIDEO_NAME.equals(name)) {
                                    mPrivateVideoId = joInfo3.getString("id");
                                } else if (AUDIO_NAME.equals(name)) {
                                    mPrivateAudioId = joInfo3.getString("id");
                                } else if (PHOTO_NAME.equals(name)) {
                                    mPrivatePhotoId = joInfo3.getString("id");
                                } else
                                {
                                    Log.e(TAG, name + " not defined.");
                                }
                            }
                            break;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    public boolean getPublicTypeId(JSONObject jobject) {
        Log.d(TAG, "=========getPublicTypeId===========");
        String ret = null;
        try {
            int i, j;
            JSONArray jarray = jobject.getJSONArray("data");
            for (i = 0; i < jarray.length(); i++) {
                JSONObject jo = jarray.getJSONObject(i);
                JSONObject info = jo.getJSONObject("info");
                Log.d(TAG, "info->name: " + info.getString("name"));

                if ("PUBLIC".equals(info.getString("name"))) {
                    Log.d(TAG, "PUBLIC id: " + info.getString("id"));
                    JSONArray subArr = jo.getJSONArray("subclass");
                    for (j = 0; j < subArr.length(); j++) {
                        JSONObject subinfo = subArr.getJSONObject(j).getJSONObject("info");
                        String name = subinfo.getString("name");
                        Log.d(TAG, name + " id: " + subinfo.getString("id"));
                        if (VIDEO_NAME.equals(name)) {
                            mPublicVideoId = subinfo.getString("id");
                        } else if (AUDIO_NAME.equals(name)) {
                            mPublicAudioId = subinfo.getString("id");
                        } else if (PHOTO_NAME.equals(name)) {
                            mPublicPhotoId = subinfo.getString("id");
                        } else {
                            Log.e(TAG, name + " not defined.");
                            return false;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*    public String getPrivateRootId(JSONObject jobject) {
            String ret = null;
            try {
                int i;
                JSONArray jarray = jobject.getJSONArray("data");
                for (i = 0; i < jarray.length(); i++)
                {
                    JSONObject jo = jarray.getJSONObject(i);
                    JSONObject info = jo.getJSONObject("info");
                    //Log.e(TAG, "info->name: " + info.getString("name"));

                    if ("PRIVATE".equals(info.getString("name"))) {
                        ret = info.getString("id");
                        Log.e(TAG, "PRIVATE id: " + ret);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }*/

    public void qqClassInit() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    params.clear();
                    String result = module.call("DescribeAllClass", params);

                    MyLog.e(TAG,"qqClassInit()>>>>>>>>result:"+result);

                    JSONObject jobject = new JSONObject(result);

                    if (jobject.getInt("code") != 0) {
                        Log.e(TAG, jobject.getString("message"));
                        return;
                    }

                    if (getPublicTypeId(jobject)) {
                        mPrefs.edit().putString(PREF_PUBLIC_VIDEO, mPublicVideoId).apply();
                        mPrefs.edit().putString(PREF_PUBLIC_AUDIO, mPublicAudioId).apply();
                        mPrefs.edit().putString(PREF_PUBLIC_PHOTO, mPublicPhotoId).apply();
                    }

                    mPrivateUserId = getPrivateTypeId(jobject, mDeviceName);  //"001608052343A"
                    MyLog.e(TAG,"qqClassInit()>>>>>>>>mPrivateUserId:"+mPrivateUserId);
                    if (mPrivateUserId == null) {
                        if (createMyClass()) {
                            Log.d(TAG, "Create my private folders ok.");
                        }
                    }
                    mPrefs.edit().putString(PREF_PRIVATE_VIDEO, mPrivateVideoId).apply();
                    mPrefs.edit().putString(PREF_PRIVATE_AUDIO, mPrivateAudioId).apply();
                    mPrefs.edit().putString(PREF_PRIVATE_PHOTO, mPrivatePhotoId).apply();
                    if (!TextUtils.isEmpty(mPrivateVideoId) && !TextUtils.isEmpty(mPrivateAudioId) && !TextUtils.isEmpty(mPrivatePhotoId)) {
                        mPrefs.edit().putBoolean(PREF_INIT_DONE, true).apply();
                        QQCloudInitService.this.stopSelf();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //CreateClass result: {"code":0,"message":"","codeDesc":"Success","newClassId":"26199"}
    //CreateClass result: {"code":4000,"message":"(1907)父分类下已经有该分类","codeDesc":1907}

    /**
     * 账号文件夹创建成功。根据实际情况该账号文件夹下面的 VIDEO , PHOTO ,AUDIO 可能并没有创建。
     * @return
     */
    private boolean createMyClass() {//
        Log.d(TAG, "=========createMyClass===========");
        String mNewVideoId = null;
        String mNewAudioId = null;
        String mNewPhotoId = null;
        String result = null;
        JSONObject jobject = null;
        boolean createFail = false;
        //mDeviceName = "tom";
        params.clear();
        params.put("className", mDeviceName);
        params.put("parentId", mPrivateRootId);  //PRIVATE folder id

        try {
            result = module.call("CreateClass", params);
            jobject = new JSONObject(result);
            Log.d(TAG, "CreateClass result: " + jobject);
            int ret = jobject.getInt("code");
            if (ret == 0) {
                mPrivateUserId = jobject.getString("newClassId");

                params.clear();
                params.put("className", "VIDEO");
                params.put("parentId", mPrivateUserId);
                result = module.call("CreateClass", params);
                jobject = new JSONObject(result);
                if (jobject.getInt("code") == 0) {
                    mNewVideoId = jobject.getString("newClassId");
                    Log.d(TAG, mDeviceName + "-VIDEO create ok. id: " + mNewVideoId);
                } else {
                    Log.e(TAG, mDeviceName + "-VIDEO create fail.");
                    createFail = true;
                }
                //---------------------------------------------------------------------------
                params.clear();
                params.put("className", "AUDIO");
                params.put("parentId", mPrivateUserId);
                result = module.call("CreateClass", params);
                jobject = new JSONObject(result);
                if (jobject.getInt("code") == 0) {
                    mNewAudioId = jobject.getString("newClassId");
                    Log.d(TAG, mDeviceName + "-AUDIO create ok. id: " + mNewAudioId);
                } else {
                    Log.e(TAG, mDeviceName + "-AUDIO create fail.");
                    createFail = true;
                }
                //---------------------------------------------------------------------------
                params.clear();
                params.put("className", "PHOTO");
                params.put("parentId", mPrivateUserId);
                result = module.call("CreateClass", params);
                jobject = new JSONObject(result);
                if (jobject.getInt("code") == 0) {
                    mNewPhotoId = jobject.getString("newClassId");
                    Log.d(TAG, mDeviceName + "-PHOTO create ok. id: " + mNewPhotoId);
                } else {
                    Log.e(TAG, mDeviceName + "-PHOTO create fail.");
                    createFail = true;
                }
                //---------------------------------------------------------------------------
                if (createFail) {
                    params.clear();
                    params.put("classId", mPrivateUserId);
                    result = module.call("DeleteClass", params);
                    jobject = new JSONObject(result);
                    Log.w(TAG, mPrivateUserId + " DeleteClass result: " + jobject);
                    return false;
                } else {
                    mPrivateVideoId = mNewVideoId;
                    mPrivateAudioId = mNewAudioId;
                    mPrivatePhotoId = mNewPhotoId;
                    return true;
                }
            } else {
                Log.w(TAG, jobject.getString("message") + mDeviceName);
                return false;
            }

        } catch (Exception e) {
            Log.w(TAG, "Exception:"+ e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        flagLoadingFileData = false;

        unregisterReceiver(mIntentActionReceiver);
    }

    private BroadcastReceiver mIntentActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    NetworkInfo networkInfo =
                            (NetworkInfo) extras.get(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (networkInfo == null) return;

                    Log.d(TAG, "BroadcastReceiver Network state = " + networkInfo.getState());
                    if (networkInfo.getState() != NetworkInfo.State.CONNECTED &&
                            networkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                        return;
                    }

                    if (mNetworkConnected != networkInfo.isConnected()) {
                        mNetworkConnected = networkInfo.isConnected();
                        Log.d(TAG, "mNetworkConnected = " + mNetworkConnected);

                        if (mNetworkConnected)
                            if (!mPrefs.getBoolean(PREF_INIT_DONE, false))
                                qqClassInit();

                    }
                }
            }
        }
    };

}