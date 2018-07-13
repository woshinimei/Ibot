package com.infomax.ibotncloudplayer.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.infomax.ibotncloudplayer.ListViewGroupAdapter;
import com.infomax.ibotncloudplayer.R;

/**
 * Created by Pang on 2017/6/7 0007.
 * 搭配listView的滑动块
 */

public class ScrollSlidingBlock extends RelativeLayout {
    private static final int ANI_ENTER = 1;
    private static final int ANI_EXIT = 2;
    private static final int UPDATE_MARGIN = 3;
    //多少秒后隐藏
    private static final long TIME_SCROLL_HIDE_DELAY = 2000;

    //自定义属性的默认值
    private static final int ATTR_DEF = -1;

    private ImageView mImageView;
    private int mMax;
    private int mProgress;
    private int mCurrentProgress;
    private int mCurrentY;
    private ListView mListView;
    private Animation aniEnter;

    //是否正在播放进入动画
    private boolean isAniEnter;

    private Animation aniExit;

    //是否正在播放退出动画
    private boolean isAniExit;

    //listView可滚动的总高度
    private int mListViewHeight;

    //正在滚动
    private boolean isScroll;

    //储存所有Item高度
    private SparseArray itemHeightArray = new SparseArray(0);

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ANI_ENTER:
                    if (aniEnter != null && !isAniEnter && getVisibility() == GONE) {
                        startAnimation(aniEnter);
                    }
                    break;
                case ANI_EXIT:
                    if (!isScroll && aniExit != null && !isAniExit && getVisibility() == VISIBLE) {
                        startAnimation(aniExit);
                    }
                    break;
                case UPDATE_MARGIN:
                    int dy = (int) message.obj;
                    setImageViewMargin(dy);
                    break;
            }
            return false;
        }
    });

    public ScrollSlidingBlock(Context context) {
        this(context, null);
    }

    public ScrollSlidingBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollSlidingBlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollSlidingBlock);
        int imgWidth = typedArray.getDimensionPixelOffset(R.styleable.ScrollSlidingBlock_imgWidth, ATTR_DEF);
        if (ATTR_DEF == imgWidth) {
            imgWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        int imgHeigh = typedArray.getDimensionPixelOffset(R.styleable.ScrollSlidingBlock_imgHeight, ATTR_DEF);
        if (ATTR_DEF == imgHeigh) {
            imgHeigh = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        int imgRes = typedArray.getResourceId(R.styleable.ScrollSlidingBlock_imgRes, R.drawable.media_scrollbar_thumb);
        mImageView = new ImageView(context);
        RelativeLayout.LayoutParams imgParams = new LayoutParams(imgWidth, imgHeigh);
        mImageView.setLayoutParams(imgParams);
        mImageView.setImageResource(imgRes);
        addView(mImageView);
        int padding = getResources().getDimensionPixelOffset(R.dimen.dp_5);
        setPadding(0, 0, 0, 0);
        /*aniEnter = new TranslateAnimation(1,Animation.RELATIVE_TO_SELF,
                0,Animation.ABSOLUTE,
                1,Animation.RELATIVE_TO_SELF,
                1,Animation.RELATIVE_TO_SELF);*/
//        aniEnter = new TranslateAnimation(-mImageView.getWidth(),0,getHeight(),getHeight());
//        aniEnter = new AlphaAnimation(DisplayUtils.dip2px(context,20),0,DisplayUtils.dip2px(context,20),DisplayUtils.dip2px(context,20));
        aniEnter = new AlphaAnimation(0, 1);
        aniEnter.setDuration(50);
        aniEnter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAniEnter = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAniEnter = false;
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

       /* aniExit = new TranslateAnimation(0,Animation.RELATIVE_TO_SELF,
                1,Animation.RELATIVE_TO_SELF,
                1,Animation.RELATIVE_TO_SELF,
                1,Animation.RELATIVE_TO_SELF);*/
        aniExit = new AlphaAnimation(1, 0);
        aniExit.setDuration(50);
        aniExit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAniExit = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAniExit = false;
                setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mHandler.removeMessages(ANI_EXIT);
                isScroll = true;
                if (mImageView != null) {
                    int mY = (int) event.getY();
                    setImageViewMargin(mY);
//                    mHandler.obtainMessage(UPDATE_MARGIN,mY).sendToTarget();//onTouchEvent
                    mProgress = (int) (mY * mMax * 1.0 / getHeight());
                    updateListView();
                }
                break;
            case MotionEvent.ACTION_UP:
                isScroll = false;
                mHandler.sendEmptyMessageDelayed(ANI_EXIT, TIME_SCROLL_HIDE_DELAY);
                break;
        }
        return true;
    }

    /**
     * * 更新listView
     */
    private void updateListView() {
        if (mCurrentProgress != 0 && mCurrentProgress == mProgress) {
            return;
        }
        if (mListView != null && mMax != 0) {
//            mListView.smoothScrollToPositionFromTop();
//            mListView.smoothScrollToPosition(mProgress);
            mListView.setSelection(mProgress);
            mCurrentProgress = mProgress;
        }
    }

    private void setImageViewMargin(int dY) {
        Log.i("phc", "dY:" + dY);
        if (dY == mCurrentY)
            return;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        int top = 0;
        if (dY > mImageView.getHeight() / 2 && dY < getHeight() - mImageView.getHeight()) {
            top = dY - (mImageView.getHeight() / 2);
        } else if (dY >= getHeight() - mImageView.getHeight()) {
            top = getHeight() - mImageView.getHeight();
        }

        layoutParams.topMargin = top;
        mImageView.setLayoutParams(layoutParams);
        mCurrentY = dY;
    }

    /**
     * 适配listView 必须项
     *
     * @param listView
     */
    public void setListView(final ListView listView) {
        mListView = listView;
        getTotalHeightofListView(listView);
        if (mListView != null) {
            mMax = mListView.getAdapter().getCount();
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
//                    Log.i("phc","i"+i);
                    if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        isScroll = false;
                        mHandler.sendEmptyMessageDelayed(ANI_EXIT, TIME_SCROLL_HIDE_DELAY);
                    } else {
                        isScroll = true;
                        mHandler.removeMessages(ANI_EXIT);
                        mHandler.sendEmptyMessage(ANI_ENTER);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                    int dY = (int) (firstVisibleItem*1.0/mMax*(getHeight()-mImageView.getHeight()));
                    //dy/滑动条长度 = listView滚动高度/listView所有item高度和
                    int dY = (int) (getListViewScorllY() * 1.0 / (mListViewHeight - mListView.getHeight()) //需要减掉listVeiw的高度 不然滑块滑不到底部
                            * (getHeight()));
//                    Log.i("phc", "onScroll:dY:" + dY + "  getListViewScorllY：" + getListViewScorllY() + " ");
                    mHandler.obtainMessage(UPDATE_MARGIN, dY).sendToTarget();
                    View view = absListView.getChildAt(0);
                    if (view == null)
                        return;
                    if (itemHeightArray.get(firstVisibleItem) == null)
                        itemHeightArray.append(firstVisibleItem, view.getHeight());
                }


            });
        }
    }


    /**
     * 获取listView滚动的高度
     *
     * @return
     */
    private int getListViewScorllY() {
        View view = mListView.getChildAt(0);
        if (view == null) {
            return 0;
        }
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int height = 0;
        for (int i = 0; i < firstVisiblePosition; i++) {
            if (itemHeightArray.get(i) != null) {
                height += ((Integer) itemHeightArray.get(i));
            }
        }
        int top = view.getTop();
//        Log.i("phc", "firstVisiblePosition:" + firstVisiblePosition + " top:" + top + " height:" + view.getHeight());
        return -top + height;
    }

    /**
     * 获取listView所有item的高度之和
     *
     * @param listView
     */
    public void getTotalHeightofListView(ListView listView) {
        ListAdapter mAdapter = (ListAdapter) listView.getAdapter();
        if (mAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            //mView.measure(0, 0);
            totalHeight += mView.getMeasuredHeight();
            //  Log.w("HEIGHT" + i, String.valueOf(totalHeight));
        }
        mListViewHeight = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount() - 1));

    }
}
