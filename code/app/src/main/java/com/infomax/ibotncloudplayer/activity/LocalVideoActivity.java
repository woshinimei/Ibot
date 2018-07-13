//package com.infomax.ibotncloudplayer.activity;
//
//import android.content.ContentResolver;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.v4.util.LruCache;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.GridView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//
//import com.bumptech.glide.Glide;
//import com.infomax.ibotncloudplayer.FullScreenActivity;
//import com.infomax.ibotncloudplayer.R;
//import com.infomax.ibotncloudplayer.adapter.LocalVideoGVAdapter;
//import com.infomax.ibotncloudplayer.adapter.EcLocalVideoFolderAdapter;
//import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
//import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
//import com.infomax.ibotncloudplayer.view.GlideRoundTransform;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//
/**
 * Created by jy on 2016/10/14.
 * 包含本地视频activity
    该类已经关闭【被LocalVideoFragment取代】。以后可能会用到，暂时保留。
 */
//public class LocalVideoActivity extends FullScreenActivity {
//
//    private LayoutInflater myInflater;
//    /**
//     * 默认集合，当前目录下没有文件夹时，使用该集合
//     */
//    ArrayList<LocalVideoBean> defaultArrayLists = new ArrayList<LocalVideoBean>();
//    private LruCache<String, Bitmap> mBmpCache;
//    final String  TAG = "LocalVideoActivity";
//    private GridView gv_act_local_video;
//    private ListView lv_act_local_video;
//    private LinearLayout ll_act_local_video;
//    private ImageView iv_act_local_video;
//    /**
//     * VIDEO文件夹下面的所有一级文件夹集合
//     */
//    private ArrayList<EcVideoFolderBean> childFolders = new ArrayList<EcVideoFolderBean>();
//    private HashMap<String,ArrayList<LocalVideoBean>> hashMap = new HashMap<String,ArrayList<LocalVideoBean>>();
//
//    private EcLocalVideoFolderAdapter myListViewAdapter ;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_local_video);
//
//        Log.d(TAG, "onCreate");
//
//        myInflater = LayoutInflater.from(this);
//
//        initViews();
//
//        registListener();
//
//        initData();
//
//
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    private void initViews() {
//        findViewById(R.id.back_button_container).setOnClickListener(mFunctionOnClickListener);
//        findViewById(R.id.rl_act_local_video).setOnClickListener(mFunctionOnClickListener);
//
//        gv_act_local_video = (GridView) findViewById(R.id.gv_act_local_video);
//        lv_act_local_video = (ListView) findViewById(R.id.lv_act_local_video);
//        ll_act_local_video = (LinearLayout) findViewById(R.id.ll_act_local_video);
//        iv_act_local_video = (ImageView) findViewById(R.id.iv_act_local_video);
//
//        //设置adapter
//        myListViewAdapter = new EcLocalVideoFolderAdapter(LocalVideoActivity.this,childFolders);
//        lv_act_local_video.setAdapter(myListViewAdapter);
//    }
//    private void registListener() {
//        lv_act_local_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                for (int i = 0; i < childFolders.size(); i++) {
//                    if (i == position) {
//                        childFolders.get(i).selected = true;
//
//                    } else {
//                        childFolders.get(i).selected = false;
//                    }
//                }
//
//                myListViewAdapter.setData(childFolders);
//
//                ArrayList<LocalVideoBean> arrayList = (ArrayList<LocalVideoBean>) hashMap.get(childFolders.get(position).name);
//                localVideoGVAdapter = new LocalVideoGVAdapter(LocalVideoActivity.this, arrayList, gv_act_local_video, mOnRequestVideoListener);
//                gv_act_local_video.setAdapter(localVideoGVAdapter);
////                localVideoGVAdapter.setData(arrayList);这样写不行，这个类代码要分析 TODO
//                Log.d(TAG, "" + childFolders.get(position).name +
//                        "ln");
//            }
//        });
//    }
//
//    private  void changeView(boolean flag){
//        if (flag){
//            ll_act_local_video.setVisibility(View.VISIBLE);
//        }else {
//            ll_act_local_video.setVisibility(View.GONE);
//        }
//    }
//
//
//    View.OnClickListener mFunctionOnClickListener = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//
//            switch (v.getId())
//            {
//                case R.id.back_button_container:
//                    returnHome();
//                    break;
//                case R.id.rl_act_local_video:
//
//                    if (lv_act_local_video.getVisibility() == View.VISIBLE) {
//                        lv_act_local_video.setVisibility(View.GONE);
//                        iv_act_local_video.setBackgroundResource(R.drawable.selector_iv_arrow_right);
//                    }else {
//                        lv_act_local_video.setVisibility(View.VISIBLE);
//                        iv_act_local_video.setBackgroundResource(R.drawable.selector_iv_arrow_left);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//
//
//    private void initData() {
//        Log.d(TAG, "->>>路径:" + Environment.getExternalStorageDirectory().getAbsolutePath() +
//                "\n" + Environment.getRootDirectory().getAbsolutePath());
//        ///storage/sd-ext/STUDY/VIDEO
////        File file = new File(File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY"+File.separator+"VIDEO");
//        File file = new File(File.separator+"storage"+File.separator+"sd-ext"+File.separator+"STUDY");
//        // ->>>cursor:/storage/sd-ext/STUDY/VIDEO/0a-Unit1 Hello.mp4
////        File file = new File("/storage/emulated/0/DCIM/Camera/");
////        File file = new File("/storage/emulated/0/DCIM/");
//       //w我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
//        //storage/sd-ext/STUDY/VIDEO    //视频文件路径
//        getAllFiles(file);
//        getLocalVideos(file.getAbsolutePath());
//    }
//
//    /**
//     * 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
//     * 然后将该路径下面的所有文件夹列出来
//     */
//    private void getAllFiles(File root){
//        childFolders.clear();
//        File files[] = root.listFiles();
//        if(files != null){
//            for (File f : files){
//                if(f.isDirectory()){
//                    Log.d(TAG, "文件夹》》》》：" + f.getName());
//                    EcVideoFolderBean bean = new EcVideoFolderBean(f.getName(),false);
//                    childFolders.add(bean);
////                    getAllFiles(f);
//                }else{
////                    Log.d(TAG, "文件名称》》》》：" + f.getName());
//                }
//            }
//        }
//
//        //文件夹名称作为hashMap的key
//        hashMap.clear();
//
//        if(childFolders.size() == 0){//当前目录下没有任何文件夹，就模拟一个文件夹
////            childFolders.add(new EcVideoFolderBean(getString(R.string.text_folder),true));
//        }
//
//        for (EcVideoFolderBean bean : childFolders){
//            ArrayList<LocalVideoBean> temp = new ArrayList<LocalVideoBean>();
//            hashMap.put(bean.name,temp);
//        }
//
//        if (childFolders.size() > 0 ){
//            changeView(true);
//            //首次更新lv，第一个文件夹默认选中
//            childFolders.get(0).selected = true;
//            myListViewAdapter.setData(childFolders);
//
//        }else{
//            changeView(false);
//        }
//    }
//
//    private void getLocalVideos(String mPath){
//        /**
//         * 文件夹是否有对应视频文件，false为没有
//         */
//        boolean flagCurrentFolderHasVideo = false;
//
//        defaultArrayLists.clear();
//
//        StringBuilder selection = new StringBuilder();
//        selection.append("(" + MediaStore.Video.Media.DATA + " LIKE '" + mPath +File.separator+ "%')");
//        Log.d(TAG,"-->>>>>>>>"+selection.toString());
//
//        try {
//        ContentResolver contentResolver = getContentResolver();
//        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
//                String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
//                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
//                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
//                long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
//                //-------
////                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
////                String album = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
////                String artist = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
////                String mimeType = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
////                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
//                Log.d(TAG,"->>>cursor:"+path);
//
//                LocalVideoBean bean = new LocalVideoBean(id,date,path,displayName,size);
//                defaultArrayLists.add(bean);
//
//                //分文件夹遍历文件，存入对应集合中
//                for(EcVideoFolderBean folderBean : childFolders){
//
//                    ////我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4
//                    int i = bean.getPath().lastIndexOf(bean.getDisplayName());
//                    String tempPath = bean.getPath().substring(0,i);//去掉文件名的路径
//                    if (tempPath.contains(folderBean.name)){//只有文件路径（不包含文件的。后名称）包含文件夹集合中的一项
//                        flagCurrentFolderHasVideo = true;
//                        //将当前bean添加到hashMap中key为folderName
//                        hashMap.get(folderBean.name).add(bean);
//                    }
//                }
//            }
//        }
//
//        cursor.close();
//        }catch (Exception e){
//            e.printStackTrace();
//
//            flagCurrentFolderHasVideo = false;
//            defaultArrayLists.clear();
//        }
//
//        if (!flagCurrentFolderHasVideo){
//            if (childFolders.size() > 0){
//                if (hashMap.get(childFolders.get(0).name) != null){
//                    hashMap.get(childFolders.get(0).name).clear();
//                    hashMap.get(childFolders.get(0).name).addAll(defaultArrayLists);//第一个文件夹添加数据
//                }
//            }
//        }
//
//        if (childFolders.size() > 0){
//            //默认加载第一个文件夹下的视频
//            ArrayList<LocalVideoBean> arrayList = (ArrayList<LocalVideoBean>)hashMap.get(childFolders.get(0).name);
//            localVideoGVAdapter = new LocalVideoGVAdapter(LocalVideoActivity.this, arrayList,gv_act_local_video,mOnRequestVideoListener);
//            gv_act_local_video.setAdapter(localVideoGVAdapter);
//        }else{
//            localVideoGVAdapter = new LocalVideoGVAdapter(LocalVideoActivity.this, defaultArrayLists,gv_act_local_video,mOnRequestVideoListener);
//            gv_act_local_video.setAdapter(localVideoGVAdapter);
//        }
//}
//    LocalVideoGVAdapter localVideoGVAdapter ;
//    /**
//     *请求视频缩略图回调
//     */
//    private LocalVideoGVAdapter.OnRequestVideoListener mOnRequestVideoListener = new LocalVideoGVAdapter.OnRequestVideoListener() {
//
//        @Override
//        public void onRequestImage(String imgPath, ImageView iv) {
////            Bitmap bmp = getBmpFromCache(imgPath);
////            if (bmp != null){
////                iv.setImageBitmap(bmp);
////            }
//            /**
//             * 使用glide加载视频-缩略图(圆角)，glide不需要iv设置tag。glide内部处理了
//             */
//            Glide.with(LocalVideoActivity.this).load(Uri.fromFile(new File(imgPath))).transform(new GlideRoundTransform(LocalVideoActivity.this,20)).into(iv);
//        }
//
//    };
//
//    private void returnHome()
//    {
//        finish();
//    }
//}
