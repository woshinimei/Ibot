package com.infomax.ibotncloudplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmDialog extends AlertDialog{
	
	View.OnClickListener mCallbackOnClickListener = null;
	private int mMessageID;	
	
	public ConfirmDialog(Context context,
			View.OnClickListener onCallbackBtnClickListener, int messageID) {
        super(context);
        
        mCallbackOnClickListener = onCallbackBtnClickListener;
        mMessageID = messageID;     
	}		
	
	@Override
	protected void onCreate(Bundle arg0) {
		
		super.onCreate(arg0);
		setContentView(R.layout.confirm_dialog);
		
		TextView tvMessage = (TextView) findViewById(R.id.tv_message);
		tvMessage.setText(this.getContext().getString(mMessageID));
	        
	    Button btnCancel = (Button) findViewById(R.id.btn_cancel);
	    Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
	    btnCancel.setOnClickListener(mOnClickListener);
	    btnConfirm.setOnClickListener(mOnClickListener);
	    btnCancel.setTag(mMessageID);
	    btnConfirm.setTag(mMessageID);
	    setCancelable(false);
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			dismiss();
			if(mCallbackOnClickListener != null){
				mCallbackOnClickListener.onClick(v);
			}
		}
	};
	
}
	
