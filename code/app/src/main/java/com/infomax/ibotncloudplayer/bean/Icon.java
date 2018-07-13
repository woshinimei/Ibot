package com.infomax.ibotncloudplayer.bean;

/**
 * Created by hushaokun on 2018/6/6.
 */

public class Icon {
    private String name;
    private int url;

    public Icon(String name, int url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUrl() {
        return url;
    }

    public void setUrl(Integer url) {
        this.url = url;
    }
}
