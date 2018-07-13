package com.infomax.ibotncloudplayer.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.infomax.ibotncloudplayer.MyApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/*
 * @author  2017/4/1
 */
public class DevicePath{

	private static ArrayList<String> totalDevicesList;
	private static final String TAG = DevicePath.class.getSimpleName();
	private static DevicePath instance;
	public DevicePath(Context context)
	{
		///////////
//		totalDevicesList = new ArrayList<String>();
//		StorageManager stmg = (StorageManager) context.getSystemService(context.STORAGE_SERVICE);
//		String[] list = stmg.getVolumePaths();//不能直接使用stmg.getVolumePaths()该方法类型为@hide
//		for(int i = 0; i < list.length; i++)
//		{
//			totalDevicesList.add(list[i]);
//		}
		///////////

		/////////
		totalDevicesList = (ArrayList<String>) getStoragePaths(context);
		////////

	}

	public static DevicePath getInstance(){
		if (instance == null){
			synchronized (DevicePath.class){
				if (instance == null){
					instance = new DevicePath(MyApplication.getInstance());
				}
			}
		}

		totalDevicesList = (ArrayList<String>) getStoragePaths(MyApplication.getInstance());

		/*final StringBuffer sb = new StringBuffer();
		if (totalDevicesList != null){
			for (final String path: totalDevicesList) {
				MyLog.d(TAG, ">>>>>>getInstance()>>>path:" + path);
				sb.append(path + " ");
			}
		}
		ThreadUtils.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				ToastUtils.showCustomToast(sb.toString());
			}
		});*/
		return instance;
	}

	/**
	 *
	 * @param cxt
	 * @return 各个设备存储路径集合
	 */
	public static List<String> getStoragePaths(Context cxt) {
		ArrayList<String> pathsList = new ArrayList<String>();

			StorageManager storageManager = (StorageManager) cxt.getSystemService(Context.STORAGE_SERVICE);
			try {
				Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
				method.setAccessible(true);
				Object result = method.invoke(storageManager);
				if (result != null && result instanceof String[]) {
					String[] pathes = (String[]) result;
					StatFs statFs;
					for (String path : pathes) {
						if (!TextUtils.isEmpty(path) && new File(path).exists()) {
							statFs = new StatFs(path);
							if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
								pathsList.add(path);

								MyLog.d(TAG,">>>>>>getStoragePaths()>>>path:" + path);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				File externalFolder = Environment.getExternalStorageDirectory();
				if (externalFolder != null) {
					pathsList.add(externalFolder.getAbsolutePath());
				}
			}

//		return pathsList.toArray(new String[pathsList.size()]);
		return pathsList;
	}

	/**
	 * get external sd path.
	 * @return has sd ，return path,otherwise ""
	 */
	public String getSdStoragePath()
	{
		String path = "";
		for(int i = 0; i < totalDevicesList.size(); i++)
		{
			if(!totalDevicesList.get(i).equals(Environment.getExternalStorageDirectory().getPath()))
			{
				if(totalDevicesList.get(i).contains("sd"))
				{
					path = totalDevicesList.get(i);
					return path;
				}
			}
		}
		return path;
	}
	
	public String getInterStoragePath()
	{
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * get Usb path.
	 * @return has Usb ，return path;otherwise null
	 */
	public String getUsbStoragePath()
	{
		String path = null;
		for(int i = 0; i < totalDevicesList.size(); i++)
		{
			if(!totalDevicesList.get(i).equals(Environment.getExternalStorageDirectory().getPath()))
			{
				if(totalDevicesList.get(i).contains("host"))
				{
					path = totalDevicesList.get(i);
					if (!TextUtils.isEmpty(path) && new File(path).exists()) {
						final StatFs statFs = new StatFs(path);
						/*ThreadUtils.runOnUIThread(new Runnable() {
						@Override
							public void run() {
								ToastUtils.showCustomToast("" +statFs.getBlockCount() * statFs.getBlockSize());
							}
						});*/
						if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
							return path;
						}else {
							return "";
						}
					}else {
						return "";
					}
				}
			}
		}
		return path;
	}
	
	public boolean hasMultiplePartition(String dPath)
	{
		try
		{
			File file = new File(dPath);
			String minor = null;
			String major = null;
			for(int i = 0; i < totalDevicesList.size(); i++)
			{
				if(dPath.equals(totalDevicesList.get(i)))
				{
					String[] list = file.list();
					for(int j = 0; j < list.length; j++)
					{
						int lst = list[j].lastIndexOf("_");
						if(lst != -1 && lst != (list[j].length() -1))
						{
							major = list[j].substring(0, lst);
							minor = list[j].substring(lst + 1, list[j].length());
							try
							{
							
								Integer.valueOf(major);
								Integer.valueOf(minor);
							}
							catch(NumberFormatException e)
							{
								return false;
							}
						}
						else 
						{
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}
		catch(Exception e)
		{
			//Log.e(TAG, "hasMultiplePartition() exception e");
			return false;
		}
	}
}
