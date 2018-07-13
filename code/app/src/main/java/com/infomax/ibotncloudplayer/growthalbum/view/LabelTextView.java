package com.infomax.ibotncloudplayer.growthalbum.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.infomax.ibotncloudplayer.growthalbum.utils.OtherUtils;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/8.
 * @Function:
 */

public class LabelTextView extends android.support.v7.widget.AppCompatTextView {

    Context mContext;
    int mWidth;
    public LabelTextView(Context context) {
        this(context,null);
    }

    public LabelTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        int screenWidth = OtherUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - OtherUtils.dip2px(mContext, 12))/5;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, heightMeasureSpec);
    }

}
