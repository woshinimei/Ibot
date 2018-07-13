package com.infomax.ibotncloudplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.infomax.ibotncloudplayer.growthalbum.utils.OtherUtils;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.onedriver.AppOneDriver;
import com.wifidirect.WifiDirectConstant;
import com.xiaweizi.cornerslibrary.CornersProperty;
import com.xiaweizi.cornerslibrary.RoundCornersTransformation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MGridViewAdapter extends BaseAdapter {

    private static final String TAG = "MGridViewAdapter";
    private Context mContext = null;
    private LayoutInflater myInflater;
    ArrayList<DayMediaItem> mItems = null;
    private long mGroupDate = 0;
    private OnRequestImageListener mOnRequestImageListener = null;
    private GridView mGridView = null;
    private boolean mEditMode = false;
    private ArrayList<DayMediaGroup> mGroupList;
    private DayMediaGroup mDayMediaGroup;
    CornersProperty cornersProperty;
    RoundCornersTransformation transformation;

    public interface OnRequestImageListener {

        void onRequestImage(String imgPath, ImageView iv, long groupDate);

        void onChecked(String imgPath, long groupDate, boolean bChecked);
    }

    public MGridViewAdapter(Context c, ArrayList<DayMediaGroup> groupList, DayMediaGroup dg, GridView gv, OnRequestImageListener onRequestImageListener) {
        mContext = c;
        mDayMediaGroup = dg;
        mItems = dg.getItemList();
        mGroupDate = dg.getDate();
        myInflater = LayoutInflater.from(c);
        mOnRequestImageListener = onRequestImageListener;
        mGridView = gv;
        this.mGroupList = groupList;
        cornersProperty = new CornersProperty().setCornersRadius(OtherUtils.dip2px(mContext, 28));
        transformation = new RoundCornersTransformation(mContext, cornersProperty);
        mGridView.setOnItemClickListener(mOnItemClickListener);
        mGridView.setOnItemLongClickListener(mOnItemLongClickListener);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position < mItems.size()) {
                if (mEditMode) {
                    if (mOnRequestImageListener != null) {
                        boolean bChecked = mItems.get(position).getChecked();
                        mOnRequestImageListener.onChecked(mItems.get(position).getPath(), mGroupDate, !bChecked);
                    }

                    notifyDataSetChanged();
                    MyLog.d(TAG, "mOnItemClickListener-->>>>>:" + mItems.get(position).getDisplayName());
                } else {
                    /*if (reverseEditModeForLongClickAgain){
                        return;
					}*/
                    DayMediaItem item = mItems.get(position);
                    int dgIndex = mGroupList.indexOf(mDayMediaGroup);
                    int itemPos = 0;
                    for (int i = 0; i < dgIndex; i++) {
                        itemPos += mGroupList.get(i).getItemList().size();
                    }
                    itemPos += position;
                    Intent intent = new Intent(MediaManager.ACTION_OPEN_FULLSCREEN);
                    intent.putExtra(MediaManager.KEY_IMAGE_PATH, item.getPath());
                    intent.putExtra(MediaManager.KEY_MEDIA_TYPE, item.getType());
                    intent.putExtra("itemPos", itemPos);

                    mContext.sendBroadcast(intent);

                    MyLog.d(TAG, "mOnItemClickListener-->>>>>getPath:" + item.getPath()
                            + "\n getType():" + item.getType()
                    );
                }
            }
        }
    };

    /**
     * 如果是编辑模式，再次长按取消编辑模式。使用该变量，对点击事件进行控制-此情况下不执行点击逻辑。
     */
    private boolean reverseEditModeForLongClickAgain;
    private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            MyLog.d(TAG, "onItemLongClick() mGroupDate/path:" + getDateString() + "/" + mItems.get(position).getPath());
            //检查是否有正在传输
            String tempFilePath = mItems.get(position).getPath().toLowerCase();
            if (!TextUtils.isEmpty(tempFilePath)) {
                if (tempFilePath.endsWith(".png") || tempFilePath.endsWith(".jpg")) {
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>onItemLongClick>>clqForUploadingImageOnedrive:" + AppOneDriver.getInstance().clqForUploadingImageOnedrive.size());
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>onItemLongClick>>clqImageWifiDirect:" + WifiDirectConstant.clqImageWifiDirect.size());
                    if (!AppOneDriver.getInstance().clqForUploadingImageOnedrive.isEmpty()) {
                        ToastUtils.showCustomToast(mContext.getString(R.string.tip_file_in_transmission_try_later));
                        return true;
                    }
                    if (!WifiDirectConstant.clqImageWifiDirect.isEmpty()) {
                        ToastUtils.showCustomToast(mContext.getString(R.string.tip_file_in_transmission_try_later));
                        return true;
                    }
                } else if (tempFilePath.endsWith(".mp4")) {
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>onItemLongClick>>clqForUploadingVideoOnedrive:" + AppOneDriver.getInstance().clqForUploadingVideoOnedrive.size());
                    MyLog.d(Constant.TAG_COMMON_ONE_DRIVER, TAG + ">>onItemLongClick>>clqImageWifiDirect:" + WifiDirectConstant.clqVideoWifiDirect.size());
                    if (!AppOneDriver.getInstance().clqForUploadingVideoOnedrive.isEmpty()) {
                        ToastUtils.showCustomToast(mContext.getString(R.string.tip_file_in_transmission_try_later));
                        return true;
                    }
                    if (!WifiDirectConstant.clqVideoWifiDirect.isEmpty()) {
                        ToastUtils.showCustomToast(mContext.getString(R.string.tip_file_in_transmission_try_later));
                        return true;
                    }
                }
            }
            if (!mEditMode) {
                Intent it = new Intent(MediaManager.ACTION_OPEN_EDIT_MODE);
                mContext.sendBroadcast(it);

//				reverseEditModeForLongClickAgain = false;
            } else {
//				reverseEditModeForLongClickAgain = true;
                mContext.sendBroadcast(new Intent(MediaManager.ACTION_CLOSE_EDIT_MODE));
            }
            return false;
        }
    };

    public ArrayList<String> getSelectedPaths() {
        ArrayList<String> paths = new ArrayList<String>();

        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getChecked() == true) {
                paths.add(mItems.get(i).getPath());
            }
        }

        return paths;
    }

    public void setCheckAll() {
        for (int i = 0; i < mItems.size(); i++) {
            mItems.get(i).setChecked(true);
        }

        notifyDataSetChanged();
    }

    public void setCheckNone() {
        for (int i = 0; i < mItems.size(); i++) {
            mItems.get(i).setChecked(false);
        }

        notifyDataSetChanged();
    }

    public void deleteChecked() {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getChecked() == true) {
                DayMediaItem item = mItems.get(i);
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                if (item.getType() == MediaManager.MEDIA_TYPE_LOCAL_PHOTO) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                File f = new File(item.getPath());
                if (f.exists()) {
                    boolean b = f.delete();
                    if (b) {
                        mContext.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=?", new String[]{item.getPath()});
                    } else {
                        MyLog.d(TAG, "deleteChecked() fail, path:" + item.getPath());
                    }
                }
            }
        }
    }

    public void setEditMode(boolean isEdit) {
        mEditMode = isEdit;

        if (!mEditMode) {
            for (int i = 0; i < mItems.size(); i++) {
                mItems.get(i).setChecked(false);
            }
        }

        notifyDataSetChanged();

        //reverseEditModeForLongClickAgain = false;
    }

    @Override
    public int getCount() {
        if (mItems != null) {
            return mItems.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mItems != null && position < mItems.size()) {
            return mItems.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.one_media_item, null);
            holder = new ItemHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        if (holder != null) {
            if (mEditMode) {
                holder.chk.setVisibility(View.VISIBLE);
                holder.chk.setChecked(mItems.get(position).getChecked());
            } else {
                holder.chk.setVisibility(View.GONE);
            }
            DayMediaItem mediaItem = mItems.get(position);
            final String path = mediaItem.getPath();
            long duration = mediaItem.getDuration();
            if (duration != 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                String hms = formatter.format(duration);
                holder.tv_duration.setText(hms + "");
                holder.tv_duration.setVisibility(View.VISIBLE);
            } else {
                holder.tv_duration.setVisibility(View.GONE);
            }

            Glide.with(mContext).load(path)
                    .bitmapTransform(transformation).into(holder.image);
            MyLog.d(TAG, ">>>>>>>>getView()>>>>>>>>>>path:" + path);

//            iv.setTag(path);

            if (mOnRequestImageListener != null) {
                mOnRequestImageListener.onRequestImage(path, holder.image, mGroupDate);
            }

            if (mItems.get(position).getType() == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
                holder.imagePlayVideo.setVisibility(View.VISIBLE);
            } else {
                holder.imagePlayVideo.setVisibility(View.GONE);
            }

            MyLog.d(TAG, "onRequestImage() ----path:" + path);
        }

        return convertView;
    }

    private String getDateString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        return df.format(new Date(mGroupDate));
    }

    static class ItemHolder {
        ImageView image;
        CheckBox chk;
        ImageView imagePlayVideo;
        TextView tv_duration;

        public ItemHolder(View view) {
            image = (ImageView) view.findViewById(R.id.image);
            chk = (CheckBox) view.findViewById(R.id.chk);
            imagePlayVideo = (ImageView) view.findViewById(R.id.iv_video_play);
            tv_duration = (TextView) view.findViewById(R.id.tv_duration);
        }
    }
}
