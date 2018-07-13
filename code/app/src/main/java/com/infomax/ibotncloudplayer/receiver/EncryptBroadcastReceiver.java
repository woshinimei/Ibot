package com.infomax.ibotncloudplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;
import com.infomax.ibotncloudplayer.utils.VideoEncryptUtils;

import java.io.File;

/**
 * Created by jack_zou on 2016/11/28.
 */

public class EncryptBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            ThreadUtils.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    String path = SharedPreferenceUtils.getVideoPatch(context);
                    Log.d("EncryptBroadcastReceiv", "************ path:" + path);
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (VideoEncryptUtils.obtainFileState(file) == 1) {
                            VideoEncryptUtils.EncryptVideoFile(file);
                            SharedPreferenceUtils.setVideoPath(context, "");
                        }
                    }
                }
            });
        }
    }
}
