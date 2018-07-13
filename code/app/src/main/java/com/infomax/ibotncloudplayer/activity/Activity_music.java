package com.infomax.ibotncloudplayer.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.adapter.Culture_musicAdater;
import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.bean.MusicBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.infomax.ibotncloudplayer.view.LoadingDialog;
import com.infomax.ibotncloudplayer.view.ScrollSlidingBlock;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hushaokun on 2018/6/7.
 */

public class Activity_music extends FullScreenActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    TextView tvBack;
    ListView lvContent;
    TextView tvNone;
    private Culture_musicAdater culture_musicAdater;
    private RelativeLayout rlContent;
    private TextView tvAlltime;
    private TextView tvNowTime;
    private TextView tvName;
    private TextView tvTitle;
    private String title;
    private SeekBar seekBar;//拖动条
    ImageView ivPlay;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突
    private Timer timer;
    ObjectAnimator animator;
    ScrollSlidingBlock scrollBar;
    //弹出自定义播放器弹窗
    Dialog dialog;
    //音乐播放器
    /**
     * MediaPlayer 使用注意要点:
     * 1.只有在isPlaying()状态才能拿getDuration()，否则出现IllegalStateException异常
     * 2.切换歌曲需重新调用reset（）重置方法
     */
    MediaPlayer mplayer = null;
    //当前选择的歌曲
    int item = 0;
    ArrayList<MusicBean> arrayList = null;
    final static String TAG = Activity_music.class.getSimpleName();
    /**
     * 默认集合，当前目录下没有文件夹时，使用该集合
     */
    ArrayList<MusicBean> defaultArrayLists = new ArrayList<MusicBean>();

    /**
     * AUDIO文件夹下面的所有一级文件夹集合
     */
    private LinkedList<EcVideoFolderBean> childFolders = new LinkedList<EcVideoFolderBean>();
    /**
     * AUDIO文件夹下面的所有一级文件夹集合,内容和childFolders第一复制后的一样；只是以后不会变化，而childFolders根据需要随时变更
     */
    private LinkedList<EcVideoFolderBean> unChangeChildFolders = new LinkedList<EcVideoFolderBean>();


    /**
     * 音乐文件的HassMap,key文件夹，value为该文件夹下面的所有音频(mp3)文件
     */
    private HashMap<String, ArrayList<MusicBean>> hashMap = new HashMap<String, ArrayList<MusicBean>>();

    private final int Request_Code101 = 101;
    private LearnTrajectoryUtil.LearnTrajectoryHolder trajectoryHolder = new LearnTrajectoryUtil.LearnTrajectoryHolder();
    /**
     * 文件夹列表对应的数据加载 msg what;
     */
    private final int MSG_WHAT_FOLDER_LOAD_DATA = 101;
    /**
     * 展示listview，对应的 msg what;
     */
    private final int MSG_WHAT_SHOW_LV_DATA = 102;

    private LoadingDialog loadingDialog;

    /**
     * 包含语音传递的文件夹状态
     */
    private boolean containVoiceFolderFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initView();
        initClick();
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        if (title.contains("国语故事") || title.contains("绘本故事")) {
            tvTitle.setText("中文故事");
        } else {
            tvTitle.setText(title + "");
        }

        Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = getIntent().getBooleanExtra(Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE, false);
        Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = getIntent().getStringExtra(Constant.MyIntentProperties.EXTRA_FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);
        preInitData(true);
    }


    /**
     * 立马打开播放去，播放器加载视频文件不能显示图标<br/>
     * 二次优化处理 ，单在LocalVideoFragment中添加进度提示经测试是不够的(v1.1.5及之前的版本都是这样处理的)。<br/>
     * 因为此时用户开机立即点击，系统数据库没有加载完成所有视频等文件。<br/>
     * 使用开机ibotncloudplayer启动后添加计时器，计时时间180s。180s期间，用户点击播放器【音乐/视频】，以【系统文件初始化中....】提示给用户。如果此时用户语音播放【音乐/视频】；如果此时用户遥控播放【音乐/视频】--待添加中，也都给同样的提示。<br/>
     *
     * @param isShowLoading 是否显示加载进度，true 为显示
     */
    private void preInitData(boolean isShowLoading) {

        MyLog.d(TAG, ">>>>>preInitData()>>>>");
        //step 1 检查外置sd卡是否可用
        DevicePath devicePath = new DevicePath(getBaseContext());
        if (TextUtils.isEmpty(devicePath.getSdStoragePath())) {
            initData(isShowLoading);

        } else {//外置sd卡是否可用
            initData(isShowLoading);
        }
    }
////////////////////////////////////// 新方式加载数据--根据server中计时器处理/////////end

    /**
     * @param isShowLoading
     */
    private void initData(boolean isShowLoading) {
        Log.d(TAG, ">>>>initData()>>>path:" + Environment.getExternalStorageDirectory().getAbsolutePath() +
                "\n" + Environment.getRootDirectory().getAbsolutePath());
        if (isShowLoading) {
            ThreadUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.setContent(R.string.text_loading);
                    loadingDialog.showLoadingDialog(loadingDialog);
                }
            });
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                File file = new File(Constant.Config.Education_Content_Audio_File_Root_Path);
                getAllFolders(file);
                //使用Cursor 获取 音频文件
                getLocalAudiosContentResolver(file.getAbsolutePath());
                dealRelationDataAfterRecursion();
            }
        }.start();
    }

    /**
     * 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
     * 然后将该路径下面的所有文件夹列出来
     */
    private void getAllFolders(File root) {
        childFolders.clear();
        ArrayList<String> tempFolders = new ArrayList<>();
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    MyLog.d(TAG, "folder>>>>：" + f.getName());
                    tempFolders.add(f.getName());
                } else {
                    Log.d(TAG, "文件名称>>>：" + f.getName());
                }
            }
        }
        //对音乐文件根据前三位序号排序，num001,num002,num003，...;如果是旧的sd卡，没有num,存储时就不去掉前六位
        Collections.sort(tempFolders);
        for (String name : tempFolders) {
            if (name.startsWith("num")) {
                name = name.substring(6);
            }
            Log.e("--name---", name + "");
            EcVideoFolderBean bean = new EcVideoFolderBean(name, false);
            childFolders.add(bean);
        }


        //文件夹名称作为hashMap的key
        hashMap.clear();
        for (EcVideoFolderBean bean : childFolders) {
            ArrayList<MusicBean> temp = new ArrayList<MusicBean>();
            hashMap.put(bean.name, temp);
            unChangeChildFolders.add(bean);//要遍历集合重新单独赋值。
        }
        MyLog.e(TAG, "getAllFolders()>>>>>folder num>>>>：" + childFolders.size());
    }


    /**
     * 文件遍历完成后再调用该方法，过滤文件，展示数据。
     */
    private void dealRelationDataAfterRecursion() {
        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);
        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == MSG_WHAT_FOLDER_LOAD_DATA) {
                MyLog.d(TAG, "mHandler>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>");
                if (childFolders.size() > 0) {

                    for (int i = 0; i < childFolders.size(); i++) {
                        EcVideoFolderBean bean = childFolders.get(i);
                        if (bean.name.equals(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE)) {
                            containVoiceFolderFlag = true;
                            bean.selected = true;


                            MyLog.d(TAG, "mHandler>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:" + bean.name);
                        } else {
                            bean.selected = false;
                        }
                    }
                    if (!containVoiceFolderFlag) {//没有包含语音传递的文件夹，更新lv，第一个文件夹默认选中----------
                        childFolders.get(0).selected = true;

                    }

                    containVoiceFolderFlag = false;//此时可以重置该值

                }

            } else if (what == MSG_WHAT_SHOW_LV_DATA) {
                MyLog.d(TAG, "mHandler>>>>>MSG_WHAT_SHOW_LV_DATA>>>");

                showLvData();
            }
        }
    };

    /**
     * 展示listview数据
     */
    private void showLvData() {
        if (childFolders.size() > 0) {//有文件夹
            if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE) {//获取语音传递的文件夹，命令词即【播放 + 文件夹】
                //注意：：原有的【播放音乐】命令要保持不变。即此时没有文件夹
                if (TextUtils.isEmpty(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE)) {
                    final int lenth = childFolders.size();
                    int randomIndex = (int) (Math.random() * lenth);
                    if (lenth == 0) {
                        randomIndex = 0;

                    } else if (randomIndex >= lenth) {
                        randomIndex = lenth - 1;
                    }
                    final String randomFolderName = childFolders.get(randomIndex).name;
                    arrayList = (ArrayList<MusicBean>) hashMap.get(randomFolderName);
                    for (int i = 0; i < childFolders.size(); i++) {
                        EcVideoFolderBean bean = childFolders.get(i);
                        if (bean.name.equals(randomFolderName)) {
                            containVoiceFolderFlag = true;
                            bean.selected = true;
                            MyLog.d(TAG, "showLvData>>>>>MSG_WHAT_FOLDER_LOAD_DATA>>>i:" + i + ",name:" + bean.name);
                        } else {
                            bean.selected = false;
                        }
                    }
                } else {
                    arrayList = (ArrayList<MusicBean>) hashMap.get(Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE);
                }
            } else {//不是语音时，默认加载传统文化文件夹下的音频列表
                if (title != null && !TextUtils.isEmpty(title)) {
//                    if (title.contains("国语故事")) {
////                        title = "清华幼儿汉语故事";
//                    }
                    if (title.contains("英文儿歌")) {
                        title = "Englishsong";
                    }
                    if (title.contains("国语故事") || title.contains("绘本故事")) {
                        arrayList = (ArrayList<MusicBean>) hashMap.get("清华幼儿汉语故事");
//                        Log.e("----list--", arrayList.size() + "");
                        arrayList.addAll(hashMap.get("绘本故事"));
//                        Log.e("----list--", arrayList.size() + "");
                    } else {
                        arrayList = (ArrayList<MusicBean>) hashMap.get(title);
                    }

                }

                if (arrayList == null) {
//                    arrayList = (ArrayList<MusicBean>) hashMap.get(childFolders.get(0).name);
                    arrayList = new ArrayList<>();
                }
            }

            if (culture_musicAdater == null) {
                culture_musicAdater = new Culture_musicAdater(arrayList, getBaseContext());
//                Log.e("arrayList---", arrayList.size() + "--");
                lvContent.setAdapter(culture_musicAdater);
                lvContent.setVisibility(View.VISIBLE);

            } else {

            }

            if (arrayList != null) {
                //更新控件
                if (arrayList.size() <= 0) {
                    tvNone.setVisibility(View.VISIBLE);
                    lvContent.setVisibility(View.GONE);
                } else {
                    tvNone.setVisibility(View.GONE);
                    lvContent.setVisibility(View.VISIBLE);
                    scrollBar.setListView(lvContent);
                    scrollBar.setVisibility(View.VISIBLE);

                    if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE) {
                        final int lenth = arrayList.size();
                        int randomIndex = (int) (Math.random() * lenth);
                        if (lenth == 0) {
                            randomIndex = 0;
                        } else if (randomIndex >= lenth) {
                            randomIndex = lenth - 1;
                        }
                        MusicBean localAudioBean = arrayList.get(randomIndex);
                        playAudioByVoiceWithBean(localAudioBean);
                        MyLog.e(TAG, "showLvData()>>>>>getPath:" + localAudioBean.getPath());
                    }
                }
            }

        } else {//没有文件夹
//            arrayList = defaultArrayLists;
//            if (culture_musicAdater == null) {
//                culture_musicAdater = new Culture_musicAdater(arrayList, getBaseContext());
//                Log.e("arrayList---", arrayList.size() + "--");
//                lvContent.setAdapter(culture_musicAdater);
//            } else {
//
//            }
            //更新控件
            if (defaultArrayLists.size() <= 0) {
                tvNone.setVisibility(View.VISIBLE);
            } else {
                tvNone.setVisibility(View.GONE);
                //语音随机播放音乐
                if (Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE) {

                    //////////////////语音播放--语音传递的文件夹下面的第一首音乐
                    //playAudioByVoiceWithBean(defaultArrayLists.get(0));
                    ///////////////

                    ////////////////修改：语音【播放音乐】，调整为随机播放文件。

                    final int lenth = defaultArrayLists.size();
                    int randomIndex = (int) (Math.random() * 10);
                    if (randomIndex >= lenth) {
                        randomIndex = lenth - 1;
                    }
                    MusicBean localAudioBean = defaultArrayLists.get(randomIndex);
                    playAudioByVoiceWithBean(localAudioBean);

                    MyLog.e(TAG, "showLvData()>>>>>getPath:" + localAudioBean.getPath());

                    ////////////////
                }
            }
        }
        if (loadingDialog != null) {
            loadingDialog.dismissLoadingDialog(loadingDialog);
        }
        Constant.MyIntentProperties.START_CLOUDPLAYER_AUDIO_BY_VOICE_VALUE = false;
        Constant.FOLDER_START_CLOUDPLAYER_AUDIO_BY_VOICE = "";
    }

    /**
     * playAudioByVoiceWithBean 语音自动播放音乐时 调用
     *
     * @param bean
     */
    private void playAudioByVoiceWithBean(MusicBean bean) {

        if (bean == null) {

            ToastUtils.showToast(getBaseContext(), getString(R.string.tip_no_audio));
            return;
        }
        if (TextUtils.isEmpty(bean.getPath())) {
            ToastUtils.showToast(getBaseContext(), getString(R.string.tip_no_audio));
            return;
        }

        //播放audio
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(bean.getPath())), "audio/*");//"audio/*"
        //当前所有的音乐播放器都被手动强行停止后或者【没有音乐播放器时】。点击播放音乐文件就会异常停止。应该弹出是否使用【视频播放器来播放】
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Request_Code101);
        } else {
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setDataAndType(Uri.fromFile(new File(bean.getPath())), "video/*");//"video/*"
            if (intent2.resolveActivity(getPackageManager()) != null) {

                ToastUtils.showToast(getBaseContext(), getString(R.string.tip_audio_player_disable));
                startActivityForResult(intent2, Request_Code101);

            } else {
                ToastUtils.showToast(getBaseContext(), getString(R.string.tip_player_disable));
            }
        }
    }


    /**
     * 不加载专辑图片 ，默认album 复制 ""
     *
     * @param folderRootPath 使用ContentResolver方式加载文件，该方式，需要收到sd卡扫描完成的广播后才能获取到内容。
     */
    private void getLocalAudiosContentResolver(String folderRootPath) {

        /**文件夹是否有对应音频文件，false为没有*/
        boolean flagCurrentFolderHasVideo = false;

        long time = SystemClock.currentThreadTimeMillis();

        defaultArrayLists.clear();

        StringBuilder selection = new StringBuilder();
        //音频
        selection.append("(" + MediaStore.Audio.Media.DATA + " LIKE '" + folderRootPath + File.separator + "%')");
        Log.d(TAG, "-->>>>>>>>" + selection.toString());

        try {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲文件的路径
                    //音频文件对应的专辑图片的album_id
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑id
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//文件的大小
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));//添加时间

                    // 歌曲的歌手名
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//
                    long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName)) {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith(".mp3"))//过滤mp3
                        {
                            MusicBean bean = new MusicBean(false, 0, displayName, path, album_id, artist, duration);
                            defaultArrayLists.add(bean);
                            //分文件夹遍历文件，存入对应集合中
                            for (EcVideoFolderBean folderBean : childFolders) {
                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
                                int i = bean.getPath().lastIndexOf(bean.getName());
                                String tempPath = bean.getPath().substring(0, i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
                                Log.d(TAG, ">>>getLocalAudiosContentResolver()>>>>tempPath():" + tempPath
                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                if (tempPath.endsWith(folderBean.name)) {//
//                                    flagCurrentFolderHasVideo = true;
                                    //将当前bean添加到hashMap中key为folderName
                                    hashMap.get(folderBean.name).add(bean);
                                }

                            }
                        }
                    }

                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.e(TAG, "Exception--->>:" + e.getMessage());
            defaultArrayLists.clear();
        }


        mHandler.sendEmptyMessage(MSG_WHAT_FOLDER_LOAD_DATA);

        mHandler.sendEmptyMessage(MSG_WHAT_SHOW_LV_DATA);

    }


    private void initClick() {
        tvBack.setOnClickListener(this);
    }

    private void initView() {
        tvBack = (TextView) findViewById(R.id.tv_back);
        lvContent = (ListView) findViewById(R.id.lv_content);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvNone = (TextView) findViewById(R.id.tv_none);
        scrollBar = (ScrollSlidingBlock) findViewById(R.id.myscrollbar);
        lvContent.setOnItemClickListener(this);
        loadingDialog = new LoadingDialog(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (arrayList != null && arrayList.size() > 0) {
            item = i;
//            pBar = (ProgressBar) view.findViewById(R.id.pbar);
            showMusicDilog();
        }
    }

    private void showMusicDilog() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        Log.e("--item-", item + "");
        final MusicBean bean = arrayList.get(item);
        bean.setSelected(true);
        culture_musicAdater.notifyDataSetChanged();
        final String path = bean.getPath();
        String name = bean.getName().replace(".mp3", "");
        final long duration = bean.getDuration();//音频时长
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String hms = formatter.format(duration);
        initMediaPlayer(path);//初始化
        dialog = new Dialog(this, R.style.music_dialog);
        View view = View.inflate(this, R.layout.culture_music_dialog, null);
        dialog.setContentView(view);
        dialog.show();
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvNowTime = (TextView) view.findViewById(R.id.tv_now_time);
        tvAlltime = (TextView) view.findViewById(R.id.tv_all_time);
        TextView tvBack = (TextView) view.findViewById(R.id.tv_back);
        seekBar = (SeekBar) view.findViewById(R.id.sbar);//拖动条
        ImageView ivUp = (ImageView) view.findViewById(R.id.iv_up);
        ImageView ivNext = (ImageView) view.findViewById(R.id.iv_next);
        ivPlay = (ImageView) view.findViewById(R.id.iv_play);
        ImageView ivCen = (ImageView) view.findViewById(R.id.iv_center);
        animator = setAnim(ivCen);
        tvName.setText(name + "");
        tvAlltime.setText(hms + "");
        seekBar.setMax(mplayer.getDuration());
        seekBar.setProgress(mplayer.getCurrentPosition());
        tvBack.setOnClickListener(dilogListener);
        ivNext.setOnClickListener(dilogListener);
        ivUp.setOnClickListener(dilogListener);
        ivPlay.setOnClickListener(dilogListener);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mplayer != null) {
                    if (mplayer.isPlaying()) {
//                        mplayer.pause();
                        mplayer.stop();
                        if (ivPlay != null) {
                            ivPlay.setImageResource(R.drawable.culture_music_btn_play);
                        }
                    }
                    for (MusicBean musicBean : arrayList) {
                        musicBean.setSelected(false);
                    }

                    arrayList.get(item).setCurDuration(0);
                    culture_musicAdater.notifyDataSetChanged();

                }
                if (animator != null && animator.isRunning()) {
                    animator.cancel();
                }


            }
        });
        //拖动时不断调用
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            //拖动条开始拖动的时候调用,应当暂停后台定时器
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = true;
                Log.e("-seekBar-onStart--", isSeekBarChanging + "");
            }

            //拖动条停止拖动的时候调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = false;
                Log.e("-seekBar-onStart--", isSeekBarChanging + "");
                int max = seekBar.getMax();
                int progress = seekBar.getProgress();
                long durtime = mplayer.getDuration();
                long now = durtime * progress / max;
                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                String hms = formatter.format(now);
                tvNowTime.setText(hms + "");
                mplayer.seekTo((int) now);
            }
        });
    }

    private ObjectAnimator setAnim(ImageView ivCen) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivCen, "rotation", 0f, 360f);
        animator.setRepeatCount(ValueAnimator.INFINITE);//重复次数
        animator.setRepeatMode(ValueAnimator.RESTART);//重复模式
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(6000);
        animator.start();
        return animator;
    }

    private void play(String path) {
        if (mplayer != null) {
            try {
                mplayer.stop();
                mplayer.reset();
                mplayer.setDataSource(path);
                mplayer.prepare();
                mplayer.start();


            } catch (Exception e) {
                e.printStackTrace();
                Log.e("-----e----", e + "");
            }

        }
    }

    private void initMediaPlayer(final String path) {
//        destoryPlayer();
        if (mplayer == null) {
            mplayer = new MediaPlayer();
        }

        //播放错误回调
        mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.e("----i--", i + "");
                Log.e("----i1--", i1 + "");
                Log.e("---e---", "播放异常");
//                Toast.makeText(getBaseContext(), "播放异常，请重新播放", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        try {

            mplayer.reset();//每次点击前都调用一次mMusicPlayer.reset()，可以清除以前播放器的状态
            mplayer.setDataSource(path);
            mplayer.prepare();
            mplayer.start();

            //开启定时器，更新时间和进度
            if (timer == null) {
                timer = new Timer();
                Log.e("---timer--", "timer");
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isSeekBarChanging) {//非手动拖动进度条时
                        if (mplayer == null) {
                            return;
                        }

                        try {
                            if (mplayer != null && mplayer.isPlaying() && tvNowTime != null) {
                                int max = seekBar.getMax();
                                int progress = seekBar.getProgress();
                                long durtime = mplayer.getDuration(); //注意：mplayer 只有在isplaying()状态下才能拿getDuration()
                                long now = durtime * progress / max;
                                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                                final String hms = formatter.format(now);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvNowTime.setText(hms + "");
                                        seekBar.setProgress(mplayer.getCurrentPosition());
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e("----e---", e.toString() + "");
                        }
                    }
                }
            }, 0, 80);

            //播放完成时回调
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mplayer.start();
                    mplayer.setLooping(true);
                    if (seekBar != null) {
                        seekBar.setProgress(0);
                        tvNowTime.setText("0:00");
                    }


                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("---e---", e.toString() + "");
        }
    }


    View.OnClickListener dilogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_back:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                case R.id.iv_up:
                    item--;
                    if (item >= 0) {
                        flushView((item + 1));
                        if (ivPlay != null) {
                            ivPlay.setImageResource(R.drawable.culture_music_btn_play_start);
                        }
                    } else {
                        item++;
                        Toast.makeText(getBaseContext(), "已经是第一首啦", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.iv_next:
                    item++;
                    if (arrayList != null && item < arrayList.size()) {
                        flushView((item - 1));
                        if (ivPlay != null) {
                            ivPlay.setImageResource(R.drawable.culture_music_btn_play_start);
                        }
                    } else {
                        item--;
                        Toast.makeText(getBaseContext(), "已经是最后一首啦", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.iv_play:
                    if (mplayer != null) {
                        if (mplayer.isPlaying()) {
                            mplayer.pause();
                            if (ivPlay != null) {
                                ivPlay.setImageResource(R.drawable.culture_music_btn_play);
                            }
                            if (timer != null) {
                                timer.purge();
                            }
                            if (animator != null) {
                                animator.pause();
                            }
                        } else {
                            mplayer.start();
                            if (ivPlay != null) {
                                ivPlay.setImageResource(R.drawable.culture_music_btn_play_start);
                            }
                            if (animator != null) {
                                animator.resume();
                            }
                        }
                    }
                    break;
            }
        }
    };

    //刷新dialog
    private void flushView(int i) {
        //i 前一个item
        arrayList.get(i).setSelected(false);
        MusicBean bean = arrayList.get(item);
        bean.setSelected(true);
        arrayList.get(i).setCurDuration(0);
        culture_musicAdater.notifyDataSetChanged();
        String name = bean.getName().replace(".mp3", "");
        long duration = bean.getDuration();
        String path = bean.getPath();
        play(path);//播放上/下一首
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String hms = formatter.format(duration);
        if (tvName != null) {
            tvName.setText(name + "");
        }
        if (tvAlltime != null) {
            tvAlltime.setText(hms + "");
        }
        if (seekBar != null) {
            seekBar.setMax((int) duration);
            seekBar.setProgress(0);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mplayer != null) {
            if (mplayer.isPlaying()) {
                mplayer.pause();
            }
            mplayer.release();
            mplayer = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }
}
