package com.infomax.ibotncloudplayer.bean;

/**
 * Created by hushaokun on 2018/6/8.
 */

public class MusicBean {


    public boolean isSelected;//是否选中
    public long curDuration;//当前进度时间
    public String name;//音乐的名称
    public String path;//音乐的路径
    public int albumUri;//通过专辑的Uri地址,获取音乐的封面
    public String artist;
    public long duration;//音乐的时长


    public MusicBean(boolean isSelected, long curDuration, String name, String path, int albumUri, String artist, long duration) {
        this.isSelected = isSelected;
        this.curDuration = curDuration;
        this.name = name;
        this.path = path;
        this.albumUri = albumUri;
        this.artist = artist;
        this.duration = duration;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public long getCurDuration() {
        return curDuration;
    }

    public void setCurDuration(long curDuration) {
        this.curDuration = curDuration;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAlbumUri() {
        return albumUri;
    }

    public void setAlbumUri(int albumUri) {
        this.albumUri = albumUri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
