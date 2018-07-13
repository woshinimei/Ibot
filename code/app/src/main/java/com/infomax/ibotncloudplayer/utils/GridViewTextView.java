package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自定义textview实现在GridView实现跑马灯的效果，复写view里面的isFocused()方法，默认情况下是不会有效果的，
 * 而且gridview也不可点击
 *
 * @author dennis
 */
public class GridViewTextView extends TextView {

    public GridViewTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public GridViewTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}

