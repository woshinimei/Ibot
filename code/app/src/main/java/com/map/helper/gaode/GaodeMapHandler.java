package com.map.helper.gaode;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDLocation;
import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.NetworkConnectivityUtils;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;

/**
 * Created by jy on 2017/4/11.
 * 附加数据处理工具类。<br/>
 * 1.请参考<日志系统设计文档>
 * 2.在线地图。
 * 3.注意不使用的时候调用 stop()方法。
 */
public class GaodeMapHandler {
    private static final String TAG = GaodeMapHandler.class.getSimpleName();
    private static GaodeMapHandler instance;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLC = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private MyLocationListener myLocationListener;
    /** 固定时间间隔发起一次定位请求 */
    private static final long PERIOD_FOR_UPLOAD_MAP_DATA = 1 * 10 * 1000;

    /**
     * uploadMap params
     */
    private Map<String,String> uploadMapParams;

    /**
     * 上传地图url
     */
    private final static String UPLOAD_MAP_URL = "";
    private GaodeMapHandler(){};

    /**
     * 获取GaodeMapHandler实例
     * @return GaodeMapHandler
     */
    public static GaodeMapHandler getInstance(){
        if (instance == null){
            synchronized (GaodeMapHandler.class){
                if (instance == null){
                    instance = new GaodeMapHandler();
                }
            }
        }
        return instance;
    }

    private Timer timer = new Timer();
    /**
     * fixed period time 60m to requestLocationInfo
     */
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

        MyLog.d(TAG, TAG + "timerTask>>>>thread-name:" + Thread.currentThread().getName());
        if (NetworkConnectivityUtils.isConnectedToNetwork(MyApplication.getInstance()))
        {
            requestLocationInfo();
        }
        }
    };

    /**
     * init
     */
    public void init(Context context){
        initGaodeLocationMap(context);
    }

    /**
     * logLoginIinit
     */
    private void initGaodeLocationMap(Context context){

        mLC = new AMapLocationClient(context);

        if (myLocationListener == null){
            myLocationListener  = new MyLocationListener();
        }
        mLC.setLocationListener(myLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(PERIOD_FOR_UPLOAD_MAP_DATA);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //给定位客户端对象设置定位参数
        mLC.setLocationOption(mLocationOption);

        requestLocationInfo();
        //timer.schedule(timerTask, 0, PERIOD_FOR_UPLOAD_MAP_DATA);
    }
    /**
     * requestLocationInfo
     */
    private  void requestLocationInfo() {
        MyLog.d(TAG, TAG + ">>>>requestLocationInfo()>>>>>"
                        + "\n mLC:" + mLC
        );
        if (mLC != null && !mLC.isStarted()){
            mLC.startLocation();
        }
    }

    /**
     * stopLocationClient
     */
    private void stopLocationClient() {
    }
    /**
     * setLocationOption
     */
    private  void setLocationOption() {
    }

    private class MyLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                    MyLog.d(TAG, TAG + ">>>>onLocationChanged()>>>"
                                    + "\n logtype:" + 3
                                    + "\n Devid:" + Utils.getDeviceSerial()
                                    + "\n Addr:" + amapLocation.getAddress()
                                    + "\n XaddrIndex:" + amapLocation.getLatitude()
                                    + "\n YaddrIndex:" + amapLocation.getLongitude()
                                    + "\n ProviceAddr:" + amapLocation.getProvince()
                                    + "\n CirtyAddr:" + amapLocation.getCity()
                                    + "\n CountyAddr:" + amapLocation.getDistrict()
                                    + "\n Street:" + amapLocation.getStreet()
                                    + "\n getStreetNumber:" + amapLocation.getStreetNum()
                                    + "\n getFloor:" + amapLocation.getFloor()
                                    + "\n getLocationDetail:" + amapLocation.getLocationDetail()
                                    + "\n getBuildingID:" + amapLocation.getBuildingId()
                                    + "\n getAoiName:" + amapLocation.getAoiName()
                    );
                    /*amapLocation.getLatitude();//获取纬度
                    amapLocation.getLongitude();//获取经度
                    amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    amapLocation.getCountry();//国家信息
                    amapLocation.getProvince();//省信息
                    amapLocation.getCity();//城市信息
                    amapLocation.getDistrict();//城区信息
                    amapLocation.getStreet();//街道信息
                    amapLocation.getStreetNum();//街道门牌号信息
                    amapLocation.getAoiName();//获取当前定位点的AOI信息
                    amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
                    amapLocation.getFloor();//获取当前室内定位的楼层*/

                }else {

                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    MyLog.e(TAG,"onLocationChanged>>>>location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    }

    /***
     * 正在上传标志，防止同时上传多条。
     */
    private AtomicBoolean aiUploading = new AtomicBoolean(false);
    /**
     * 60s,执行一次。
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
        if (timer != null)
        {
            timer.cancel();
        }

        stopLocationClient();
    }
}
