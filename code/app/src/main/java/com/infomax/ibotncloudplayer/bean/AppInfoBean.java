package com.infomax.ibotncloudplayer.bean;

import android.graphics.drawable.Drawable;

/**
 * add by jy 20170613
 * appinfo
 */
public class AppInfoBean {

    /**
	 * 应用名字
	 */
	public String appName;
	/**
	 * 应用包名
	 */
	public String packageName;
	
	/**
	 * 应用大小
	 */
	public long appSize;
	
	/**
	 * 应用图标
	 */
	public Drawable appIcon;
	
	/**
	 * 是否是sd卡中的
	 */
	public boolean isInSd;
	
	/**
	 * 是否是系统应用
	 */
	public boolean isSys;
	
}