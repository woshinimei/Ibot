package com.infomax.ibotncloudplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;
/**
 * 同一天的视频或图片组
 */
public class DayMediaGroup {

	private static final String TAG = "DayMediaGroup";
	private int mType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
	private long mDate = 0;
	private ArrayList<DayMediaItem> mItems = null;
	
	public DayMediaGroup(int type, long date)
	{
		mType = type;
		mDate = date;
		mItems = new ArrayList<DayMediaItem>();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		Log.d(TAG, "DayMediaGroup() time:" + df.format(new Date(mDate)));    	
	}
	
	public int getType() { return mType; }
	public long getDate() { return mDate; }
	public ArrayList<DayMediaItem> getItemList() { return mItems; }
	public void setItemList(ArrayList<DayMediaItem> items) { mItems = items; }
	
	public void clearItems(){
		if(mItems != null){
			mItems.clear();
		}
	}
}
