package com.qcloud.Module;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

import com.infomax.ibotncloudplayer.utils.MyLog;
import com.qcloud.Utilities.SHA1;

public class Vod extends Base {
	private final static String TAG = Vod.class.getSimpleName();
	public Vod(){
		serverHost = "vod.api.qcloud.com";
	}

	public String MultipartUploadVodFile(TreeMap<String, Object> params) throws NoSuchAlgorithmException, IOException {
		serverHost = "vod.qcloud.com";
		
		String actionName = "MultipartUploadVodFile";

        String fileName = params.get("file").toString();

		MyLog.e(TAG,"fileName:"+fileName);
        params.remove("file");
        File f= new File(fileName);  
        
        if (!params.containsKey("fileSize")){
        	params.put("fileSize", f.length());
        }
        if (!params.containsKey("fileSha")){
        	params.put("fileSha", SHA1.fileNameToSHA(fileName));
        }
        MyLog.e(TAG,"actionName:"+actionName+",params:"+params+",fileName:"+fileName);
        return call(actionName, params, fileName);
	}
}
