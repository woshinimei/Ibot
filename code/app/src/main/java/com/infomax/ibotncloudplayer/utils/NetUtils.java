package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import okhttp3.Call;
/**
 * Created by jy on 2017/2/7 ;8:56.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 * 包含post上传okhttputils。及网络连接类型工具类。
 */
public class NetUtils {
    final static String  TAG = NetUtils.class.getSimpleName();
    /**
     * 检测网络是否连接
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isConnected();
//                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 检测网络是否连接
     *
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断WIFI网络是否可用
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     *
     * 只是上传生成的音频文档，<br/>
     */
    public static void uploadGenerateProps(){
        File file = new File(Constant.UPLOAD_AUDIO_FILE_ADSOLUTE_PATH);
        if (FileUtils.isFileExists(file))
        {
            //step 3,上传properties文件
            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadGenerateProps()>>>"
                            + "\n deviceId:" + Utils.getDeviceSerial()
                            + "\n folderType:" + Constant.AUDIO
                            + "\n file.getName():" + file.getName()
            );

            OkHttpUtils.post()
                    .addFile("file", file.getName(), file)
                    .url("http://log.ibotn.com/upload")
                    .addParams("deviceId", Utils.getDeviceSerial())
                    .addParams("folderType", Constant.AUDIO)//指定上传目录。是服务器目录
                    .addParams("upfile_type", "3")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadGenerateProps()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadGenerateProps()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>uploadGenerateProps()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                MyLog.d(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>uploadGenerateProps()>>JSONException:" + e.getMessage()
                                );
                            }
                        }
                    });
        }
    }

    /**
     * 获取本地内网IP
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> enIp = ni.getInetAddresses();
                while (enIp.hasMoreElements()) {
                    InetAddress inet = enIp.nextElement();
                    if (!inet.isLoopbackAddress() && (inet instanceof Inet4Address)) {
                        return inet.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "0";//return error ip
    }
}
