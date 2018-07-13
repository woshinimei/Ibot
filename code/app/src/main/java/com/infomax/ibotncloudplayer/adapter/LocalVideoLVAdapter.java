package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LocalVideoBean;
import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SharedPreferenceUtils;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * 1.本地视频ListView,adapter,不传递listview对象
 * 2.本地视频历史记录 lv也使用该adapter，保留传递listview对象
 *
 * 2017/7/20 phc
 * 不是很明白为什么一会传递listview一会不传，据观察传入listview是为了设置itemonclick，
 * 故此全部采用不传入listview的方式，构造方法中去掉listview，这样在onclick中进行一些主界面的操作比较方便
 *
 */
public class LocalVideoLVAdapter extends BaseAdapter{

	private final String TAG = "LocalVideoLVAdapter";
	private Context mContext = null;
	private LayoutInflater myInflater;
	List<LocalVideoBean> mItems = null;
//	private OnRequestVideoListener mOnRequestVideoListener = null;
	private OnRequestVideoListener mOnRequestVideoListener;
//	private ListView mListView = null;

	private SharedPreferences sp;
	private LinkedList<LocalVideoBean> tempList;

	public interface OnRequestVideoListener {

        void OnRequestImage(String imgPath, ImageView iv);
    }

	public LocalVideoLVAdapter(Context c, List<LocalVideoBean> items , OnRequestVideoListener onRequestVideoListener)
	{
		mContext = c;
		mItems = items;
		myInflater = LayoutInflater.from(c);
		mOnRequestVideoListener = onRequestVideoListener;
//		mListView = lv;
/*		if(mListView != null){
			mListView.setOnItemClickListener(mOnItemClickListener);
		}*/

	}

	public List<LocalVideoBean> getData(){
		return this.mItems;
	}
	public void setData(List<LocalVideoBean> items){
		this.mItems = items;
		notifyDataSetChanged();
		MyLog.d(TAG,">>>>setData:");
	}


	private void saveWatchHitoryToSp(int position){
		//保存bean到sp
		sp = mContext.getSharedPreferences(Constant.MySharedPreference.SP_NAME,Context.MODE_PRIVATE);

		String localList = sp.getString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST, "");
		LocalVideoBean currentBean = mItems.get(position);

		MyLog.d(TAG, ">>saveWatchHitoryToSp--->>local-->>:"+localList);

		if (TextUtils.isEmpty(localList))
		{
			LinkedList<LocalVideoBean> tempList = new LinkedList<LocalVideoBean>();
			tempList.addFirst(currentBean);

			try {
				String string = SharedPreferenceUtils.object2String(tempList);
				sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,string).commit();

			}catch (Exception e)
			{
				MyLog.d(TAG, ">>>>Exception:" + e.getMessage());

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
					MyLog.d(TAG,"----包含---currentBean:"+currentBean.getDisplayName());
					tempList.remove(currentBean);
					tempList.addFirst(currentBean);
				}else
				{
					MyLog.d(TAG,"----不包含---currentBean:"+currentBean.getDisplayName());
					tempList.addFirst(currentBean);
				}

				String tempStr = SharedPreferenceUtils.object2String(tempList);
				sp.edit().putString(Constant.MySharedPreference.SP_KEY_LOCAL_LIST,tempStr).commit();
			} catch (Exception e) {
				MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
			}
		}

		MyLog.d(TAG, "---->>saveWatchHitoryToSp--->>tempList-->>:"+(tempList==null ? 0 : tempList.size()));
	}

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
		MyLog.d(TAG, "getView>>>>>>>>>path:" + path);
//			iv.setTag(path);
		/**
		 * 使用glide加载视频-缩略图(圆角)，glide不需要iv设置tag。glide内部处理了
		 */
		String tempDisplayName = mItems.get(position).getDisplayName().toLowerCase();
		if(mOnRequestVideoListener != null){
			if (tempDisplayName.endsWith(".mp4")){
				mOnRequestVideoListener.OnRequestImage(mItems.get(position).getThumbnailPath(), iv);

			}else if(tempDisplayName.endsWith(".swf")){
				mOnRequestVideoListener.OnRequestImage(path, iv);
			}else {
				mOnRequestVideoListener.OnRequestImage(path, iv);
			}
		}

//		holder.imagePlayVideo.setVisibility(View.VISIBLE);
//		GlideUtils.load(MyApplication.getInstance(), path, iv);

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
