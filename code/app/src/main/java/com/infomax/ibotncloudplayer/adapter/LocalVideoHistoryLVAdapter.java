package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.MyLog;

import java.io.File;
import java.util.List;

/**
 * Created by juying on 2016/10/21.
 * 本地视频ListView,adapter
 */
public class LocalVideoHistoryLVAdapter extends BaseAdapter{

	private static final String TAG = "LocalVideoHistoryLVAdapter";
	private Context mContext = null;
	private LayoutInflater myInflater;
	List<LocalVideoBean> mItems = null;

	private ListView mListView;
	private OnRequestVideoListener mOnRequestVideoListener = null;

	private SharedPreferences sp;

    public interface OnRequestVideoListener {

        void OnRequestImage(String imgPath, ImageView iv);
    }

	public LocalVideoHistoryLVAdapter(Context c, List<LocalVideoBean> items,ListView lv ,OnRequestVideoListener onRequestVideoListener)
	{
		mContext = c;
		mItems = items;
		myInflater = LayoutInflater.from(c);

		mListView = lv;

		mListView.setOnItemClickListener(mOnItemClickListener);
		mOnRequestVideoListener = onRequestVideoListener;
	}
	public void setData(List<LocalVideoBean> items){
		this.mItems = items;
		notifyDataSetChanged();
		MyLog.d(TAG,"-->>>>setData:");
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if(position<mItems.size())
			{
				//播放video
				Intent it = new Intent(Intent.ACTION_VIEW);
				it.setDataAndType(Uri.fromFile(new File(mItems.get(position).getPath())), "video/*");
				mContext.startActivity(it);

			}
		}
	};

	@Override
	public int getCount() {
		if(mItems != null){
			return mItems.size();
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(mItems != null && position < mItems.size()){
			return mItems.get(position);
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = null;		
		if(convertView == null)
		{
			convertView = myInflater.inflate(R.layout.item_lv_common, null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
//			holder.imagePlayVideo = (ImageView) convertView.findViewById(R.id.iv_video_play);
			convertView.setTag(holder);
		}
		else{
			holder = (ItemHolder) convertView.getTag();
		}

			final ImageView iv = holder.image;
			final String path = mItems.get(position).getPath();
//			iv.setTag(path);
		/**
		 * 使用glide加载视频-缩略图(圆角)，glide不需要iv设置tag。glide内部处理了
		 */
//		Glide.with(mContext).load(Uri.fromFile(new File(path))).into(holder.image);
			if(mOnRequestVideoListener != null){
				mOnRequestVideoListener.OnRequestImage(path, iv);
			}

//		holder.imagePlayVideo.setVisibility(View.VISIBLE);

		holder.name.setText(mItems.get(position).getDisplayName());

			//Log.d(TAG, "onRequestImage() mGroupDate/path:" + getDateString(mGroupDate) + "/" + path);
		return convertView;
	}
	
	static class ItemHolder
	{
		ImageView image;
//		ImageView imagePlayVideo;
		TextView name;
	}
}
