package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 本地视频GridView,adapter
 */
public class LocalVideoGVAdapter extends BaseAdapter{

	private static final String TAG = "LocalVideoGVAdapter";
	private Context mContext = null;
	private LayoutInflater myInflater;
	ArrayList<LocalVideoBean> mItems = null;
	private OnRequestVideoListener mOnRequestVideoListener = null;
	private GridView mGridView = null;

	private LinkedList<LocalVideoBean> tempList;

    public interface OnRequestVideoListener {

        void OnRequestImage(String imgPath, ImageView iv);
    }

	public LocalVideoGVAdapter(Context c, ArrayList<LocalVideoBean> items,  GridView gv, OnRequestVideoListener onRequestVideoListener)
	{
		mContext = c;
		mItems = items;
		myInflater = LayoutInflater.from(c);
		mOnRequestVideoListener = onRequestVideoListener;
		mGridView = gv;

//		mGridView.setOnItemClickListener(mOnItemClickListener);
	}
	public void setData(ArrayList<LocalVideoBean> items){
		this.mItems = items;
		notifyDataSetChanged();
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
			if(position<mItems.size())
			{
				//播放video
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(mItems.get(position).getPath())), "video/*");
				//当前所有的视频播放器都被手动强行停止后或者【没有视频播放器时】。点击播放视频文件就会异常停止。应该弹出是否使用【视频播放器来播放】
				if (intent.resolveActivity(mContext.getPackageManager()) != null) {
					mContext.startActivity(intent);
					saveWatchHitoryToSp(position);

				}else {
					ToastUtils.showToast(mContext, mContext.getString(R.string.tip_video_player_disable));
				}
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
			convertView = myInflater.inflate(R.layout.item_gv_act_local_video , null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.imagePlayVideo = (ImageView) convertView.findViewById(R.id.iv_video_play);
			convertView.setTag(holder);
		}
		else{
			holder = (ItemHolder) convertView.getTag();
		}

		final ImageView iv = holder.image;
		String tempDisplayName = mItems.get(position).getDisplayName().toLowerCase();
		if(mOnRequestVideoListener != null){
			if (tempDisplayName.endsWith(".mp4")){
				mOnRequestVideoListener.OnRequestImage(mItems.get(position).getThumbnailPath(), iv);
			}else if(tempDisplayName.endsWith(".swf")){
				mOnRequestVideoListener.OnRequestImage(mItems.get(position).getPath(), iv);
			}else {
				mOnRequestVideoListener.OnRequestImage(mItems.get(position).getPath(), iv);
			}
		}

		holder.imagePlayVideo.setVisibility(View.VISIBLE);

		holder.name.setText(mItems.get(position).getDisplayName());

		return convertView;
	}
	
	static class ItemHolder
	{
		ImageView image;
		ImageView imagePlayVideo;
		TextView name;
	}

	private void saveWatchHitoryToSp(int position){
		//保存bean到sp
		SharedPreferences sp = mContext.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);

		String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");
		LocalVideoBean currentBean = mItems.get(position);

		MyLog.d(TAG, "---->>mOnItemClickListener--->>local-->>:" + localList);

		if (TextUtils.isEmpty(localList))
		{
			LinkedList<LocalVideoBean> tempList = new LinkedList<LocalVideoBean>();
			tempList.addFirst(currentBean);

			try {
				String string = SharedPreferenceUtils.object2String(tempList);
				sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,string).commit();

			}catch (Exception e)
			{
				MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

			}
			tempList = null;

		}else
		{
			try {

				tempList = (LinkedList<LocalVideoBean>) SharedPreferenceUtils.string2Object(localList);

				if (tempList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
				{
					tempList.removeLast();//删除最后一个
				}

				if (tempList.contains(currentBean))
				{
					MyLog.d(TAG,"----contain---currentBean:"+currentBean.getDisplayName());
					tempList.remove(currentBean);
					tempList.addFirst(currentBean);
				}else
				{
					MyLog.d(TAG,"----uncontain---currentBean:"+currentBean.getDisplayName());
					tempList.addFirst(currentBean);
				}

				String tempStr = SharedPreferenceUtils.object2String(tempList);
				sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,tempStr).commit();
			} catch (Exception e) {
				MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
			}
		}

		MyLog.d(TAG, "---->>mOnItemClickListener--->>tempList-->>:"+(tempList==null ? 0 : tempList.size()));
	}
}
