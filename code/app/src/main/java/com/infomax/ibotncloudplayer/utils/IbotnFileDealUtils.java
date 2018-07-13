package com.infomax.ibotncloudplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.bean.EcVideoFolderBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jy on 2017/3/10 ;13:20.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 * 只是针对ibotn 中文件处理的工具类；只获取传入路径下面所有的一级文件夹，及其包含的文件。
 */
public class IbotnFileDealUtils {
    final static String  TAG = IbotnFileDealUtils.class.getSimpleName();
    /**
     * AUDIO文件夹下面的所有一级文件夹集合
     */
    private  LinkedList<EcVideoFolderBean> childFolders = new LinkedList<EcVideoFolderBean>();
    /**
     * 音乐文件的HassMap,key文件夹，value为该文件夹下面的所有音频(mp3)文件
     */
    private  HashMap<String,ArrayList<LocalAudioBean>> hashMap = new HashMap<String,ArrayList<LocalAudioBean>>();

    private static IbotnFileDealUtils instance;

    private IbotnFileDealUtils(){}

    public static IbotnFileDealUtils getInstance(){
        if (instance == null){
            synchronized (IbotnFileDealUtils.class){
                if (instance == null){
                    instance = new IbotnFileDealUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
     * 然后将该路径下面的所有文件夹列出来
     */
    public synchronized LinkedList<EcVideoFolderBean> getAllFolders(File root) {

        if (root == null){
            return null;
        }

        MyLog.d(TAG, "getAllFolders>>>isFile>：" + root.isFile()
                + "\n FileUtils.isDir(root)::" + FileUtils.isDir(root));
        if (!FileUtils.isDir(root)){
            return null;
        }

        ArrayList<String> tempFolders = new ArrayList<>();

        childFolders.clear();

        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    MyLog.d(TAG, "folder>>>>：" + f.getName());
                    ///////
//                    EcVideoFolderBean bean = new EcVideoFolderBean(f.getName(), false);

                    //为了把ibotn文件夹放到文件夹列表首位 考虑大小写情况----start。
//                    tempName = f.getName();
//                    tempName.toLowerCase();
//
//                    if (tempName.startsWith("ibotn")) {
//
//                        childFolders.addFirst(bean);
//                    } else {
//                        childFolders.add(bean);
//                    }
                    /////////
                    tempFolders.add(f.getName());

                } else {
                    Log.d(TAG, "文件名称>>>>：" + f.getName());
                }
            }
        }

        //对音乐文件根据前三位序号排序，num001,num002,num003，...;如果是旧的sd卡，没有num***,存储时就不去掉前六位
        Collections.sort(tempFolders);
        for (String name : tempFolders)
        {
            if (name.startsWith("num")){
                name = name.substring(6);
            }
            EcVideoFolderBean bean = new EcVideoFolderBean(name, false);
            childFolders.add(bean);
        }

        MyLog.e(TAG, "getAllFiles()>>>>>folder num>>>>：" + childFolders.size());

        hashMap.clear();
        //给hashmap赋值   ，文件夹名称作为hashMap的key
        for (EcVideoFolderBean bean : childFolders){
            ArrayList<LocalAudioBean> temp = new ArrayList<LocalAudioBean>();
            hashMap.put(bean.name, temp);
        }

        return childFolders;
    }

    /**
     * 不加载专辑图片 ，默认album 复制 ""
     * @param mPath
     */
    public synchronized  HashMap<String,ArrayList<LocalAudioBean>> getLocalVideo(final Context ctx,String mPath,LinkedList<EcVideoFolderBean> childFolders){

        /**文件夹是否有对应音频文件，false为没有*/
//        boolean flagCurrentFolderHasVideo = false;

        int number = 0;

        long time = SystemClock.currentThreadTimeMillis();

        StringBuilder selection = new StringBuilder();
        //音频
        selection.append("(" + MediaStore.Video.Media.DATA + " LIKE '" + mPath +File.separator+ "%')");
        Log.d(TAG, "-->>>>>>>>" + selection.toString());

        /**
         * 从MediaStore.Video.Thumbnail查询中获得的列的列表。
         */
        String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID };

        /**
         * 从MediaStore.Video.Media查询中获得的列的列表。
         */
        String[] mediaColumns = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE
        };

        try {
            ContentResolver contentResolver = ctx.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    number ++ ;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));//歌曲ID
                    String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));//文件名称
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));//歌曲文件的路径
                    //音频文件对应的专辑图片的album_id
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));//专辑id
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));//文件的大小
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));//添加时间

                    Log.d(TAG, "->>>cursor:" + path+",album_id:"+album_id);//// TODO: 2017/2/23
                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName))
                    {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith(".mp4"))//过滤mp3
                        {
                            LocalAudioBean bean = new LocalAudioBean(id,date,path,displayName,size);

                            String albumArtPath = ""/*getAlbumArtPath(album_id)*/;
                            bean.setAlbumArtPath(albumArtPath);

//                            Log.d(TAG, "->>>bean.setAlbumArtPath():" + bean.getAlbumArtPath());// TODO: 2017/2/23

                            //分文件夹遍历文件，存入对应集合中
                            for(EcVideoFolderBean folderBean : childFolders){

                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4

                                int index = bean.getPath().lastIndexOf(bean.getDisplayName());

                                String tempPath = bean.getPath().substring(0,index-1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”

                                ///////////
                                //log::: D/IbotnFileDealUtils: >>>>tempPath:/storage/sd-ext/STUDY/AUDIO/小i英文/,folderBean.name:故事
                                MyLog.d(TAG,">>>>tempPath:" + tempPath
                                        + "\n folderBean.name:"+folderBean.name);
                                //////////

                                if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
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
        }catch (Exception e){
            e.printStackTrace();

            MyLog.e(TAG, ">>>getLocalAudio()>>>Exception>>>>>>:" + e.getMessage());

            hashMap.clear();

//            flagCurrentFolderHasVideo = false;
        }

       /* if (!flagCurrentFolderHasVideo){
            hashMap.clear();
        }*/

        MyLog.e(TAG, "number>>>>>>>>>>>>>>>>>>>:"+number);

        time = SystemClock.currentThreadTimeMillis() - time;

        MyLog.e(TAG, "elapsedRealtime>>>>>>>>>>>>>>>>>>>:" + (time));

        return hashMap;
    }

    /**
     * 不加载专辑图片 ，默认album 复制 ""
     * @param mPath
     */
    public synchronized  HashMap<String,ArrayList<LocalAudioBean>> getLocalAudio(final Context ctx,String mPath,LinkedList<EcVideoFolderBean> childFolders){

        /**文件夹是否有对应音频文件，false为没有*/
//        boolean flagCurrentFolderHasVideo = false;

        int number = 0;

        long time = SystemClock.currentThreadTimeMillis();

        StringBuilder selection = new StringBuilder();
        //音频
        selection.append("(" + MediaStore.Audio.Media.DATA + " LIKE '" + mPath +File.separator+ "%')");
        Log.d(TAG, "-->>>>>>>>" + selection.toString());

        try {
            ContentResolver contentResolver = ctx.getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection.toString(), null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    number ++ ;

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                    String displayName = cursor .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名称
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲文件的路径
                    //音频文件对应的专辑图片的album_id
                    int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑id
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//文件的大小
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));//添加时间

                    Log.d(TAG, "->>>cursor:" + path+",album_id:"+album_id);//// TODO: 2017/2/23
                    //过滤mp4,测试发现有mpg格式的。
                    if (!TextUtils.isEmpty(displayName))
                    {
                        String tempDisplayName = displayName.toLowerCase();
                        if (tempDisplayName.endsWith(".mp3"))//过滤mp3
                        {
                            LocalAudioBean bean = new LocalAudioBean(id,date,path,displayName,size);

                            String albumArtPath = ""/*getAlbumArtPath(album_id)*/;
                            bean.setAlbumArtPath(albumArtPath);

//                            Log.d(TAG, "->>>bean.setAlbumArtPath():" + bean.getAlbumArtPath());// TODO: 2017/2/23

                            //分文件夹遍历文件，存入对应集合中
                            for(EcVideoFolderBean folderBean : childFolders){

                                //我的手机目录 ->>>cursor:/storage/emulated/0/DCIM/Camera/VID_20160521_154653.mp4

                                int index = bean.getPath().lastIndexOf(bean.getDisplayName());

                                String tempPath = bean.getPath().substring(0,index-1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”

                                ///////////
                                //log::: D/IbotnFileDealUtils: >>>>tempPath:/storage/sd-ext/STUDY/AUDIO/小i英文/,folderBean.name:故事
                                MyLog.d(TAG,">>>>tempPath:" + tempPath
                                        + "\n folderBean.name:"+folderBean.name);
                                //////////

                                if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
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
        }catch (Exception e){
            e.printStackTrace();

            MyLog.e(TAG, ">>>getLocalAudio()>>>Exception>>>>>>:" + e.getMessage());

            hashMap.clear();

//            flagCurrentFolderHasVideo = false;
        }

       /* if (!flagCurrentFolderHasVideo){
            hashMap.clear();
        }*/

        MyLog.e(TAG, "number>>>>>>>>>>>>>>>>>>>:"+number);

        time = SystemClock.currentThreadTimeMillis() - time;

        MyLog.e(TAG, "elapsedRealtime>>>>>>>>>>>>>>>>>>>:" + (time));

        return hashMap;
    }

    /**
     *
     * @param folderRootPath
     * 使用递归方式直接遍历文件夹。
     */
    public synchronized HashMap<String,ArrayList<LocalAudioBean>> getLocalVideosWithRecursion(Context ctx,String folderRootPath){

        MyLog.d(TAG, ">>>getLocalVideosWithContentResolver>>>>>mPath：" + folderRootPath );
        ArrayList<LocalAudioBean> defaultArrayLists = new ArrayList<LocalAudioBean>();

        if (FileUtils.isFileExists(folderRootPath)){

            File file = new File(folderRootPath);
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File tempFile : files)  {

//                    if (!canLoadingData){
//                        return;
//                    }

                    if (FileUtils.isDir(tempFile)){
                        getLocalVideosWithRecursion(ctx,tempFile.getAbsolutePath());

                    }else {
                        String displayName = tempFile.getName();
                        if (!TextUtils.isEmpty(displayName)){
                            int id = 0;
                            long date = 0;
                            String path = tempFile.getAbsolutePath();
                            String tempDisplayName = displayName.toLowerCase();
                            long size = tempFile.length();
                            boolean existFileType = false;
                            for (String fileType : Constant.CONFIG_LOAD_VIDEO_TYPES)
                            {
                                if (tempDisplayName.endsWith(fileType))
                                {
                                    existFileType = true;
                                }
                            }
                            if (existFileType)
                            {
                                LocalVideoBean bean = new LocalVideoBean(id,date,path,displayName,size);

                                /////////close this way to generate png
                                if (tempDisplayName.endsWith(".mp4")){//mp4文件才创建缩略图
                                    FileForBitmapUtils.createPngForMP4(bean);
                                }
                                ////////

                                defaultArrayLists.add(bean);

                                for(EcVideoFolderBean folderBean : childFolders){

                                    int i = bean.getPath().lastIndexOf(bean.getDisplayName());
                                    String tempPath = bean.getPath().substring(0,i - 1);//去掉文件名的路径,i-1是为了去掉文件名前面的“/”
//                                MyLog.d(TAG, ">>>getLocalVideosWithContentResolver()>>>>tempPath():" + tempPath
//                                        + ",folderBean.name:" + folderBean.name);// TODO: 2017/2/23
                                    if (tempPath.endsWith(folderBean.name)){//只有文件路径（不包含文件名称）最后结尾在文件夹集合中的一项
                                        hashMap.get(folderBean.name).add(bean);
                                    }

                                    String tempFolderName = childFolders.get(0).name;

                                    if(tempFolderName.toLowerCase().startsWith("ibotn"))
                                    {
                                        String fileName =  bean.getDisplayName().toLowerCase();
                                        if (fileName.startsWith("ibotn")){
                                            MyLog.d(TAG, ">>>>>>>>ibotn:" + bean.getPath());
                                            SharedPreferenceUtils.setIbotnFile(ctx,bean.getPath());
                                            FileEnhancedUtils.dealFileForLevel(ctx, SharedPreferenceUtils.getIbotnFile(ctx));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return hashMap;

    }


}
