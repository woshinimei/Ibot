package com.infomax.ibotncloudplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.MGridViewAdapter.OnRequestImageListener;
import com.infomax.ibotncloudplayer.utils.MyLog;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ListViewGroupAdapter extends BaseAdapter implements OnScrollListener {

    private static final String TAG = "ListViewGroupAdapter";
    private Context mContext = null;
    private LayoutInflater myInflater;
    private ArrayList<DayMediaGroup> mGroupList = null;
    private ContentResolver mContentResolver = null;
    private LruCache<String, Bitmap> mBmpCache;
    private Set<BitmapWorkerTask> mBmpWorkerTasks = new HashSet<BitmapWorkerTask>();
    /**
     * 默认drawable
     */
    private Drawable mDefaultDrawable;
    private boolean mUseMiniThumbnail = true;
    private boolean isFirstEnter = true;
    private int mFirstVisibleItem = 0;
    private int mVisibleItemCount = 0;
    private ListView mListView = null;
    private final int MAX_PRELOAD_ITEMS = 16;
    private boolean mEditMode = false;
    private Bitmap mNoThumbnailVideo = null;
    private Bitmap mNoThumbnailPhoto = null;

    /**
     * 为外部留的滚动接口监听
     */
    private OnScrollListener mOnScrollListener;

    private OnRequestImageListener mOnRequestImageListener = new OnRequestImageListener() {

        @Override
        public void onRequestImage(String imgPath, ImageView iv, long groupDate) {

            Bitmap bmp = getBmpFromCache(imgPath);

            MyLog.d(TAG, TAG + ">>onRequestImage>>imgPath:" + imgPath
                    + ",groupDate:" + groupDate
                    + ",bmp:" + bmp
            );
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            }
            /*else
                iv.setImageDrawable(mDefaultDrawable);	*/
        }

        @Override
        public void onChecked(String imgPath, long groupDate, boolean bChecked) {

            MyLog.e(TAG, "onChecked(*)-->>>>>>>>>>>imgPath:" + imgPath);
            DayMediaItem item = getSubItem(groupDate, imgPath);
            if (item != null) {
                item.setChecked(bChecked);
            }
        }
    };

    public ListViewGroupAdapter(Context c, ListView lv, ArrayList<DayMediaGroup> groupList) {
        mContext = c;
        mListView = lv;
        this.mGroupList = groupList;
        myInflater = LayoutInflater.from(c);
        mContentResolver = c.getContentResolver();
        mDefaultDrawable = mContext.getResources().getDrawable(R.drawable.media_item_frame);
        mNoThumbnailVideo = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.no_thumbnail_video)).getBitmap();
        mNoThumbnailPhoto = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.no_thumbnail_photo)).getBitmap();

        //Find out maximum memory available to application
        //1024 is used because LruCache constructor takes int in kilobytes
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/x th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;
        Log.d(TAG, "max memory " + maxMemory + " cache size " + cacheSize);

        // LruCache takes key-value pair in constructor
        // key is the string to refer bitmap
        // value is the stored bitmap
        mBmpCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes
                return bitmap.getByteCount() / 1024;
            }
        };

        mListView.setOnScrollListener(this);
    }

    public void setItems(ArrayList<DayMediaGroup> groupList) {
        mGroupList = groupList;
        MyLog.d(TAG, TAG + ">>setItems>>>>mGroupList:" + (mGroupList == null ? "null" : mGroupList.size()));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mGroupList != null) {
            return mGroupList.size();
        }

        return 0;
    }

    public DayMediaItem getSubItem(long groupDate, String imgPath) {
        if (mGroupList != null) {
            for (DayMediaGroup dg : mGroupList) {
                for (DayMediaItem item : dg.getItemList()) {
                    if (item.getPath().equals(imgPath)) {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Object getItem(int position) {
        if (mGroupList != null && position < getCount()) {
            return mGroupList.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private String getDateString(long lDatetime) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(new Date(lDatetime));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.listview_group_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            DayMediaGroup dg = (DayMediaGroup) getItem(position);
            long duration = dg.getDate();
            Date date = new Date(duration);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            holder.tvDate1.setText(day + "");
            holder.tvDate2.setText(year + "." + month);
            MGridViewAdapter adp = new MGridViewAdapter(mContext, mGroupList, dg, holder.mGridView, mOnRequestImageListener);
            holder.mGridView.setAdapter(adp);
            adp.setEditMode(mEditMode);
            holder.mGridView.setTag(dg.getDate());
            //GridViewUtils.updateGridViewLayoutParams(holder.mGridView, MediaManager.THUMBNAILS_COLUMNS);
        }

        return convertView;
    }

    public void releaseAll() {
        cancelAllTasks();
        mBmpCache.evictAll();
    }

    /**
     *
     */
    private void cancelAllTasks() {
        for (BitmapWorkerTask task : mBmpWorkerTasks) {
            task.cancel(false);    //与	task.cancel(true)一样
        }
    }

    /**
     * @param key
     * @param bitmap
     */
    public void addBmpToCache(String key, Bitmap bitmap) {
        if (getBmpFromCache(key) == null) {
            mBmpCache.put(key, bitmap);
        }
    }

    public Bitmap getBmpFromCache(String key) {
        if (mBmpCache != null) {

            return mBmpCache.get(key);
        }
        return null;
    }

    public String readableFileSize(long size) {
        if (size <= 0) return "0 MB";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * @param path
     * @param groupDate
     * @param bmp
     */
    private void setImageViewByData(String path, long groupDate, Bitmap bmp) {
        MyLog.d(TAG, TAG + ">>setImageViewByData>>path:" + path + ",groupDate:" + groupDate + ",getHeight:" + bmp);
        if (bmp == null || path == null) {

            return;
        }

        MGridView gv = (MGridView) mListView.findViewWithTag(groupDate);
        if (gv != null) {
            ImageView iv = (ImageView) gv.findViewWithTag(path);
            if (iv != null) {
                iv.setImageBitmap(bmp);
            }
        }
    }

    private class BitmapWorkerTask extends AsyncTaskEx<String, Void, Bitmap> {

        private String mPath;
        private int mType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
        private long mGroupDate = 0;

        public void setData(int type, long groupDate) {
            mType = type;
            mGroupDate = groupDate;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            mPath = params[0];

            MyLog.d(TAG, TAG + ">>doInBackground>>mPath:" + mPath);

            Bitmap bmp = getMediaThumbnail(mPath);
            if (bmp != null) {
                addBmpToCache(mPath, bmp);
            }

            return bmp;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPostExecute(bmp);
            MyLog.d(TAG, TAG + ">>onPostExecute>>bmp.getHeight:" + bmp.getHeight()
                    + ",mPath:" + mPath
            );
            setImageViewByData(mPath, mGroupDate, bmp);

            mBmpWorkerTasks.remove(this);
        }

        private Bitmap getMediaThumbnail(String imageUrl) {
            MyLog.d(TAG, TAG + ">>>>getMediaThumbnail()>>>imageUrl:" + imageUrl);
            Bitmap bmp = null;
            Cursor ca = null;

            if (mType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                ca = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{imageUrl}, null);
            } else if (mType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                ca = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{imageUrl}, null);
            }

            if (ca != null) {
                if (ca.moveToFirst()) {
                    int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    if (mType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                        if (mUseMiniThumbnail)
                            bmp = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                        else
                            bmp = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
                    } else if (mType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                        if (mUseMiniThumbnail)
                            bmp = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, options);
                        else
                            bmp = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
                    }

                    if (bmp == null) {
                        Log.d(TAG, "run getMediaThumbnail = null, path:" + imageUrl);
                        if (mType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO)
                            bmp = mNoThumbnailVideo;
                        else if (mType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO)
                            bmp = mNoThumbnailPhoto;
                    }
                }
                ca.close();
            }

            return bmp;
        }
    }

    static class ViewHolder {
        MGridView mGridView;
        TextView tvDate1;
        TextView tvDate2;

        public ViewHolder(View view) {
            tvDate1 = (TextView) view.findViewById(R.id.tv_date1);
            tvDate2 = (TextView) view.findViewById(R.id.tv_date2);
            mGridView = (MGridView) view.findViewById(R.id.gv_group);
        }
    }

    /**
     * @param firstVisibleItem
     * @param visibleItemCount
     */
    public void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        MyLog.d(TAG, TAG + ">>loadBitmaps>>");
        if (mGroupList != null) {
            int count = 0;
            boolean bMax = false;

            // current group is first priority.
            DayMediaGroup dg1 = mGroupList.get(firstVisibleItem);
            MGridView gv1 = (MGridView) mListView.findViewWithTag(dg1.getDate());
            if (gv1 != null) {
                ArrayList<DayMediaItem> items1 = dg1.getItemList();
                for (int i = 0; i < items1.size(); i++) {
                    if (count > MAX_PRELOAD_ITEMS) {
                        bMax = true;
                        break;
                    }

                    ImageView iv = (ImageView) gv1.findViewWithTag(items1.get(i).getPath());
                    MyLog.d(TAG, TAG + ">>loadBitmaps>>iv:" + iv + ", path:" + items1.get(i).getPath());
                    if (iv != null) {
                        Drawable d = iv.getDrawable();
                        MyLog.d(TAG, TAG + ">>loadBitmaps>>>>Drawable:" + d);
                        if (d != null) {
                            MyLog.d(TAG, TAG + ">>loadBitmaps>>Drawable>>getMinimumHeight" + d.getMinimumHeight());
                            if (d == mDefaultDrawable) {
                                MyLog.d(TAG, TAG + ">>loadBitmaps>>>ImageView d == mDefaultDrawable, path:" + items1.get(i).getPath());
                                BitmapWorkerTask task = new BitmapWorkerTask();
                                task.setData(items1.get(i).getType(), dg1.getDate());
                                mBmpWorkerTasks.add(task);
                                task.execute(items1.get(i).getPath());
                                count += 1;
                            }
                        }
                    }
                }
            }

            for (int i = firstVisibleItem; i < mGroupList.size(); i++) {
                if (bMax) {
                    break;
                }
                MyLog.d(TAG, TAG + ">>loadBitmaps>>>>firstVisibleItem:" + firstVisibleItem);
                DayMediaGroup dg = mGroupList.get(i);
                ArrayList<DayMediaItem> items = dg.getItemList();
                MyLog.d(TAG, TAG + ">>loadBitmaps>>>>items.size:" + items.size());
                for (int j = 0; j < items.size(); j++) {
                    MyLog.d(TAG, TAG + ">>loadBitmaps>>>>count:" + count);
                    if (count > MAX_PRELOAD_ITEMS) {
                        bMax = true;
                        break;
                    }

                    DayMediaItem item = items.get(j);
                    Bitmap bmp = getBmpFromCache(item.getPath());
                    MyLog.d(TAG, TAG + ">>loadBitmaps>>>>item.getPath():" + item.getPath()
                            + ",bitmap:" + bmp
                    );
                    if (bmp == null) {
                        BitmapWorkerTask task = new BitmapWorkerTask();
                        task.setData(item.getType(), dg.getDate());
                        mBmpWorkerTasks.add(task);
                        task.execute(item.getPath());
                        count += 1;
                    } else {
                        setImageViewByData(item.getPath(), dg.getDate(), bmp);
                    }
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        MyLog.d(TAG, "-->>>>>>onScrollStateChanged-->getFirstVisiblePosition:" + view.getFirstVisiblePosition() +
                ", scrollState:" + scrollState
        );

        if (scrollState == SCROLL_STATE_IDLE) {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        } else {
            cancelAllTasks();
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;

        MyLog.d(TAG, "-->>>>>>onScroll-->firstVisibleItem:" + firstVisibleItem +
                ", visibleItemCount:" + visibleItemCount +
                ", totalItemCount:" + totalItemCount +
                ", isFirstEnter:" + isFirstEnter
        );
        if (isFirstEnter && visibleItemCount > 0) {
            MyLog.d(TAG, "-->>>>>>onScroll-->firstVisibleItem:" + firstVisibleItem +
                    "\n visibleItemCount:" + visibleItemCount +
                    "\n totalItemCount:" + totalItemCount);
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;

        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    /**
     * set edit mode
     *
     * @param isEdit
     */
    public void setEditMode(boolean isEdit) {
        mEditMode = isEdit;

        if (mGroupList != null) {
            for (DayMediaGroup dg : mGroupList) {
                if (!mEditMode) {
                    for (DayMediaItem item : dg.getItemList()) {
                        item.setChecked(false);
                    }
                }

                MGridView gv = (MGridView) mListView.findViewWithTag(dg.getDate());
                if (gv != null) {
                    if (gv.getAdapter() != null) {
                        ((MGridViewAdapter) gv.getAdapter()).setEditMode(mEditMode);
                    }
                }
            }
        }
    }

    public void setCheckAll() {
        if (mGroupList != null) {
            for (DayMediaGroup dg : mGroupList) {
                for (DayMediaItem item : dg.getItemList())
                    item.setChecked(true);

                MGridView gv = (MGridView) mListView.findViewWithTag(dg.getDate());
                Log.e("----", dg.getDate() + "");
                if (gv != null) {
                    if (gv.getAdapter() != null)
                        ((MGridViewAdapter) gv.getAdapter()).setCheckAll();
                }
            }
        }
    }

    public void deleteChecked() {
        if (mGroupList != null) {
            for (DayMediaGroup dg : mGroupList) {
                MGridView gv = (MGridView) mListView.findViewWithTag(dg.getDate());
                if (gv != null) {
                    if (gv.getAdapter() != null) {
                        ((MGridViewAdapter) gv.getAdapter()).deleteChecked();
                    }
                }
            }
        }
    }

    public void setCheckNone() {
        if (mGroupList != null) {
            for (DayMediaGroup dg : mGroupList) {
                MGridView gv = (MGridView) mListView.findViewWithTag(dg.getDate());
                Log.e("----", dg.getDate() + "");
                if (gv != null) {
                    if (gv.getAdapter() != null) {
                        ((MGridViewAdapter) gv.getAdapter()).setCheckNone();
                    }
                }
            }
        }
    }

    public void setOnScrollListener(OnScrollListener scrollListener) {
        this.mOnScrollListener = scrollListener;

    }
}
