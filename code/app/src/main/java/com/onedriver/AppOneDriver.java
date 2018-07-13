
package com.onedriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;

import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.concurrency.IProgressCallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.core.OneDriveErrorCodes;
import com.onedrive.sdk.extensions.Folder;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.Item;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.logger.LoggerLevel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.onedrive.sdk.options.Option;
import com.onedrive.sdk.options.QueryOption;

import org.json.JSONObject;

/**
 * 2016/12/28
 * jy< <br/>
 * OneDriver 总操控类
 */
public class AppOneDriver {
    private final static String TAG = AppOneDriver.class.getSimpleName();
    /**
     * 为了保存登录后的IOneDriveClient实例
     */
    private final AtomicReference<IOneDriveClient> mClient = new AtomicReference<>();

    private static AppOneDriver instance;

    /**
     * The item id for this item root
     */
    final String mItemRootId = "root";
    /**
     * The item id for current item
     */
    private String mCurrentItemId = "";

    /**
     * The backing item representation
     */
    private Item mItem;
    /**
     * The prefix for the item breadcrumb when the parent reference is unavailable
     */
    private static final String DRIVE_PREFIX = "/drive/";

    /**
     * If the current fragment should prioritize the empty view over the visualization
     */
    private final AtomicBoolean mEmpty = new AtomicBoolean(false);

    /**
     * Expansion options to get all children, thumbnails of children, and thumbnails
     */
    private static final String EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS = "children(expand=thumbnails),thumbnails";

    /**
     * Expansion options to get all children, thumbnails of children, and thumbnails when limited
     */
    private static final String EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS_LIMITED = "children,thumbnails";

    /**文件夹 onedriver服务器上Ibotn文件夹下面的文件夹列表**/
    private List<String> bellowIbotnFolderList = new LinkedList();
    /**文件夹 onedriver服务器上root根文件夹下面的文件夹列表**/
    private List<String> bellowRootList = new LinkedList();

    private IOneDriveClient oneDriveClient = null;
    /** 正在上传图片集合到onedrive服务器；如果有内容，长按图片不再执行操作 */
    public ConcurrentLinkedQueue clqForUploadingImageOnedrive = new ConcurrentLinkedQueue();
    /** 正在上传视频集合到onedrive服务器；如果有内容，长按视频不再执行操作 */
    public ConcurrentLinkedQueue clqForUploadingVideoOnedrive = new ConcurrentLinkedQueue();
    private void AppOneDriveClient() {
    }
    public static AppOneDriver getInstance(){

        if (instance == null){
            synchronized (AppOneDriver.class){
                if (instance == null){
                    instance = new AppOneDriver();
                }
            }
        }
        return  instance;
    }

//
//	final ADALAuthenticator adalAuthenticator = new ADALAuthenticator() {
//		@Override
//		public String getClientId() {
//			return "2af62822-6424-48a4-b461-b52853a24c0c;
//		}
//
//		@Override
//		protected String getRedirectUrl() {
//			return "https://localhost";
//		}
//	}

    /**
     * 获取 IOneDriveClient。注意关闭进程，等操作后mClient的值就为空了。需要再次登录
     * TODO 改方法可以改进  不用抛异常，为null时直接返回null.调用者自行判断处理。
     * @return
     */
    synchronized public IOneDriveClient getOneDriveClient() {
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>getOneDriveClient()>>>>>>mClient.get():" + mClient.get());
        if (mClient.get() == null) {
            throw new UnsupportedOperationException("Unable to generate a new service object");
        }
        return mClient.get();
    }

    /**
     * 加载oneDriver ，如果没有登录就登录appOneDriver.createOneDriveClientForLogin(MainActivity.this, serviceCreated);<br/>
     * 如果已经登录过了就提示并调用appOneDriver.checkOrCreateFolders。
     * 对于通过IbotnCoreService调用该方法，不会执行appOneDriver.createOneDriveClientForLogin()
     */
    public void loadOnedrive(final Context mContext) {
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>loadOnedrive()>>>>>>:");
        final String mItemRootId = "root";
        final AppOneDriver appOneDriver = AppOneDriver.getInstance();
        final ICallback<Void> serviceCreated = new DefaultCallback<Void>(mContext) {
            @Override
            public void success(final Void result) {
//                ToastUtils.showCustomToast("Connect successfully to OneDrive.");
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>loadOnedrive>>success()>>>>>>:");
                appOneDriver.checkOrCreateFolders(mContext,mItemRootId);
            }

            @Override
            public void failure(ClientException error) {
                super.failure(error);
//                ToastUtils.showCustomToast(mContext, "Connect to OneDrive failed");
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>loadOnedrive>>failure()>>>>>>:"+error.getMessage());
            }
        };
        try {
            IOneDriveClient iOneDriveClient = appOneDriver.getOneDriveClient();

            if (iOneDriveClient != null){//此条件说明是登录过了
//                ToastUtils.showCustomToast(null,mContext.getString(R.string.tip_logined_onedrive));
            }

            appOneDriver.checkOrCreateFolders(mContext,mItemRootId);
        } catch (final UnsupportedOperationException ignored) {
            MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>UnsupportedOperationException>>>>>>serviceCreated:" + serviceCreated);
            if (Constant.mainActivity != null)
            {
                appOneDriver.createOneDriveClientForLogin(Constant.mainActivity /*MainActivity.getInstance()*/, serviceCreated);
            }
        }
    }

    private IClientConfig createConfig() {
        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
            @Override
            public String getClientId() {
//                return "2af62822-6424-48a4-b461-b52853a24c0c";
                return Config.CLIENT_ID;
            }

            @Override
            public String[] getScopes() {
                return new String[] {"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access"};
            }
        };

        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
        config.getLogger().setLoggingLevel(LoggerLevel.Debug);

        return config;
    }

    /**
     * Used to setup the Services
     * @param activity the current activity
     * @param serviceCreated the callback
     * 该函数主要用于登录oneDriver。
     */
    public synchronized void createOneDriveClientForLogin(final Activity activity, final ICallback<Void> serviceCreated) {

        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER,TAG + ">>>>createOneDriveClientForLogin()>>>>>>:");

        final DefaultCallback<IOneDriveClient> callback = new DefaultCallback<IOneDriveClient>(activity) {
            @Override
            public void success(final IOneDriveClient result) {

                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>success()>>>>>>:");

                mClient.set(result);
                serviceCreated.success(null);
            }

            @Override
            public void failure(final ClientException error) {
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>failure()>>>>>>:");
                serviceCreated.failure(error);
            }
        };
        new OneDriveClient
                .Builder()
                .fromConfig(createConfig())
                .loginAndBuildClient(activity, callback);
    }

    /**
     * @param context
     * @param itemId 要查询的文件夹id,根目录时是root。查询root下面的子文件夹时是item.id
     * 1.检查服务器是否有【IBOTN文件夹】，如果没有就创建<br/>
     * 2.在第一次执行该方法完成后。即【IBOTN文件夹】创建成功后，或者已经有【IBOTN文件夹】；就第二次调用该方法，检查{【PHOTO】，【VIDEO】...}有没有创建，如果没有就创建<br/>
     * 注意：该方法只执行两次，根据上面特定情况具体分析。
     */
    public synchronized void checkOrCreateFolders(final Context context,final String itemId) {

        mCurrentItemId = itemId;

        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>checkOrCreateFolders()>>>>>>itemId:" + itemId);

        final IOneDriveClient oneDriveClient = getOneDriveClient();
        final ICallback<Item> itemCallback = getItemCallback(context,mCurrentItemId);

        oneDriveClient
                .getDrive()
                .getItems(itemId)
                .buildRequest()
                .expand(getExpansionOptions(oneDriveClient))
                .get(itemCallback)
                ;
    }
    /**
     * Gets the expansion options for requests on items
     * @see {https://github.com/OneDrive/onedrive-api-docs/issues/203}
     * @param oneDriveClient the OneDrive client
     * @return The string for expand options
     */
    @NonNull
    private String getExpansionOptions(final IOneDriveClient oneDriveClient) {
        final String expansionOption;
        switch (oneDriveClient.getAuthenticator().getAccountInfo().getAccountType()) {
            case MicrosoftAccount:
                expansionOption = EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS;
                break;

            default:
                expansionOption = EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS_LIMITED;
                break;
        }
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>getExpansionOptions()>>>>expansionOption>>:"+expansionOption);
        return expansionOption;
    }

    /**
     * Creates a callback for drilling into an item
     * @param context The application context to display messages
     * @param mCurrentItemId
     * @return The callback to refresh this item with
     * 目前支持查询root,及IBOTN 时调用<br/>
     * 注意：该方法只执行两次，根据上面特定情况具体分析。
     *  描述注意参考：checkOrCreateFolders 注释说明
     */
    private synchronized ICallback<Item> getItemCallback(final Context context, final String mCurrentItemId) {
        bellowIbotnFolderList.clear();
        bellowRootList.clear();

        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>getItemCallback()>>>>>>mCurrentItemId:"+mCurrentItemId);

        return new DefaultCallback<Item>(context) {
            @Override
            public void success(final Item item) {
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>getItemCallback()>>success()>>>>>>mCurrentItemId:" + mCurrentItemId);
                mItem = item;

                String text = null;
                try {
                    String rawString = item.getRawObject().toString();
                    final JSONObject object = new JSONObject(rawString);
                    final int intentSize = 3;
                    text = object.toString(intentSize);

//                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ",mItem+"+mItem+",\n"+",text:"+text);

                } catch (final Exception e) {
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + "Unable to parse the response body to json" +"mCurrentItemId:" + mCurrentItemId);
                }

                final String fragmentLabel;
                if (mItem.parentReference != null) {//如果当前是root根目录， mItem.parentReference == null
                    fragmentLabel = mItem.parentReference.path
                            + File.separator
                            + mItem.name;
                } else {
                    fragmentLabel = DRIVE_PREFIX + mItem.name;
                }

                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>fragmentLabel>>>:" + fragmentLabel
                        + "\n mCurrentItemId:" + mCurrentItemId);

                mEmpty.set(item.children == null || item.children.getCurrentPage().isEmpty());

                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>:"
                        + ",\n mCurrentItemId" + mCurrentItemId
                        + ",\n item.children:" + (item.children == null ? "null" : item.children.getCurrentPage().size())
                        );

                if (item.children == null || item.children.getCurrentPage().isEmpty())
                {//查询的当前目录下没有内容
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>item.children:" + item.children
                            + ",\n mCurrentItemId:" + mCurrentItemId);
                    //TODO
                } else
                {//查询的当前目录下有内容
                    for (final Item childItem : item.children.getCurrentPage())
                    {
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                                + ",\n childItem :"+childItem
                                + ",\n childItem.id :"+childItem.id
                                + ",\n childItem.children :"+ (childItem.children == null ? "null" : childItem.children.getCurrentPage().size() )
                                + ",\n childItem.name :"+childItem.name
                                + ",\n childItem.folder :"+childItem.folder
                                + ",\n childItem.parentReference.path :"+childItem.parentReference.path
                                );

                        if(mCurrentItemId.equals(mItemRootId))
                        {//当前查询的是root根目录下面的文件夹
                            if (childItem.folder != null){
                                bellowRootList.add(childItem.name);
                            }
                            if (Constant.IBOTN_FOLDER_ONEDRIVER.equals(childItem.name)){
                                Constant.IBOTN_FOLDER_ID_ONEDRIVER = childItem.id;
                            }
                        }else
                        {
                            //添加所有的文件夹
                            if (childItem.folder != null)
                            {
                                bellowIbotnFolderList.add(childItem.name);
                            }

                            if (Constant.PHOTO_FOLDER_ONEDRIVER.equals(childItem.name))
                            {
                                //已存在PHOTO文件夹，取出该文件夹对应的id；
                                Constant.PHOTO_FOLDER_ID_ONEDRIVER = childItem.id;//对id赋值
                            }else if(Constant.VIDEO_FOLDER_ONEDRIVER.equals(childItem.name))
                            {
                                //已存在VIDEO文件夹，取出该文件夹对应的id；
                                Constant.VIDEO_FOLDER_ID_ONEDRIVER = childItem.id;//对id赋值
                            }
                        }

                    }
                }
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                        + "\n bellowRootList:" + bellowRootList.size());
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                        + "\n bellowIbotnFolderList:" + bellowIbotnFolderList.size());

                if (mCurrentItemId.equals(mItemRootId))
                {//当前查询的是root根目录
                    if (!bellowRootList.contains(Constant.IBOTN_FOLDER_ONEDRIVER))
                    {
                        //去创建IBOTN文件夹
                        createFolder(context,item,mItemRootId,Constant.IBOTN_FOLDER_ONEDRIVER);
                    }else
                    {
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                                +"，already created before:" + Constant.IBOTN_FOLDER_ONEDRIVER);

                        //此时检查IBOTN文件夹下面是否有【PHOTO】，【VIDEO】...
                        checkOrCreateFolders(context, Constant.IBOTN_FOLDER_ID_ONEDRIVER);
                    }

                }else
                {//当前查询的是/root:/IBOTN根目录
                    if (!bellowIbotnFolderList.contains(Constant.PHOTO_FOLDER_ONEDRIVER))
                    {
                        createFolder(context,item,Constant.IBOTN_FOLDER_ID_ONEDRIVER,Constant.PHOTO_FOLDER_ONEDRIVER);

                    }else {
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                                +"already created before:" + Constant.PHOTO_FOLDER_ONEDRIVER);
                    }
                    if (!bellowIbotnFolderList.contains(Constant.VIDEO_FOLDER_ONEDRIVER))
                    {
                        createFolder(context,item,Constant.IBOTN_FOLDER_ID_ONEDRIVER,Constant.VIDEO_FOLDER_ONEDRIVER);
                    }else
                    {
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>>mCurrentItemId:" + mCurrentItemId
                                +"already created before:" + Constant.VIDEO_FOLDER_ONEDRIVER);
                    }
                }


            }

            @Override
            public void failure(final ClientException error) {
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + "failure>>>>>>>>:"+error.getMessage());

                //失败：比如在ibotn上注册的微软账户需要在电脑上授权认证后，才可以在手机及ibotn上登录。
                /**
                 *  OneDrive Service exception Error code: itemNotFound
                     Error message: Item does not exist
                 */

                //可以在这提示用户到电脑端登录oneDriver授权认证。
                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_use_your_onedrive_account_to_login_and_cofig_on_computer));
                    }
                });

            }
        };
    }
    /**
     * Creates a folder
     * @param context
     * @param item item The parent of the folder to create
     * @param belongToItemId  将要创建的文件夹所属的父目录id
     * @param toCreatedFolderName 将要创建的文件夹
     * 注意：getOneDriveClient()的异常无需再次捕获；因调用createFolder方法前或上层已经 判断过 mClient.get() ！= null
     */
    private synchronized void createFolder(final Context context,final Item item,final  String belongToItemId,final String toCreatedFolderName) {

        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>createFolder()>>belongToItemId>>>>:" + belongToItemId
            + "\n toCreatedFolderName:" +    toCreatedFolderName
        );

        final ICallback<Item> callback = new DefaultCallback<Item>(context) {
            @Override
            public void success(final Item createdItem) {
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>createFolder()>>success()>>>:"
                        + "\n mCurrentItemId:" + mCurrentItemId
                        + "\n createdItem.name:" +createdItem.name
                        + "\n item.name:" + item.name
                        );
//                      refresh();//TODO

                        if (Constant.IBOTN_FOLDER_ONEDRIVER.equals(toCreatedFolderName)){
                            Constant.IBOTN_FOLDER_ID_ONEDRIVER = createdItem.id;

                            //此时检查IBOTN文件夹下面是否有【PHOTO】，【VIDEO】...
                            checkOrCreateFolders(context,Constant.IBOTN_FOLDER_ID_ONEDRIVER);
                        }

                        if (Constant.PHOTO_FOLDER_ONEDRIVER.equals(toCreatedFolderName))
                        {
                            Constant.PHOTO_FOLDER_ID_ONEDRIVER = createdItem.id;//对id赋值
                        }else if(Constant.VIDEO_FOLDER_ONEDRIVER.equals(toCreatedFolderName))
                        {
                            Constant.VIDEO_FOLDER_ID_ONEDRIVER = createdItem.id;
                        }

            }

            @Override
            public void failure(final ClientException error) {
                super.failure(error);
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>createFolder()>>>failure()>>>:"
                        + "\n mCurrentItemId:" + mCurrentItemId
                        + "\n new_folder_error，" + item.name);
            }
        };

        final Item newItem = new Item();
        newItem.name = toCreatedFolderName;
        newItem.folder = new Folder();

        getOneDriveClient()
                .getDrive()
                .getItems(belongToItemId)
                .getChildren()
                .buildRequest()
                .create(newItem, callback);
    }

    public void oneDrive_Login(final Context context, final Activity activity) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            return;
        }

        try {
            getOneDriveClient();
            // TBD
        } catch (final UnsupportedOperationException ignored) {
            final DefaultCallback<IOneDriveClient> callback = new DefaultCallback<IOneDriveClient>(activity) {
                @Override
                public void success(final IOneDriveClient result) {
                    mClient.set(result);
                    Toast.makeText(context, "Connect successfully to OneDrive.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(final ClientException error) {
                    Toast.makeText(context, "Connect to OneDrive failed", Toast.LENGTH_LONG).show();
                }
            };

            new OneDriveClient
                .Builder()
                .fromConfig(createConfig())
                .loginAndBuildClient(activity, callback);
        }
    }

    /**
     * Clears out the auth token from the application store
     */
    void oneDrive_LogOut() {
        if (mClient.get() == null) {
            return;
        }

        mClient.get().getAuthenticator().logout(new ICallback<Void>() {
            @Override
            public void success(final Void result) {
                mClient.set(null);
                //final Intent intent = new Intent(getApplicationContext(), ApiExplorer.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
            }

            @Override
            public void failure(final ClientException ex) {
                //Toast.makeText(getBaseContext(), "Logout error " + ex, Toast.LENGTH_LONG).showToastDebug();
            }
        });
    }

    /**
     * 上传文件
     * @param filePath
     * @param showUI 是否展示UI,及toast。这一个还不够，还需要第二个标志控制。
     */
    public synchronized void uploadFile(final String filePath,final String itemId, final boolean showUI) {
        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>uploadFile()>>>>filePath: " + filePath
                + ",itemId:" + itemId
                + ",oneDriveClient:" + oneDriveClient
                + ",showUI:" + showUI
                );

        try {
            oneDriveClient = getOneDriveClient();
        }catch (UnsupportedOperationException e)
        {
            if (showUI)
            {
                ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_to_main_view_login_onedrive));
            }
            return;
        }

        if (oneDriveClient == null)
        {
            if (showUI){
                ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_to_main_view_login_onedrive));
            }
            return;
        }else if (TextUtils.isEmpty(itemId))
        {//注意：通过ibotn或手机中oneDriver内置注册界面注册的账号，登录后itemId的值是为空的。需要到电脑端配置
            if (showUI){
                ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_to_main_view_login_onedrive));
            }
            return;
        }

        final AsyncTask<Void, Void, Void> uploadFileAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>doInBackground>>>uploadFileAsyncTask>> start>>>filePath:"+filePath);
                if (!TextUtils.isEmpty(filePath)){
                    final String tempFilePath = filePath.toLowerCase();
                    try {
                        //正在准备上传文件时加入列表
                        if (!TextUtils.isEmpty(tempFilePath)){
                            if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>doInBackground>>>uploadFileAsyncTask>>clqForUploadingImageOnedrive:"+AppOneDriver.getInstance().clqForUploadingImageOnedrive.size());
                                AppOneDriver.getInstance().clqForUploadingImageOnedrive.add(filePath);
                            }else if (tempFilePath.endsWith(".mp4")){
                                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>doInBackground>>>uploadFileAsyncTask>>clqForUploadingVideoOnedrive:"+AppOneDriver.getInstance().clqForUploadingVideoOnedrive.size());
                                AppOneDriver.getInstance().clqForUploadingVideoOnedrive.add(filePath);
                            }
                        }
                        FileInputStream inputStream  = new FileInputStream(filePath);
                        final String fileName = Uri.parse(filePath).getLastPathSegment();
                        File tempFile = new File(filePath);
    //                    ByteArrayOutputStream baos = new ByteArrayOutputStream((int) tempFile.length());
                        int fileSize = (int) tempFile.length();
                        byte[] fileInMemory = new byte[fileSize];
    //                    byte[] fileInMemory = memorySteam.toByteArray();
                        inputStream.read(fileInMemory, 0, fileSize);

                        final long currentTime = SystemClock.uptimeMillis();
                        long elapseTime = 0;

                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>uploadFile()>>doInBackground>>filePath: " + filePath
                                        + ",fileName:" + fileName
                                        + ",tempFile.length():" + tempFile.length()
                                        + ",fileInMemory.length:" + fileInMemory.length
                                 );

                        final Option option = new QueryOption("@name.conflictBehavior", "fail");
                        oneDriveClient
                                .getDrive()
                                .getItems(itemId)
                                .getChildren()
                                .byId(fileName)
                                .getContent()
                                .buildRequest(Collections.singletonList(option))
                                .put(fileInMemory,
                                        new IProgressCallback<Item>() {
                                            @Override
                                            public void success(final Item item) {
                                                //上传完成后从列表中移除
                                                if (!TextUtils.isEmpty(tempFilePath)){
                                                    if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                                        AppOneDriver.getInstance().clqForUploadingImageOnedrive.remove(filePath);
                                                    }else if (tempFilePath.endsWith(".mp4")){
                                                        AppOneDriver.getInstance().clqForUploadingVideoOnedrive.remove(filePath);
                                                    }
                                                }
                                                if (showUI && Constant.SHOW_UI_TIP)
                                                {
                                                    ToastUtils.showCustomToast(fileName + " " + MyApplication.getInstance().getString(R.string.tip_upload_success));
                                                }
                                                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>uploadFile()>>doInBackground>>>success()>filePath: " + filePath
                                                                + "\n fileName:" + fileName
                                                                + "\n elapse time:" + (SystemClock.uptimeMillis() - currentTime)
                                                );

                                            }

                                            @Override
                                            public void failure(final ClientException error) {
                                                //有异常时从列表中移除
                                                if (!TextUtils.isEmpty(tempFilePath)){
                                                    if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                                        AppOneDriver.getInstance().clqForUploadingImageOnedrive.remove(filePath);
                                                    }else if (tempFilePath.endsWith(".mp4")){
                                                        AppOneDriver.getInstance().clqForUploadingVideoOnedrive.remove(filePath);
                                                    }
                                                }
                                                if (error.isError(OneDriveErrorCodes.NameAlreadyExists)) {
                                                    if (showUI && Constant.SHOW_UI_TIP){
                                                        ToastUtils.showCustomToast(fileName
                                                                        + " " + MyApplication.getInstance().getString(R.string.tip_upload_failure)
                                                                        + " " + MyApplication.getInstance().getString(R.string.tip_upload_file_already_exists)
                                                        );

                                                    }
                                                } else {
                                                    if (showUI && Constant.SHOW_UI_TIP){
                                                        ToastUtils.showCustomToast(fileName + " " + MyApplication.getInstance().getString(R.string.tip_upload_failure));
                                                    }
                                                }
                                                /**
                                                    1. log:服务器已存在 :Error code: nameAlreadyExists  Error message: An item with the same name already exists under the parent
                                                    2. 11.已经登录。【正在上传文件a】,此时重置服务器将该文件夹重命名。【结果上传失败】。因为程序中文件夹id使用的是静态变量。【解决办法：重新启用onedriver,创建文件夹】
                                                       a)打印出的log。error: Error code: itemNotFound Error message: Item does not exist
                                                 */
                                                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>uploadFile()>>failure()>>error:"+ error.getMessage());
                                                if (error.isError(OneDriveErrorCodes.ItemNotFound)) {
                                                    if (showUI && Constant.SHOW_UI_TIP){
                                                        ToastUtils.showCustomToast(MyApplication.getInstance().getString(R.string.tip_to_main_view_login_onedrive));
                                                    }
                                                }
                                            }

                                            @Override
                                            public void progress(final long current, final long max) {
                                                if (showUI && Constant.SHOW_UI_TIP){
                                                    ToastUtils.showCustomToast(fileName + " "
                                                            + MyApplication.getInstance().getString(R.string.tip_upload_progress)
                                                            + ":" + (current * 100 / max) + "%");
                                                }
                                            }
                                        });

                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>doInBackground>>>uploadFileAsyncTask>> end>>>filePath:"+filePath);
                    } catch (final Exception e) {
                        MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>uploadFile>>>>>" + e.getMessage());
                        //有异常时从列表中移除
                        if (!TextUtils.isEmpty(tempFilePath)){
                            if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")){
                                AppOneDriver.getInstance().clqForUploadingImageOnedrive.remove(filePath);
                            }else if (tempFilePath.endsWith(".mp4")){
                                AppOneDriver.getInstance().clqForUploadingVideoOnedrive.remove(filePath);
                            }
                        }
                    }
                }
                return null;
            }
        };

        uploadFileAsyncTask.execute();
    }

}
