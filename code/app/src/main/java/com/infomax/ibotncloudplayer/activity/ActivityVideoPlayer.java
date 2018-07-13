package com.infomax.ibotncloudplayer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;

public class ActivityVideoPlayer extends FullScreenActivity implements View.OnClickListener {
    VideoView mvideoView;
    String path = "";
    TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        initView();
        path = getIntent().getStringExtra("path");
        initData();
    }

    private void initData() {
        //设置视频的来源
        Log.e("---path----", path + "");

        mvideoView.setVideoPath(path);
        //实列化媒体控制器
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(mvideoView);
        mvideoView.setMediaController(mediaController);
        mvideoView.start();
    }

    private void initView() {
        mvideoView = (VideoView) findViewById(R.id.vd_player);
        tvBack = (TextView) findViewById(R.id.tv_back);
        tvBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
