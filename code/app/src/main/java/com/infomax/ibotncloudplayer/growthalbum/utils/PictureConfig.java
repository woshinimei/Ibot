package com.infomax.ibotncloudplayer.growthalbum.utils;

import android.support.annotation.DrawableRes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zhuyong on 2017/6/8.
 */

public class PictureConfig {
    public static boolean mIsShowNumber = true;//是否显示数字下标
    public static boolean needDownload = false;
    public static String path = "pictureviewer";
    public static int resId = 0;//占位符资源图片
    public static int position = 0;//下标
    public static ArrayList<String> list;
    //Begin jinlong.zou
    public static ArrayList<String> thumbnialList;
    //End jinlong.zou

    public PictureConfig(Builder builder) {
        this.mIsShowNumber = builder.mIsShowNumber;
        this.needDownload = builder.needDownload;
        this.path = builder.path;
        this.resId = builder.resId;
        this.position = builder.position;
        this.list = builder.list;
        //Begin jinlong.zou
        this.thumbnialList = builder.thumbnailList;
        //End jinlong.zou
    }

    public static class Builder implements Serializable {

        private boolean mIsShowNumber = true;//是否显示数字下标
        private boolean needDownload = false;
        private int resId = 0;
        private String path = "pictureviewer";
        private int position = 0;
        private ArrayList<String> list;
        //Begin jinlong.zou
        private ArrayList<String> thumbnailList;
        //End jinlong.zou

        public Builder() {
            super();
        }

        //Begin jinlong.zou
        public Builder setThumbnailListData(ArrayList<String> list) {
            this.thumbnailList = list;
            return this;
        }
        //End jinlong.zou

        public Builder setListData(ArrayList<String> list) {
            this.list = list;
            return this;
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder setIsShowNumber(boolean mIsShowNumber) {
            this.mIsShowNumber = mIsShowNumber;
            return this;
        }

        public Builder needDownload(boolean needDownload) {
            this.needDownload = needDownload;
            return this;
        }

        public Builder setDownloadPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setPlacrHolder(@DrawableRes int resId) {
            this.resId = resId;
            return this;
        }

        public PictureConfig build() {
            return new PictureConfig(this);
        }
    }

}
