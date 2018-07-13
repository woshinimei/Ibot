package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.view.GlideRoundTransform;

import java.io.File;

/**
 * Created by jy on 2016/10/19.
 */
public class GlideUtils {

    private static final String TAG = GlideUtils.class.getSimpleName();

    /**
     *
     * @param ctx
     * @param filePath
     * @param iv
     * 加载视频，圆角矩形，配置默认图片
     */
    public static void load(Context ctx,final String filePath,ImageView iv){
        int radius = DisplayUtils.dip2px(ctx, 10.0f);

        if (TextUtils.isEmpty(filePath))
        {
            MyLog.e(TAG,"filePath---->>>:"+filePath);

            iv.setBackgroundResource(R.drawable.default_ibotn);

            return;
        }

        Glide.with(ctx)
                .load(Uri.fromFile(new File(filePath)))
                .placeholder(R.drawable.default_ibotn)//默认
                .error(R.drawable.default_ibotn)//
                .transform(new GlideRoundTransform(ctx, radius))
                .into(iv);

    }
    private static int currentId = 0;
    /**加载视频，圆角矩形，配置默认图片*/
    /**
     * @param ctx
     * @param filePath
     * @param iv
     */
    public static void loadWithListener(Context ctx, final String filePath,ImageView iv){

//        ++currentId;
//
//        MyLog.e(TAG,"loadWithListener-->>currentId:"+currentId + ",filePath"+filePath);

//        //dealFileForLevel
//        EncryptFileUtils.dealFileForLevel(filePath);

        int radius = DisplayUtils.dip2px(ctx, 20.0f);

         Glide.with(ctx)
                .load(Uri.fromFile(new File(filePath)))
                .placeholder(R.drawable.default_ibotn)//默认
                .error(R.drawable.default_ibotn)//
                .transform(new GlideRoundTransform(ctx, radius))
                .listener(new RequestListener<Uri, GlideDrawable>() {//加载监听
                    @Override
                    public boolean onException(Exception e, Uri uri, Target<GlideDrawable> target, boolean b) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, Uri uri, Target<GlideDrawable> target, boolean b, boolean b1) {
                        //encryption

//                        MyLog.e(TAG,"Glide-->--onResourceReady>>:"+currentId+",filePath"+filePath);

                        return false;
                    }
                })
                .into(iv);

    }
}
