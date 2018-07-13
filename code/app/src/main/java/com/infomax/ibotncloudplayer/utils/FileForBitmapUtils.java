package com.infomax.ibotncloudplayer.utils;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.mymedia.ThumbnailUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jy on 2016/11/23.
 * 操作bitmap 的文件工具类
 */
public class FileForBitmapUtils {

    private static final String TAG = FileForBitmapUtils.class.getSimpleName();


    /**
     * 保存成功 return true
     * 根据未加密的视频文件，创建缩略图，保存到和视频文件对应的位置
     * 视频文件只针对mp4格式;
     * 1.测试发现swf文件无法创建图片
     * @param bean
     * @return
     * 注意：
     */
    public synchronized static boolean createPngForMP4(LocalVideoBean bean) /*throws Exception*/
    {
        boolean flag = false;

        int i = bean.getPath().lastIndexOf(bean.getDisplayName());
        String tempUnDisplayNamePath = bean.getPath().substring(0, i);//去掉文件名的路径

        //文件名去掉后缀 ，大小写的都去掉.mp4
        String toLowerDisplayName = bean.getDisplayName().toLowerCase();

        int indexSuffix = toLowerDisplayName.lastIndexOf(".mp4");

        String tempPngDisplayName = toLowerDisplayName.substring(0,indexSuffix) + ".png";//run png

        File pngFile = new File(tempUnDisplayNamePath + tempPngDisplayName);

        //check png size ,if size = 0,delete it ,and create it again
        if (FileUtils.isFile(pngFile)){

            MyLog.e(TAG, "createPngForMP4()>>>>>>>>png>>length " + pngFile.length());
            if (pngFile.length() <= 0 ){
                FileUtils.deleteFile(pngFile);
                //对已加密的文件解密操作，因为加密的视频文件不能
                VideoEncryptUtils.processVideoEncryptFunction(MyApplication.getInstance(),bean.getPath());
            }
        }

        FileOutputStream out = null;
        if (pngFile.exists()){
            bean.setThumbnailPath(pngFile.getAbsolutePath());

        }else {
            try {

                //if (file.exists()){
                //file.delete();
                //}

                out = new FileOutputStream(pngFile);
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(bean.getPath(), MediaStore.Images.Thumbnails.MICRO_KIND);
                MyLog.e(TAG,"bitmap--->>:"+(bitmap == null ? "null" : "not null"));
                if (bitmap != null)
                {
                    if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
                    {
                        out.flush();
                        out.close();
                        flag = true;
                        bean.setThumbnailPath(pngFile.getAbsolutePath());
                        MyLog.e(TAG, "success-->thumbnail path:" + pngFile.getAbsolutePath());
                    }
                    bitmap.recycle();
                    bitmap = null;
                }else
                {//视频文件加密或已被破坏，此时情况--如果没有缩略图，全部以ibotn默认图标显示。现以""空字符串作为标记
                    bean.setThumbnailPath(Constant.Thumbnail_Empty);
                }
                //对已解密的文件加密操作
                VideoEncryptUtils.toEncryptVideoForDecryptedVideo(MyApplication.getInstance(), bean.getPath());

            }catch (Exception e){
                e.printStackTrace();
                MyLog.e(TAG, "Exception--->>:" + e.getMessage());
                bean.setThumbnailPath("");
                flag = false;

                //对已解密的文件加密操作
                VideoEncryptUtils.toEncryptVideoForDecryptedVideo(MyApplication.getInstance(), bean.getPath());
            }finally {
                if (out != null)
                {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return flag;
    }
}
