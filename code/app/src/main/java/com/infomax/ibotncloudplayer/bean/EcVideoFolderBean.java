package com.infomax.ibotncloudplayer.bean;

/**
 * Created by juying on 2016/10/17. <br/>
 * 教育内容-本地视频文件夹 bean <br/>
 * 因数据格式一样，所以该类可以供视频，音乐使用
 */
public class EcVideoFolderBean extends BaseFolderBean{

    public EcVideoFolderBean(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }
}
