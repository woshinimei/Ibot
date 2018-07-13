package com.infomax.ibotncloudplayer.growthalbum;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.growthalbum.bean.MessageEvent;
import com.infomax.ibotncloudplayer.growthalbum.fragment.ImageDetailFragment;
import com.infomax.ibotncloudplayer.growthalbum.utils.PictureConfig;
import com.infomax.ibotncloudplayer.growthalbum.view.HackyViewPager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 图片查看器
 * Created by zhuyong on 2017/5/12.
 */
public class ImagePagerActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "ImagePagerActivity";
    private static final String STATE_POSITION = "STATE_POSITION";
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    //Begin jinlong.zou
    public static final String EXTRA_IMAGE_THUMBNAIL_URLS = "image_thumnnail_urls";
    //End jinlong.zou

    private HackyViewPager mPager;
    private int pagerPosition;
    private TextView indicator;
    //Begin jinlong.zou
    private ImageView markImage;
    private LinearLayout backBtn;
    private static final String STATE_SELECTED = "STATE_SELECTED";
    //End jinlong.zou
    private static boolean mIsShowNumber = true;//是否显示数字下标
    private MessageEvent.Event event = new MessageEvent.Event();
    private MessageEvent messageEvent;

    private View mDecorView;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //透明状态栏
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
        setContentView(R.layout.activity_image_detail_pager);
        mDecorView = getWindow().getDecorView();
        pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        List<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        //Begin jinlong.zou
        List<String> thumbnails = getIntent().getStringArrayListExtra(EXTRA_IMAGE_THUMBNAIL_URLS);
        //End jinlong.zou
        mPager = (HackyViewPager) findViewById(R.id.pager);
        ImagePagerAdapter mAdapter = new ImagePagerAdapter(
                getSupportFragmentManager(), urls, thumbnails);
        mPager.setAdapter(mAdapter);
        indicator = (TextView) findViewById(R.id.indicator);
        //Begin jinlong.zou
        markImage = (ImageView) findViewById(R.id.iv_mark);
        backBtn = (LinearLayout) findViewById(R.id.header_bar);
        markImage.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        messageEvent = new MessageEvent();
//        event.setPosition(pagerPosition);
//        messageEvent.sendMessageEvent(event, MessageEvent.GET_MARK_STATUS);
//        EventBus.getDefault().post(messageEvent);
//        markImage.setSelected(event.isMark());
        messageEvent.sendMessageEvent(event, MessageEvent.GET_EDIT_STATUS);
        EventBus.getDefault().post(messageEvent);
        boolean isEditStatus = event.isEdit();
        if(isEditStatus)
            markImage.setVisibility(View.VISIBLE);
        //End jinlong.zou

        CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
        indicator.setText(text);
        // 更新下标
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int arg0) {
                CharSequence text = getString(R.string.viewpager_indicator,
                        arg0 + 1, mPager.getAdapter().getCount());
                indicator.setText(text);
                //Begin jinlong.zou
                event.setPosition(arg0);
                messageEvent.sendMessageEvent(event, MessageEvent.GET_MARK_STATUS);
                EventBus.getDefault().post(messageEvent);
                markImage.setSelected(event.isMark());
                //End jinlong.zou
            }

        });
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
            markImage.setSelected(savedInstanceState.getBoolean(STATE_SELECTED));
        }

        mPager.setOffscreenPageLimit(1);
        mPager.setCurrentItem(pagerPosition);
        indicator.setVisibility(mIsShowNumber ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, mPager.getCurrentItem());
        //Begin jinlong.zou
        outState.putBoolean(STATE_SELECTED, markImage.isSelected());
        //End jinlong.zou
    }

    //Begin jinlong.zou
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_mark:
                boolean isSelect = markImage.isSelected();
                markImage.setSelected(!isSelect);
                event.setPosition(mPager.getCurrentItem());
                event.setMark(!isSelect);
                messageEvent.sendMessageEvent(event, MessageEvent.SET_MARK_STATUS);
                EventBus.getDefault().post(messageEvent);
                break;
            case R.id.header_bar:
                finish();
                break;
        }
    }
    //End jinlong.zou

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public List<String> fileList;
        //Begin jinlong.zou
        public List<String> thumbnailList;
        //End jinlong.zou

        public ImagePagerAdapter(FragmentManager fm, List<String> fileList, List<String> list) {
            super(fm);
            this.fileList = fileList;
            //Begin jinlong.zou
            this.thumbnailList = list;
            //End jinlong.zou
        }

        @Override
        public int getCount() {
            return fileList == null ? 0 : fileList.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = fileList.get(position).toString();
            //Begin jinlong.zou
            String thumbnialUrl = thumbnailList.get(position).toString();
            return ImageDetailFragment.newInstance(url, thumbnialUrl);
            //End jinlong.zou
        }

    }

    public static void startActivity(Context context, PictureConfig config) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, config.list);
        //Begin jinlong.zou
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_THUMBNAIL_URLS, config.thumbnialList);
        //End jinlong.zou
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, config.position);
        mIsShowNumber = config.mIsShowNumber;
        context.startActivity(intent);
    }
}