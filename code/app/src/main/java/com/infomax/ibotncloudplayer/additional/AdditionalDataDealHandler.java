package com.infomax.ibotncloudplayer.additional;

import android.os.Build;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.utils.DateUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.NetUtils;
import com.infomax.ibotncloudplayer.utils.NetworkConnectivityUtils;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

/**
 * Created by jy on 2017/4/10.<br/>
 * 附加数据处理工具类。<br/>
 * 1.请参考<日志系统设计文档>
 * 2.包含日志登录，在线地图。
 * 3.注意不使用的时候调用 stop()方法。
 */
public class AdditionalDataDealHandler {

    private final String TAG = AdditionalDataDealHandler.class.getSimpleName();
    private static AdditionalDataDealHandler instance;
    /**
     * 上传日志登录url
     */
    private final static String UPLOAD_LOGIN_LOG_URL = "http://log.ibotn.com/realtime";
    /**
     * 定时上传登录信息的时间间隔
     */
    private static final long PERIOD_FIX_TIME = 10 * 60 * 1000;

    /**
     * login params
     */
    private Map<String,String> loginParams;
    private AdditionalDataDealHandler(){};

    public static AdditionalDataDealHandler getInstance(){
        if (instance == null){
            synchronized (AdditionalDataDealHandler.class){
                if (instance == null){
                    instance = new AdditionalDataDealHandler();
                }
            }
        }
        return instance;
    }

    Timer loginTimer = new Timer();
    TimerTask loginTimerTask = new TimerTask() {
        @Override
        public void run() {

            boolean isConnectedToNetwork = NetworkConnectivityUtils.isConnectedToNetwork(MyApplication.getInstance());
            MyLog.d(TAG, ">>>>loginTimerTask()>>isConnectedToNetwork:" +isConnectedToNetwork);

            /*int wifiSignal = NetworkConnectivityUtils.getWifiSignal(MyApplication.getInstance(), 3);
            MyLog.d(TAG, ">>>>loginTimerTask()>>wifiSignal:" + wifiSignal);

            boolean checkNetworkStatus = NetworkConnectivityUtils.checkUnusefulNetworkByPing(MyApplication.getInstance());
            MyLog.d(TAG, ">>>>loginTimerTask()>>checkUnusefulNetworkByPing:" + checkNetworkStatus);*/

//            boolean isConnect = NetworkConnectivityUtils.isConnect();
//            MyLog.d(TAG, ">>>>loginTimerTask()>>isConnect:" + isConnect);

            if (isConnectedToNetwork)
            {
                logLogin();
            }

        }
    };

    /**
     * init
     */
    public void init(){
        logLoginInit();
    }

    /**
     * logLoginInit
     */
    private void logLoginInit(){

        loginTimer.schedule(loginTimerTask, 0, PERIOD_FIX_TIME);
    }

    /**
     * 10 * 60s,执行一次。
     */
    private void logLogin(){
        loginParams = new HashMap<>();
        loginParams.put("logtype", "2");//日志类型
        loginParams.put("Devid", Utils.getDeviceSerial());//终端ibotn编号
        String loginTime = DateUtils.formatDate(new Date(), 1);
        loginParams.put("LoginTime", loginTime);//登录时间
        loginParams.put("TerminalVersion", Build.DISPLAY);//终端当前版本号，可用于升级及问题定位
        loginParams.put("InnerIp", NetUtils.getLocalIpAddress());//内网IP
        loginParams.put("PhoneNum", "");//关联手机号;多个手机号用’;’分割
        loginParams.put("DevType", "");//设备类型(为以后各型号终端做区分)
        loginParams.put("DevVersion", "");//设备型号
        loginParams.put("GroupId", "");//群组ID(没有群组为0)(为以后组织购买做准备)

        MyLog.d(TAG, TAG + ">>>>logLogin()>>>"
                        + "\n logtype:" + 2
                        + "\n Devid:" + Utils.getDeviceSerial()
                        + "\n LoginTime:" + DateUtils.formatDate(new Date(), 1)
                        + "\n LoginTime>>Calendar>>:" + DateUtils.formatDate(new Date(Calendar.getInstance().getTimeInMillis()), 1)
                        + "\n TerminalVersion:" + Build.DISPLAY
                        + "\n InnerIp:" + NetUtils.getLocalIpAddress()
                        + "\n PhoneNum:" + ""
                        + "\n DevType:" + ""
                        + "\n DevVersion:" + ""
                        + "\n GroupId:" + ""
        );

        OkHttpUtils.post()
                .url(UPLOAD_LOGIN_LOG_URL)
                .params(loginParams)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyLog.d(TAG, TAG + ">>>>logLogin()>>onError>>>"
                                        + "\n id:" + id
                                        + "\n Exception:" + e.getMessage()
                        );
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        MyLog.d(TAG, ">>>>logLogin()>>onResponse>>>"
                                        + "\n id:" + id
                                        + "\n response:" + response
                        );

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject != null) {
                                MyLog.d(TAG, ">>>>logLogin()>>onResponse>>>"
                                                + "\n Message:" + jsonObject.get("Message")
                                                + "\n Status:" + jsonObject.getInt("Status")
                                );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyLog.d(TAG, ">>>>logLogin()>>JSONException:" + e.getMessage()
                            );
                        }

                    }
                });

    }

    /**
     * stop <br/>
     * all  timer cancel <br/>
     */
    public void stop(){
        if (loginTimer != null)
        {
            loginTimer.cancel();
        }
    }
}
