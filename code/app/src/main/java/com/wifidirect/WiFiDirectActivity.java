package com.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.wifidirect.DeviceListFragment.DeviceActionListener;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends FullScreenActivity implements ChannelListener, DeviceActionListener {

    public static final String TAG = "WiFiDirectActivity";
    private LinearLayout back_button_container;
    private TextView tv_direct_enable;
    private TextView tv_direct_discover;

    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_wifi_direct);

        //add necessary intent values to be matched.

        initViews();

        registerListener();

        initData();
    }

    private void initViews() {
        back_button_container = (LinearLayout) findViewById(R.id.back_button_container);
        tv_direct_enable = (TextView) findViewById(R.id.tv_direct_enable);
        tv_direct_discover = (TextView) findViewById(R.id.tv_direct_discover);
    }

    private void registerListener() {
        back_button_container.setOnClickListener(onClickListener);
        tv_direct_enable.setOnClickListener(onClickListener);
        tv_direct_discover.setOnClickListener(onClickListener);

    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == tv_direct_enable)
            {
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    MyLog.e(TAG, TAG +">>>>channel or manager is null");
                }
            }else if (v == tv_direct_discover)
            {
                if (!isWifiP2pEnabled) {
                    ToastUtils.showCustomToast(getString(R.string.please_connection_wifi));
                    return ;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        //Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                        //        Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                       // Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,//        Toast.LENGTH_SHORT).show();
                    }
                });
            }else if (v == back_button_container){
                finish();
            }

        }
    };
    private void initData() {
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        //WifiDirectConstant.FILE_PATH = getIntent().getStringExtra(WifiDirectConstant.EXTRAS_FILE_PATH);
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);//表明可用的对等点的列表发生了改变
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        MyLog.d(TAG, TAG +">>>>showDetails>>>>");
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }
    /**
     * 创建Group，在两个设备中谁调用这个方法，谁就是服务端。
     * Create a p2p group with the current device as the group owner
     * 1.注意：应在createGroup的ActionListener的onSuccess()中调用connect(.)
     */
    private void beGroupOwener(final WifiP2pConfig config) {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                MyLog.d(TAG, TAG +">>>>createGroup>>>onSuccess()>>");

                manager.connect(channel, config, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                MyLog.e(TAG, TAG +">>>>createGroup>>>onFailure()>>reason: " + reason);
            }
        });
    }

    @Override
    public void connect(WifiP2pConfig config) {

        MyLog.d(TAG, TAG +">>>>connect()>>>deviceAddress:" + config.deviceAddress
                + ",wps.setup:" + config.wps.setup);

        // Create a p2p group with the current device as the group owner
        if (WifiDirectConstant.RUN_ON_IBOTN_DEVICE){

            manager.connect(channel, config, new ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                     MyLog.d(TAG, TAG +">>>>connect()>>>onSuccess:");
                }

                @Override
                public void onFailure(int reason) {
                    MyLog.d(TAG, TAG +">>>>connect()>>>onFailure:");
                    //Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                    //        Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            beGroupOwener(config);

        }
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                MyLog.d(TAG, TAG +">>>>Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG, TAG +">>>onDestroy>>");
    }
}