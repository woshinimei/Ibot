package com.ysx.qqcloud;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.infomax.ibotncloudplayer.MediaManager;
import com.infomax.ibotncloudplayer.R;
import com.infomax.ibotncloudplayer.bean.CloudFolderBean;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.OkHttpDownLoadUtils;
import com.infomax.ibotncloudplayer.utils.ToastUtils;
import com.qcloud.Module.Vod;
import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Utilities.SHA1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QQCloudObject {
	private Context mContext = null;
	private final static String TAG = QQCloudObject.class.getSimpleName();
	private static final String PREF_PUBLIC_VIDEO = "PUBLIC_VIDEO";
	private static final String PREF_PUBLIC_AUDIO = "PUBLIC_AUDIO";
	private static final String PREF_PUBLIC_PHOTO = "PUBLIC_PHOTO";
	private static final String PREF_PRIVATE_VIDEO = "PRIVATE_VIDEO";
	private static final String PREF_PRIVATE_AUDIO = "PRIVATE_AUDIO";
	private static final String PREF_PRIVATE_PHOTO = "PRIVATE_PHOTO";
	public static String MSG_QQCLOUD_INFO = TAG + ".QQINFO";
	public static String MSG_QQCLOUD_INFO_UPDATE = MSG_QQCLOUD_INFO + ".UPDATE";
	public static String MSG_QQCLOUD_VIDEOURL = TAG + ".QQVIDEOURL";
	public static String MSG_QQCLOUD_ERROR = TAG + ".ERROR";

	public static String MSG_QQCLOUD_FOLDER = TAG + ".QQINFO.FOLDER";//文件夹
	/**加载更多*/
	public static String MSG_QQCLOUD_INFO_LOADMORE = TAG + ".QQINFO.LOADMORE";

	public static String MSG_QQCLOUD_NO_DATA = TAG + "_QQINFO_NO_DATA";


	public long UploadSize=0;
	public long UploadStatus=0;
	public final static String secretId = "AKIDdNAKV8JtIUjubaaqEKmbm1bvtHzEiwja"; //"AKIDKz5OYG6KJP7M8YGqohiSsi5wTWnpwFfb";
	public final static String secretKey = "18hKuI3L3I3nNE7ozvuLauXdxoxwkgjx"; //"9U6EWQ29NPu3vWj49PTgTeUhVBS26glM";
	//public String secretId = "AKIDTF1L0nFAgkLjrLr29gxfiDS7sE2Urrkj";
	//public String secretKey = "WQeLDiZFBlFGRxyV7DbUzteZw2WnIL2C";
	
	public List<QQCloudFileInfo> QQFileList = null;
	private TreeMap<String, Object> config = null;
	private QcloudApiModuleCenter module = null;
	
	private String servicestring = Context.DOWNLOAD_SERVICE;
	private DownloadManager downloadmanager;
	
	private static QQCloudObject instance = null;

	private SharedPreferences mPrefs;
	private String mPublicVideoId = null;
	private String mPublicAudioId = null;
	private String mPublicPhotoId = null;
	private String mPrivateVideoId = null;
	private String mPrivateAudioId = null;
	private String mPrivatePhotoId = null;

	/** 云端 FoldersUnderPublicVideo;PUBLIC-VIDEO 默认会将 VIDEO这个根文件夹添加到集合中 */
	public ArrayList<CloudFolderBean> folderList;

	/** 默认一页显示条目数 */
	private final int PAGE_SIZE = 10;

	public static QQCloudObject sharedInstance(){
		return instance;
	}
	
	public QQCloudObject(Context c){
		mContext = c;
		if (config == null){
			config = new TreeMap<String, Object>();
			config.put("SecretId", secretId);
			config.put("SecretKey", secretKey);
			config.put("RequestMethod", "GET");
			config.put("DefaultRegion", "gz");
		}
		
		if (module == null) {
			module = new QcloudApiModuleCenter(new Vod(), config);
		}
		
		if (QQFileList == null){
			QQFileList = new ArrayList<QQCloudFileInfo>();
		}
		if (folderList == null){
			folderList = new ArrayList<CloudFolderBean>();
		}

		downloadmanager = (DownloadManager) c.getSystemService(servicestring);
		
		instance = this;

		mPrefs = c.getSharedPreferences("qqCloud", Context.MODE_PRIVATE);
		mPublicVideoId = mPrefs.getString(PREF_PUBLIC_VIDEO, null);
		mPublicAudioId = mPrefs.getString(PREF_PUBLIC_AUDIO, null);
		mPublicPhotoId = mPrefs.getString(PREF_PUBLIC_PHOTO, null);

		mPrivateVideoId = mPrefs.getString(PREF_PRIVATE_VIDEO, null);
		mPrivateAudioId = mPrefs.getString(PREF_PRIVATE_AUDIO, null);
		mPrivatePhotoId = mPrefs.getString(PREF_PRIVATE_PHOTO, null);
		MyLog.e(TAG,"QQCloudObject(.)>>>>>>mPrivatePhotoId>>>>>>>>>>>>>>:"+mPrivatePhotoId);
	}

	public void getQQCloudInfo(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				runQQCloudInfo();
			}}).start(); 
	}
	public void getQQCloudInfoByClassId(final int classId){
		new Thread(new Runnable(){

			@Override
			public void run() {
				runQQCloudInfosByClassId(classId);
			}}).start();
	}
	/**加载更多*/
	public void getQQCloudInfoLoadMoreByClassId(final int classId,final int currentPageIndex){
		new Thread(new Runnable(){

			@Override
			public void run() {
				runQQCloudInfosLoadMoreByClassId(classId, currentPageIndex);
			}}).start();
	}

	/**
	 *
	 * @param info  传递当前bean
	 * @param download
	 */
	public void getQQCloudGetVideoUrl(final QQCloudFileInfo info, boolean download) {
		final boolean _download = download;
		new Thread(new Runnable(){

			@Override
			public void run() {
//				runQQCloudGetVideoUrl(fileIndex, _download);
				runQQCloudGetVideoUrl(info, _download);
			}}).start(); 
	
	}
	public void getQQCloudGetVideoUrl(int index, boolean download) {
		final int fileIndex = index;
		final boolean _download = download;
		new Thread(new Runnable(){

			@Override
			public void run() {
				runQQCloudGetVideoUrl(fileIndex, _download);
			}}).start();

	}
	public void getQQCloudGetVideoUrl(String fileId) {
		final String _fileId = fileId;
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				runQQCloudGetVideoUrl(_fileId);
			}}).start(); 
	}

	/**
	 * 获取视频信息列表 PUBLIC VIDEO下面
	 * 域名：vod.api.qcloud.com
	 * 接口名: DescribeVodInfo
	 */
	private void  runQQCloudInfo(){
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		String result = null;
		try {
			/* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
			params.put("classId", mPublicVideoId);
			params.put("pageSize", "100");
			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodInfo", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d("QQCloud", json_result.toString());
			QQFileList.clear();
			parseJsonInfo(json_result);

			final Intent intent = new Intent(MSG_QQCLOUD_INFO);
			mContext.sendBroadcast(intent);
			
			downloadImage();
			//downloadVideoUrl();
			
		} catch (Exception e) {
			//System.out.println("error..." + e.getMessage());
			MyLog.d("QQCloud", "error..." + e.getMessage());
			
		}
	}
	/**
	 * 获取视频信息列表 PUBLIC VIDEO下面
	 * 域名：vod.api.qcloud.com
	 * 接口名: DescribeVodInfo
	 */
	private void  runQQCloudInfosByClassId(final int classId){
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		String result = null;
		try {
			/* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
//			params.put("classId", mPublicVideoId);//原来的mPublicVideoId
			params.put("classId", classId);
			params.put("pageSize", PAGE_SIZE);
			//orderby	否	Int	结果排序，默认按时间降序; 0：按时间升序 1：按时间降序
			params.put("orderby", 1);

			MyLog.d(TAG, "---->>>>>>>runQQCloudInfosByClassId--->>>classId:" + classId);//mPublicVideoIdclassId:25453

			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodInfo", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d(TAG, json_result.toString());

			parseJsonInfo(json_result);

			final Intent intent = new Intent(MSG_QQCLOUD_INFO);
			mContext.sendBroadcast(intent);

			downloadImage();
			//downloadVideoUrl();

		} catch (Exception e) {
			//System.out.println("error..." + e.getMessage());
			MyLog.d("QQCloud", "error..." + e.getMessage());

			final Intent intent = new Intent(MSG_QQCLOUD_ERROR);//TODO 待修改
			mContext.sendBroadcast(intent);
		}
	}
	/**
	 * 获取视频信息列表  加载更多(下一页)
	 * 域名：vod.api.qcloud.com
	 * 接口名: DescribeVodInfo
	 * currentPageIndex
	 */
	private void  runQQCloudInfosLoadMoreByClassId(final int classId,int currentPageIndex){
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		String result = null;
		try {
			/* call 方法正式向指定的接口名发送请求，并把请求参数params传入，返回即是接口的请求结果。 */
//			params.put("classId", mPublicVideoId);//原来的mPublicVideoId
			params.put("classId", classId);
			params.put("pageSize", PAGE_SIZE);
			params.put("pageNo", currentPageIndex);
			//orderby	否	Int	结果排序，默认按时间降序; 0：按时间升序 1：按时间降序
			params.put("orderby", 1);

			MyLog.d(TAG, "---->>>>>>>runQQCloudInfosByClassId--->>>classId:" + classId + ",pageNo:" + currentPageIndex);//mPublicVideoIdclassId:25453

			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodInfo", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d(TAG, "--->>>>>>>pageNo:" + currentPageIndex + ",json:" + json_result.toString());

			parseJsonInfoLoadMore(json_result);


			//downloadVideoUrl();

		} catch (Exception e) {
			//System.out.println("error..." + e.getMessage());
			MyLog.d(TAG, "---->>>>>runQQCloudInfosLoadMoreByClassId----->>error..." + e.getMessage());

			final Intent intent = new Intent(MSG_QQCLOUD_ERROR);//TODO 待修改
			mContext.sendBroadcast(intent);
		}
	}

	
	//private List<String> fileList = new ArrayList<String>();
	public int parseJsonInfo(JSONObject json) throws JSONException{
		if (!json.has("code"))
		{
			return -1;
		}
		if (json.getInt("code") != 0) {
			MyLog.d(TAG, "error..." + json.getString("message"));
			ErrorReport(json);
			return json.getInt("code");
		}

		QQFileList.clear();

		//注意：当该文件夹下面没有数据时返回的json 就没有"fileSet"字段.
		/*{
			"code": 0,
				"codeDesc": "Success",
				"message": "",
				"totalCount": 0
		}*/
		if(json.has("totalCount") && json.getInt("totalCount") == 0)
		{
			return 0;
		}else if (json.has("totalCount") && json.getInt("totalCount") > 0)
		{

		JSONArray array = json.getJSONArray("fileSet");
		List<QQCloudFileInfo> _QQFileList = new ArrayList<QQCloudFileInfo>();
		for (int i=0;i<array.length();i++) {
			JSONObject fileSet = array.getJSONObject(i);
			/*	status:
			 * 视频状态，过滤使用，
			 * -1：未上传完成，不存在；
			 * 0：初始化，暂未使用；
			 * 1：审核不通过，暂未使用；
				2：正常；
				3：暂停；
				4：转码中；
				5：发布中；
				6：删除中；
				7：转码失败；
				100：已删除
			 * */
			String status = fileSet.getString("status");
			MyLog.d(TAG, "File status " + status);
			if (!status.equals("2"))
			{
				continue;
			}

			String fileId = fileSet.getString("fileId");
			String fileName = fileSet.getString("fileName");
			String updateTime = fileSet.getString("updateTime");
			String imageUrl = fileSet.getString("imageUrl");
			String fileSize = fileSet.getString("size");

			QQCloudFileInfo info = new QQCloudFileInfo();
			info.fileId = fileId;
			info.fileName = (fileName == null ? "" : fileName);
			info.updateTime = updateTime;
			info.imageUrl = imageUrl;
			info.fileSize = fileSize;

			String cameraPath = MediaManager.getSingleCameraPath(true);
			if(cameraPath != null)
			{
				info.imagePath = cameraPath + File.separator + info.fileId+".jpg";
				info.localVideoURL30 = cameraPath + File.separator + info.fileId+"_f30.mp4";
				info.localVideoURL20 = cameraPath + File.separator + info.fileId+"_f20.mp4";
			}
			else
			{
				/*
				info.imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/VideoImage" + "/" + info.fileId+".jpg";

				info.localVideoURL30 = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/Video" + "/" + info.fileId+"_f30.mp4";
				info.localVideoURL20 = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/Video" + "/" + info.fileId+"_f20.mp4";*/
			}

			_QQFileList.add(info);
			info = null;
			fileSet = null;
		}
		
		array = null;

		for (int i= 0; i<_QQFileList.size() ;i++)
		{
			QQFileList.add(_QQFileList.get(i));
		}
		_QQFileList = null;
		}
		return 0;
	}

	public int parseJsonInfoLoadMore(JSONObject json) throws JSONException{
		if (!json.has("code"))
		{
			return -1;
		}
		if (json.getInt("code") != 0) {
			MyLog.d(TAG, "error..." + json.getString("message"));
			ErrorReport(json);
			return json.getInt("code");
		}

//		QQFileList.clear();

		//注意：当该文件夹下面没有数据时返回的json 就没有"fileSet"字段,或者分页时。
		/*{
			"code": 0,
				"codeDesc": "Success",
				"message": "",
				"totalCount": 0
		}*/

			if (!json.has("fileSet"))//
			{
				MyLog.d(TAG, "--->>>parseJsonInfoLoadMore: fileSet:" + json.has("fileSet"));
				final Intent intent = new Intent(MSG_QQCLOUD_NO_DATA);
				mContext.sendBroadcast(intent);
				return -2;//没有数据了
			}
			JSONArray array = json.getJSONArray("fileSet");
			List<QQCloudFileInfo> _QQFileList = new ArrayList<QQCloudFileInfo>();
			for (int i=0;i<array.length();i++) {
				JSONObject fileSet = array.getJSONObject(i);
			/*	status:
			 * 视频状态，过滤使用，
			 * -1：未上传完成，不存在；
			 * 0：初始化，暂未使用；
			 * 1：审核不通过，暂未使用；
				2：正常；
				3：暂停；
				4：转码中；
				5：发布中；
				6：删除中；
				7：转码失败；
				100：已删除
			 * */
				String status = fileSet.getString("status");
				MyLog.d(TAG, "File status " + status);
				if (!status.equals("2"))
				{
					continue;
				}

				String fileId = fileSet.getString("fileId");
				String fileName = fileSet.getString("fileName");
				String updateTime = fileSet.getString("updateTime");
				String imageUrl = fileSet.getString("imageUrl");
				String fileSize = fileSet.getString("size");

				QQCloudFileInfo info = new QQCloudFileInfo();
				info.fileId = fileId;
				info.fileName = (fileName == null ? "" : fileName);
				info.updateTime = updateTime;
				info.imageUrl = imageUrl;
				info.fileSize = fileSize;

				String cameraPath = MediaManager.getSingleCameraPath(true);
				if(cameraPath != null)
				{
					info.imagePath = cameraPath + File.separator + info.fileId+".jpg";
					info.localVideoURL30 = cameraPath + File.separator + info.fileId+"_f30.mp4";
					info.localVideoURL20 = cameraPath + File.separator + info.fileId+"_f20.mp4";
				}
				else
				{
				/*
				info.imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/VideoImage" + "/" + info.fileId+".jpg";

				info.localVideoURL30 = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/Video" + "/" + info.fileId+"_f30.mp4";
				info.localVideoURL20 = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/IBOTN_DATA/Video" + "/" + info.fileId+"_f20.mp4";*/
				}

				_QQFileList.add(info);
				info = null;
				fileSet = null;
			}

			array = null;

			for (int i= 0; i<_QQFileList.size() ;i++)
			{
				QQFileList.add(_QQFileList.get(i));
			}
			_QQFileList = null;

		final Intent intent = new Intent(MSG_QQCLOUD_INFO_LOADMORE);
		mContext.sendBroadcast(intent);

		downloadImage();

		return 0;
	}
	
	private void downloadImage() {		
		new Thread(new Runnable(){

			@Override
			public void run() {
				boolean update = false;
				for (int i=0; i<QQFileList.size(); i++) {
					QQCloudFileInfo info = QQFileList.get(i);
					if (info.imageUrl!= null && info.imageUrl.startsWith("http")){
//						MyLog.d(TAG, info.imageUrl);
						//
//						MyLog.d(TAG, "path:"+info.imagePath);

						File mFilePath = new File(info.imagePath);
						if (!mFilePath.exists()){

							///////////// 旧的方式下载，下载到内置sd卡中。有问题
//							info.downloadID = StartDownload(info.imageUrl, info.fileId+".jpg");
							/////////////

							//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
							final String destFileDir = MediaManager.getSingleCameraPath(true);
							if (!TextUtils.isEmpty(destFileDir))
							{//非空就下载
								OkHttpDownLoadUtils.downloadFile(info.imageUrl,destFileDir,info.fileId+".jpg");
							}

							////////////

							MyLog.d(TAG, "download ID " + info.downloadID);
							update = true;
						} else {
							MyLog.d(TAG, "download uri " + info.imagePath);
						}
						
						//Uri uri = downloadmanager.getUriForDownloadedFile(info.downloadID);
						//MyLog.d(TAG, "download uri " + uri);
						QQFileList.set(i, info);
					}
				}
				
				if (update) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					final Intent intent = new Intent(MSG_QQCLOUD_INFO_UPDATE); //TODO  什么用?
//					mContext.sendBroadcast(intent);
				}
				
			}}).start();
	}

	/*
	private void downloadVideoUrl(){
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				for (int i=0; i<QQFileList.size(); i++)
					runQQCloudGetVideoUrl(i, false);
			}
		}).start();
	}
	*/

	/**
	 *
	 * @param url
	 * @param name
	 * @return
	 * 使用downloadmanager下载，只能下载到内置sd卡的外部存储目录。对于外置sd卡的存储目录，不能使用该方式下载。
	 */
	public long StartDownload(String url, String name) {
		
		Uri uri = Uri.parse(url);
		Request request = new Request(uri);
		MyLog.d(TAG, "Start Download " + url + " , " + name);

		String cameraPath = MediaManager.getSingleCameraPath(true);
		if(cameraPath != null)
		{
			File f = new File(cameraPath + File.separator + name);
			Uri urlFile = Uri.fromFile(f);			
			request.setDestinationUri(urlFile);
			request.allowScanningByMediaScanner();
			/*if(name.endsWith(".jpg"))
				request.setDestinationInExternalPublicDir(cameraPath, name);
			else
				request.setDestinationInExternalPublicDir(cameraPath, name);*/
		}
		else
		{
			if(name.endsWith(".jpg")){
				request.setDestinationInExternalPublicDir("/IBOTN_DATA/VideoImage", name);
			}
			else{
				request.setDestinationInExternalPublicDir("/IBOTN_DATA/Video", name);
			}
		}
		
		MyLog.d(TAG, "End Download " + url + " , " + name);
		return downloadmanager.enqueue(request);
		
	}
	
	private void runQQCloudGetVideoUrl(String fileID){
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		params.put("fileId", fileID);
		String result = null;
		try {
			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodPlayUrls", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d(TAG, json_result.toString());
		
			if (currentInfo == null)
				currentInfo = new QQCloudFileInfo();
			currentInfo.fileId = fileID;
			
			String cameraPath = MediaManager.getSingleCameraPath(true);
			if(cameraPath != null)
				currentInfo.imagePath = cameraPath + File.separator + currentInfo.fileId+".jpg";
			else
			{
				/*currentInfo.imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/IBOTN_DATA/VideoImage" + "/" + currentInfo.fileId+".jpg";*/
			}
			int ret = parseJsonVideoInfo(json_result);
			if (ret == 0) {
				File mFilePath = new File(currentInfo.imagePath);
				if (!mFilePath.exists()) {
					///////////// 旧的方式下载，下载到内置sd卡中。有问题
//					StartDownload(currentInfo.videoURL0, currentInfo.fileId+".jpg");
					/////////////

					//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
					final String destFileDir = MediaManager.getSingleCameraPath(true);
					if (!TextUtils.isEmpty(destFileDir))
					{//非空就下载
						OkHttpDownLoadUtils.downloadFile(currentInfo.videoURL0,destFileDir,currentInfo.fileId+".jpg");
					}

					////////////

				}
				
				final Intent intent = new Intent(MSG_QQCLOUD_VIDEOURL);
				intent.putExtra("fileUrl", currentInfo.imagePath);
				mContext.sendBroadcast(intent);
			}
		} catch (Exception e) {

			final Intent intent = new Intent(MSG_QQCLOUD_ERROR);
			mContext.sendBroadcast(intent);

			//System.out.println("error..." + e.getMessage());
			MyLog.d(TAG, "error..." + e.getMessage());
		}
	}
	
	QQCloudFileInfo currentInfo;
	private void runQQCloudGetVideoUrl(int index, boolean download) {
		MyLog.d(TAG, "------>>>>>>>>runQQCloudGetVideoUrl----->>>QQFileList.size:"+QQFileList.size());
		currentInfo = QQFileList.get(index);
		String fileID = currentInfo.fileId;
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		params.put("fileId", fileID);
		String result = null;
		try {
			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodPlayUrls", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d(TAG, "------>>>>>>>>runQQCloudGetVideoUrl----->>>json:"+json_result.toString());
		
			int ret = parseJsonVideoInfo(json_result);
			if (ret == 0) {//0: 成功
				final Intent intent = new Intent(MSG_QQCLOUD_VIDEOURL);
				intent.putExtra("videoIndex", index);
				intent.putExtra("isDownload", download);
				//intent.putExtra("videoURL0", currentInfo.videoURL0);
				//intent.putExtra("videoURL20", currentInfo.videoURL20);
				//intent.putExtra("videoURL30", currentInfo.videoURL30);
				if (download) {
					if (!currentInfo.videoURL30.isEmpty()) {
						File mFilePath = new File(currentInfo.localVideoURL30);
						if (!mFilePath.exists()) {

							///////////// 旧的方式下载，下载到内置sd卡中。有问题
//							StartDownload(currentInfo.videoURL30, currentInfo.fileId+"_f30.mp4");
							/////////////
							//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
							final String destFileDir = MediaManager.getSingleCameraPath(true);
							if (!TextUtils.isEmpty(destFileDir))
							{//非空就下载
								OkHttpDownLoadUtils.downloadFile(currentInfo.videoURL30,destFileDir,currentInfo.fileId+"_f30.mp4");
							}

							////////////
						}
					}
					if (!currentInfo.videoURL20.isEmpty()) {
						File mFilePath = new File(currentInfo.localVideoURL20);
						if (!mFilePath.exists()) {

							///////////// 旧的方式下载，下载到内置sd卡中。有问题
//							StartDownload(currentInfo.videoURL20, currentInfo.fileId+"_f20.mp4");
							/////////////
							//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
							final String destFileDir = MediaManager.getSingleCameraPath(true);
							if (!TextUtils.isEmpty(destFileDir))
							{//非空就下载
								OkHttpDownLoadUtils.downloadFile(currentInfo.videoURL20,destFileDir,currentInfo.fileId+"_f20.mp4");
							}

							////////////
						}
					}
				}
				mContext.sendBroadcast(intent);
				
			}
		} catch (Exception e) {

			final Intent intent = new Intent(MSG_QQCLOUD_ERROR);
			mContext.sendBroadcast(intent);

			//System.out.println("error..." + e.getMessage());
			MyLog.d(TAG, "error..." + e.getMessage());
		}
	}

	/**直接传递 当前bean */
	private void runQQCloudGetVideoUrl(QQCloudFileInfo info, boolean download) {
//		currentInfo = QQFileList.run(index);info
		currentInfo = info;
		String fileID = currentInfo.fileId;
		TreeMap<String, Object> params = new TreeMap<String, Object>();
		params.put("fileId", fileID);
		String result = null;
		try {
			module.setConfigRequestMethod("GET");
			result = module.call("DescribeVodPlayUrls", params);
			JSONObject json_result = new JSONObject(result);
			MyLog.d(TAG, "------>>>>>>>>runQQCloudGetVideoUrl----->>>json:"+json_result.toString());

			int ret = parseJsonVideoInfo(json_result);
			if (ret == 0) {//0: 成功
				final Intent intent = new Intent(MSG_QQCLOUD_VIDEOURL);
//				intent.putExtra("videoIndex", index); 更换为currentInfo TODO
				Bundle myBundle = new Bundle();
				myBundle.putSerializable("currentInfo",currentInfo);
//				intent.putExtra(Constant.MyIntentProperties.NAME_KEY_01,myBundle);
				intent.putExtras(myBundle);
				intent.putExtra("isDownload", download);
				//intent.putExtra("videoURL0", currentInfo.videoURL0);
				//intent.putExtra("videoURL20", currentInfo.videoURL20);
				//intent.putExtra("videoURL30", currentInfo.videoURL30);
				if (download) {
					if (!currentInfo.videoURL30.isEmpty()) {
						File mFilePath = new File(currentInfo.localVideoURL30);
						if (!mFilePath.exists()) {
							///////////// 旧的方式下载，下载到内置sd卡中。有问题
//							StartDownload(currentInfo.videoURL30, currentInfo.fileId+"_f30.mp4");
							/////////////
							//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
							final String destFileDir = MediaManager.getSingleCameraPath(true);
							if (!TextUtils.isEmpty(destFileDir))
							{//非空就下载
								OkHttpDownLoadUtils.downloadFile(currentInfo.videoURL30,destFileDir,currentInfo.fileId+"_f30.mp4");
							}

							////////////
						}
					}
					if (!currentInfo.videoURL20.isEmpty()) {
						File mFilePath = new File(currentInfo.localVideoURL20);
						if (!mFilePath.exists()) {

							///////////// 旧的方式下载，下载到内置sd卡中。有问题
//							StartDownload(currentInfo.videoURL20, currentInfo.fileId+"_f20.mp4");
							/////////////
							//////////// 新的方式下载，下载到，外置sd卡中，如果没有外置sd卡就不下载图片。
							final String destFileDir = MediaManager.getSingleCameraPath(true);
							if (!TextUtils.isEmpty(destFileDir))
							{//非空就下载
								OkHttpDownLoadUtils.downloadFile(currentInfo.videoURL20,destFileDir,currentInfo.fileId+"_f20.mp4");
							}

							////////////
						}
					}
				}
				mContext.sendBroadcast(intent);

			}
		} catch (Exception e) {

			final Intent intent = new Intent(MSG_QQCLOUD_ERROR);
			mContext.sendBroadcast(intent);

			//System.out.println("error..." + e.getMessage());
			MyLog.d(TAG, "error..." + e.getMessage());
		}
	}
	
	public int parseJsonVideoInfo(JSONObject json) throws JSONException {
		if (!json.has("code")) return -1;
		if (json.getInt("code") != 0) {
			//MyLog.d(TAG, "error..." + json.getString("message"));
			ErrorReport(json);
			return json.getInt("code");
		}
		
		if (!json.has("playSet"))
		{
			return -1;

		}
		JSONArray array = json.getJSONArray("playSet");
		
		//{"code":0,"message":"","playSet":[{"url":"http:\/\/200002661.vod.myqcloud.com\/200002661_33b7251ebaa311e5a7798bb229b49695.f0.jpg","definition":0,"vbitrate":0,"vheight":0,"vwidth":0}]}
		
		for (int i=0;i<array.length();i++) {
			JSONObject fileSet = array.getJSONObject(i);
			int definition = fileSet.getInt("definition");
			String url = fileSet.getString("url");
			
			if (definition == 0){
				currentInfo.videoURL0 = url;
				MyLog.d(TAG, "[原版] " + url);
			}
			if (definition == 20){
				currentInfo.videoURL20 = url;
				MyLog.d(TAG, "[標清] " + url);
			}
			if (definition == 30){
				currentInfo.videoURL30 = url;
				MyLog.d(TAG, "[高清] " + url);
			}
			fileSet = null;
		}
		
		array = null;
		
		return 0;
	}
	
	private void ErrorReport(JSONObject json) throws JSONException {
		if (!json.has("code"))
		{
			return;
		}

		int errorCode = json.getInt("code");
		String errorMsg = json.getString("message");
		
		final Intent intent = new Intent(MSG_QQCLOUD_ERROR);
		intent.putExtra("errorCode", errorCode);
		intent.putExtra("errorMsg", errorMsg);
		mContext.sendBroadcast(intent);
	}
	
	public QQCloudFileInfo getFileInfo(int index) {
		return QQFileList.get(index);
	}

	//{"code":-3002,"location":"com.qcloud.Common.Request:331","message":"api sdk throw exception!
	// protocol doesn't support input or the character Encoding is not supported.details: java.net.SocketException: sendto failed: EPIPE (Broken pipe)"}
	public void UploadMediaFile(String file, final Handler handler) {

		MyLog.e(TAG,"UploadMediaFile()--->>>>>filepath:"+file);

		final String fileName = file;
		final String privateTypeId;
		if (fileName.endsWith("mp4"))
		{
			privateTypeId = mPrivateVideoId;
		}
		else if (fileName.endsWith("jpg"))
		{
			privateTypeId = mPrivatePhotoId;
		}
		else{
			privateTypeId = mPrivateAudioId;
		}

		MyLog.e(TAG,"privateTypeId>>>>>>>>>>>>>>:"+privateTypeId);

		new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					
					File f = new File(fileName);
					long fileSize = f.length();
					MyLog.d(TAG, "starting... " + fileSize);
					UploadSize = fileSize;
					UploadStatus = 0;
					
					String fileSHA1 = SHA1.fileNameToSHA(fileName);
					
					int fixDataSize = 1024*1024*2;  //每次上传字节数，可自定义
					int firstDataSize = 1024*10;    //切片上传：最小片字节数（默认不变）,如果：dataSize + offset > fileSize,把这个值变小即可
					int tmpDataSize = firstDataSize;
					long remainderSize = fileSize;
					int tmpOffset = 0;
					int code, flag;
					String fileId;
					String result = null;
					int progStatus = 0;

					if(remainderSize<=0){
						MyLog.d(TAG, "wrong file path...");
					}
					while (remainderSize>0) {
						TreeMap<String, Object> params = new TreeMap<String, Object>();
						/*
						 * 亲，输入参数的类型，记得参考wiki详细说明
						 */
						params.put("fileSha", fileSHA1);
						params.put("fileType", "mp4");
						params.put("fileName", f.getName());
						params.put("fileSize", fileSize);
						params.put("dataSize", tmpDataSize);
						params.put("offset", tmpOffset);
						params.put("file", fileName); //多余的参数，造成异常InvocationTargetException
						params.put("isTranscode", 1);
						params.put("isScreenshot", 0);
						params.put("isWatermark", 0);
						params.put("classId", privateTypeId);

						//tags.n	否	String	视频的标签列表 TODO 未使用
//						params.put("tags.n",f.getName());

						module.setConfigRequestMethod("POST");
						MyLog.d(TAG, "module>>>>>>>>>>" + module);
						result = module.call("MultipartUploadVodFile", params);

						MyLog.d(TAG, result);

						JSONObject json_result = new JSONObject(result);
						code = json_result.getInt("code");

						MyLog.d(TAG, "code>>>>>>>>>>"+code);

						if (code == -3002) {               //服务器异常返回，需要重试上传(offset=0, dataSize=10K,满足大多数视频的上传)
							tmpDataSize = firstDataSize;
							tmpOffset = 0;
							continue;
						} else if (code == 10004) {
							UploadStatus = -1;
							return;
						}	else if (code == -24990)
						{// 服务器没有 对应的文件夹路径 {"code":-24990,"msg":"invoke set cache route fail, invalid response","offset":0}

							ToastUtils.showToast(mContext, R.string.text_server_bus);
							return;
						}else if (code != 0) {
							ToastUtils.showToast(mContext, R.string.text_server_bus);
							return;
						}
						flag = json_result.getInt("flag");

						if (flag == 1) {
							fileId = json_result.getString("fileId");
							progStatus = 100;
							Message m = new Message();
							m.what = MediaManager.MESSAGE_UPLOAD_PROGRESS;
							m.arg1 = progStatus;
							handler.sendMessage(m);
							break;
						} else {
							tmpOffset = Integer.parseInt(json_result.getString("offset"));
							progStatus = (int)((long)tmpOffset*100/fileSize);
							Message m = new Message();
							m.what = MediaManager.MESSAGE_UPLOAD_PROGRESS;
							m.arg1 = progStatus;
							handler.sendMessage(m);
						}
						MyLog.d(TAG, "tmpOffset " + tmpOffset + ", progStatus: " + progStatus);
						remainderSize = fileSize - tmpOffset;
						if (fixDataSize < remainderSize) {
							tmpDataSize = fixDataSize;
						} else {
							tmpDataSize = (int) remainderSize;
						}
						UploadStatus = tmpDataSize;
					}
					MyLog.d(TAG, "end...");
					UploadStatus = UploadSize;
				}
				catch (Exception e) {
					MyLog.d(TAG, "error..."+e.toString());
					e.printStackTrace();
				}
			}}).start(); 
		
	}

	/**
	 * 域名：vod.api.qcloud.com
	  接口名: DescribeClass
	 */
	public void getFoldersUnderPublicVideo(){

		new Thread(){
			@Override
			public void run() {
				super.run();
				runGetFoldersUnderPublicVideo();
			}
		}.start();


	}
	private void runGetFoldersUnderPublicVideo() {
		TreeMap<String, Object> params = new TreeMap<String, Object>();

		String result2 = "";
		try {
			if (module != null){

				result2 =  module.call("DescribeAllClass", params);
				MyLog.d(TAG, "response--DescribeAllClass>>>>>>>>json:" + result2.toString());

				JSONObject jobject = new JSONObject(result2);

				if (jobject.getInt("code") != 0) {

					MyLog.e(TAG, jobject.getString("message"));
					Intent intent = new Intent(MSG_QQCLOUD_ERROR);
					intent.putExtra("errorMsg", jobject.getString("message"));
					mContext.sendBroadcast(intent);
				}else {

					parseJsonFoldersUnderPublicVideo(jobject);

				}

			}

		}catch (Exception e){
			MyLog.d(TAG,""+ e.getMessage());
		}
	}

	private  void  parseJsonFoldersUnderPublicVideo(JSONObject jobject) throws JSONException{

		JSONArray dataArray =  jobject.getJSONArray("data");

		for (int i = 0 ; i < dataArray.length() ; i++)
		{
			JSONObject jsonObject =  dataArray.getJSONObject(i);

			if (jsonObject.getJSONObject("info").getString("name").equals("PUBLIC"))//PUBLIC
			{
				JSONArray jsonArray2 = jsonObject.getJSONArray("subclass");
				for (int j = 0 ; j < jsonArray2.length() ; j++)
				{
					JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
					if (jsonObject2.getJSONObject("info").getString("name").equals("VIDEO"))//PUBLIC下面的VIDEO
					{
						JSONObject object3 = jsonObject2.getJSONObject("info");
						folderList .clear();
						CloudFolderBean bean = new CloudFolderBean(object3.getInt("id"),
								object3.getString("name"),
								object3.getInt("file_num"),
								false);
						folderList.add(bean);//添加根 VIDEO

						JSONArray subclasses = jsonObject2.getJSONArray("subclass");//is PUBLIC-VIDEO ,subclass
						for (int z = 0 ; z < subclasses.length() ; z++)
						{
							JSONObject object4  =  subclasses.getJSONObject(z);
							JSONObject info4 = object4.getJSONObject("info");
							CloudFolderBean bean2 = new CloudFolderBean(info4.getInt("id"),
									info4.getString("name"),
									info4.getInt("file_num"),
									false);
							folderList.add(bean2);
						}

						//发送广播
						Intent intent = new Intent(MSG_QQCLOUD_FOLDER);
						mContext.sendBroadcast(intent);

					}
				}

			}
		}

//		JSONObject jsonObject =  dataArray.getJSONObject(1).getJSONArray("subclass").getJSONObject(0);
//		JSONObject info =  jsonObject.getJSONObject("info");//is PUBLIC-VIDEO
//
//		folderList .clear();
//		CloudFolderBean bean = new CloudFolderBean(info.getInt("id"),
//				info.getString("name"),
//				info.getInt("file_num"),
//				false);
//		folderList.add(bean);//添加根 VIDEO
//
//		JSONArray subclasses =  jsonObject.getJSONArray("subclass");//is PUBLIC-VIDEO ,subclass
//
//		for (int i = 0 ; i < subclasses.length() ; i++)
//		{
//			JSONObject jsonObject2  =  subclasses.getJSONObject(i);
//			JSONObject info2 = jsonObject2.getJSONObject("info");
//			CloudFolderBean bean2 = new CloudFolderBean(info2.getInt("id"),
//					info2.getString("name"),
//					info2.getInt("file_num"),
//					false);
//			folderList.add(bean2);
//		}
	}

}
