package com.infomax.ibotncloudplayer.growthalbum;

import java.io.Serializable;

/**
 * Created by jy on 2018/3/7.<br/>
 * 简单的响应结果对应的实体
 */
public class CommonSimpleResponseBean implements Serializable {

    /*
     *
     "Status": 200,
     " Message ": ok
     */
    public int Status;
    public String Message;

    @Override
    public String toString() {
        return "CommonSimpleResponseBean{" +
                "Status=" + Status +
                ", Message='" + Message + '\'' +
                '}';
    }
}
