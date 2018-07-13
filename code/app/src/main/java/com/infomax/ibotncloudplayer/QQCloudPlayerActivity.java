package com.infomax.ibotncloudplayer;

import java.util.ArrayList;
import java.util.List;

import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.tencent.qcload.playersdk.ui.PlayerActionInterface;
import com.tencent.qcload.playersdk.ui.TitleMenu;
import com.tencent.qcload.playersdk.ui.VideoRootFrame;
import com.tencent.qcload.playersdk.util.PlayerListener;
import com.tencent.qcload.playersdk.util.VideoInfo;
import com.ysx.qqcloud.QQCloudFileInfo;
import com.ysx.qqcloud.QQCloudObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QQCloudPlayerActivity extends Activity {
	private static final String TAG = QQCloudPlayerActivity.class.getSimpleName();
	private QQCloudObject qqObject; 	
	private QQCloudFileInfo info = null;
	
	private VideoRootFrame player = null;
	private int SelectIndex = -1;
	private AudioManager am = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_qqcloud_player);  
		qqObject = QQCloudObject.sharedInstance();

		///////////
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
		MyLog.d(TAG, ">>>>>>>>>testRequestAudioFocus()>>>result:" + result);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
			initPlayer();
		}
		//////////

		//////////
		//停止语音
		sendBroadcast(new Intent(Constant.MyBroadCast.ACTION_STOP_IFLYTEK_VOICE));
		/////////
	}

	AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			MyLog.d(TAG, ">>>>>>>>>onAudioFocusChange()>>>focusChange:" + focusChange);
			if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
			{
				if (player != null)
				{
					player.play();
				}

			}else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
			{
				/*if (player != null)
				{
					player.release();
				}*/
				finish();
			}else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
			{
				/*if (player != null)
				{
					player.pause();
				}*/
				finish();
			}
		}
	};
	private void testRequestAudioFocus(final Intent intent){

		int result = am.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
		MyLog.d(TAG, ">>>>>>>>>testRequestAudioFocus()>>>result:" + result);
//		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
//			////////获取音频焦点后再播放
//		}

	}

	private void initPlayer() 
	{		
		player = (VideoRootFrame) findViewById(R.id.player);
		
		FrameLayout flroot = (FrameLayout) player.findViewById(R.id.root);

		MyLog.d(TAG,"--->>>>initPlayer--flroot:");

		if(flroot != null)
		{
			LinearLayout bar = (LinearLayout) flroot.findViewById(R.id.qcloud_player_settings_container);
       		if(bar != null)
       		{
       			TextView tvExit = (TextView) bar.findViewById(R.id.tv_exit);
       			if(tvExit != null)
       			{
       				tvExit.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							finish();							
						}
					});
       			}
       		}        		
		} 
		
        List<TitleMenu> videoTitleMenus=new ArrayList<TitleMenu>();
        TitleMenu icon1=new TitleMenu();
        icon1.iconId=R.drawable.ic_share;
        icon1.action=new PlayerActionInterface(){
            @Override
            public void action() {
                Toast.makeText(getApplicationContext(),"share icon taped",Toast.LENGTH_SHORT).show();
            }
        };
        videoTitleMenus.add(icon1);
        TitleMenu icon2=new TitleMenu();
        icon2.iconId=R.drawable.ic_favorite;
        videoTitleMenus.add(icon2);
        TitleMenu icon3=new TitleMenu();
        icon3.iconId=R.drawable.ic_perm_identity;
        videoTitleMenus.add(icon3);
        player.setMenu(videoTitleMenus);
        
        player.setListener(new PlayerListener(){

			@Override
			public void onError(Exception arg0) {
				arg0.printStackTrace();
				
			}

			@Override
			public void onStateChanged(int arg0) {
				Log.d(TAG, "player states:" + arg0);
//				am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			}        	
        });

        List<VideoInfo> videos=new ArrayList<VideoInfo>();
        /*
         	格式， 0: ["", "原始"], 1: ["带水印", "原始"], 10: ["手机", "mp4"], 
         	20: ["标清", "mp4"],30: ["高清", "mp4"], 
         	110: ["手机", "flv"], 120: ["标清", "flv"], 130: ["高清", "flv"] 
         * */
        SelectIndex = getIntent().getIntExtra("videoIndex", -1);

		MyLog.d(TAG,"SelectIndex--->>>:"+SelectIndex);
		VideoInfo videoInfo = null;
		if (SelectIndex == -1) {        //历史记录播放时

//			Bundle myBundle = getIntent().getBundleExtra(Constant.MyIntentProperties.NAME_KEY_01);
			Bundle myBundle = getIntent().getExtras();
			if (myBundle != null)
			{
				info = (QQCloudFileInfo) myBundle.getSerializable("currentInfo");
				MyLog.d(TAG,"info--->>>:"+info.fileName);
			}

		}else
		{   //非历史记录播放时
			info = qqObject.getFileInfo(SelectIndex);
		}

		if (info != null){
			ToastUtils.showToastDebug(this, "info:" + info.fileName);
			String videoURL20 = info.videoURL20;
			String videoURL30 = info.videoURL30;

			if (!videoURL20.isEmpty()) {
				videoInfo = new VideoInfo();
				videoInfo.description = getString(R.string.video_sd);
				videoInfo.type=VideoInfo.VideoType.MP4;
				videoInfo.url=videoURL20;
				videos.add(videoInfo);
				videoInfo = null;
				Log.d(TAG, "Add video " + videoURL20);
			}
			if (!videoURL30.isEmpty()) {
				videoInfo = new VideoInfo();
				videoInfo.description = getString(R.string.video_hd);
				videoInfo.type=VideoInfo.VideoType.MP4;
				videoInfo.url=videoURL30;
				videos.add(videoInfo);
				videoInfo = null;
				Log.d(TAG, "Add video " + videoURL30);
			}
	    /*if (!videoURL0.isEmpty()) {
	        videoInfo = new VideoInfo();
	        videoInfo.description = getString(R.string.video_ori);
	        videoInfo.type=VideoInfo.VideoType.MP4;
	        videoInfo.url=videoURL0;
	        videos.add(videoInfo);
	        videoInfo = null;
	        Log.d(TAG, "Add video " + videoURL0);
	    }*/

			player.play(videos);
		}else {

		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		MyLog.d(TAG, "---ibotncloudplayer-QQCloudPlayerActivity-->>>>onDestroy():");

		////////
//		player.setTop(0);
		///////

		if (player != null)
		{
			player.release();
		}

		if (am != null)
		{
			am.abandonAudioFocus(audioFocusChangeListener);
		}

		//////////开启语音
		sendBroadcast(new Intent(Constant.MyBroadCast.ACTION_START_IFLYTEK_VOICE));
		/////////
	}
}
