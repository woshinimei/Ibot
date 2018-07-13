package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;

/**
 * Created by jy on 2016/11/30.
 */
public class FileEnhancedUtils {

    private static final String TAG = FileEnhancedUtils.class.getSimpleName();



    /**
     * 将Object集合转换成字符串，base64加密
     * @param obj
     * @return
     * @throws IOException
     */
    public static String object2StringEncode(Object obj) throws IOException {
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
     * 将字符串转换成压缩时的对象,base64解密
     * @param <T>
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T string2ObjectDecode(String objString)
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

    /**
     * 将Object集合转换成字符串
     * @param obj
     * @return
     * @throws IOException
     */
    public static String object2String(Object obj)/* throws IOException */{
        String listString = null;
        try {
            // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // 然后将得到的字符数据装载到ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    byteArrayOutputStream);
            // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
            objectOutputStream.writeObject(obj);

            listString = new String(byteArrayOutputStream.toByteArray());
            objectOutputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listString;
    }

    /**
     * 将字符串转换成压缩时的对象
     * @param <T>
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T string2Object(String objString){

        T obj = null;
        try {
            byte[] mobileBytes = objString.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    mobileBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    byteArrayInputStream);
            obj = (T) objectInputStream.readObject();
            byteArrayInputStream.close();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 获取sd卡的根目录，没有sd卡再获取应用的缓存目录
     * @return
     */
    public static String getSdRootPath(Context context) {
        String rootPath = "";//根路径
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else {
            context.getFilesDir().getAbsolutePath();
        }

        return rootPath;
    }

    /**
     * 文件操作，读取用户级别
     * @param context
     */
    public static void run(final Context context){
        MyLog.e(TAG, "run>>>>>>>>>>>>>>>>>>");
        new Thread(){
            @Override
            public void run() {
                super.run();

//                PropertiesUtils.getUserLevel(context);
//                String ibotnFilePath = Constant.Config.Education_Content_Video_File_Root_Path +File.separator+ "ibotn简介"+File.separator + "ibotn看护机器人宣传片.MP4";
//                String ibotnFilePath_v = Constant.Config.Education_Content_Video_File_Root_Path +File.separator+ "ibotn简介"+File.separator + "ibotn_v.MP4";
//                String ibotnFilePath_n = Constant.Config.Education_Content_Video_File_Root_Path +File.separator+ "ibotn简介"+File.separator + "ibotn_n.MP4";
//                file2Byte(context,ibotnFilePath);

//                VideoEncryptUtils.obtainFileState(new File(ibotnFilePath));

//               dealFileForLevel(ibotnFilePath);
//                dealFileForLevel(ibotnFilePath_v);
//                dealFileForLevel(ibotnFilePath_n);
//                String ibotnFilePath_V = Constant.Config.Education_Content_Video_File_Root_Path +File.separator+ "ibotn简介"+File.separator + "ibotn看护机器人宣传片_v.png";
//                String ibotnFilePath_N = Constant.Config.Education_Content_Video_File_Root_Path +File.separator+ "ibotn简介"+File.separator + "ibotn看护机器人宣传片_n.png";

            }
        }.start();

    }


    //图片到byte数组
    public static byte[] file2Byte(Context context,String path){
        //读取属性文件
//        InputStream input = FileEnhancedUtils.class.getResourceAsStream("/assets/ibotn看护机器人宣传片_v.png");

//       String ibotnFolder = SharedPreferenceUtils.getIbotnFolder(context);

        byte[] data = null;
        try {

            InputStream input = new FileInputStream(new File(path));

            MyLog.e(TAG,"path>>>>>>>>>>>>>>>>>>:"+path);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);

                String ms = new String(buf);

                MyLog.e(TAG, "buf>>>>>>>>>>>>>>>>>>:" + ms);
            }
            data = output.toByteArray();
            MyLog.e(TAG,"data>>>>>>>>>>>>>>>>>>:"+data.toString());
            int length = data.length;
            String s = new String(data);
            String ss = s.substring(length-16,length);//最后16 个字节

            MyLog.e(TAG,"ss>>>>>>>>>>>>>>>>>>:"+ss);

            output.close();
            input.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    /**
     * 解析视频文件的最后十六个字符，开头的十个如果是tag:ibotnv 即2 ; tag:ibotnn 即 1,其余的情况值也指定为 1
     * 同步：
     * @param strFile 源文件绝对路径
     * @return
     */
    public synchronized static boolean dealFileForLevel(Context context, String strFile) {
        try {
            File f = new File(strFile);
            MyLog.e(TAG,"exists>>>>>>>>>>>>>>>>>>:"+f.exists());
            if (f.exists()){
                RandomAccessFile raf = new RandomAccessFile(f, "rw");
                int totalLen = (int)raf.length();

                int offset = totalLen - 16;
                raf.seek(offset);

                byte[] buffer = new byte[16];

                raf.read(buffer);

                String s = new String(buffer,"UTF-8");
                MyLog.e(TAG, "s>>>>>>>>>>>>>>>>>>:" + s);

               /* if (s.startsWith("tag:ibotnv")){

                    SharedPreferenceUtils.setUserLevel(context,2);

                }else if (s.startsWith("tag:ibotnn"))
                {
                    SharedPreferenceUtils.setUserLevel(context,1);
                }else
                {
                    SharedPreferenceUtils.setUserLevel(context,1);
                }
*/

//                byte[] buffer = new byte[16];
//                int length = totalLen;
//
//                int n = 0;
//
//                byte[] x = new byte[16];
//                for (int i = length - 1;i>=0;i--)
//                {
//
//                    x[n] = buffer[i];
//                    MyLog.e(TAG, ",buffer["+i+"]:"+buffer[i]);
//
//                    n ++ ;
//                    if (n>=15){
//                        break;
//                    }
//
//                }
//                String s = new String(x,"UTF-8");
//                MyLog.e(TAG, "s>>>>>>>>>>>>>>>>>>:" + s);


//                String s = new String(buffer);//走不下去了
//
//                String ss = s.substring(length-16,length);//最后16 个字节
//
//                MyLog.e(TAG, "ss>>>>>>>>>>>>>>>>>>:" + ss);

                raf.close();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
