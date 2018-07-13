package com.wifidirect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {
    public final String TAG = "FileTransferService";
    private static final int SOCKET_TIMEOUT = 10000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>>>onHandleIntent()>>>thread-name:" + Thread.currentThread().getName());

        //sendFile(intent);

        sendFileWithThreadPool(intent);

        //sendMultiFileWithSingleConnection(intent);
    }

    /**
     * send file to server ,use socket<br/>
     * unuse;
     */
    public void sendFile(Intent intent){
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String filePath = intent.getExtras().getString(EXTRAS_FILE_PATH);

            //fileUri for example:>>>>"file:///storage/sdcard/DCIM/Camera/IMG_20170428_145029600.jpg"
            String fileUri = null;
            MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>sendFile>>filePath:" + filePath);

            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            DataOutputStream dos = null;
            InputStream is = null;
            DataInputStream dis = null;
            try {
                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendFile>>>Opening client socket - >>>");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendFile>>>Client socket send file start>>>");
                OutputStream stream = socket.getOutputStream();
                dos = new DataOutputStream(stream);
                //should use file absolute path ,not file uri
                final File file = new File(filePath);
                if (file != null && file.exists()){
                    MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendFile>>>filename:" + file.getName() + ",length:" + file.length());
                    //write file name
                    dos.writeUTF(file.getName());
                    dos.flush();

                    //write file length
                    dos.writeLong(file.length());
                    dos.flush();

                    ContentResolver cr = context.getContentResolver();
                    try {
                        //is = cr.openInputStream(Uri.parse(fileUri));
                        is = cr.openInputStream(Uri.fromFile(file));
                        dis = new DataInputStream(is);

                        DeviceDetailFragment.copyFile(dis, dos);
                        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendFile>>>Client socket send file finish!");

                        ThreadUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showCustomToast(file.getName() + getString( R.string.transfer_complete));
                            }
                        });

                        /*if (WifiDirectConstant.wifiDirectActivity != null)
                        {
                            WifiDirectConstant.wifiDirectActivity.finish();
                        }*/
                    } catch (FileNotFoundException e) {
                        Log.d(WiFiDirectActivity.TAG, TAG + ">>sendFile>>> FileNotFoundException:" + e.toString());
                    }
                }
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG,  TAG + ">>sendFile>>>IOException:" + e.getMessage());

                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showCustomToast(getString(R.string.transfer_fail));
                    }
                });
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (is != null)
                {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dis != null)
                {
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dos != null)
                {
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        SystemClock.sleep(50);
    }
    /**
     * send file to server ,use socket.
     */
    public void sendFileWithThreadPool(final Intent intent){
        for (final String filePath : WifiDirectConstant.FILE_PATHS){

            if (FileUtils.isFile(filePath)){
                singleThreadExecutor.execute(new SendFileSocketRunnable(intent,filePath));
            }
        }
    }
    private class SendFileSocketRunnable implements Runnable{

        private Intent intent;
        private String filePath;

        /**
         *
         * @param intent
         * @param filePath
         */
        public SendFileSocketRunnable(Intent intent, final String filePath){
            this.intent = intent;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            Context context = getApplicationContext();
            if (intent.getAction().equals(ACTION_SEND_FILE)) {
                //fileUri for example:>>>>"file:///storage/sdcard/DCIM/Camera/IMG_20170428_145029600.jpg"
                String fileUri = null;
                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>SendFileSocketRunnable>>filePath:" + filePath);

                String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
                Socket socket = new Socket();
                DataOutputStream dos = null;
                InputStream is = null;
                DataInputStream dis = null;
                try {
                    MyLog.d(WiFiDirectActivity.TAG, TAG + ">>SendFileSocketRunnable>>>Opening client socket - >>>");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    MyLog.d(WiFiDirectActivity.TAG, TAG + ">>SendFileSocketRunnable>>>Client socket send file start>>>");
                    OutputStream stream = socket.getOutputStream();
                    dos = new DataOutputStream(stream);
                    //should use file absolute path ,not file uri
                    final File file = new File(filePath);
                    if (file != null && file.exists()){
                        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>SendFileSocketRunnable>>>filename:" + file.getName() + ",length:" + file.length());
                        //上传时加入文件列表
                        final String tempFilePath =  filePath.toLowerCase();//转化为小写
                        if (!TextUtils.isEmpty(tempFilePath)){
                            if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                WifiDirectConstant.clqImageWifiDirect.add(filePath);
                            }else if (tempFilePath.endsWith(".mp4")){
                                WifiDirectConstant.clqVideoWifiDirect.add(filePath);
                            }
                        }

                        //write file name
                        dos.writeUTF(file.getName());
                        dos.flush();

                        //write file length
                        dos.writeLong(file.length());
                        dos.flush();

                        ContentResolver cr = context.getContentResolver();
                        try {
                            //is = cr.openInputStream(Uri.parse(fileUri));
                            is = cr.openInputStream(Uri.fromFile(file));
                            dis = new DataInputStream(is);

                            DeviceDetailFragment.copyFile(dis, dos);
                            MyLog.d(WiFiDirectActivity.TAG, TAG + ">>SendFileSocketRunnable>>>Client socket send file finish!");

                            ThreadUtils.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showCustomToast(file.getName() + getString( R.string.transfer_complete));
                                }
                            });
                            //上传完成时从文件列表中移除
                            if (!TextUtils.isEmpty(tempFilePath)){
                                if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                    WifiDirectConstant.clqImageWifiDirect.remove(filePath);
                                }else if (tempFilePath.endsWith(".mp4")){
                                    WifiDirectConstant.clqVideoWifiDirect.remove(filePath);
                                }
                            }

                        } catch (FileNotFoundException e) {
                            Log.d(WiFiDirectActivity.TAG, TAG + ">>SendFileSocketRunnable>>> FileNotFoundException:" + e.toString());
                            //上传异常时从文件列表中移除
                            if (!TextUtils.isEmpty(tempFilePath)){
                                if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                    WifiDirectConstant.clqImageWifiDirect.remove(filePath);
                                }else if (tempFilePath.endsWith(".mp4")){
                                    WifiDirectConstant.clqVideoWifiDirect.remove(filePath);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(WiFiDirectActivity.TAG,  TAG + ">>SendFileSocketRunnable>>>IOException:" + e.getMessage());

                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showCustomToast(getString(R.string.transfer_fail));
                        }
                    });
                    //上传异常时从文件列表中移除
                    final String tempFilePath =  filePath.toLowerCase();//转化为小写
                    if (!TextUtils.isEmpty(tempFilePath)){
                        if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                            WifiDirectConstant.clqImageWifiDirect.remove(filePath);
                        }else if (tempFilePath.endsWith(".mp4")){
                            WifiDirectConstant.clqVideoWifiDirect.remove(filePath);
                        }
                    }
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (is != null)
                    {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (dis != null)
                    {
                        try {
                            dis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (dos != null)
                    {
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * send file to server ,use socket.
     */
    public void sendMultiFileWithSingleConnection(Intent intent){
        Context context = getApplicationContext();
        String action = intent.getAction();
        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>sendMultiFileWithSingleConnection>>action:" + action);
        if (action.equals(ACTION_SEND_FILE)) {
            //fileUri for example:>>>>"file:///storage/sdcard/DCIM/Camera/IMG_20170428_145029600.jpg"
            String fileUri = null;
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            DataOutputStream dos = null;
            InputStream is = null;
            DataInputStream dis = null;
            try {
                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendMultiFileWithSingleConnection>>>Opening client socket - >>>");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendMultiFileWithSingleConnection>>>Client socket send file start>>>");
                OutputStream stream = socket.getOutputStream();
                dos = new DataOutputStream(stream);

                for (String filePath : WifiDirectConstant.FILE_PATHS){

                    if (FileUtils.isFile(filePath)){
                        //should use file absolute path ,not file uri
                        final File file = new File(filePath);
                        if (file != null && file.exists()){
                            MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendMultiFileWithSingleConnection>>>filename:" + file.getName() + ",length:" + file.length());
                            //write file name
                            dos.writeUTF(file.getName());
                            dos.flush();

                            //write file length
                            dos.writeLong(file.length());
                            dos.flush();

                            ContentResolver cr = context.getContentResolver();
                            //is = cr.openInputStream(Uri.parse(fileUri));
                            is = cr.openInputStream(Uri.fromFile(file));
                            dis = new DataInputStream(is);

                            DeviceDetailFragment.copyFile(dis, dos);

                            dis.close();
                            dos.flush();

                            MyLog.d(WiFiDirectActivity.TAG, TAG + ">>sendMultiFileWithSingleConnection>>>Client socket send file finish!");

                            ThreadUtils.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showCustomToast(file.getName() + getString( R.string.transfer_complete));
                                }
                            });

                        /*if (WifiDirectConstant.wifiDirectActivity != null)
                        {
                            WifiDirectConstant.wifiDirectActivity.finish();
                        }*/
                        }
                    }
                }

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG,  TAG + ">>sendMultiFileWithSingleConnection>>>IOException:" + e.getMessage());

                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showCustomToast(getString(R.string.transfer_fail));
                    }
                });
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (is != null)
                {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dis != null)
                {
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dos != null)
                {
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
