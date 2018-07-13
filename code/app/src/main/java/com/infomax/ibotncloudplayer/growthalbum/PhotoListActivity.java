package com.infomax.ibotncloudplayer.growthalbum;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.growthalbum.bean.AlbumInfo;
import com.infomax.ibotncloudplayer.growthalbum.bean.MessageEvent;
import com.infomax.ibotncloudplayer.growthalbum.fragment.LabeledFragment;
import com.infomax.ibotncloudplayer.growthalbum.fragment.UnlabeledFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/7.
 * @Function:
 */

public class PhotoListActivity extends FragmentActivity implements View.OnClickListener {

    private ViewPager mViewpager;
    private TextView mUnlabeledTextView;
    private TextView mLabeledTextView;
    private LinearLayout mHeader;
    private LinearLayout mHeaderBar;

    private ArrayList<Fragment> mFragments;
    private Fragment currentFragment;
    private String mFaceId;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        mDecorView = getWindow().getDecorView();
        mFaceId = getIntent().getStringExtra("faceId");
        EventBus.getDefault().register(this);
        initView();
    }

    private void initView() {
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        mUnlabeledTextView = (TextView) findViewById(R.id.tv_unlabeled);
        mLabeledTextView = (TextView) findViewById(R.id.tv_labeled);
        mHeader = (LinearLayout) findViewById(R.id.header);
        mHeaderBar = (LinearLayout) findViewById(R.id.header_bar);

        mUnlabeledTextView.setOnClickListener(this);
        mLabeledTextView.setOnClickListener(this);
        mHeaderBar.setOnClickListener(this);

        mFragments = new ArrayList<>();
        mFragments.add(UnlabeledFragment.newInstance(true, mFaceId));
        mFragments.add(LabeledFragment.newInstance(true, mFaceId));
        mHeader.setVisibility(View.VISIBLE);

        mViewpager.setAdapter(new PhotoListPagerAdapter(getSupportFragmentManager(),mFragments));
        mViewpager.addOnPageChangeListener(new PhotoListChangeListener());
        mViewpager.setCurrentItem(0);
        currentFragment = mFragments.get(0);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.tv_unlabeled:
                mViewpager.setCurrentItem(0);
                break;
            case R.id.tv_labeled:
                mViewpager.setCurrentItem(1);
                break;
            case R.id.header_bar:
                finish();
                break;
        }
    }

    private class PhotoListPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        public PhotoListPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private class PhotoListChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch(position)
            {
                case 0:
                    mUnlabeledTextView.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
                    mLabeledTextView.setTextColor(Color.argb(0x7F, 0xFF, 0xFF, 0xFF));
                    currentFragment = mFragments.get(0);
                    break;
                case 1:
                    mLabeledTextView.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
                    mUnlabeledTextView.setTextColor(Color.argb(0x7F, 0xFF, 0xFF, 0xFF));
                    currentFragment = mFragments.get(1);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public boolean getStatus(int position){
        if(currentFragment instanceof UnlabeledFragment){
            return ((UnlabeledFragment)currentFragment).getStatus(position);
        }else if(currentFragment instanceof LabeledFragment){
            return ((LabeledFragment)currentFragment).getStatus(position);
        }
        return false;
    }

    public void setStatus(int position,boolean status){
        if(currentFragment instanceof UnlabeledFragment){
            ((UnlabeledFragment)currentFragment).setStatus(position,status);
        }else if(currentFragment instanceof LabeledFragment){
            ((LabeledFragment)currentFragment).setStatus(position,status);
        }
    }

    public boolean getEditStatus(){
        if(currentFragment instanceof UnlabeledFragment){
            return ((UnlabeledFragment)currentFragment).getEditStatus();
        }else if(currentFragment instanceof LabeledFragment){
            return ((LabeledFragment)currentFragment).getEditStatus();
        }
        return false;
    }

    public void refers(List<AlbumInfo.DataBean> list, boolean mark){
        if(mark){
            ((LabeledFragment)mFragments.get(1)).refers(list);
        }else{
            ((UnlabeledFragment)mFragments.get(0)).refers(list);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if(messageEvent.type == MessageEvent.GET_MARK_STATUS){
            messageEvent.event.setMark(getStatus(messageEvent.event.getPosition()));
        }else if(messageEvent.type == MessageEvent.SET_MARK_STATUS){
            setStatus(messageEvent.event.getPosition(), messageEvent.event.isMark());
        }else if(messageEvent.type == MessageEvent.GET_EDIT_STATUS){
            boolean isEdit = getEditStatus();
            messageEvent.event.setEdit(isEdit);
        }
    }
}
