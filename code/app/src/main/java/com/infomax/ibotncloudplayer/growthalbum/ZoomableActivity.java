package com.infomax.ibotncloudplayer.growthalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.infomax.ibotncloudplayer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/22.
 * @Function:
 */

public class ZoomableActivity extends Activity implements ViewPager.OnPageChangeListener {

    private static final String EXTRA_ZOOMABLE_PATHS = "extra_zoomable_paths";
    private static final String EXTRA_ZOOMABLE_INDEX = "extra_zoomable_index";
    private static final String TAG = ZoomableActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private TextView mZoomableIndex;
    private ArrayList<String> mPaths;
    private int mIndex;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomable);
        getExtraData();
        initView();
        setupViewPager();
    }

    private void getExtraData() {
        mPaths = getIntent().getStringArrayListExtra(EXTRA_ZOOMABLE_PATHS);
        mIndex = getIntent().getIntExtra(EXTRA_ZOOMABLE_INDEX, 0);
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mZoomableIndex = (TextView) findViewById(R.id.zoomable_index);
    }

    private void setupViewPager() {
        mViewPager.setAdapter(new ZoomableViewPagerAdapter(this, mPaths));
        mViewPager.setCurrentItem(mIndex);
        mZoomableIndex.setText(mIndex + 1 + "/" + mPaths.size());
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mZoomableIndex.setText(position + 1 + "/" + mPaths.size());
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static void goToPage(Context context, ArrayList<String> paths, int index) {
        Intent intent = new Intent(context, ZoomableActivity.class);
        intent.putStringArrayListExtra(EXTRA_ZOOMABLE_PATHS, paths);
        intent.putExtra(EXTRA_ZOOMABLE_INDEX, index);
        context.startActivity(intent);
    }

    private class ZoomableViewPagerAdapter extends PagerAdapter{
        private ArrayList<String> mPaths;
        private Context mContext;
        public ZoomableViewPagerAdapter(Context context, ArrayList<String> paths) {
            mContext = context;
            mPaths = paths;
        }

        @Override
        public int getCount() {
            return mPaths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final String img_url = mPaths.get(position);
            Log.i(TAG, "jlzou instantiateItem img_url:" + img_url);
            View view = LayoutInflater.from(mContext).inflate(R.layout.zoomable_view_pager_item, null);
            final PhotoDraweeView mPhotoDraweeView = (PhotoDraweeView) view.findViewById(R.id.photoView);
            //添加点击事件
            mPhotoDraweeView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });

            mPhotoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.i(TAG, "jlzou SaveImageFromDataSource");
                    SaveImageFromDataSource(img_url, "/storage/sdcard/ibotn/" + System.currentTimeMillis() + ".jpg");
                    return false;
                }
            });

            if (!TextUtils.isEmpty(img_url)) {
                Log.i(TAG, "jlzou isEmpty:" + img_url);
                PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
//            controller.setUri(img_url);//设置图片url
                controller.setOldController(mPhotoDraweeView.getController());
//                controller.setLowResImageRequest(ImageRequest.fromUri(mThumbnialUrl));
                controller.setImageRequest(ImageRequest.fromUri(img_url));
                controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        Log.i(TAG, "jlzou onFinalImageSet:" + img_url);
                        if (imageInfo == null || mPhotoDraweeView == null) {
                            return;
                        }
                        mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                mPhotoDraweeView.setController(controller.build());
            } else {
                Toast.makeText(ZoomableActivity.this, "图片获取失败", Toast.LENGTH_SHORT).show();
            }
            container.addView(view);
            return view;
        }
    }

    private void SaveImageFromDataSource(String url, final String localSavePath) {
        Log.i(TAG, "jlzou SaveImageFromDataSource start");
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true).build();

        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(imageRequest, ZoomableActivity.this);

        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> source) {
                Log.i(TAG, "jlzou onFailureImpl:" + source.getFailureCause().getMessage());
            }

            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> source) {
                Log.i(TAG, "jlzou onNewResultImpl");
                CloseableReference<CloseableImage> reference = source.getResult();
                CloseableImage image = reference.get();

                if(image instanceof CloseableBitmap){
                    CloseableBitmap bitmapimage = (CloseableBitmap) image;//图片转为bitmap
                    Bitmap picbitmap = bitmapimage.getUnderlyingBitmap();
                    Log.i(TAG, "jlzou save ipg");
                    saveBitmap(picbitmap, localSavePath);
                }

            }
        }, CallerThreadExecutor.getInstance());
    }

    public Boolean saveBitmap(Bitmap bitmap, String localSavePath) {

        if (TextUtils.isEmpty(localSavePath)) {
            throw new NullPointerException("保存的路径不能为空");
        }

        File f = new File(localSavePath);
        if (f.exists()) {// 如果本来存在的话，删除
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;

        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }

        return true;

    }

}
