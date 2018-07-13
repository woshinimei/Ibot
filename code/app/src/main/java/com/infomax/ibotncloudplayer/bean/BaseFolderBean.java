package com.infomax.ibotncloudplayer.bean;

import java.io.Serializable;

/**
 * Created by juying on 2016/10/17.
 * 文件夹,抽象基类 bean
 */
public abstract class BaseFolderBean  implements Serializable{
    static final long serialVersionUID = 1L;
    /** 文件夹名称*/
    public String name;
    /** 文件夹是否选中*/
    public boolean selected;

    @Override
    public boolean equals(Object o) {

        if (o instanceof BaseFolderBean){
            BaseFolderBean bean = (BaseFolderBean) o;
            return this.name.equals(bean.name);
        }
        return super.equals(o);
    }
}
