package com.wifidirect;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.FileEnhancedUtils;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.wifidirect.DeviceListFragment.DeviceActionListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {
    public static final String TAG = "DeviceDetailFragment";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    /**
     * 控制线程一直在运行
     */
    private boolean isRun = false;

    private ServerSocket serverSocket;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.frg_device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (device != null){
                    if (device.status == WifiP2pDevice.CONNECTED){
                        ToastUtils.showCustomToast(getString(R.string.wifi_state_connected));
                        return;
                    }
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    // TODO 开发者无法决定GroupOwner是哪台设备，但是可以通过WifiP2pConfig.groupOwnerIntent参数进行建议。【该参数不可靠】
                    //config.groupOwnerIntent = 15;
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    progressDialog = ProgressDialog.show(getActivity(), getString(R.string.connecting),
                            getString(R.string.connecting_to)+ " : " + device.deviceAddress, true, true,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // ((DeviceActionListener) getActivity()).cancelDisconnect(); //// TODO: 2017/5/24
                                }
                            }
                    );

                    MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>onClick()>>deviceAddress: " + config.deviceAddress
                            + ",wps.setup:" + config.wps.setup);
                    ((DeviceActionListener) getActivity()).connect(config);
                }
            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();

                        WifiDirectConstant.isCancel = true;
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        return mContentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>EXTRAS_FILE_PATH:" + uri.toString()
                + "\n EXTRAS_GROUP_OWNER_ADDRESS:" + info.groupOwnerAddress.getHostAddress()
                + "\n EXTRAS_GROUP_OWNER_PORT:" + WifiDirectConstant.DEFAUT_PORT
        );
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, WifiDirectConstant.DEFAUT_PORT);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>onConnectionInfoAvailable()"
                + ",isGroupOwner:" + info.isGroupOwner
                + ",groupOwnerAddress:" + info.groupOwnerAddress.getHostAddress()
                + ",device:" + info.toString());

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        resetBtnConnect();

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));
        view.setVisibility(View.GONE);
        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        //因为只提供了群主的ip，所以群主只能接收文件，所以自己传IP到群成员
        if (WifiDirectConstant.isReceiveServer && !info.isGroupOwner) {//是接收端又不是群主，主动链接群主，让群主发送过来
            sendGroupMemberIP();
            receiveConnection();
        } else if (!WifiDirectConstant.isReceiveServer && info.isGroupOwner) {//是发送端却是群主
            acceptGroupMemberIP();

        } else {
            if (info.groupFormed && WifiDirectConstant.isReceiveServer) {
                receiveConnection();
          /*  new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case, we enable the
                // get file button.
            /*mContentView.findViewById(R.id.btn_start_client).setVisibility(View.INVISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));*/

                //sendSingleFile();
                //sendMultiFiles();
                startFileTransferService(info.groupOwnerAddress.getHostAddress());
            }
        }
        //mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    private void resetBtnConnect() {
        if (device == null){
            if (mContentView != null){
                mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
            }
        }else {
            if (mContentView != null){
                mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendGroupMemberIP() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MyLog.d(WiFiDirectActivity.TAG,">>>>>>>>>>>>>>>>>>>>>sendGroupMemberIP>>>>>>>>>>>>>>>>>");
                    Socket socket = new Socket(info.groupOwnerAddress, WifiDirectConstant.DEFAUT_PORT);
                    socket.getOutputStream().write(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void acceptGroupMemberIP() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MyLog.d(WiFiDirectActivity.TAG,">>>>>>>>>>>>>>>>>>>>>acceptGroupMemberIP>>>>>>>>>>>>>>>>>");
                    Socket socket= new ServerSocket(WifiDirectConstant.DEFAUT_PORT).accept();
                    String hostAddress = socket.getInetAddress().getHostAddress();
                    String localAddress = socket.getLocalAddress().getHostAddress();
                    MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>acceptGroupMemberIP>>hostAddress:"+hostAddress + ",localAddress:" + localAddress);
                    startFileTransferService(hostAddress);

                } catch (IOException e) {
                    e.printStackTrace();
                    MyLog.d(WiFiDirectActivity.TAG,"acceptGroupMemberIP Exception:"+e.getMessage());
                }
            }
        });

    }

    private void receiveConnection() {
        if (isRun) {
            MyLog.d(WiFiDirectActivity.TAG, "receiveConnection Run 已在運行，不重複運行");
            return;
        }
        isRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>>receiveConnection>>>>new connection start>>");
                    serverSocket = new ServerSocket(WifiDirectConstant.DEFAUT_PORT);
                    while (isRun) {
                        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>>receiveConnection>>>>Socket.accept()>>");
                        Socket socket = serverSocket.accept();
                        String remoteHostAddress = socket.getInetAddress().getHostAddress();//此socket对应的远端地址
                        String localAddress = socket.getLocalAddress().getHostAddress();//此socket对应的本地地址
                        MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>receiveConnection>>remoteHostAddress:"+remoteHostAddress + ",localAddress:" + localAddress);
                        singleThreadExecutor.submit(new ReceiveFileRunnable(socket));
                    }
                } catch (IOException e) {
                    MyLog.d(WiFiDirectActivity.TAG, TAG + ">>receiveConnection IOException:" + e.getMessage());
                    e.printStackTrace();
                    shutDownSocket();
                }
            }
        }).start();
    }
    private void shutDownSocket() {
        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>shutDownSocket");
        if (serverSocket != null) {
//            singleThreadExecutor.shutdownNow();
            try {
                serverSocket.close();
                isRun = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     */
    private void startFileTransferService(String hostAddress){
        MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>startFileTransferService()");

        MyLog.d(WiFiDirectActivity.TAG, TAG +">>>startFileTransferService()>>>EXTRAS_FILE_PATH:" + ""
                + "\n EXTRAS_GROUP_OWNER_ADDRESS:" + info.groupOwnerAddress.getHostAddress()
                + "\n EXTRAS_GROUP_OWNER_PORT:" + WifiDirectConstant.DEFAUT_PORT
                + "\n hostAddress:" + hostAddress
        );
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "");
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                hostAddress);
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, WifiDirectConstant.DEFAUT_PORT);
        getActivity().startService(serviceIntent);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;

        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>showDetails()"
                + ",deviceAddress:" + device.deviceAddress
                + ",device:" + device.toString());

        if (mContentView != null){
            mContentView.setVisibility(View.VISIBLE);
            TextView view = (TextView) mContentView.findViewById(R.id.device_address);
            view.setText(device.deviceAddress);
            view = (TextView) mContentView.findViewById(R.id.device_info);
            view.setText(device.toString());
        }

        resetBtnConnect();
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            DataInputStream dis = null;
            ServerSocket serverSocket = null;
            Socket client = null;
            try {
                serverSocket = new ServerSocket(WifiDirectConstant.DEFAUT_PORT);
                MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>Server: Socket opened");
                client = serverSocket.accept();
                MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>Server: connection done");
                MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>server: copying files ");
                dis = new DataInputStream(client.getInputStream());
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();

                MyLog.d(WiFiDirectActivity.TAG, TAG + ">>>>>filename:" + fileName + ",fileLength:" + fileLength);

                final File f = new File(Constant.Config.ROOT_PATH_FOR_WIFIDIRECT_STORAGE,fileName);
                File dirs = new File(f.getParent());
                if (!dirs.exists()){
                    dirs.mkdirs();
                }
                f.createNewFile();

                copyFile(dis, new DataOutputStream(new FileOutputStream(f)));

                return f.getAbsolutePath();
            } catch (IOException e) {
                MyLog.e(WiFiDirectActivity.TAG, TAG +">>>>IOException:" +  e.getMessage());
                return null;
            }finally {

                if (client != null)
                {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                if (serverSocket != null)
                {
                    try {
                        serverSocket.close();
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
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied finish " + result);
                //only for test to receive image ,and show image
                /*Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image*//*");
                context.startActivity(intent);*/
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    /**
     * ReceiveFile Runnable
     */
    private class ReceiveFileRunnable implements Runnable {
        private Socket socket;

        public ReceiveFileRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            DataInputStream dis = null;
            DataOutputStream dos = null;
            try {
                Log.d(WiFiDirectActivity.TAG, TAG + ">>>>ReceiveFileRunnable>>>>Server: connection done");
                dis = new DataInputStream(socket.getInputStream());

                final String fileName = dis.readUTF();
                long fileLength = dis.readLong();

                Log.d(WiFiDirectActivity.TAG, TAG + ">>>>>ReceiveFileRunnable>>>>filename:" + fileName + ",fileLength:" + fileLength);

                if (!TextUtils.isEmpty(fileName)) {
                    String fileNameLower = fileName.toLowerCase();

                    //current only seperate image of jpg,png;video of mp4。all these file are send by ibotncloudplayer
                    String fileStoragePath = FileUtils.getCurrentSdRootPathFirst(getActivity()) + File.separator+ WifiDirectConstant.Config.ROOT_PATH_OF_WIFIDIRECT_IMAGE_STORAGE;//default: ROOT_PATH_OF_WIFIDIRECT_IMAGE_STORAGE
                   /* if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".png")) {
                        fileStoragePath = FileRelatedUtils.getCurrentSdRootPathFirst(getActivity()) + WifiDirectConstant.Config.ROOT_PATH_OF_WIFIDIRECT_IMAGE_STORAGE;
                    } else if (fileNameLower.endsWith(".mp4")) {
                        fileStoragePath = FileRelatedUtils.getCurrentSdRootPathFirst(getActivity()) + WifiDirectConstant.Config.ROOT_PATH_OF_WIFIDIRECT_VIDEO_STORAGE;
                    }*/
                    Log.d(WiFiDirectActivity.TAG, TAG + ">>>>>ReceiveFileRunnable>>>>fileStoragePath:" + fileStoragePath);

                    boolean createOrExistsDir = FileUtils.createOrExistsDir(fileStoragePath);
                    Log.d(WiFiDirectActivity.TAG, TAG + ">>>>>ReceiveFileRunnable>>>>createOrExistsDir:" + createOrExistsDir);

                    final File file = new File(fileStoragePath, fileName);
                    file.createNewFile();

                    dos = new DataOutputStream(new FileOutputStream(file));
                    copyFile(dis, dos);

                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), fileName + getActivity().getString(R.string.transfer_complete), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, TAG + ">>>>ReceiveFileRunnable>>>>IOException:" + e.getMessage());
            } finally {

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     *
     * @param inputStream
     * @param out
     * @return
     */
    public static boolean copyFile(DataInputStream inputStream, DataOutputStream out) {

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            MyLog.d(WiFiDirectActivity.TAG, TAG +">>>>IOException:" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.getView() != null){
            this.getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>onDestroyView");
    }

    @Override
    public void onDestroy() {
        MyLog.d(WiFiDirectActivity.TAG, TAG + ">>onDestroy");
        shutDownSocket();
        super.onDestroy();
    }
}