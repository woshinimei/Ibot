package com.infomax.ibotncloudplayer.bean;


import java.io.Serializable;

/**
 * Created by juying on 2016/10/17.<br/>
 * 教育内容-本地音频文件bean	<br/>
 */
public class LocalAudioBean implements Serializable{
	static final long serialVersionUID = 1L;
	private long mId = 0;
	private long mDate = 0;

	/**音频文件的绝对路径*/
	private String mPath = "";
	private String mDisplayName = "";
	private long mSize = 0;

	/**音频文件的缩略图的路径*/
	private String thumbnailPath = "";

	/**音频文件对应的专辑图片的绝对路径*/
	private String albumArtPath = "";

	public LocalAudioBean(long id, long date, String path, String name, long size)
	{
		mId = id;
		mDate = date;
		mPath = path;
		mDisplayName = name;
		mSize = size;
	}
	public LocalAudioBean(String path)
	{
		mPath = path;
	}
	
	public long getId() { return mId; }
	public long getDate() { return mDate; }
	/**获取音频文件的绝对路径*/
	public String getPath() { return mPath; }

	/**音频文件名含后缀名称*/
	public String getDisplayName() { return mDisplayName; }
	public long getSize() { return mSize; }
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	/**获取 音频文件对应的专辑图片的绝对路径*/
	public String getAlbumArtPath() {
		return albumArtPath;
	}

	/**设置 音频文件对应的专辑图片的绝对路径*/
	public void setAlbumArtPath(String albumArtPath) {
		this.albumArtPath = albumArtPath;
	}

	/**
	 * 如果需要依据对象属性值是否相同来判断ArrayList是否包含某一对象，则需要重写Object的equals方法，并在equals方法中一一比较对象的每个属性值
	 */
	@Override
	public boolean equals(Object o) {

		if (o instanceof LocalAudioBean)
		{
			LocalAudioBean bean = (LocalAudioBean) o;
			return  this.mId == bean.mId
					&& this.mDate == bean.mDate
					&& this.mPath.equals(bean.mPath)
					&& this.mDisplayName.equals(bean.mDisplayName)
					&& this.mSize == bean.mSize;
		}

		return super.equals(o);
	}
}
