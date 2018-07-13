package com.ysx.qqcloud;

import java.io.Serializable;

public class QQCloudFileInfo implements Serializable {

	public String fileId = "";
	public String fileName = "";
	public String updateTime = "";
	public String imageUrl = "";
	public String imagePath = "";
	public long downloadID = 0;
	public String videoURL0 = "";
	public String videoURL20 = "";
	public String videoURL30 = "";
	public String localVideoURL20 = "";
	public String localVideoURL30 = "";

	/**视频大小*/
	public String fileSize = "";

	@Override
	public boolean equals(Object o) {
		/**
		 * 云端历史记录，每一个条目都会重复添加，linkedlist.contains(o),
		 * o 已经重写equals方法。--原因--点击播放后当前bean，的属性值变了。
		 * Equals只需重写比较部分属性。
		 * videoURL0 ,videoURL20 ,videoURL30 点击播放 会重新赋值
		 *
		 */

		if (o instanceof QQCloudFileInfo)
		{
			QQCloudFileInfo bean = (QQCloudFileInfo) o;

			return this.fileId.equals(bean.fileId)
					&& this.fileName.equals(bean.fileName)
					&& this.updateTime.equals(bean.updateTime)
					&& this.imageUrl.equals(bean.imageUrl)
					&& this.imagePath.equals(bean.imagePath)
//					&& this.downloadID == bean.downloadID
//					&& this.videoURL0.equals(bean.videoURL0)
//					&& this.videoURL20.equals(bean.videoURL20)
//					&& this.videoURL30.equals(bean.videoURL30)
					&& this.localVideoURL20.equals(bean.localVideoURL20)
					&& this.localVideoURL30.equals(bean.localVideoURL30);
		}

		return super.equals(o);
	}
}
