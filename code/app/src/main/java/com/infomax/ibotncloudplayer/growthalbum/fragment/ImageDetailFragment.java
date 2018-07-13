package com.infomax.ibotncloudplayer.growthalbum.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
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

import me.relex.photodraweeview.PhotoDraweeView;

public class ImageDetailFragment extends Fragment {
    private static final String TAG = ImageDetailFragment.class.getSimpleName();
    private String mImageUrl;
    //Begin jinlong.zou
    private String mThumbnialUrl;
    //End jinlong.zou
    private PhotoDraweeView mPhotoDraweeView;
    private SimpleDraweeView draweeView;

    public static ImageDetailFragment newInstance(String imageUrl, String thumbnialUrl) {
        final ImageDetailFragment imageDetailFragment = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        //Begin jinlong.zou
        args.putString("thumbnialUrl", thumbnialUrl);
        //End jinlong.zou
        imageDetailFragment.setArguments(args);

        return imageDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
        //Begin jinlong.zou
        mThumbnialUrl = getArguments() != null ? getArguments().getString("thumbnialUrl") : null;
        //End jinlong.zou
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_detail, container, false);
        draweeView = (SimpleDraweeView) v.findViewById(R.id.my_image_view);
//        draweeView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });
//        mPhotoDraweeView = (PhotoDraweeView) v.findViewById(R.id.photoView);
        //添加点击事件
//        mPhotoDraweeView.setOnPhotoTapListener(new OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(View view, float x, float y) {
//                getActivity().finish();
//            }
//        });

//        mPhotoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setMessage(getResources().getString(R.string.save_picture));
//                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        SaveImageFromDataSource(mImageUrl, Filed.SAVE_IMAGE_PATH + System.currentTimeMillis() + ".jpg");
//                    }
//                });
//                builder.create().show();
//
//                return false;
//            }
//        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!TextUtils.isEmpty(mImageUrl)) {
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setOldController(draweeView.getController());
            controller.setLowResImageRequest(ImageRequest.fromUri(mThumbnialUrl));
            controller.setImageRequest(ImageRequest.fromUri(mImageUrl));
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null || draweeView == null) {
                        return;
                    }
//                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            draweeView.setController(controller.build());
        } else {
            //Toast.makeText(getActivity(), "图片获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveImageFromDataSource(String url, final String localSavePath) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true).build();

        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline()
                .fetchDecodedImage(imageRequest, getActivity());

        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<CloseableImage>>() {

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> source) {
                Log.i(TAG, "jlzou onFailureImpl:" + source.getFailureCause().getMessage());
            }

            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> source) {
                CloseableReference<CloseableImage> reference = source.getResult();
                CloseableImage image = reference.get();

                if(image instanceof CloseableBitmap){
                    CloseableBitmap bitmapimage = (CloseableBitmap) image;//图片转为bitmap
                    Bitmap picbitmap = bitmapimage.getUnderlyingBitmap();
                    saveBitmap(picbitmap, localSavePath);
                }

            }
        }, CallerThreadExecutor.getInstance());
    }

    public Boolean saveBitmap(Bitmap bitmap, String localSavePath) {

        if (TextUtils.isEmpty(localSavePath)) {
            throw new NullPointerException("save path cant be null");
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
            // 最后通知图库更新
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(f.getPath()))));
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.save_picture_path) + localSavePath , Toast.LENGTH_SHORT).show();
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
