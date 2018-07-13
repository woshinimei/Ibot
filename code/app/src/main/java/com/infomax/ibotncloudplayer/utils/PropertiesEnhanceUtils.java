package com.infomax.ibotncloudplayer.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by juying on 2016/11/29.
 * Properties 文件,写入，读取,处理工具类
 */
public class PropertiesEnhanceUtils {
    final static String TAG = PropertiesEnhanceUtils.class.getSimpleName();
    /**
     * 加载属性文件
     * @param filePath 文件路径
     * @return
     */
    public static Properties loadProps(String filePath){
        Properties properties = new Properties();
        try {
            InputStream in =new BufferedInputStream(new FileInputStream(filePath));
            properties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 读取配置文件
     * @param properties
     * @param key
     * @return  取值要使用getBytes("ISO-8859-1") 再用"utf-8"编码
     */
    public static String getString(Properties properties,String key){

        String value = null;
        try {
            value = new String((properties.getProperty(key)).getBytes("ISO-8859-1"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ////////
//        return properties.getProperty(key);
        ///////
        return  value;
    }
    /** 更新properties文件的键值对
    * 如果该主键已经存在，更新该主键的值；
    * 如果该主键不存在，则插入一对键值。
    * @param keyname 键名
    * @param keyvalue 键值
    */
    public static void updateProperty(Properties properties,String filePath,String keyname,String keyvalue) {
        try {

            // 从输入流中读取属性列表（键和元素对）
            properties.setProperty(keyname, keyvalue);
            FileOutputStream outputFile = new FileOutputStream(filePath);
            properties.store(outputFile, null);
            outputFile.flush();
            outputFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param prefixStr
     * @param filePath
     * @return
     * 解析properties ,根据传递的包含前缀的key，找出所有对应的value。
     */
    public static List<String> getAnalyzePropData(final String prefixStr,String filePath){
//        MyLog.e(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>>>getAnalyzePropData()>>>>prefixStr:"+prefixStr);

        List<String> datas = new LinkedList<>();
        Properties prop = new Properties();
        InputStream ins = null;
        try{

            //读取属性文件.properties
            /////////////////////////
            //方式1；getResourceAsStream。读取sd卡上的文件，in = null；
//            InputStream in = PropertiesUtils.class.getResourceAsStream(filePath);//or null if the resource is not found.
//            PropertiesUtils.class.getResourceAsStream(filePath);//or null if the resource is not found.
            //////////////////////
            ins = new FileInputStream(filePath);
            if (ins != null)
            {
//                MyLog.e(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>>getAnalyzePropData()>>>>in:" + ins.toString());
                prop.load(ins);     ///加载属性列表
                Iterator<String> it = prop.stringPropertyNames().iterator();
                while(it.hasNext()) {

                    String key = it.next();
                    if (key != null && key.startsWith(prefixStr))
                    {
                        //                    String tempName = prop.getProperty(key);
                        //////////////// 对于直接打开properties文件添加key,value的方式，读取key，value需要的方式
//                        String value = new String((prop.getProperty(key)).getBytes("ISO-8859-1"),"utf-8");
                        ////////////////////
                        ///////////////////对于通过程序写入properties的方式，可以直接获取key,value
                        String value = new String((prop.getProperty(key)).getBytes(),"utf-8");
                        //////////////////
                        datas.add(value);

//                        MyLog.e(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>>key:" + key + ",value:" + value);
                    }
                }

            }else {
                MyLog.e(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>in is null" );
                datas.clear();
            }
        } catch (Exception e){
            MyLog.e(Constant.TrackTag.TAG_REMOTE_CONTROL_PALY_FILE, TAG + ">>>Exception:" + e);
            datas.clear();
        }
        finally {
            if (ins != null)
            {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return datas;
    }

}
