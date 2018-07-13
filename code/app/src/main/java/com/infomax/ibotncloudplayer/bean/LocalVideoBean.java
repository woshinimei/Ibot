package com.infomax.ibotncloudplayer.bean;


import com.infomax.ibotncloudplayer.MediaManager;

import java.io.Serializable;

/**
 * Created by jy on 2016/10/17.<br/>
 * 教育内容-本地视频文件bean	<br/>
 */
public class LocalVideoBean  extends LocalAudioBean implements Serializable{
	static final long serialVersionUID =1L;

	public LocalVideoBean(long id, long date, String path, String name, long size) {
		super(id, date, path, name, size);
	}

	public LocalVideoBean(String path) {
		super(path);
	}
	/*private long mId = 0;
	private long mDate = 0;

	*//**视频文件的绝对路径*//*
	private String mPath = "";
	private String mDisplayName = "";
	private long mSize = 0;

	*//**视频文件的缩略图的路径*//*
	private String thumbnailPath = "";

	public LocalVideoBean( long id, long date, String path, String name, long size)
	{
		mId = id;
		mDate = date;
		mPath = path;
		mDisplayName = name;
		mSize = size;
	}

	public long getId() { return mId; }
	public long getDate() { return mDate; }
	*//**获取视频文件的绝对路径*//*
	public String getPath() { return mPath; }

	*//**视频文件名含后缀名称*//*
	public String getDisplayName() { return mDisplayName; }
	public long getSize() { return mSize; }
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	*//**
	 * 如果需要依据对象属性值是否相同来判断ArrayList是否包含某一对象，则需要重写Object的equals方法，并在equals方法中一一比较对象的每个属性值
	 *//*
	@Override
	public boolean equals(Object o) {

		if (o instanceof  LocalVideoBean)
		{
			LocalVideoBean bean = (LocalVideoBean) o;
			return  this.mId == bean.mId
					&& this.mDate == bean.mDate
					&& this.mPath.equals(bean.mPath)
					&& this.mDisplayName.equals(bean.mDisplayName)
					&& this.mSize == bean.mSize;
		}

		return super.equals(o);
	}*/
}
