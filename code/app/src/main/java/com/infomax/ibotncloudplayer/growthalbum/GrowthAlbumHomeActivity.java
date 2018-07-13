package com.infomax.ibotncloudplayer.growthalbum;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.MediaManager;
import com.infomax.ibotncloudplayer.MultimediaListActivity;
import com.infomax.ibotncloudplayer.R;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/22.
 * @Function:
 */

public class GrowthAlbumHomeActivity extends Activity implements View.OnClickListener {

    private TextView netAlbumBtn;
    private TextView localAlbumBtn;
    private LinearLayout headerBar;
    private View mDecorView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_growthalbum_home);
        mDecorView = getWindow().getDecorView();
        netAlbumBtn = (TextView) findViewById(R.id.net_album_btn);
        localAlbumBtn = (TextView) findViewById(R.id.local_album_btn);
        headerBar = (LinearLayout) findViewById(R.id.header_bar);
        netAlbumBtn.setOnClickListener(this);
        localAlbumBtn.setOnClickListener(this);
        headerBar.setOnClickListener(this);
    }

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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.net_album_btn:
                startActivity(new Intent(GrowthAlbumHomeActivity.this, AlbumListActivity.class));
                break;
            case R.id.local_album_btn:
                openLocalMedia(false);
                break;
            case R.id.header_bar:
                finish();
                break;
        }
    }

    private void openLocalMedia(boolean isVideo) {
        Intent intent = new Intent(GrowthAlbumHomeActivity.this, MultimediaListActivity.class);
        if (isVideo) {
            intent.putExtra(MediaManager.KEY_MEDIA_TYPE, MediaManager.MEDIA_TYPE_LOCAL_VIDEO);
        } else {
            intent.putExtra(MediaManager.KEY_MEDIA_TYPE, MediaManager.MEDIA_TYPE_LOCAL_PHOTO);
        }

        startActivity(intent);
    }
}
