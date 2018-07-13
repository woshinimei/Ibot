package com.map.helper.baidu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;

/**
 * Created by jy on 2017/4/11
 * 附加数据处理工具类。<br/>
 * 1.请参考<日志系统设计文档>
 * 2.在线地图。
 * 3.注意不使用的时候调用 stop()方法。
 * 4.在IbotnBroadcastReceiver中和MainActivity中都会调用该类
 */
public class BaiduMapHandler {
    private static final String TAG = BaiduMapHandler.class.getSimpleName();

    private static final int MSG_REQUEST_LOCATION = 1;
    private static BaiduMapHandler instance;
    private static LocationClient mLC = null;
    /**
     * 上传地图url
     */
    private final static String UPLOAD_MAP_URL = "http://log.ibotn.com/realtime";

    /** 固定时间间隔发起一次定位请求 */
//    private static final long PERIOD_FOR_UPLOAD_MAP_DATA =     1000;
    private static final long PERIOD_FOR_UPLOAD_MAP_DATA =    1 * 60 * 1000;

    private MyLocationListener myLocationListener;
    /**
     * uploadMap params
     */
    private Map<String,String> uploadMapParams;
    private BaiduMapHandler(){};

/*    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REQUEST_LOCATION:
                    requestLocationInfo();
                    break;
            }
            return false;
        }
    });*/
    /**
     * 获取BaiduMapHandler实例
     * @return BaiduMapHandler
     */
    public static BaiduMapHandler getInstance(){
        if (instance == null){
            synchronized (BaiduMapHandler.class){
                if (instance == null){
                    instance = new BaiduMapHandler();
                }
            }
        }
        return instance;
    }
    ////////////////////取消定时器，使用百度自带的定时请求。
//    private Timer timer = new Timer();
//    /**
//     * fixed period time to requestLocationInfo
//     */
//    private TimerTask timerTask = new TimerTask() {
//        @Override
//        public void run() {
//
//            MyLog.d(TAG, TAG + "timerTask>>>>thread-name:" + Thread.currentThread().getName());
//            if (NetworkConnectivityUtils.isConnectedToNetwork(MyApplication.getInstance()))
//            {
//                requestLocationInfo();
//
//                if (timer != null)
//                {
//                    timer.cancel();
//                }
//            }
//        }
//    };
    ////////////

    /**
     * init
     */
    public void init(Context context){
        baiduMapInit(context);
    }

    /**
     *
     * @param context  - 需要全局有效的context,建议通过getApplicationContext传入
     */
    private void baiduMapInit(Context context){

        //LocationClient类必须在主线程中声明，需要Context类型的参数。参考百度api
        if (mLC == null)
        {
            mLC = new LocationClient(context);
            setLocationOption();
        }
        if (myLocationListener == null){
            myLocationListener  = new MyLocationListener();
        }
        mLC.registerLocationListener(myLocationListener);

        //timer.schedule(timerTask, 0, PERIOD_FOR_UPLOAD_MAP_DATA);
        stop();
        requestLocationInfo();
    }
    /**
     * requestLocationInfo
     */
    private  void requestLocationInfo() {
//        mHandler.removeMessages(MSG_REQUEST_LOCATION);
        MyLog.d(TAG, TAG + ">>>>requestLocationInfo()>>>>>"
                        + "\n mLC:" + mLC
        );

        if (mLC != null && !mLC.isStarted()){
            mLC.start();
        }

        if (mLC != null && mLC.isStarted())
        {
            mLC.requestLocation();
            mLC.requestHotSpotState();
        }
//        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_LOCATION,PERIOD_FOR_UPLOAD_MAP_DATA);

    }

    /**
     * stopLocationClient
     */
    private void stopLocationClient() {
        if (mLC != null && mLC.isStarted()){
            mLC.stop();
        }
    }
    /**
     * setLocationOption。固定时间间隔发起请求
     */
    private static void setLocationOption() {
        LocationClientOption option = new LocationClientOption();

        //int span=3000;
        option.setScanSpan((int)PERIOD_FOR_UPLOAD_MAP_DATA);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        //option.setOpenGps(true);// 是否使用GPS 现在关闭；室内使用wifi定位

        /**
         * 目前国内主要有以下三种坐标系：
         1. WGS84：为一种大地坐标系，也是目前广泛使用的GPS全球卫星定位系统使用的坐标系；
         2. GCJ02：表示经过国测局加密的坐标；
         3. BD09：为百度坐标系，其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标；
         Android定位SDK产品，支持全球定位，能够精准的获取经纬度信息。根据开发者的设置，在国内获得的坐标系类型可以是：国测局坐标、百度墨卡托坐标 和 百度经纬度坐标。在海外地区，只能获得WGS84坐标。请开发者在使用过程中注意坐标选择
         */
        option.setCoorType("bd09ll");// 设置坐标类型
        //option.setServiceName("com.baidu.location.service_v2.2");//
        //option.setPoiExtraInfo(true);
        option.setAddrType("all");// 详细地址
        //option.setPoiNumber(10);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.disableCache(true);
        option.setPriority(LocationClientOption.NetWorkFirst);
        //option.setOpenGps(true);
        //option.setEnableSimulateGps(true);
        mLC.setLocOption(option);
    }

    private class MyLocationListener implements BDLocationListener {

        public void onReceiveLocation(BDLocation location) {
            MyLog.d(TAG, "onReceiveLocation>>>location:"+ location );
            if (location == null) {
                return;
            }
            MyLog.d(TAG, "onReceiveLocation>>>"+ location.getCity()
                    + ",getLocType:"  + location.getLocType()
            );
            if (TextUtils.isEmpty(location.getCity())){//没有开启网络时,就为null.

            }else
            {
                uploadMapData(location); // TODO: 2017/4/17
                sendBroadcastWithLocation(location);

                //stop request location
//                stop();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            MyLog.d(TAG, TAG + ">>>>onConnectHotSpotMessage()>>>"
                            + "\n s:" + s
                            + "\n i:" + i
            );
        }

        public void onReceivePoi(BDLocation poilocation) {
            MyLog.d(TAG, TAG + ">>>>uploadMapData()>>>"
                            + "\n logtype:" + 3
                            + "\n Devid:" + Utils.getDeviceSerial()
                            + "\n Addr:" + poilocation.getAddrStr()
                            + "\n XaddrIndex:" + poilocation.getLatitude()
                            + "\n YaddrIndex:" + poilocation.getLongitude()
                            + "\n ProviceAddr:" + poilocation.getProvince()
                            + "\n CirtyAddr:" + poilocation.getCity()
                            + "\n CountyAddr:" + poilocation.getDistrict()
                            + "\n Street:" + poilocation.getStreet()
                            + "\n getStreetNumber:" + poilocation.getStreetNumber()
                            + ", getFloor:" + poilocation.getFloor()
            );
        }
    }

    private void sendBroadcastWithLocation(BDLocation location) {
        if (location != null)
        {
            Intent intent = new Intent(Constant.MyBroadCast.ACTION_SEND_LOCATION_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("KEY_CITY",location.getCity());
            intent.putExtra("EXTRA_LOCATION_BUNDLE",bundle);
            intent.putExtra("Addr",location.getAddrStr())
            .putExtra("XaddrIndex",location.getLongitude())
            .putExtra("YaddrIndex",location.getLatitude())
            .putExtra("ProviceAddr",location.getProvince())
            .putExtra("CirtyAddr",location.getCity())
            .putExtra("CountyAddr",location.getDistrict())
            .putExtra("AboutAddr",location.getLocationDescribe())
            .putExtra("LocationTime",location.getTime());
            MyLog.d(TAG, TAG + ">>>>sendBroadcastWithLocation()>>>>>"
                    + "\n action:" + Constant.MyBroadCast.ACTION_SEND_LOCATION_DATA
                    + "\n KEY_CITY:" + location.getCity()
            );
            MyApplication.getInstance().sendBroadcast(intent);
        }
    }

    /***
     * 正在上传标志，防止同时上传多条。
     */
    private AtomicBoolean aiUploading = new AtomicBoolean(false);
    /**
     *
     */
    private void uploadMapData(BDLocation location){

        if (location != null )
        {
            uploadMapParams = new HashMap<>();
            uploadMapParams.put("logtype", "3");//日志类型
            uploadMapParams.put("Devid", Utils.getDeviceSerial());//终端ibotn编号
            uploadMapParams.put("Addr", location.getAddrStr());//详细地址
            uploadMapParams.put("XaddrIndex", "" + location.getLatitude());//X轴坐标,即是纬度，横线
            uploadMapParams.put("YaddrIndex", "" + location.getLongitude());//Y轴坐标
            uploadMapParams.put("ProviceAddr", "" + location.getProvince());//省份
            uploadMapParams.put("CirtyAddr", "" + location.getCity());//城市
            uploadMapParams.put("CountyAddr", "" + location.getDistrict());//县/区
            uploadMapParams.put("AboutAddr", "" + location.getLocationDescribe());//附近地址

            MyLog.d(TAG, TAG + ">>>>uploadMapData()>>>"
                            + "\n logtype:" + 3
                            + "\n Devid:" + Utils.getDeviceSerial()
                            + "\n Addr:" + location.getAddrStr()
                            + "\n XaddrIndex:" + location.getLatitude()
                            + "\n YaddrIndex:" + location.getLongitude()
                            + "\n ProviceAddr:" + location.getProvince()
                            + "\n CirtyAddr:" + location.getCity()
                            + "\n CountyAddr:" + location.getDistrict()
                            + "\n Street:" + location.getStreet()
                            + "\n getStreetNumber:" + location.getStreetNumber()
                            + "\n getFloor:" + location.getFloor()
                            + "\n getLocationDescribe:" + location.getLocationDescribe()
                            + "\n getBuildingID:" + location.getBuildingID()
                            + "\n getBuildingName:" + location.getBuildingName()
                            + "\n getIndoorLocationSurpportBuidlingName:" + location.getIndoorLocationSurpportBuidlingName()
                            + "\n getIndoorLocationSurpportBuidlingID:" + location.getIndoorLocationSurpportBuidlingID()
                            + "\n getPoiList:" + location.getPoiList()
                            + "\n isIndoorLocMode:" + location.isIndoorLocMode()
            );

            OkHttpUtils.post()
                    .url(UPLOAD_MAP_URL)
                    .params(uploadMapParams)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(TAG, TAG + ">>>>uploadMapData()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(TAG, ">>>>uploadMapData()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(TAG, ">>>>uploadMapData()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                MyLog.d(TAG, ">>>>uploadMapData()>>JSONException:" + e.getMessage()
                                );
                            }
                        }
                    });
        }
    }
    /**
     * stop <br/>
     * all  timer cancel <br/>
     */
    public void stop(){
        /*if (timer != null)
        {
            timer.cancel();
        }*/

        stopLocationClient();
//        mHandler.removeCallbacksAndMessages(null);
    }
}
