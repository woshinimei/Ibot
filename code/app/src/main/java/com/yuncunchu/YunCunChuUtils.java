package com.yuncunchu;

import android.text.TextUtils;
import android.util.Base64;

import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DateUtils;
import com.infomax.ibotncloudplayer.utils.HttpPostUtil;
import com.infomax.ibotncloudplayer.utils.MD5Util;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jy on 2017/2/7 ;8:56.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 */
public class YunCunChuUtils {

    final static String  TAG = YunCunChuUtils.class.getSimpleName();

    /**
     * 用于实现用户登录授权
     * @param netUtilType 1是使用OkHttpUtils作为网络请求【失败】；2 是使用HttpURLConnection作为网络请求；【成功】
     *                    3 是android-async-http作为网络请求【失败】；4 ，使用okhttp【失败】
     */
    public static void loginAuthorization(final int netUtilType ){

        String devId = Utils.getDeviceSerial();
        String key = "!@#1234qaWSXaz";
        String timestamp = DateUtils.formatDate(new Date(System.currentTimeMillis()), 1);

        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU,TAG+">>>>loginAuthorization()>>>"
                        + "\n devId:" + devId
                        + "\n key:" + key
                        + "\n timestamp:" + timestamp
        );
        /********************************************************
         * Authorization  是包头验证信息
         1 》.  使用 Base64 编码（ 编码（ devId +  冒号 +  时间戳）
         2 》.  冒号为英文冒号
         3 》.  时间戳是当前系统时间（24  小时制），格式“yyyyMMddHHmmss”，需与 ”，需与
         SigParameter  中时间戳相同。
         */

        final String authorization = new String(Base64.encode((devId + ":" + timestamp).getBytes(), Base64.DEFAULT));
        /**************************************************
         *请求参数说明  MD5（devId +授权令牌(ms 下发 key )
         + 时间戳），共 32 位(注:转成小写)
         时间戳是当前系统时间（24 小时制）
         格式“yyyyMMddHHmmss”。时间
         戳有效时间为 50 分钟
         */
        String mSigParameter = "";
        mSigParameter = MD5Util.string2MD5((devId + key + timestamp));
        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU,TAG+">>>>loginAuthorization()>>>"
                        + ", devId:" + devId
                        + ", key:" + key
                        + ", timestamp:" + timestamp
                        + ", authorization:" + authorization
                        + ", mSigParameter:" + mSigParameter
        );
        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU,TAG+">>>>loginAuthorization()>>>"
                        + ", mSigParameter:" + mSigParameter
        );

        final String finalMSigParameter = mSigParameter;

        if (netUtilType == 1)
        {
            ///////////////使用OkHttpUtils作为网络请求///////////////
            /**
             * java.lang.IllegalArgumentException: Unexpected char 0x0a at 40 in Authorization value: MDAxNjA5MjQwOTIyQjoyMDE3MDIwNzE2NDcxMw==
             */
                OkHttpUtils
                        .post()
                        .url("http://ycc.ibotn.com/Api/auth")
                        .addHeader("Authorization", authorization)
                        .addParams("SigParameter", mSigParameter)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onError()>>>"
                                                + "\n Exception:" + e.getMessage()

                                );

                            }

                            @Override
                            public void onResponse(String response, int id) {
                                MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                                + "\n response:" + response
                                                + "\n id:" + id

                                );
                                ///////////////json 解析/////////////
                                /**
                                 * {"Status":200,"Token":"191e7f8da3e518f141bf8e07a2d6c523","Message":"\u83b7\u53d6Token\u6210\u529f"}
                                 */
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    if (jsonObject != null) {
                                        int status = jsonObject.getInt("Status");
                                        if (status == 200) {
                                            String token = jsonObject.getString("Token");
                                            if (!TextUtils.isEmpty(token)) {
                                                Constant.YUN_CUN_CHU_TOKEN = token;
                                            }

                                        }
                                        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                                        + ", Status:" + jsonObject.getInt("Status")
                                                        + ", Message:" + jsonObject.getString("Message")
                                        );
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /////////////json 解析---end/////////////////
                            }
                        });
            ///////////////使用OkHttpUtils作为网络请求///////////////

        }else if (netUtilType == 2){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    ///////////////使用HttpURLConnection作为网络请求///////////////
                    /**
                     * 因 okhttp 网络请求，添加头验证信息，异常。换用HttpURLConnection
                     02-06 16:51:59.904 12711-12753/? E/AndroidRuntime: FATAL EXCEPTION: Thread-789
                     Process: com.infomax.ibotncloudplayer, PID: 12711
                     java.lang.IllegalArgumentException: Unexpected char 0x0a at 40 in Authorization value: MDAxNjA5MjQwOTIyQjoyMDE3MDIwNjE2NTE1OQ==
                     02-06 16:51:59.911 177-531/? E/AudioPolicyManager: getNewOutputDevice 4436 to hdmi
                     */
                    try {
                        //传递参数可以拼接
                        String param = "SigParameter=" + URLEncoder.encode(finalMSigParameter, "UTF-8");
                        //建立连接
                        URL url = new URL("http://ycc.ibotn.com/Api/auth");
                        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                        //设置参数
                        httpConn.setDoOutput(true);   //需要输出
                        httpConn.setDoInput(true);   //需要输入
                        httpConn.setUseCaches(false);  //不允许缓存
                        httpConn.setRequestMethod("POST");   //设置POST方式连接
                        httpConn.setConnectTimeout(10 * 1000);
                        httpConn.setReadTimeout(10 * 1000);
                        //设置请求属性
                        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                        httpConn.setRequestProperty("Charset", "UTF-8");
                        httpConn.setRequestProperty("Authorization", authorization);//添加授权头
                        //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect
                        httpConn.connect();
                        //建立输入流，向指向的URL传入参数
                        DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                        dos.writeBytes(param);
                        dos.flush();
                        dos.close();
                        //获得响应状态
                        int resultCode = httpConn.getResponseCode();
                        if (HttpURLConnection.HTTP_OK == resultCode) {
                            StringBuffer sb = new StringBuffer();
                            String readLine = new String();
                            BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                            while ((readLine = responseReader.readLine()) != null) {
                                sb.append(readLine).append("\n");
                            }
                            responseReader.close();
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                            + ", response:" + sb.toString()
                                            + ", resultCode:" + resultCode
                            );

                            ///////////////json 解析/////////////
                            /**
                             * {"Status":200,"Token":"191e7f8da3e518f141bf8e07a2d6c523","Message":"\u83b7\u53d6Token\u6210\u529f"}
                             */
                            JSONObject jsonObject = new JSONObject(sb.toString());

                            if (jsonObject != null) {
                                int status = jsonObject.getInt("Status");
                                if (status == 200) {
                                    String token = jsonObject.getString("Token");
                                    if (!TextUtils.isEmpty(token)) {
                                        Constant.YUN_CUN_CHU_TOKEN = token;
                                    }

                                }
                                MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                                + ", Status:" + jsonObject.getInt("Status")
                                                + ", Message:" + jsonObject.getString("Message")
                                );
                            }


                            /////////////json 解析---end/////////////////

                            //获取文件列表
//                            getFiles(1);

                        } else {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                            + ",resultCode:" + resultCode
                            );
                        }

                    } catch (Exception e) {
                        MyLog.d(TAG, e.getMessage());
                    }
                }
                }.start();
                    ///////////////使用HttpURLConnection作为网络请求----end---///////////////

        }else if (netUtilType == 4)
        {
            /**
             * 使用 okhttp 授权异常：
             * 异常：
             * java.lang.IllegalArgumentException: Unexpected char 0x0a at 40 in Authorization value
             */
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
            RequestBody requestBodyPost = new FormBody.Builder()
                    .add("SigParameter", finalMSigParameter)
                    .build();
            Request requestPost = new Request.Builder()
                    .url("http://ycc.ibotn.com/Api/auth")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Charset", "UTF-8")
//                    .removeHeader("Authorization")//先移除。：java.lang.IllegalArgumentException: Unexpected char 0x0a at 40 in Authorization value
                    .removeHeader("cookie")
                    .header("Authorization", authorization/*"MDAxNjA5MjQwOTIyQjoyMDE3MDIwNzE0NDIwMw=="*/)//授权码有误,请重试
                    .post(requestBodyPost)
                    .build();
//            Request requestPost = new Request.Builder()
//                    .url("http://ycc.ibotn.com/Api/auth")
//                    .headers(new Headers.Builder()
//                            .add("Content-Type", "application/x-www-form-urlencoded")
//                            .add("Charset", "UTF-8")
//                            .add("Authorization", authorization)
//                            .build())
//                    .post(requestBodyPost)
//                    .build();
            client.newCall(requestPost).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onFailure()>>>"
                                    + "\n IOException:" + e.getMessage()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String string = response.body().string();
                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                    + "\n string:" + string
                    );
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                        + ", Status:" + jsonObject.getInt("Status")
                                        + ", Message:" + jsonObject.getString("Message")
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    public static void upload(final File file,int netUtilType) {
        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>>"
                        + ", Constant.YUN_CUN_CHU_TOKEN:" + Constant.YUN_CUN_CHU_TOKEN
                        + ", file:" + (file == null ? "null" : file.getAbsolutePath())
        );

        if (file == null )
        {
            return;
        }
        if (TextUtils.isEmpty(Constant.YUN_CUN_CHU_TOKEN))
        {
            return;
        }

        final String  url = "http://ycc.ibotn.com/Api/upload";

        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU,TAG+">>>>netUtilType:"+netUtilType
                        + ", Devid:" +  Utils.getDeviceSerial()
                        + ", Homeid:" + Utils.getDeviceSerial()
                        + ", Token:" + Constant.YUN_CUN_CHU_TOKEN
                        + ", url:" + url

        );

        if (netUtilType == 1)
        {
            final  String fileName = file.getName();

            OkHttpUtils.post()
                    .addFile("Files", file.getName(), file)
                    .url("http://ycc.ibotn.com/Api/upload")
                    .addParams("Devid", Utils.getDeviceSerial())
                    .addParams("Homeid", Utils.getDeviceSerial())
                    .addParams("Path", Constant.AUDIO_FOLDER_YUNCUNCHU)//指定上传目录。是服务器目录
                    .addParams("Shareid", Utils.getDeviceSerial())//  // TODO: 2017/2/7  待确认组合类型
                    .addParams("Token", Constant.YUN_CUN_CHU_TOKEN)//
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }else if (netUtilType == 3)
        {
            /**
             * 1. 遇到异常
             * Exception:android.os.NetworkOnMainThreadException
             */
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    HttpPostUtil u = null;
                    try {
                        u = new HttpPostUtil(url);
                        u.addFileParameter("Files", file);//文件

                        u.addTextParameter("Devid", Utils.getDeviceSerial());
                        u.addTextParameter("Homeid", Utils.getDeviceSerial());
                        u.addTextParameter("Path", Constant.AUDIO_FOLDER_YUNCUNCHU);//指定上传目录。是服务器目录文件夹
                        u.addTextParameter("Shareid",Utils.getDeviceSerial());
                        u.addTextParameter("Token", Constant.YUN_CUN_CHU_TOKEN);
                        byte[] b = u.send();
                        String result = new String(b);
                        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                        + ", result:" + result
                        );
                        /**
                         *   Message:文件格式有误，请重试
                         Status:107
                         */
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject != null)
                            {
                                MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                                + ", Status:" + (jsonObject.has("Status") ? jsonObject.getInt("Status") : "null")
                                                + ", Message:" + (jsonObject.has("Message") ? jsonObject.getString("Message") : "null")
                                                + ", Message:" + (jsonObject.has("Fileid") ? jsonObject.getString("Fileid") : "null")
                                                + ", Originalurl:" + (jsonObject.has("Originalurl") ? jsonObject.getString("Originalurl") : "null")
                                                + ", Thumbnailurl:" + (jsonObject.has("Thumbnailurl") ? jsonObject.getString("Thumbnailurl") : "null")
                                );
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                            + ", JSONException:" + e.getMessage()
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                        + ", Exception:" + e
                                        + ", Exception:" + e.getMessage()
                        );
                    }
                }
            }.start();
        }

    }

    /**
     * @param netUtilType 1是使用OkHttpUtils作为网络请求；2 是android-async-http作为网络请求
     * @param netUtilType
     */
    public static void getFiles(int netUtilType){
        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>getFiles()>>>"
                        + ", Constant.YUN_CUN_CHU_TOKEN:" + Constant.YUN_CUN_CHU_TOKEN
        );

        if (TextUtils.isEmpty(Constant.YUN_CUN_CHU_TOKEN))
        {
            return;
        }

        if (netUtilType == 1){

            OkHttpUtils.post()
                    .url("http://ycc.ibotn.com/Api/list")
                    .addParams("Devid", Utils.getDeviceSerial())
                    .addParams("Homeid", Utils.getDeviceSerial())
                    .addParams("Path", Constant.AUDIO_FOLDER_YUNCUNCHU)//指定上传目录。是服务器目录
                    .addParams("Token", Constant.YUN_CUN_CHU_TOKEN)//
                    .addParams("Offset", "0")
                    .addParams("Limit", "10")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>getFiles()>>onError>>>"
                                            + "\n id:" + id
                                            + "\n Exception:" + e.getMessage()
                            );
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>getFiles()>>onResponse>>>"
                                            + "\n id:" + id
                                            + "\n response:" + response
                            );

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject != null) {
                                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>onResponse>>>"
                                                    + "\n Message:" + jsonObject.get("Message")
                                                    + "\n Status:" + jsonObject.getInt("Status")
                                    );

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }
    }

    /* 上传文件，WithHttpurlconnection */
    public static void uploadFileWithHttpurlconnection(final File file) {

        new Thread(){
            @Override
            public void run() {
                super.run();
                String end = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                String newName = file.getName();
                String uploadFile = file.getAbsolutePath();
                String actionUrl = "http://ycc.ibotn.com/Api/upload";
                try {
                     //拼接参数
                    String params = "Devid=" + URLEncoder.encode(Utils.getDeviceSerial(), "UTF-8")
                         + "&Homeid="+ URLEncoder.encode(Utils.getDeviceSerial(), "UTF-8")
                         + "&Path="+ URLEncoder.encode(Constant.AUDIO_FOLDER_YUNCUNCHU, "UTF-8")
                         + "&Shareid="+ URLEncoder.encode(Utils.getDeviceSerial(), "UTF-8")//  TODO: 2017/2/7  待确认组合类型 //文件格式有误，请重试
                         + "&Token="+ URLEncoder.encode(Constant.YUN_CUN_CHU_TOKEN, "UTF-8");
                    URL url = new URL(actionUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
			        /* 允许Input、Output，不使用Cache */
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);

                    con.connect();

                    DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                    ds.writeBytes(params);
                    ds.writeBytes(end);
                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes(end);
                    ds.writeBytes("Content-Disposition: form-data; name=\"" + "Files"
                            + "\"; filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\"\r\n");//TODO
                    ds.writeBytes(end);
                    FileInputStream fStream = new FileInputStream(uploadFile);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;
                    while ((length = fStream.read(buffer)) != -1) {
                        ds.write(buffer, 0, length);
                    }
                    ds.writeBytes(end);
                    ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                    fStream.close();
                    ds.flush();
                    ds.close();

//			        /* 取得Response内容 */
                    //////////////////////////////////////////
//                    InputStream is = con.getInputStream();
//                    int ch;
//                    StringBuffer b = new StringBuffer();
//                    while ((ch = is.read()) != -1) {
//                        b.append((char) ch);
//                    }
//                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>success>>>"
//                                    + "\n b.toString().trim():" + b.toString().trim()
//                    );
                    //////////////////////////////////


                    //////////////////////////////////////
                    //获得响应状态
                    int resultCode = con.getResponseCode();
                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                    + ", resultCode:" + resultCode
                    );
                    if (HttpURLConnection.HTTP_OK == resultCode) {
                        StringBuffer sb = new StringBuffer();
                        String readLine = new String();
                        BufferedReader responseReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                        while ((readLine = responseReader.readLine()) != null) {
                            sb.append(readLine).append("\n");
                        }
                        responseReader.close();
                        MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                        + ", response:" + sb.toString()
                                        + ", resultCode:" + resultCode
                        );

                        ///////////////json 解析/////////////

                        JSONObject jsonObject = new JSONObject(sb.toString());

                        if (jsonObject != null) {
                            MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>onResponse()>>>"
                                            + ", Status:" + (jsonObject.has("Status") ? jsonObject.getInt("Status") : "null")
                                            + ", Message:" + (jsonObject.has("Message") ? jsonObject.getString("Message") : "null")
                                            + ", Message:" + (jsonObject.has("Fileid") ? jsonObject.getString("Fileid") : "null")
                                            + ", Originalurl:" + (jsonObject.has("Originalurl") ? jsonObject.getString("Originalurl") : "null")
                                            + ", Originalurl:" + (jsonObject.has("Originalurl") ? jsonObject.getString("Originalurl") : "null")
                            );
                        }


                        /////////////json 解析---end/////////////////
                        /////////////////////////////////////
                    }else
                    {

                    }
                } catch (Exception e) {
                    MyLog.d(Constant.TAG_COMMON_YUNCUNCHU, TAG + ">>>>upload()>>failed>>>"
                                    + "\n Exception:" + e.getMessage()
                    );
                }
            }
        }.start();

    }

}
