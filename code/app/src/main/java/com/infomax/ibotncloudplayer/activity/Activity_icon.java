package com.infomax.ibotncloudplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.MainActivity;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.adapter.IconGvAdater;
import com.infomax.ibotncloudplayer.adapter.Vp_Adater;
import com.infomax.ibotncloudplayer.bean.Icon;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DisplayUtils;
import com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

/**
 * Created by hushaokun on 2018/6/6.
 */

public class Activity_icon extends FullScreenActivity implements ViewPager.OnPageChangeListener {
    ViewPager vp;
    LinearLayout llContent;
    Vp_Adater vpAdater;
    List<RelativeLayout> vpList = new ArrayList<>();//viewPager存放的view
    List<Icon> iconList;//图标列表
    IconGvAdater gvAdater;
    ImageView ivBot;
    int left = 0;//翻页圆心距父控件左边的距离

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon);
        setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        initView();
        vpList.clear();
        initGV1();
        initGV2();
        initGV3();
        initGV4();
        initVp();
    }


    private void initGV1() {
        RelativeLayout relativeLayout = new RelativeLayout(getBaseContext());
        GridView gv = new GridView(getBaseContext());
        relativeLayout.addView(gv);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gv.setLayoutParams(params);
        gv.setNumColumns(4);
        gv.setVerticalSpacing(20);
        Icon icon1 = new Icon(getString(R.string.icon_list_video_call), R.drawable.icon_list_video_call);
        Icon icon2 = new Icon(getString(R.string.icon_list_online_classroom), R.drawable.icon_list_online_classroom);
        Icon icon3 = new Icon(getString(R.string.icon_list_online_service), R.drawable.icon_list_online_service);
        Icon icon4 = new Icon(getString(R.string.icon_list_visitor_pattern), R.drawable.icon_list_visitor_pattern);
        Icon icon5 = new Icon(getString(R.string.icon_list_grow_album), R.drawable.icon_list_grow_album);
        Icon icon6 = new Icon(getString(R.string.icon_list_scan_read), R.drawable.icon_list_scan_read);
        Icon icon7 = new Icon(getString(R.string.icon_list_face_sign), R.drawable.icon_list_face_sign);
        Icon icon8 = new Icon(getString(R.string.icon_list_face_recognition), R.drawable.icon_list_face_recognition);
        iconList = new ArrayList<>();
        iconList.add(icon1);
        iconList.add(icon2);
        iconList.add(icon3);
        iconList.add(icon4);
        iconList.add(icon5);
        iconList.add(icon6);
        iconList.add(icon7);
        iconList.add(icon8);
        gvAdater = new IconGvAdater(iconList, getBaseContext());
        gv.setAdapter(gvAdater);
        vpList.add(relativeLayout);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        startActivity(new Intent(getBaseContext(), Activity_album_video.class));//成长相册
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                }
            }
        });
    }

    private void musicIntent(String title) {
        Intent intent = new Intent(getBaseContext(), Activity_music.class);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private void initGV2() {
        RelativeLayout relativeLayout = new RelativeLayout(getBaseContext());
        GridView gv = new GridView(getBaseContext());
        relativeLayout.addView(gv);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gv.setLayoutParams(params);
        gv.setNumColumns(4);
        gv.setVerticalSpacing(20);
        Icon icon1 = new Icon(getString(R.string.icon_list_follow_me), R.drawable.icon_list_follow_me);
        Icon icon2 = new Icon(getString(R.string.icon_list_baby_bus), R.drawable.icon_list_babybus);
        Icon icon3 = new Icon(getString(R.string.icon_list_scene_study), R.drawable.icon_list_study);
        Icon icon4 = new Icon(getString(R.string.icon_list_game), R.drawable.icon_list_game);
        Icon icon5 = new Icon(getString(R.string.icon_list_camera), R.drawable.icon_list_camera);
        Icon icon6 = new Icon(getString(R.string.icon_list_oral_practice), R.drawable.icon_list_oral_practice);
        Icon icon7 = new Icon(getString(R.string.icon_list_english_anim), R.drawable.icon_list_english_anim);
        Icon icon8 = new Icon(getString(R.string.icon_list_chinese_anim), R.drawable.icon_list_chinese_anim);
        iconList = new ArrayList<>();
        iconList.add(icon1);
        iconList.add(icon2);
        iconList.add(icon3);
        iconList.add(icon4);
        iconList.add(icon5);
        iconList.add(icon6);
        iconList.add(icon7);
        iconList.add(icon8);
        gvAdater = new IconGvAdater(iconList, getBaseContext());
        gv.setAdapter(gvAdater);
        vpList.add(relativeLayout);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        startActivity(new Intent(getBaseContext(), Activity_game_and_babyBus.class));
                        break;
                    case 2:
                        break;
                    case 3://编程游戏
                        String game = getString(R.string.intelligence_game);
                        //根据包名跳转到应用
                        String packUrl = Constant.ThirdPartAppPackageName.OTHER_APK_PACKAGENAME_SRCATCHJR;
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packUrl);
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            ToastUtils.showCustomToast(null, "please install the game of  :" + game);
                        }

                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        startActivity(new Intent(getBaseContext(), Activity_english_anim.class));//英语动画
                        break;
                    default:
                        startActivity(new Intent(getBaseContext(), Activity_chinese_anim.class));//汉语动画
                        break;
                }
            }
        });
    }

    private void initGV3() {
        RelativeLayout relativeLayout = new RelativeLayout(getBaseContext());
        GridView gv = new GridView(getBaseContext());
        relativeLayout.addView(gv);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gv.setLayoutParams(params);
        gv.setNumColumns(4);
        gv.setVerticalSpacing(20);

        Icon icon1 = new Icon(getString(R.string.icon_list_chinese_story), R.drawable.icon_list_chinese_story);
        Icon icon2 = new Icon(getString(R.string.icon_list_english_story), R.drawable.icon_list_english_story);
        Icon icon3 = new Icon(getString(R.string.icon_list_picturebook_story), R.drawable.icon_list_picturebook_story);
        Icon icon4 = new Icon(getString(R.string.icon_list_chinese_song), R.drawable.icon_list_chinese_song);
        Icon icon5 = new Icon(getString(R.string.icon_list_english_childsong), R.drawable.icon_list_english_childsong);
        Icon icon6 = new Icon(getString(R.string.icon_list_tradition_culture), R.drawable.icon_list_tradition_culture);
        Icon icon7 = new Icon(getString(R.string.icon_list_music), R.drawable.icon_list_music);
        Icon icon8 = new Icon(getString(R.string.icon_list_riddle), R.drawable.icon_list_riddle);
        iconList = new ArrayList<>();
        iconList.add(icon1);
        iconList.add(icon2);
        iconList.add(icon3);
        iconList.add(icon4);
        iconList.add(icon5);
        iconList.add(icon6);
        iconList.add(icon7);
        iconList.add(icon8);
        gvAdater = new IconGvAdater(iconList, getBaseContext());
        gv.setAdapter(gvAdater);
        vpList.add(relativeLayout);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        musicIntent(getString(R.string.icon_list_chinese_story));//汉语故事界面
                        break;
                    case 1:
                        musicIntent(getString(R.string.icon_list_english_story));//英语故事界面
                        break;
                    case 2:
                        musicIntent(getString(R.string.icon_list_picturebook_story));//绘本故事界面
                        break;
                    case 3:
                        musicIntent(getString(R.string.icon_list_chinese_song));//中文儿歌界面
                        break;
                    case 4:
                        musicIntent(getString(R.string.icon_list_english_childsong));//英文儿歌界面
                        break;
                    case 5:
                        musicIntent(getString(R.string.icon_list_tradition_culture));//传统文化界面
                        break;
                    case 6:
                        musicIntent(getString(R.string.icon_list_music));//轻音乐界面
                        break;
                    default:
                        musicIntent(getString(R.string.icon_list_riddle));//谜语界面
                        break;
                }
            }
        });
    }

    private void initGV4() {
        RelativeLayout relativeLayout = new RelativeLayout(getBaseContext());
        GridView gv = new GridView(getBaseContext());
        relativeLayout.addView(gv);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gv.setLayoutParams(params);
        gv.setNumColumns(4);
        gv.setVerticalSpacing(20);
        Icon icon1 = new Icon(getString(R.string.icon_list_setting_app), R.drawable.icon_list_setting_app);
        Icon icon2 = new Icon(getString(R.string.icon_list_setting_system), R.drawable.icon_list_setting_system);
        Icon icon3 = new Icon(getString(R.string.icon_list_message_remind), R.drawable.icon_list_message_remind);
        Icon icon4 = new Icon(getString(R.string.icon_list_smart_remind), R.drawable.icon_list_smart_remind);
        Icon icon5 = new Icon(getString(R.string.icon_list_other_app), R.drawable.icon_list_other_app);

        iconList = new ArrayList<>();
        iconList.add(icon1);
        iconList.add(icon2);
        iconList.add(icon3);
        iconList.add(icon4);
        iconList.add(icon5);

        gvAdater = new IconGvAdater(iconList, getBaseContext());
        gv.setAdapter(gvAdater);
        vpList.add(relativeLayout);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        startActivity(new Intent(getBaseContext(), Activity_otherApp.class));//其他应用
                        break;


                }
            }
        });
    }

    //初始化viewPager
    private void initVp() {
        vpAdater = new Vp_Adater(vpList, getBaseContext());
        vp.setAdapter(vpAdater);
        vp.setOffscreenPageLimit(4);
        initPoint();
        vp.addOnPageChangeListener(this);
    }

    //创建底部翻页点
    private void initPoint() {
        if (vpList != null) {
            for (int i = 0; i < vpList.size(); i++) {
                ImageView view = new ImageView(getBaseContext());
                view.setImageResource(R.drawable.icon_point_bg);
                llContent.addView(view);
            }
        }

    }

    private void initView() {
        vp = (ViewPager) findViewById(R.id.vp_content);
        llContent = (LinearLayout) findViewById(R.id.ll_bot);
        ivBot = (ImageView) findViewById(R.id.iv_bot);
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        left = (int) ((position + positionOffset) * DisplayUtils.dip2px(getBaseContext(),32));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                , RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = left;
        ivBot.setLayoutParams(params);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
