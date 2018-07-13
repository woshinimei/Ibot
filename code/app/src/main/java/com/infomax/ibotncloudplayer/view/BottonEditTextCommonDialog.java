package com.infomax.ibotncloudplayer.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DisplayUtils;

/**
* @author jy
* 2016年12月23日上午10:33:50
* @des 该共用dialog带标题，带两个按钮,一个文本编辑框 。单独写这个类是为了全屏显示。DialogUtils工具类中的无法满足全屏。
*/
public class BottonEditTextCommonDialog extends Dialog {

    private final String TAG = BottonEditTextCommonDialog.class.getSimpleName();

    private Context context;

    private Button btn_left;
    private Button btn_right;
    private TextView tv_title;
    private EditText et_password;

    private View.OnClickListener mClickListener;

    public BottonEditTextCommonDialog(Context context) {
        super(context, R.style.Theme_MyDialog_Shape_Transparent_Fillet);
        this.context = context;

        setContentView(R.layout.dialog_with_btn_et);

        initViews();

//        // 设置对话框窗体属性
        setProperty(this.context);

    }

	private void initViews() {
		btn_left = (Button) findViewById(R.id.btn_left);
	    btn_right = (Button) findViewById(R.id.btn_right);
	    tv_title = (TextView) findViewById(R.id.tv_title);
        et_password = (EditText) findViewById(R.id.et_password);

    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		 hideNavigationBar();
	}
	/**
	 *此时只是隐藏导航功栏 ,因为activity配置了android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
	 */
	public void hideNavigationBar(){
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	private void registerListener() {

		if (mClickListener != null) {
			btn_right.setOnClickListener(mClickListener);
			btn_left.setOnClickListener(mClickListener);
		}
	}

	/**
	 * 供外部调用的点击监听方法
	 * @param onClickListener
	 */
	public void setOnClickListener(View.OnClickListener onClickListener){
		this.mClickListener = onClickListener;
		registerListener();
	}

    /**
     * 设置对话框窗体属性
     * @param context 上下文
     */
	public void setProperty(Context context) {

    	//是否可以取消
        setCancelable(Constant.Config.CANCEL_DIALOG_PROGRESS);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = DisplayUtils.getDisplayWidthHeight(context)[0] * 1 / 3;
        lp.height =  ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.alpha = 0.95f;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);

    }
	/**
	 * 设置标题
	 */
	public void setTitle(int resid){
		tv_title.setText(resid);
	}
    /**
     * 设置标题
     */
    public void setTitle(String res){
        tv_title.setText(res);
    }
    /**
     * 设置左边按钮内容
     */
    public void setTextBtnLeft(String res){
        btn_left.setText(res);
    }
    /**
     * 设置右边按钮内容
     */
    public void setTextBtnRight(String res){
        btn_right.setText(res);
    }

    public EditText getEditTextPassword(){
        return et_password;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);
    }

    public void showDialog(BottonEditTextCommonDialog dialog) {
        if (null != dialog && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismissDialog(BottonEditTextCommonDialog dialog) {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//
                }
            });
        }
    }

}