package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by jy on 2017/6/7.
  语音相关控制类
 */

public class TtsControl {
    /**
     * intent，语音tts请求
     */
    public  static final String TTS_EXT_REQUEST = "com.ibotn.ibotnvoice.TTS_EXT_REQUEST";
    /**
     * 播放语音接口
     * @param content  语音内容
     */
    public static void startTtsSpeaker(Context context, String content) {
        if (!TextUtils.isEmpty(content)) {
            Intent intent = new Intent(TTS_EXT_REQUEST);
            intent.putExtra("content", content);
            context.sendBroadcast(intent);
        }
    }
}
