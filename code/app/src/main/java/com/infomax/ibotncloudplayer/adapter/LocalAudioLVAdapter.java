package com.infomax.ibotncloudplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;
import com.infomax.ibotncloudplayer.bean.LocalAudioBean;
import com.infomax.ibotncloudplayer.utils.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by juying on 2016/12/12.<br/>
 * 1.本地音频ListView,adapter,不传递listview对象 	<br/>
 * 2.历史记录 lv也使用该adapter，构造时传递listview对象,并设置条目点击事件	<br/>
 */
public class LocalAudioLVAdapter extends BaseAdapter{

	private final String TAG = LocalAudioLVAdapter.class.getSimpleName();
	private Context mContext = null;
	private LayoutInflater myInflater;
	private List<LocalAudioBean> mItems = null;
	private OnRequestVideoListener mOnRequestVideoListener;
	private LearnTrajectoryUtil.LearnTrajectoryHolder mLearnTrajectoryHolder;
	/**
	 * 历史记录 传递进来的，并设置条目点击
	 */
	private ListView mListView = null;

	private SharedPreferences sp;
	private LinkedList<LocalAudioBean> tempList;

	/**
	 * 请求加载图片的监听
	 */
	public interface OnRequestVideoListener {

		/**
		 * @param imgPath 图片绝对路径
		 * @param iv	显示图片的控件
		 * @param tagId 对应条目adapter的id
		 */
        void onRequestImage(String imgPath, ImageView iv, int tagId);
    }

	public LocalAudioLVAdapter(Context c, List<LocalAudioBean> items, ListView lv,
							   OnRequestVideoListener onRequestVideoListener, LearnTrajectoryUtil.LearnTrajectoryHolder learnTrajectoryHolder)
	{
		mContext = c;
		mItems = items;
		myInflater = LayoutInflater.from(c);
		mOnRequestVideoListener = onRequestVideoListener;
		mListView = lv;
		mLearnTrajectoryHolder = learnTrajectoryHolder;
		if(mListView != null){
			mListView.setOnItemClickListener(mOnItemClickListener);
		}

	}
	public void setData(List<LocalAudioBean> items){
		this.mItems = items;
		notifyDataSetChanged();
		MyLog.d(TAG,"-->>>>setData:");
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
			if(position<mItems.size())
			{
				LocalAudioBean currentBean = mItems.get(position);

				//check file
				if (!FileUtils.isFileExists(currentBean.getPath())){

					ToastUtils.showCustomToast(null, mContext.getString(R.string.text_tip_can_not_file_file));
					return;
				}

				//播放audio
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(currentBean.getPath())), "audio/*");//"audio/*"
				//当前所有的音乐播放器都被手动强行停止后或者【没有音乐播放器时】。点击播放音乐文件就会异常停止。应该弹出是否使用【视频播放器来播放】
				if (intent.resolveActivity(mContext.getPackageManager()) != null) {
					mContext.startActivity(intent);
					saveWatchHitoryToSp(position,currentBean);
				} else {
					Intent intent2 = new Intent(Intent.ACTION_VIEW);
					intent2.setDataAndType(Uri.fromFile(new File(currentBean.getPath())), "video/*");//"audio/*"
					if (intent2.resolveActivity(mContext.getPackageManager()) != null) {
						ToastUtils.showToast(mContext, mContext.getString(R.string.tip_audio_player_disable));
						mContext.startActivity(intent2);
						saveWatchHitoryToSp(position,currentBean);
					} else {
						ToastUtils.showToast(mContext, mContext.getString(R.string.tip_player_disable));
					}
				}
				LearnTrajectoryBean bean = new LearnTrajectoryBean(System.currentTimeMillis(),
						currentBean.getDisplayName(),
						LearnTrajectoryUtil.Constant.TYPE_EDU_AUDIO);
				/*bean.setName();
				bean.setStartTime();*/
				mLearnTrajectoryHolder.startLearn(bean);
				/*LearnTrajectoryBean bean = new LearnTrajectoryBean();
				bean.setName(currentBean.getDisplayName());
				bean.setStartTime(System.currentTimeMillis());
				if (mLearnTrajectoryHolder != null) {
					mLearnTrajectoryHolder.addTrajectory(bean);
				}*/

			}
		}
	};

	private void saveWatchHitoryToSp(int position,LocalAudioBean bean){
		LinkedList<LocalAudioBean> currentList = null;
		try {
			currentList = (LinkedList<LocalAudioBean>) SharedPreferenceUtils.getLocalAudioHistoryList(mContext);
		} catch (Exception e) {
			e.printStackTrace();
			currentList = null;
		}

		LocalAudioBean currentBean = bean;

		MyLog.d(TAG, "---->>mOnItemClickListener--->>local-->>:" + currentList);

		if (currentList == null)
		{
			currentList = new LinkedList<LocalAudioBean>();
			currentList.addFirst(currentBean);

			try {
				SharedPreferenceUtils.setLocalAudioHistoryList(mContext,currentList);

			}catch (Exception e)
			{
				MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());

			}
			currentList = null;

		}else
		{
			try {

				if (currentList.size() >= Constant.MySharedPreference.SAVE_WATCH_HISTORY_SIZE)
				{
					currentList.removeLast();//删除最后一个
				}

				if (currentList.contains(currentBean))
				{
					MyLog.d(TAG,"----contain---currentBean:"+currentBean.getDisplayName());
					currentList.remove(currentBean);
					currentList.addFirst(currentBean);
				}else
				{
					MyLog.d(TAG,"----uncontain---currentBean:"+currentBean.getDisplayName());
					currentList.addFirst(currentBean);
				}

				SharedPreferenceUtils.setLocalAudioHistoryList(mContext,currentList);
			} catch (Exception e) {
				MyLog.d(TAG, "---->>>>Exception:" + e.getMessage());
			}
		}

		MyLog.d(TAG, "---->>mOnItemClickListener--->>currentList-->>:" + (currentList == null ? 0 : currentList.size()));
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
			convertView.setTag(holder);
		}
		else{
			holder = (ItemHolder) convertView.getTag();
		}

			final ImageView iv = holder.image;
//			iv.setTag(position);
			final String path = mItems.get(position).getPath();
			final String albumArtPath = mItems.get(position).getAlbumArtPath();
			if(mOnRequestVideoListener != null){
				mOnRequestVideoListener.onRequestImage(albumArtPath/*mItems.get(position).getThumbnailPath()*/, iv, position);
			}


		holder.name.setText(mItems.get(position).getDisplayName());

		return convertView;
	}
	
	static class ItemHolder
	{
		ImageView image;
		TextView name;
	}
}
