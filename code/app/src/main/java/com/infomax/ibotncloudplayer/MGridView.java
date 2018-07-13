package com.infomax.ibotncloudplayer;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.GridView;

/**
 *
 */
public class MGridView extends GridView {

	public boolean mHasScrollBar = true;	
	
    public MGridView(Context context) {
    	super(context, null);
    }

    public MGridView(Context context, AttributeSet attrs) {
    	super(context, attrs, 0);
    }

    public MGridView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }
    
    public void setHaveScrollBar(boolean bHave)
    {
    	mHasScrollBar = bHave;
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = heightMeasureSpec;  
        if (mHasScrollBar) {  
            expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,  
                    MeasureSpec.AT_MOST);  
            super.onMeasure(widthMeasureSpec, expandSpec); 
        } else {  
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        }     
	}
}
