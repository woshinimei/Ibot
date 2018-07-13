package com.infomax.ibotncloudplayer.bean;

import java.io.Serializable;

/**
 * Created by juying on 2016/10/21.
 * 云端文件夹bean
 */
public class CloudFolderBean implements Serializable{

    static final long serialVersionUID =-100L;
    /** 文件夹classid*/
    public int id;
    /** 文件夹名称*/
    public String name;
    /** 文件夹是否选中*/
    public boolean selected;
    public int file_num;
    public int level;
    public int parent_id;

    public CloudFolderBean(int id,String name,int file_num, boolean selected) {
        this.id = id;
        this.name = name;
        this.file_num = file_num;
        this.selected = selected;
    }


}
