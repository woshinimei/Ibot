package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * 工具类SharedPreference  <br/>
 * 1.Created by juying on 2016/10/23.<br/>
 * 2.modify:优化日期2016-12-13
 */
public class SharedPreferenceUtils {
    public static SharedPreferences getSp(Context context){
        return context.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 根据SharedPreferences的name获取sp
     * @param spName
     * @return
     */
    public static SharedPreferences getSp(String spName){
        return MyApplication.getInstance().getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    /**
     * 将Object集合转换成字符串
     * @param obj
     * @return
     * @throws IOException
     */
    public static String object2String(Object obj) throws IOException {
        // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 然后将得到的字符数据装载到ObjectOutputStream
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                byteArrayOutputStream);
        // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
        objectOutputStream.writeObject(obj);
        // 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
        String listString = new String(Base64.encode(
                byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return listString;
    }

    /**
     * 将字符串转换成压缩时的对象
     * @param <T>
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T string2Object(String objString)
            throws StreamCorruptedException, IOException,
            ClassNotFoundException {
        byte[] mobileBytes = Base64.decode(objString.getBytes(),
                Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                mobileBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        T obj = (T) objectInputStream.readObject();
        byteArrayInputStream.close();
        objectInputStream.close();
        return obj;
    }

//    真正调用的时候
////存储操作
//
//    SharedPreferences sp = Context.getSharedPreferences("mylist",Context.MODE_PRIVATE);
//    Editor edit = sp.edit();
//    try {
//
////将list集合转成字符串
//        String listStr = PhoneSaveUtil.object2String(list);
////存储
//        edit.putString("mylistStr", listStr);
//        edit.commit();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }

//    //取值操作
//    SharedPreferences sp = mContext.getSharedPreferences("mylist",Context.MODE_PRIVATE);
//    String liststr = sp.getString("mylistStr", "");
//    if (!TextUtils.isEmpty(liststr)) {
//        try {
//            list = PhoneSaveUtil.string2Object(liststr);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void setVideoPath(Context context, String path) {
        SharedPreferences sp = context.getSharedPreferences("mylist", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("videopath", path);
        edit.commit();

}
    //取值操作
    public static String getVideoPatch(Context context) {
        SharedPreferences sp = context.getSharedPreferences("mylist", Context.MODE_PRIVATE);
        return sp.getString("videopath", "");

    }

   /* /**
     * 保存当前用户级别
     * @param context
     * @param userLevel
     */
   /* public static void setUserLevel(Context context, int userLevel) {
        SharedPreferences sp = context.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(Constant.MySharedPreference.Ibotn_User_level, userLevel);
        edit.commit();

    }*/

   /* /**
     * 获取当前用户级别
     * @param context
     * @return
     */
  /*  public static int getUserLevel(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(Constant.MySharedPreference.Ibotn_User_level, 1);
    }*/
    public static void setIbotnFile(Context context, String ibotnFile) {
        SharedPreferences sp = context.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constant.MySharedPreference.Ibotn_Video_File, ibotnFile);
        edit.commit();

    }
    public static String  getIbotnFile(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.MySharedPreference.SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(Constant.MySharedPreference.Ibotn_Video_File, "");
    }

    /**
     * 将本地音频播放历史集合，保存到sp中
     * @param context
     * @param list
     * @throws Exception
     */
    public static void setLocalAudioHistoryList(Context context,List<LocalAudioBean> list) throws Exception {
        String object2String = SharedPreferenceUtils.object2String(list);
        SharedPreferenceUtils.getSp(context)
                .edit()
                .putString(Constant.MySharedPreference.SP_KEY_LOCAL_AUDIO_LIST,object2String)
                .commit();
    }
    /**
     * 获取本地音频播放历史集合
     * @param context
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<LocalAudioBean>  getLocalAudioHistoryList(Context context) throws Exception  {
        String listString = SharedPreferenceUtils.getSp(context).getString(Constant.MySharedPreference.SP_KEY_LOCAL_AUDIO_LIST, "");
        return (List<LocalAudioBean>)SharedPreferenceUtils.string2Object(listString);
    }

    /**
     * 获取语音拍照完成后自动上传的开关
     * @return
     */
    public static boolean getSwitchStateForAutomaticPhoto(Context context){

        return getSp(context).getBoolean(Constant.MySharedPreference.TOGGLE_UPLOAD_PHOTO , false);
    }

    /**
     * 设置 语音拍照完成后自动上传的开关
     * @return
     */
    public static void setSwitchStateForAutomaticPhoto(Context context,boolean value){

        SharedPreferenceUtils.getSp(context)
                .edit()
                .putBoolean(Constant.MySharedPreference.TOGGLE_UPLOAD_PHOTO,value)
                .commit();
    }
}
