package com.infomax.ibotncloudplayer.activity.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.infomax.ibotncloudplayer.FullScreenActivity;
import com.infomax.ibotncloudplayer.R;

/**
 * Created by jy on 2017/3/3 ;10:06.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description: 对Activity类进行扩展
 */
public abstract  class BaseActivity extends FullScreenActivity {

    /**
     * 全局的Context {@link #mContext = this.getApplicationContext();}
     */
    protected Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if (layoutId != 0) {
            setContentView(layoutId);
            ////////////////////
//            // 删除窗口背景
//            getWindow().setBackgroundDrawable(null);
            ////////////////////
        }

        mContext = this.getApplicationContext();

        initViews();

        initData();

        registerLinstener();

    }
    /**
     * 布局文件ID
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化组件
     */
    protected abstract void initViews();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 注册监听
     */
    protected  void registerLinstener(){

    }

    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /**
     * 含有Bundle通过Class跳转界面,带有右进右出动画
     **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
//        overridePendingTransition(R.anim.zoom_in, 0);
    }

    /**
     * 通过Action跳转界面
     **/
    public void startActivity(String action) {
        startActivity(action, null);
    }

    /**
     * 含有Bundle通过Action跳转界面，带有右进右出动画
     **/
    public void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_in, 0);
    }

    /**
     * 含有Bundle通过Class打开编辑界面，带有右进右出动画
     **/
    public void startActivityForResult(Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.zoom_in, 0);
    }


    /**
     * 带有右进右出动画的退出
     */
    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(0, R.anim.zoom_out);
    }

    /**
     * 默认退出
     */
    public void defaultFinish() {
        super.finish();
    }

}
