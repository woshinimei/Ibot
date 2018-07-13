package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.infomax.ibotncloudplayer.service.IbotnCoreService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This utils helps to do the following:
 * 1) If device is connected to mobile
 * network
 * 2) If device is connected to wifi 3) If device is connected, either
 * to mobile network or wifi.
 * 
 * @author jy
 */
public class NetworkConnectivityUtils {

    /**
     * Log
     */
    private static final String TAG = NetworkConnectivityUtils.class.getSimpleName ();

    public static boolean isConnectedToMobile (Context context) {

        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        // mobile
        State mobile = conMan.getNetworkInfo (0).getState ();
        MyLog.d(TAG,
                            "checking if device is connected to  mobile network");
        return mobile == State.CONNECTED;

    }

    public static boolean isConnectedToWifi (Context context) {

        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        // wifi
        State wifi = conMan.getNetworkInfo (1).getState ();
        MyLog.d(TAG, "checking if device is connected to wifi");

        return wifi == State.CONNECTED;
    }

    /**
     * This is a simple way to check if you are CONNECTED or is CONNECTING to
     * network. NOTE: you need to set <uses-permission
     * android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     * in your AndroidManifest.xml
     * 
     * @param context a context used to getSystemInfo
     * @return
     */
    public static boolean isConnectedToNetwork (Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo ();
        boolean isConnected = netInfo != null && netInfo.isConnected();
        MyLog.d(TAG, "device is connected to network :  " + isConnected);
        return isConnected;
    }
    /**
     * 获取当前网络连接的类型信息 <br/>
     * {@link ConnectivityManager.TYPE_MOBILE,TYPE_WIFI...},
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

    private NetworkConnectivityUtils() {

    }

    /**
     *
     * @param context
     * @param numLevels The number of levels to consider in the calculated
     *            level.
     * @return A level of the signal, given in the range of 0 to numLevels-1
     *         (both inclusive).
     */
    public static int getWifiSignal(Context context,int numLevels){
        if (getConnectedType(context) == ConnectivityManager.TYPE_WIFI){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            int rssi = connectionInfo.getRssi();
            MyLog.d(TAG, ">>>getWifiSignal()>>>getRssi:" + rssi );
            return WifiManager.calculateSignalLevel(rssi,numLevels);
        }
        return 0;
    }

    /**
     * 该方法用于检查【假连接】，请在网络连接后调用。 <br/>
     * 1.use java.lang.Runtime.getRuntime().exec("ping ... ") to check the unreal net connected <br/>
     * 2.final String SERVER_ADDRESS_1 =  "www.ibotn.com";  <br/>
     * 3.final String SERVER_ADDRESS_2 =  "www.baidu.com";  <br/>
     * 4.为了保险起见，先ping SERVER_ADDRESS_1;如果为false,就继续ping SERVER_ADDRESS_2; <br/>
     * 5.要在工作线程调用该方法,<br/>
     * @param context
     * @param serverAddresses
     * @return  对于没有联网、假连接都返回false;
     */
    public  static boolean checkUnusefulNetworkByPing(Context context, String[] serverAddresses) {
        boolean result = false;

        if (context == null)
        {
            return result;
        }

        if (serverAddresses == null || serverAddresses.length == 0)
        {
            return result;
        }

        long beginTime  = System.currentTimeMillis();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //假连接时：netInfo:[type: WIFI[], state: CONNECTED/CONNECTED, reason: (unspecified), extra: "HONOR H30-L01", roaming: false, failover: false, isAvailable: true, isConnectedToProvisioningNetwork: false]
            MyLog.e(IbotnCoreService.TAG, TAG + ">>>>>netInfo:" + netInfo);
            if (netInfo == null || netInfo.isConnected() == false || netInfo.isAvailable() == false) {
                if (netInfo == null) {
                    MyLog.e(TAG, "Network is off");
                }
                else if (netInfo.isConnected() == false)
                {
                    MyLog.e(TAG, "Network is not connected");
                }
                else
                {
                    MyLog.e(TAG, "Network is not available");
                }
                result =  false;
            } else {
                try {
                    //-w是指执行的最后期限，也就是执行的时间，单位为秒
                    //-c是指ping的次数
                    //us.ntp.org.cn 【server address】TODO 未使用
                    for (String serverAddress : serverAddresses){
                        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 -W 2 " +serverAddress);//在Linux 下执行-c 2 -W 5 us.ntp.org.cn

                        //p1.waitFor()是阻塞的。
                        if (p1.waitFor() != 0) {

                            MyLog.d(TAG, "Network still not working");
                        }
                        else {
                            MyLog.d(TAG, "Network working");
                            result = true;
                        }
                        if (result == true)
                        {
                            break;
                        }
                    }
                } catch (Exception e) {
                    MyLog.d(TAG, "Ping test error:" + e.getMessage());
                }
            }
        }

        MyLog.d(IbotnCoreService.TAG, TAG + ">>>>checkUnusefulNetworkByPing>>>consume time:" + (System.currentTimeMillis() - beginTime));

        return result;
    }

}
