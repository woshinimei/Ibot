package com.infomax.ibotncloudplayer;

/**
 * the Media file bean with the same day
 */
public class DayMediaItem {

    private int mType = MediaManager.MEDIA_TYPE_LOCAL_PHOTO;
    private long mId = 0;
    private long mDate = 0;
    private String mPath = null;
    private String mDisplayName = null;
    private long mSize = 0;
    private boolean mChecked = false;
    private long duration = 0;

    public DayMediaItem(int mediaType, long id, long date, String path, String name, long size, long duration) {
        mType = mediaType;
        mId = id;
        mDate = date;
        mPath = path;
        mDisplayName = name;
        mSize = size;
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getType() {
        return mType;
    }

    public long getId() {
        return mId;
    }

    public long getDate() {
        return mDate;
    }

    public String getPath() {
        return mPath;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public long getSize() {
        return mSize;
    }

    public void setChecked(boolean bChecked) {
        mChecked = bChecked;
    }

    public boolean getChecked() {
        return mChecked;
    }


}
