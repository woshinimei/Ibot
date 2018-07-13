package com.infomax.ibotncloudplayer.utils;

import android.content.Context;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by juying on 2016/11/29.
 * Properties 文件处理工具类
 */
public class PropertiesUtils {

    final static String TAG = PropertiesUtils.class.getSimpleName();


    /**
     * level =1时只显示默认前11个，level=2时显示后27个全部
     * @param level
     * @param filePath
     * @return
     */
    public static List<String> get(int level,String filePath){
        MyLog.e(TAG,"---run()-level:"+level);

        List<String> datas = new LinkedList<>();

        String strFlag = "lv1_";
        if (level == 1)
        {
            strFlag = "lv1_";
        }else if (level == 2)
        {
            strFlag = "lv2_";
        }

        Properties prop = new Properties();

        try{
            //读取属性文件.properties
            InputStream in = PropertiesUtils.class.getResourceAsStream(filePath);//or null if the resource is not found.

            if (in != null)
            {
                MyLog.e(TAG,"---run()-in:"+in.toString());
                prop.load(in);     ///加载属性列表
                Iterator<String> it=prop.stringPropertyNames().iterator();
                while(it.hasNext()) {

                    String key = it.next();
                    if (key != null && key.startsWith(strFlag))
                    {
    //                    String tempName = prop.getProperty(key);
    //                    MyLog.e(TAG, key + ":" + tempName);
                        String tempName = new String((prop.getProperty(key)).getBytes("ISO-8859-1"),"utf-8");
                        datas.add(tempName);

                        MyLog.e(TAG, key + ":" + tempName);
                    }
                }

            }else {
                MyLog.e(TAG,  "in is null" );
            }
        } catch (Exception e){
            MyLog.e(TAG,  "Exception:" + e);
        }
        return datas;
    }

}
