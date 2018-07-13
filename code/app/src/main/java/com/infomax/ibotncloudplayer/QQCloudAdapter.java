package com.infomax.ibotncloudplayer;

//import java.io.InputStream;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshGridView;
		import com.infomax.ibotncloudplayer.utils.GlideUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.ysx.qqcloud.QQCloudFileInfo;

import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
		import android.widget.ImageView;
import android.widget.TextView;

public class QQCloudAdapter extends BaseAdapter{
	private  final String TAG = "QQCloudAdapter";
	private LayoutInflater myInflater;
	private OnClickListener _clickListener = null;
	public List<QQCloudFileInfo> QQFileList = null;
	private boolean ShareByOnline = true;
	private MGridView mGridView;
	private PullToRefreshGridView mGv;
	private Context ctx;
	public QQCloudAdapter(Context ctxt, List<QQCloudFileInfo> list, MGridView gv){
		ctx = ctxt;
		myInflater = LayoutInflater.from(ctxt);
		QQFileList = list;
		mGridView = gv;
		GridViewUtils.updateGridViewLayoutParams(mGridView, MediaManager.THUMBNAILS_COLUMNS);
	}
	public QQCloudAdapter(Context ctxt, List<QQCloudFileInfo> list, PullToRefreshGridView gv){
		ctx = ctxt;
		myInflater = LayoutInflater.from(ctxt);
		QQFileList = list;
		mGv = gv;
//		GridViewUtils.updateGridViewLayoutParams(mGridView, MediaManager.THUMBNAILS_COLUMNS);
	}
	
	public void setList(List<QQCloudFileInfo> list) {
		QQFileList = list;
		notifyDataSetChanged();
	}
	
	public void setClickListener(OnClickListener l) {
		_clickListener = l;
	}
	@Override
	public int getCount() {
		if (QQFileList == null){
			return 0;
		}else {
			return QQFileList.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (QQFileList == null){
			return  null;
		}else {
			return QQFileList.get(position);
		}
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
			convertView = myInflater.inflate(R.layout.item_gv_qqcloud , null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.imagePlayVideo = (ImageView) convertView.findViewById(R.id.iv_video_play);
			holder.fileName = (TextView) convertView.findViewById(R.id.tv_name);
			convertView.setTag(holder);
		} else{
			holder = (ItemHolder) convertView.getTag();
		}

		if(holder != null)
		{
			if (QQFileList != null && QQFileList.size() > 0){

				QQCloudFileInfo info = QQFileList.get(position);
	//			File imgFile = new File(info.imagePath);
				MyLog.d("QQCloud", "QQInfoItem -- " + info.imagePath);
	//			if(imgFile.exists()){
	//			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	//			    holder.image.setImageBitmap(myBitmap);
	//			    //Log.d("QQCloud", "QQInfoItem -- exists");
	//			}

				GlideUtils.load(ctx, info.imagePath, holder.image);

				holder.imagePlayVideo.setVisibility(View.VISIBLE);
				holder.fileName.setText(info.fileName);
				MyLog.d(TAG, "__>>>>>:" + info.fileName);
			}else {
				MyLog.d(TAG, "__QQFileList>>>>>:" + (QQFileList == null ? "null" : QQFileList .size()));
			}
		}
		
		return convertView;
	}
	class ItemHolder
	{
		ImageView image;
		ImageView imagePlayVideo;
		TextView fileName;
	}
	/*
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}*/
}
