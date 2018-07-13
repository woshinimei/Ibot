package com.infomax.ibotncloudplayer.fragment;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.infomax.ibotncloudplayer.QQCloudAdapter;
import com.infomax.ibotncloudplayer.QQCloudPlayerActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.activity.VideoActivity;
import com.infomax.ibotncloudplayer.adapter.CloudFolderAdapter;
import com.infomax.ibotncloudplayer.adapter.QQCloudLvAdapter;
import com.infomax.ibotncloudplayer.bean.CloudFolderBean;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.*;
import com.infomax.ibotncloudplayer.view.LoadingDialog;
import com.ysx.qqcloud.QQCloudFileInfo;
import com.ysx.qqcloud.QQCloudObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by juying on 2016/10/18.
 */
public class CloudVideoFragment extends Fragment implements  View.OnClickListener{

    private  final String TAG = "CloudVideoFragment";
    /** 视频列表gridtview */
//    private MGridView mGridView_File;
    private PullToRefreshGridView mGridView_File;

    /** 视频列表listview */
//    private ListView mListView;
    private PullToRefreshListView mListView;

    private LinearLayout ll_frg_cloud_video;

    /**文件夹 listview*/
    private ListView lv_folder;
    public List<QQCloudFileInfo> QQFileList = null;

    private QQCloudObject qqObject;
    private View mBottomBar;

    private Context ctx;

    private View currentView;

    private VideoActivity mVideoActivity;

    private Button btn_list_for_cloud;

    /** 是否显示列表状态 默认false 为九宫格*/
    private boolean isStateList;

    RelativeLayout rl_frg_cloud_video;

    private  RelativeLayout rl_nodata;
    private ImageView ivArrow;

    private LoadingDialog loadingDialog;
    /** 云端 FoldersUnderPublicVideo */
    private ArrayList<CloudFolderBean> folderList;
    private CloudFolderBean currentCloudFolderBean;
    private CloudFolderAdapter cloudFolderAdapter;

    /** 默认一页显示条目数 */
    private final int PAGE_SIZE = 10;
    private int currentPageIndex;

    /** 当前是否有数据 或者当前文件夹下*/
    private boolean currentHasData;
    private QQCloudAdapter gvAdapter;
    private QQCloudLvAdapter lvAdapter;

    /** 当前 gridview,listview id*/
    private int currentGvOrLvId;

    /**
     * 当前网络连接状态 true为连接
     */
    private boolean netConnected = false;

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoActivity != null) {
            mVideoActivity.actEndLearn();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//      View view = inflater.inflate(R.layout.fragment_local_video,container,false);//无法使用
        View view = View.inflate(getActivity(),R.layout.fragment_cloud_video,null);

        ctx = getActivity();

        loadingDialog = new LoadingDialog(ctx);

        mVideoActivity = (VideoActivity) getActivity();

        isStateList = false;

        currentView = view;

        initView(view);

        initPullToRefreshLvAndGv();

        changeViewGvOrLv(false);

        registListener();

        initData();

        return view;
    }

    private  void initView(View view){

        ll_frg_cloud_video = (LinearLayout) view.findViewById(R.id.ll_frg_cloud_video);

//      mGridView_File = (MGridView) view.findViewById(R.id.gv_group);
//      mGridView_File.setHaveScrollBar(false);
        mGridView_File = (PullToRefreshGridView) view.findViewById(R.id.gv_group);

//        mListView = (ListView) view.findViewById(R.id.lv_list);
        mListView = (PullToRefreshListView) view.findViewById(R.id.lv_list);

        lv_folder = (ListView) view.findViewById(R.id.lv_frg_cloud_video_folder);
        btn_list_for_cloud = (Button) mVideoActivity.getViewForCloud();

        rl_frg_cloud_video = (RelativeLayout) view.findViewById(R.id.rl_frg_cloud_video);
        ivArrow = (ImageView) view.findViewById(R.id.iv_frg_cloud_video);

        rl_nodata = (RelativeLayout) view.findViewById(R.id.rl_nodata);

    }
    /**
     * 初始PullToRefreshScrollView
     */
    private void initPullToRefreshLvAndGv() {
//        ILoadingLayout startLabels = mListView.getLoadingLayoutProxy(true, false);
//        startLabels.setPullLabel(getString(R.string.xialashuaxing));
//        startLabels.setReleaseLabel(getString(R.string.fankaiyishuaxing));
//        startLabels.setRefreshingLabel(getString(R.string.loading_));
        ILoadingLayout endLabels = mListView.getLoadingLayoutProxy(false, true);
        endLabels.setPullLabel(getString(R.string.text_pull_load_more));
        endLabels.setReleaseLabel(getString(R.string.text_pull_release));
        endLabels.setRefreshingLabel(getString(R.string.text_loading));

        ILoadingLayout endLabelsGv = mGridView_File.getLoadingLayoutProxy(false, true);
        endLabelsGv.setPullLabel(getString(R.string.text_pull_load_more));
        endLabelsGv.setReleaseLabel(getString(R.string.text_pull_release));
        endLabelsGv.setRefreshingLabel(getString(R.string.text_loading));

    }

    /**
     * 1.第一次加载CloudVideoFragment时调用
     * 2.切换CloudVideoFragment时也调用，重新加载
     */
    public void initData(){

        //监测网络
        if (!NetUtils.isNetworkConnected(ctx))
        {
            netConnected = false;
            ToastUtils.showToast(ctx, getString(R.string.net_unconnected_to_check));
            return;
        }
        netConnected = true;

        currentPageIndex = 1;

        currentGvOrLvId = R.id.gv_group;//默认gridview

        currentHasData = false;

        folderList = new ArrayList<CloudFolderBean>();

        showLoadingDialog();

        initFromMediaType();

//        //test
//        Intent intent = new Intent(ctx, QQCloudInitService.class);
//        ctx.startService(intent);

    }
    private void registListener() {
        //切换list
        if (btn_list_for_cloud != null){

            btn_list_for_cloud.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isStateList = !isStateList;
                    if (isStateList){
                        currentGvOrLvId = R.id.lv_list;

                    }else
                    {
                        currentGvOrLvId = R.id.gv_group;
                    }

                    changeViewGvOrLv(isStateList);
                }
            });
        }

        //视频文件列表监听
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                boolean preventMultipleRun = Utils.preventMultipleRun();
                if (preventMultipleRun)
                {
                    ToastUtils.showToast(ctx, getString(R.string.click_too_fast));
                    return;
                }

                if (qqObject != null) {

//                    ToastUtils.showToastDebug(ctx, "---:" + position);

                    showLoadingDialog();

                    saveWatchHitoryToSp(position - 1);

                    QQCloudFileInfo info = (QQCloudFileInfo) lvAdapter.getItem(position - 1);



                    MyLog.d(TAG, info.fileName+",info.fileId:"+info.fileId+",size:"+info.fileSize);
                    File file20 = new File(info.localVideoURL20);
                    File file30 = new File(info.localVideoURL30);
                    if (file20.exists() /*&& file20.isFile() && info.fileSize.equals(file20.length())*/)
                    {
                        dismissLoadingDialog();

                        MyLog.d(TAG, info.fileName + ",file:" + file20.getAbsolutePath()+",size:"+file20.length());
                        playVideoDownloadOk(file20);
                        return;
                    }else if (file30.exists() /*&& file30.isFile() && info.fileSize.equals(file30.length())*/){
                        dismissLoadingDialog();

                        MyLog.d(TAG, info.fileName + ",file:" + file30.getAbsolutePath());
                        playVideoDownloadOk(file30);

                    }else {//不存在下载好的，就重新访问网络
                        //PullToRefreshListView 有头布局，去掉头布局哦 position - 1
                        qqObject.getQQCloudGetVideoUrl(position - 1, true);

                    }
                }
            }
        });

        mGridView_File.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                boolean preventMultipleRun = Utils.preventMultipleRun();
                if (preventMultipleRun)
                {
                    ToastUtils.showToast(ctx, getString(R.string.click_too_fast));
                    return;
                }
                showLoadingDialog();
                saveWatchHitoryToSp(position);

                QQCloudFileInfo info = (QQCloudFileInfo) gvAdapter.getItem(position);

                MyLog.d(TAG, info.fileName+",info.fileId:"+info.fileId);
                File file20 = new File(info.localVideoURL20);
                File file30 = new File(info.localVideoURL30);
                if (file20.exists())
                {
                    dismissLoadingDialog();

                    MyLog.d(TAG, info.fileName + ",file:" + file20.getAbsolutePath());
                    playVideoDownloadOk(file20);
                    return;
                }else if (file30.exists()){
                    dismissLoadingDialog();

                    MyLog.d(TAG, info.fileName + ",file:" + file30.getAbsolutePath());
                    playVideoDownloadOk(file30);

                }else {//不存在下载好的，就重新访问网络

                    //PullToRefreshGridView 没有头布局。
                    qqObject.getQQCloudGetVideoUrl(position, true);
                }

            }
        });

        //listview folder
        lv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                boolean preventMultipleRun = Utils.preventMultipleRun();
                if (preventMultipleRun)
                {
                    ToastUtils.showToast(ctx, getString(R.string.click_too_fast));
                    return;
                }
                showLoadingDialog();

//                ToastUtils.showToast(ctx, "position:" + position + "---->>>>" + folderList.run(position).name);

                currentCloudFolderBean.selected = false;//将上次的取消选中

                folderList.get(position).selected = !folderList.get(position).selected;
                currentCloudFolderBean = folderList.get(position);

                cloudFolderAdapter.notifyDataSetChanged();

                currentPageIndex = 1;//每次点击就重置为1

                qqObject.getQQCloudInfoByClassId(folderList.get(position).id);
            }
        });

        //箭头点击
        rl_frg_cloud_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lv_folder.getVisibility() == View.VISIBLE) {
                    lv_folder.setVisibility(View.GONE);
                    ivArrow.setBackgroundResource(R.drawable.selector_iv_arrow_right);
                } else {
                    lv_folder.setVisibility(View.VISIBLE);
                    ivArrow.setBackgroundResource(R.drawable.selector_iv_arrow_left);
                }
            }
        });

        //
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

                if (qqObject != null) {
                    if (currentCloudFolderBean != null) {

                        if (currentCloudFolderBean.file_num < PAGE_SIZE) {
                            ptrRefreshComplete();
                            ToastUtils.showCustomToast(getString(R.string.text_no_data_more));
                        } else {
                            ++currentPageIndex;
//                          qqObject.getQQCloudInfoByClassId(qqObject.folderList.run(0).id , currentPageIndex);
                            qqObject.getQQCloudInfoLoadMoreByClassId(currentCloudFolderBean.id, currentPageIndex);

                        }

                    } else //没有文件夹列表时
                    {
                        if (qqObject.folderList.get(0).file_num < PAGE_SIZE) {
                            ptrRefreshComplete();
                            ToastUtils.showCustomToast(getString(R.string.text_no_data_more));

                        } else {
                            ++currentPageIndex;
                            qqObject.getQQCloudInfoLoadMoreByClassId(qqObject.folderList.get(0).id, currentPageIndex);
                        }
                    }
                }
            }
        });

        mGridView_File.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                if (qqObject != null) {
                    if (currentCloudFolderBean != null) {

                        if (currentCloudFolderBean.file_num < PAGE_SIZE) {
                            ptrGvRefreshComplete();
                            ToastUtils.showCustomToast(getString(R.string.text_no_data_more));
                        } else {
                            ++currentPageIndex;
                            //qqObject.getQQCloudInfoByClassId(qqObject.folderList.run(0).id , currentPageIndex);
                            qqObject.getQQCloudInfoLoadMoreByClassId(currentCloudFolderBean.id, currentPageIndex);

                        }

                    } else //没有文件夹列表时
                    {
                        if (qqObject.folderList.get(0).file_num < PAGE_SIZE) {
                            ptrGvRefreshComplete();
                            ToastUtils.showCustomToast(getString(R.string.text_no_data_more));

                        } else {
                            ++currentPageIndex;
                            qqObject.getQQCloudInfoLoadMoreByClassId(qqObject.folderList.get(0).id, currentPageIndex);
                        }
                    }
                }
            }
        });
    }

    private void playVideoDownloadOk(File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);
        String MIMEType = FileMIMEUtils.getMIMEType(file);
        //intent.setDataAndType(Uri.fromFile(file), "video/*");
        intent.setDataAndType(Uri.fromFile(file), MIMEType);
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(intent);
            startLearn(file.getName(),System.currentTimeMillis());
        }else {
            ToastUtils.showToast(ctx, ctx.getString(R.string.tip_video_player_disable));
        }
    }

    private void ptrRefreshComplete() {
        if (null != mListView && mListView.isFooterShown()) {
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.onRefreshComplete();
                }
            });
        }
    }
    private void ptrGvRefreshComplete() {
        if (null != mGridView_File && mGridView_File.isFooterShown()) {
            mGridView_File.post(new Runnable() {
                @Override
                public void run() {
                    mGridView_File.onRefreshComplete();
                }
            });
        }
    }

    private void initFromMediaType()
    {
//          mGridView_File.setVisibility(View.VISIBLE);
            register();
            //qqObject = QQCloudObject.sharedInstance();
            //if (qqObject == null)
            qqObject = new QQCloudObject(ctx);

            qqObject.getFoldersUnderPublicVideo();

//            qqObject.getQQCloudInfo();
//            qqObject.getQQCloudInfoByClassId(10);
            System.gc();
    }
    /** true为列表，false 显示九宫格*/
    private  void changeViewGvOrLv(boolean flag){

        if (flag){
            btn_list_for_cloud.setText(getString(R.string.text_palace));
            mGridView_File.setVisibility(View.GONE);
            if (currentHasData)
            {
                mListView.setVisibility(View.VISIBLE);
            }else
            {
                mListView.setVisibility(View.GONE);
            }
        }else {
            btn_list_for_cloud.setText(getString(R.string.text_list));
            if (currentHasData)
            {
                mGridView_File.setVisibility(View.VISIBLE);

            }else
            {
                mGridView_File.setVisibility(View.GONE);
            }
            mListView.setVisibility(View.GONE);
        }
    }

    private void unInitMediaType()
    {
        unregister();
    }

    private void initCloudVideoView(){
        QQFileList = qqObject.QQFileList;

        MyLog.d(TAG, "initCloudVideoView>>>>>>>QQFileList:" + QQFileList.size());

        if (gvAdapter == null)
        {
            gvAdapter = new QQCloudAdapter(ctx, QQFileList, mGridView_File);
            gvAdapter.setClickListener(this);
            mGridView_File.setAdapter(gvAdapter);
        }else
        {
            gvAdapter.setList(QQFileList);
        }

        //for listview
        if (lvAdapter == null)
        {
            lvAdapter = new QQCloudLvAdapter(ctx,QQFileList);
            mListView.setAdapter(lvAdapter);
        }else
        {
            lvAdapter.setList(QQFileList);
        }

        if (QQFileList.size() == 0){
            showNoDataView(true);
            currentHasData = false;
        }else
        {
            currentHasData = true;
            showNoDataView(false);
        }
    }

    private void updateCloudVideoView() {

        currentView.post(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "updateCloudVideoView>>>>>>:");
                initCloudVideoView();
            }
        });
    }

    private void register(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_INFO);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_INFO_LOADMORE);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_NO_DATA);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_INFO_UPDATE);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_VIDEOURL);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_FOLDER);//foler
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_ERROR);
        ctx.registerReceiver(updateReceiver, intentFilter);
    }

    private void unregister() {
        ctx.unregisterReceiver(updateReceiver);
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            MyLog.d(TAG, "--->>>>>onReceive---->>>action:"+action);
            if (action.equals(QQCloudObject.MSG_QQCLOUD_FOLDER))//folder
            {
//              dismissLoadingDialog();
                updateFolderList();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_INFO)){

                MyLog.d(TAG,"---收到广播了>>>MSG_QQCLOUD_INFO>>>:");

                dismissLoadingDialog();

                updateCloudVideoView();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_INFO_LOADMORE))
            {
                ptrGvRefreshComplete();
                ptrRefreshComplete();
                updateCloudVideoView();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_INFO_UPDATE)){
                MyLog.d(TAG,"---收到广播了>>>MSG_QQCLOUD_INFO_UPDATE>>>:");
                dismissLoadingDialog();

                updateCloudVideoView();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_NO_DATA))
            {
                ptrGvRefreshComplete();
                ptrRefreshComplete();
                ToastUtils.showCustomToast(getString(R.string.text_no_data_more));

            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_VIDEOURL)){

                MyLog.d(TAG,"---收到广播了>>>MSG_QQCLOUD_VIDEOURL>>>:");

                dismissLoadingDialog();

                boolean isDownload = intent.getBooleanExtra("isDownload", false);
                int index = intent.getIntExtra("videoIndex", -1);
                if (index == -1)
                {
                    return;
                }

                if (isDownload) {
                    Intent intentPlayer = new Intent();
                    intentPlayer.setClass(ctx, QQCloudPlayerActivity.class);
                    intentPlayer.putExtra("videoIndex", index);
                    try {
                        startActivity(intentPlayer);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    //SelectedShareByOnline(index);
                }

                //intentPlayer.putExtra("videoURL0", intent.getStringExtra("videoURL0"));
                //intentPlayer.putExtra("videoURL20", intent.getStringExtra("videoURL20"));
                //intentPlayer.putExtra("videoURL30", intent.getStringExtra("videoURL30"));

            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_ERROR)) {

                MyLog.d(TAG, "---收到广播了>>>MSG_QQCLOUD_ERROR>>>:");

                ptrGvRefreshComplete();
                ptrRefreshComplete();

                dismissLoadingDialog();

                int errorCode = intent.getIntExtra("errorCode", 0);
                String errorMsg = intent.getStringExtra("errorMsg");

                ToastUtils.showToast(ctx, R.string.text_server_bus);

//                Toast.makeText(ctx, errorCode + "," + errorMsg, Toast.LENGTH_SHORT).showToastDebug();
            }
        }};

    /**
     * 添加学习轨迹
     * @param fileName
     * @param timeMillis
     */
    private void startLearn(String fileName, long timeMillis) {
            //获取学习轨迹信息
            LearnTrajectoryBean trajectoryBean = new LearnTrajectoryBean();
            trajectoryBean.setName(fileName);
            trajectoryBean.setStartTime(timeMillis);
            trajectoryBean.setTrackType(LearnTrajectoryUtil.Constant.TYPE_EDU_VIDEO);
            mVideoActivity.actStartLearn(trajectoryBean);
    }

    /**
     * 更新文件夹列表
     */
    private void updateFolderList() {

        if (qqObject.folderList.size() == 1){//只有VIDEO根目录,就不显示文件夹
            changeLvFolder(false);

          qqObject.getQQCloudInfoByClassId(qqObject.folderList.get(0).id);
//            qqObject.getQQ
        }
        if (qqObject.folderList.size() > 1){//VIDEO下面只有二级文件夹

            qqObject.getQQCloudInfoByClassId(qqObject.folderList.get(1).id);//默认请求第一个文件夹id

//            qqObject.getQQCloudInfoLoadMoreByClassId(qqObject.folderList.run(0).id,1);

            changeLvFolder(true);

            folderList.clear();
            for (int i=0; i< qqObject.folderList.size() ;i++)
            {
                if (i >= 1) //只过滤子文件夹
                {
                    CloudFolderBean bean = qqObject.folderList.get(i);
                    if (i == 1)
                    {
                        bean.selected = true;//默认选择第一个文件夹

                        currentCloudFolderBean = bean;
                    }

                    folderList.add(bean);
                }
            }

            if (cloudFolderAdapter == null)
            {
                cloudFolderAdapter = new CloudFolderAdapter(ctx,folderList, R.layout.item_lv_common);
            }
            lv_folder.setAdapter(cloudFolderAdapter);
        }
    }

    private  void changeLvFolder(boolean show){
        if (show){
            ll_frg_cloud_video.setVisibility(View.VISIBLE);
        }else {
            ll_frg_cloud_video.setVisibility(View.GONE);
        }
    }
    /**
     * 是否显示没有数据提示控件
     * @param isShow true 显示-即没有数据
     */
    private  void showNoDataView(boolean isShow){
        if (isShow){
            rl_nodata.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mGridView_File.setVisibility(View.GONE);
        }else {
            rl_nodata.setVisibility(View.GONE);

//            mGridView_File.setVisibility(View.VISIBLE);
//            mListView.setVisibility(View.GONE);

            changeViewGvOrLv(isStateList);
        }

    }

    @Override
    public void onDestroy()
    {
        if (netConnected)
        {
            /**
             * 2016-12-9(点击云端时的)异常停止.txt
             * 分析原因：反注册时，之前没有注册就会异常报这样的错。
                 因为：1.之前修改代码，initData()中添加了【没有网络连接的处理】，没有网络就return；而后面还有广播注册。
                 2.videoActivity.重写ondestory(),并将ondestory()传递给fragment
               /////////增加netConnected条件判断/////////////////
             */
            unInitMediaType();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            default:
                    showLoadingDialog();
                    int index = (Integer) v.getTag();
                    qqObject.getQQCloudGetVideoUrl(index, false);
                    //Log.d("QQCloud", "Selected index " + index);
                    //SelectedShare(index);
                    //SelectedShareByOnline(index);
                break;
        }
    }

    private void showLoadingDialog() {
        if (null != loadingDialog && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    dialog.dismiss();
                }
            });
        }

    }
    /**播放历史保存到sp中*/
        private void saveWatchHitoryToSp(int position){

        LinkedList<QQCloudFileInfo> tempList = null;
        //保存bean到sp
        SharedPreferences sp = ctx.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);

        String cloudList = sp.getString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST, "");
        QQCloudFileInfo currentBean = qqObject.QQFileList.get(position);

        MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>-->>:"+cloudList);

        if (TextUtils.isEmpty(cloudList))
        {
            LinkedList<QQCloudFileInfo> tempList1 = new LinkedList<QQCloudFileInfo>();
            tempList1.addFirst(currentBean);

            try {
                String string = SharedPreferenceUtils.object2String(tempList1);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST,string).commit();

            }catch (Exception e)
            {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

            }
            tempList1 = null;

        }else
        {
            try {

                tempList = (LinkedList<QQCloudFileInfo>) SharedPreferenceUtils.string2Object(cloudList);

                if (tempList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
                {
                    tempList.removeLast();//删除最后一个
                }

                if (tempList.contains(currentBean))
                {
                    MyLog.d(TAG,"----包含---currentBean:"+currentBean.fileName);
                    tempList.remove(currentBean);
                    tempList.addFirst(currentBean);
                }else
                {
                    MyLog.d(TAG,"----不包含---currentBean:"+currentBean.fileName);
                    tempList.addFirst(currentBean);
                }

                String tempStr = SharedPreferenceUtils.object2String(tempList);
                sp.edit().putString(Constant.MySharedPreference.SP_KEY_CLOUD_LIST,tempStr).commit();
            } catch (Exception e) {
                MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
            }
        }

        //第一次会显示0，第二个以上对应哦
        MyLog.d(TAG, "---->>mOnItemClickListener--->>tempList-->>:"+(tempList==null ? 0 : tempList.size()));
    }

}
