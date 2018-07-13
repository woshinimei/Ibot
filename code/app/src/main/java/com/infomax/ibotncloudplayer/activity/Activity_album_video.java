package com.infomax.ibotncloudplayer.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.infomax.ibotncloudplayer.ConfirmDialog;
import com.infomax.ibotncloudplayer.DayMediaGroup;
import com.infomax.ibotncloudplayer.DayMediaItem;
import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.ListViewGroupAdapter;
import com.infomax.ibotncloudplayer.MediaManager;
import com.infomax.ibotncloudplayer.QQCloudPlayerActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.FileMIMEUtils;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.view.LoadingDialog;
import com.infomax.ibotncloudplayer.view.ScrollSlidingBlock;
import com.onedriver.AppOneDriver;
import com.wifidirect.WiFiDirectActivity;
import com.wifidirect.WifiDirectConstant;
import com.ysx.qqcloud.QQCloudFileInfo;
import com.ysx.qqcloud.QQCloudObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 包含成长视频，成长照片
 */
public class Activity_album_video extends FullScreenActivity
        implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

    private static final String TAG = Activity_album_video.class.getSimpleName();

    //    private MGridView mGridView_File;
    public List<QQCloudFileInfo> QQFileList = null;
    private QQCloudObject qqObject;
    private View mBottomBar;
    private ListView mListView_File;//

    private ViewPager vpFullScreen;
    private ProgressBar mPBar;
    private ConfirmDialog confirmDialog;
    private TextView tvBack;
    private TextView tvBack1;
    private RadioButton rbAlbum;
    private RadioButton rbVideo;
    private TextView tvDel;
    private CheckBox cbSelect;
    private RelativeLayout rl_additional_data;
    private Boolean isSelect = false;
    private int mMediaType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
    private MediaManager mMediaMgr;
    private IntentFilter mIntentFilter = null;

    private ListViewGroupAdapter listViewGroupAdapter;//数据适配器
    private LoadingDialog loadingDialog;
    private ScrollSlidingBlock mScrollBlock;
    /**
     * 记录学习轨迹
     */
    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_video);
        initView();
        String type = getIntent().getStringExtra("type");

        if (type != null && type.equals("video")) {
            rbVideo.setChecked(true);
            mMediaType = MediaManager.MEDIA_TYPE_LOCAL_VIDEO;
            Log.e("---mMediaType----", type + "");
        } else {
            if (type != null) {
                Log.e("---mMediaType----", type + "");
            }
            rbAlbum.setChecked(true);
            mMediaType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
        }

        setOnclickListener();
        saveLearn();//记录学习轨迹
        registerReceivers();//开启广播接收
        addPager();
        initData();
    }

    private void addPager() {
        vpFullScreen.setOffscreenPageLimit(1);
        vpFullScreen.setAdapter(madater);
        vpFullScreen.getAdapter().notifyDataSetChanged();
    }

    private void initData() {
//        mGridView_File.setHaveScrollBar(false);
        mBottomBar.setVisibility(View.GONE);
        mPBar.setVisibility(View.INVISIBLE);
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setContent(R.string.text_loading);
            loadingDialog.setCancelForUser(false);
        }

    }

    private void registerReceivers() {
        MyLog.d(TAG, TAG + ">>>>onCreate>>>mMediaType:" + mMediaType);
        mIntentFilter = new IntentFilter(MediaManager.ACTION_OPEN_EDIT_MODE);
        mIntentFilter.addAction(MediaManager.ACTION_CLOSE_EDIT_MODE);
        mIntentFilter.addAction(MediaManager.ACTION_TOGGLE_EDIT_MODE);
        mIntentFilter.addAction(MediaManager.ACTION_OPEN_FULLSCREEN);
        mIntentFilter.addAction(Constant.MyIntentProperties.ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO);
        registerReceiver(mIntentReceiver, mIntentFilter);
        mMediaMgr = new MediaManager(getBaseContext(), mHandler);
    }

    private void saveLearn() {
        //记录学习轨迹
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
            trajectoryHolder.startLearn(new LearnTrajectoryBean(System.currentTimeMillis(), getString(R.string.local_photo), LearnTrajectoryUtil.Constant.TYPE_GROW_PHOTO));
        } else {
            trajectoryHolder.startLearn(new LearnTrajectoryBean(System.currentTimeMillis(), getString(R.string.local_video), LearnTrajectoryUtil.Constant.TYPE_GROW_VIDEO));
        }
    }

    private void setOnclickListener() {
        tvBack.setOnClickListener(this);
        tvBack1.setOnClickListener(this);
        tvDel.setOnClickListener(this);
        cbSelect.setOnClickListener(this);
        rbAlbum.setOnClickListener(this);
        findViewById(R.id.rb_video).setOnClickListener(this);
        findViewById(R.id.rb_album).setOnClickListener(this);
        mBottomBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //cancelEditmode();
                return true;
            }
        });
    }

    PagerAdapter madater = new PagerAdapter() {
        @Override
        public int getItemPosition(Object object) {
//				return super.getItemPosition(object);
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (!mMediaMgr.getGroupList().isEmpty()) {
                for (DayMediaGroup group : mMediaMgr.getGroupList()) {
                    count += group.getItemList().size();
                }
            }
            Log.i(TAG, "getCount :" + count);
            return count;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.i(TAG, "vp instantiateItem position:" + position);
            ImageView img = new ImageView(container.getContext());
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            ViewPager.LayoutParams params = new ViewPager.LayoutParams();
            params.width = ViewPager.LayoutParams.MATCH_PARENT;
            params.height = ViewPager.LayoutParams.MATCH_PARENT;
            img.setLayoutParams(params);
            for (DayMediaGroup group : mMediaMgr.getGroupList()) {
                if (position >= group.getItemList().size()) {
                    position %= group.getItemList().size();
                } else {
                    String path = group.getItemList().get(position).getPath();
                        /*File file = new File(path);
                        if (file.exists()) {
							Log.i(TAG,"file.exists() position :"+position+" path:"+path);
							Glide.with(container.getContext())
									.load(file)
									.into(img);
							break;
						}else {
							Log.i(TAG,"file not exists()");

						}*/
                    Bitmap bmp = ((ListViewGroupAdapter) mListView_File.getAdapter()).getBmpFromCache(path);
                    if (bmp != null) {
                        img.setImageBitmap(bmp);
                    } else {
                        File file = new File(path);
                        Glide.with(container.getContext())
                                .load(file)
                                .into(img);
                    }
                    break;

                }
            }
            container.addView(img);
            return img;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//				super.destroyItem(container, position, object);
            if (object instanceof ImageView) {
                Log.i(TAG, "vp destroyItem position:" + position);
                container.removeView(((ImageView) object));
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };

    private void initView() {
//        mGridView_File = (MGridView) findViewById(R.id.gv_group);
        tvBack = (TextView) findViewById(R.id.tv_back);
        tvBack1 = (TextView) findViewById(R.id.tv_back1);
        mListView_File = (ListView) findViewById(R.id.listView_file);
        vpFullScreen = (ViewPager) findViewById(R.id.vp_fullscreen);
        mBottomBar = findViewById(R.id.bottom_bar_container);
        mPBar = (ProgressBar) findViewById(R.id.pbar);
        mScrollBlock = (ScrollSlidingBlock) findViewById(R.id.scroll_block);
        rl_additional_data = (RelativeLayout) findViewById(R.id.rl_additional_data);
        rbAlbum = (RadioButton) findViewById(R.id.rb_album);
        rbVideo = (RadioButton) findViewById(R.id.rb_video);
        tvDel = (TextView) findViewById(R.id.tv_delete);
        cbSelect = (CheckBox) findViewById(R.id.cb_select_all);

    }


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.e("-----msg.what----", msg.what + "");
            switch (msg.what) {

                case MediaManager.MESSAGE_DELETE_CHECKED:

                    break;
                case MediaManager.MESSAGE_DELETE_CHECKED_DONE:

                    break;
                case MediaManager.MESSAGE_UPDATE_LIST:
                    MyLog.d(TAG, TAG + ">>>mHandler>>>MediaManager.MESSAGE_UPDATE_LIST");
                    if (loadingDialog != null) {
                        loadingDialog.dismissLoadingDialog(loadingDialog);
                    }
                    updateListView();
                    break;
                case MediaManager.MESSAGE_UPLOAD_PROGRESS:
                    //mPBar.setProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String sAction = intent.getAction();
            if (sAction != null) {
                MyLog.d(TAG, "mIntentReceiver()>>>>>>action:" + sAction);
                if (sAction.equals(MediaManager.ACTION_OPEN_EDIT_MODE)) {
                    mBottomBar.setVisibility(View.VISIBLE);
                    if (mListView_File.getAdapter() != null) {
                        ((ListViewGroupAdapter) mListView_File.getAdapter()).setEditMode(true);
                    }
                } else if (sAction.equals(MediaManager.ACTION_CLOSE_EDIT_MODE)) {
                    if (mListView_File.getAdapter() != null) {
                        ((ListViewGroupAdapter) mListView_File.getAdapter()).setEditMode(false);
                    }
                    mBottomBar.setVisibility(View.GONE);
                } else if (sAction.equals(MediaManager.ACTION_TOGGLE_EDIT_MODE)) {
                    if (mBottomBar.getVisibility() == View.GONE) {

                        mBottomBar.setVisibility(View.VISIBLE);
                    } else {
                        mBottomBar.setVisibility(View.GONE);
                    }
                } else if (sAction.equals(MediaManager.ACTION_OPEN_FULLSCREEN)) {
                    if (mListView_File.getAdapter() != null) {
                        int type = intent.getIntExtra(MediaManager.KEY_MEDIA_TYPE, -1);
                        String path = intent.getStringExtra(MediaManager.KEY_IMAGE_PATH);

                        MyLog.d(TAG, TAG + "ACTION_OPEN_FULLSCREEN>>>>>>>getPath:" + path
                                + "\n getType():" + type);

                        if (path != null) {
                            if (type == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                                int itemPos = intent.getIntExtra("itemPos", -1);
                                if (vpFullScreen != null && itemPos != -1) {
                                    vpFullScreen.setVisibility(View.VISIBLE);
                                    tvBack1.setVisibility(View.VISIBLE);
                                    vpFullScreen.getAdapter().notifyDataSetChanged();
                                    vpFullScreen.setCurrentItem(itemPos);
                                    mListView_File.setVisibility(View.GONE);
                                    mScrollBlock.setVisibility(View.GONE);
                                }
                            } else if (type == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                                //使用视频播放器播放
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setPackage(Constant.ThirdPartAppPackageName.PACKAGE_NAME_MAOTOUYING);

                                File file = new File(path);
                                String MIMEType = FileMIMEUtils.getMIMEType(file);
                                intent2.setDataAndType(Uri.fromFile(file), MIMEType);
                                if (intent2.resolveActivity(context.getPackageManager()) != null) {
                                    Activity_album_video.this.startActivity(intent2);
                                }
                            }
                        }
                    }
                } else if (Constant.MyIntentProperties.ACTION_UPLOAD_PHOTO_FOR_VOICE_TAKE_PHOTO.endsWith(intent.getAction())) { //在成长照片界面进行拍照无照片,接受到拍照完成的广播后，重新加载视频列表。

                    if (confirmDialog != null) {
                        confirmDialog.dismiss();
                    }
                    mBottomBar.setVisibility(View.GONE);
                    initFromMediaType();
                }
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        MyLog.d(TAG, TAG + ">>>>>>onResume");

        loadingDialog.showLoadingDialog(loadingDialog);
        if (listViewGroupAdapter != null) {
            listViewGroupAdapter.notifyDataSetChanged();
        }
        if (vpFullScreen != null) {
            if (vpFullScreen.getAdapter() != null) {
                vpFullScreen.getAdapter().notifyDataSetChanged();
            }
        }

        if (mMediaMgr == null) {
            mMediaMgr.setCancel(false);
        }
        initFromMediaType();

        Constant.SHOW_UI_TIP = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        MyLog.d(TAG, TAG + ">>>>>>onPause");
        Constant.SHOW_UI_TIP = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        MyLog.d(TAG, TAG + ">>>>>>onStop");

        if (mMediaMgr == null) {
            mMediaMgr.setCancel(true);
        }
        if (loadingDialog != null) {
            loadingDialog.dismissLoadingDialog(loadingDialog);
        }
    }

    private void updateListView() {
        MyLog.d(TAG, TAG + ">>>updateListView>>>");

        if (mMediaMgr != null && mListView_File.getVisibility() == View.VISIBLE) {
            ArrayList<DayMediaGroup> groupList = mMediaMgr.getGroupList();
            if (groupList != null) {
                if (groupList.size() == 0) {
//                    toggleView(false);
                } else {
//                    toggleView(true);
                    if (listViewGroupAdapter == null) {
                        listViewGroupAdapter = new ListViewGroupAdapter(getBaseContext(), mListView_File, groupList);
                        mListView_File.setAdapter(listViewGroupAdapter);
                        mScrollBlock.setListView(mListView_File);
                    } else {
                        listViewGroupAdapter.setEditMode(false);
                        listViewGroupAdapter.setItems(groupList);
                        listViewGroupAdapter.loadBitmaps(0, 1);//需要主动调用，解决listview的onScroll()中调用loadBitmaps(..)无效
                    }
                }

				/*if (listViewGroupAdapter == null)
                {
					listViewGroupAdapter = new ListViewGroupAdapter(this, mListView_File);
					listViewGroupAdapter.setItems(groupList);
					mListView_File.setAdapter(listViewGroupAdapter);
				}else {
					listViewGroupAdapter.setItems(groupList);
				}*/

            } else {
//                toggleView(false);
            }
        }
    }


    private void initFromMediaType() {
        mListView_File.setVisibility(View.GONE);
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {

            mListView_File.setVisibility(View.VISIBLE);

            mMediaMgr.clearItemGroups();
            mMediaMgr.startGetData(MediaManager.MEDIA_TYPE_LOCAL_PHOTO);

        } else if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {

            mListView_File.setVisibility(View.VISIBLE);
            mMediaMgr.clearItemGroups();
            mMediaMgr.startGetData(MediaManager.MEDIA_TYPE_LOCAL_VIDEO);

        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {
            register();
            qqObject = new QQCloudObject(getBaseContext());
            qqObject.getQQCloudInfo();
            System.gc();
        }
    }

    private void unInitMediaType() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mListView_File.getAdapter() != null) {
                ((ListViewGroupAdapter) mListView_File.getAdapter()).releaseAll();
            }
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {
            unregister();
        }

        if (mMediaMgr != null) {
            mMediaMgr.clearItemGroups();
        }
    }

    private void initCloudVideoView() {
        QQFileList = qqObject.QQFileList;
    }

    private void updateCloudVideoView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initCloudVideoView();
            }
        });
    }


    private void register() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_INFO);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_INFO_UPDATE);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_VIDEOURL);
        intentFilter.addAction(QQCloudObject.MSG_QQCLOUD_ERROR);
        registerReceiver(updateReceiver, intentFilter);
    }

    private void unregister() {
        unregisterReceiver(updateReceiver);
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            MyLog.d(TAG, TAG + ">>>>updateReceiver" + action);
            if (action.equals(QQCloudObject.MSG_QQCLOUD_INFO)) {
                updateCloudVideoView();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_INFO_UPDATE)) {
                updateCloudVideoView();
            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_VIDEOURL)) {

                boolean isDownload = intent.getBooleanExtra("isDownload", false);
                int index = intent.getIntExtra("videoIndex", -1);
                if (index == -1) return;

                if (isDownload) {
                    Intent intentPlayer = new Intent();
                    intentPlayer.setClass(getBaseContext(), QQCloudPlayerActivity.class);
                    intentPlayer.putExtra("videoIndex", index);
                    try {
                        startActivity(intentPlayer);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {

                }


            }
            if (action.equals(QQCloudObject.MSG_QQCLOUD_ERROR)) {
                int errorCode = intent.getIntExtra("errorCode", 0);
                String errorMsg = intent.getStringExtra("errorMsg");

                Toast.makeText(getBaseContext(), errorCode + "," + errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
        } else if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {
            if (qqObject != null)
                qqObject.getQQCloudGetVideoUrl(position, true);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_back:
                onBackPressed();
                break;
            case R.id.tv_back1:
                onBackPressed();
                break;
            case R.id.tv_delete:
                doDelete();
                break;
            case R.id.cb_select_all:
                if (cbSelect.isChecked()) {
                    setCheckAll();
                } else {
                    setCheckNone();
                }
                isSelect = !isSelect;
                break;
            case R.id.rb_album:
                mMediaType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
                saveLearn();
                initFromMediaType();
                break;
            case R.id.rb_video:
                mMediaType = MediaManager.MEDIA_TYPE_LOCAL_VIDEO;
                saveLearn();
                initFromMediaType();
                break;
            default:
                if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {
                    int index = (Integer) v.getTag();
                    qqObject.getQQCloudGetVideoUrl(index, false);

                }
                break;
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param alpha
     */
    public void setWindowBackgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    private void doDelete() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            boolean bFind = false;
            if (mMediaMgr != null) {
                ArrayList<DayMediaGroup> groupList = mMediaMgr.getGroupList();
                if (groupList != null) {
                    for (DayMediaGroup dg : groupList) {
                        for (DayMediaItem item : dg.getItemList()) {
                            if (item.getChecked() == true) {
                                bFind = true;
                                break;
                            }
                        }

                        if (bFind) {
                            break;
                        }
                    }
                }
            }

            if (bFind) {
                confirmDialog = new ConfirmDialog(this, mConfirmDeleteClickListener, R.string.confirm_deleting_message);
                confirmDialog.show();
            }
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }
    }

    private OnClickListener mConfirmDeleteClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.btn_confirm) {
                confirmDelete();
            } else if (v.getId() == R.id.btn_cancel) {
                if (mBottomBar.getVisibility() == View.VISIBLE) {
                    if (mListView_File.getAdapter() != null) {
                        ((ListViewGroupAdapter) mListView_File.getAdapter()).setEditMode(false);
                    }
                    mBottomBar.setVisibility(View.GONE);
                }
            }
        }
    };

    private void confirmDelete() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            ThreadUtils.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    if (mListView_File.getAdapter() != null) {
                        ((ListViewGroupAdapter) mListView_File.getAdapter()).deleteChecked();
                    }

                    unInitMediaType();

                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mBottomBar.setVisibility(View.GONE);
                            initFromMediaType();
                        }
                    });
                }
            });

        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }
    }

    private void setCheckAll() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mListView_File.getAdapter() != null) {
                ((ListViewGroupAdapter) mListView_File.getAdapter()).setCheckAll();
            }

        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }
    }

    private void setCheckNone() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mListView_File.getAdapter() != null) {
                ((ListViewGroupAdapter) mListView_File.getAdapter()).setCheckNone();
            }

        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }
    }

    /**
     * 上传 使用qqcloud
     */
    private void doUpload() {

        int selectedUploadFileNumber = 0;//选择上传的文件个数

        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mMediaMgr != null) {
                ArrayList<DayMediaGroup> groupList = mMediaMgr.getGroupList();
                if (groupList != null) {
                    for (DayMediaGroup dg : groupList) {
                        for (DayMediaItem item : dg.getItemList()) {
                            if (item.getChecked() == true) {
                                selectedUploadFileNumber++;
                                MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>>>doUpload()-->>>getPath:" + item.getPath() + ",getDisplayName:" + item.getDisplayName());

                                if (Constant.Toggle.TOGGLE_UPLOAD_TYPE_THIRD_PATY_TYPE == 1) {
                                    // QQ Cloud   // TODO: 2016/12/30 关闭上传
                                    if (qqObject != null) {
                                        qqObject.UploadMediaFile(item.getPath(), mHandler);
                                    }
                                } else if (Constant.Toggle.TOGGLE_UPLOAD_TYPE_THIRD_PATY_TYPE == 2) {

                                    if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                                        AppOneDriver.getInstance().uploadFile(item.getPath()
                                                , Constant.PHOTO_FOLDER_ID_ONEDRIVER
                                                , true
                                        );
                                    } else if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                                        AppOneDriver.getInstance().uploadFile(item.getPath()
                                                , Constant.VIDEO_FOLDER_ID_ONEDRIVER
                                                , true
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mBottomBar.getVisibility() == View.VISIBLE) {
//                onBackPressed();
            }
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }

        if (selectedUploadFileNumber == 0) {
            ToastUtils.showCustomToast(getString(R.string.tip_no_selected_upload_file_select_first));
        }
    }

    /**
     * 检查并组装文件路径。并跳转到wifidirect界面。
     */
    private void checkMediaGroupList() {
        int selectedFileNumber = 0;

        WifiDirectConstant.FILE_PATHS.clear();

        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            ;
            if (mMediaMgr != null) {
                ArrayList<DayMediaGroup> groupList = mMediaMgr.getGroupList();
                if (groupList != null) {
                    for (DayMediaGroup dg : groupList) {
                        for (DayMediaItem item : dg.getItemList()) {
                            if (item.getChecked() == true) {
                                selectedFileNumber++;
                                WifiDirectConstant.FILE_PATHS.add(item.getPath());
                                MyLog.d(TAG, TAG + ">>>>checkMediaGroupList()>>>>>>getPath:" + item.getPath() + ",getDisplayName:" + item.getDisplayName());
                            }
                        }
                    }
                }
                if (WifiDirectConstant.FILE_PATHS.size() > 0) {
                    if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                        Intent intent = new Intent(getBaseContext(), WiFiDirectActivity.class);
                        //intent.putExtra(WifiDirectConstant.EXTRAS_FILE_PATH,item.getPath());
                        startActivity(intent);
                    } else if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                        Intent intent = new Intent(getBaseContext(), WiFiDirectActivity.class);
                        //intent.putExtra(WifiDirectConstant.EXTRAS_FILE_PATH,item.getPath());
                        startActivity(intent);
                    }
                }
            }

            if (mBottomBar.getVisibility() == View.VISIBLE) {
//                onBackPressed();
            }
        }

        if (selectedFileNumber == 0) {
            ToastUtils.showCustomToast(getString(R.string.tip_please_longclick_file_select_first));
        }

    }

    private void cancelEditmode() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mBottomBar.getVisibility() == View.VISIBLE) {
                if (mListView_File.getAdapter() != null) {

                    ((ListViewGroupAdapter) mListView_File.getAdapter()).setEditMode(false);
                }

                mBottomBar.setVisibility(View.GONE);
                return;
            }

//			if(mIV_Fullscreen.getVisibility() == View.VISIBLE)
//			{
//				mIV_Fullscreen.setVisibility(View.GONE);
//				mListView_File.setVisibility(View.VISIBLE);
//				findViewById(R.id.upload_continer).setVisibility(View.VISIBLE);
//				return;
//			}
        }
    }

    /**
     * in this method,to deal edit mode to normal mode
     */
    @Override
    public void onBackPressed() {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
            if (mBottomBar.getVisibility() == View.VISIBLE) {
                if (mListView_File.getAdapter() != null) {

                    ((ListViewGroupAdapter) mListView_File.getAdapter()).setEditMode(false);
                }

                mBottomBar.setVisibility(View.GONE);
                return;
            }

//			if(mIV_Fullscreen.getVisibility() == View.VISIBLE)
            if (vpFullScreen != null && vpFullScreen.getVisibility() == View.VISIBLE) {
//				mIV_Fullscreen.setVisibility(View.GONE);
                vpFullScreen.setVisibility(View.GONE);
                tvBack1.setVisibility(View.GONE);
                mListView_File.setVisibility(View.VISIBLE);
                mScrollBlock.setVisibility(View.VISIBLE);
                //findViewById(R.id.upload_continer).setVisibility(View.VISIBLE);

                return;
            }
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }

        super.onBackPressed();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
        } else if (mMediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
        } else if (mMediaType == MediaManager.MEDIA_TYPE_CLOUD_VIDEO) {

        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG, ">>>>>>onDestroy");

        unInitMediaType();
        mHandler = null;
        unregisterReceiver(mIntentReceiver);
        if (trajectoryHolder != null) {
            trajectoryHolder.endLearn();
            LearnTrajectoryUtil.sendBro(getBaseContext(), trajectoryHolder);
        }
    }
}
