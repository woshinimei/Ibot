package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.infomax.ibotncloudplayer.MyApplication;
import com.infomax.ibotncloudplayer.R;

/**
 * Created by jy 2016/10/21.
 */
public class ToastUtils {
    private static Toast mToast;
    private static Toast mCustomToast;
    private static TextView tv_toast_content;
    /**显示的toast */
    public static void showToast(Context context, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), resId, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        }else {
            mToast.setText(resId);
        }
        mToast.show();
    }
    /**显示的toast */
    public static void showToast(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), msg + "", Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER,0,0);
        }else {
            mToast.setText("" + msg);
        }
        mToast.show();
    }
    /** 需要仅仅是debug = true 时显示的toast */
    public static void showToastDebug(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), "" + msg, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER,0,0);
        }
        if (Constant.Config.DEBUG)
        {
            mToast.setText("" + msg);
            mToast.show();
        }
    }
    /**显示的自定义toast
     * context 已使用MyApplication.getInstance()，可以传递null
     **/
    public static void showCustomToast(Context context, String msg) {
        if (mCustomToast == null) {
            mCustomToast = new  Toast(MyApplication.getInstance());
            View view = View.inflate(MyApplication.getInstance(), R.layout.toast_custom_layout,null);
            tv_toast_content = (TextView)view.findViewById(R.id.tv_toast_content);
            mCustomToast.setView(view);
            mCustomToast.setDuration(Toast.LENGTH_LONG);
            mCustomToast.setGravity(Gravity.CENTER,0,0);
        }
        if (tv_toast_content != null){
            tv_toast_content.setText("" + msg);
        }

        mCustomToast.show();
    }
    /**显示的自定义toast
     * context 已使用MyApplication.getInstance()
     **/
    public static void showCustomToast(String msg) {
        if (mCustomToast == null) {
            mCustomToast = new  Toast(MyApplication.getInstance());
            View view = View.inflate(MyApplication.getInstance(), R.layout.toast_custom_layout,null);
            tv_toast_content = (TextView)view.findViewById(R.id.tv_toast_content);
            mCustomToast.setView(view);
            mCustomToast.setDuration(Toast.LENGTH_LONG);
            mCustomToast.setGravity(Gravity.CENTER,0,0);
        }
        if (tv_toast_content != null){
            tv_toast_content.setText("" + msg);
        }

        mCustomToast.show();
    }
}
