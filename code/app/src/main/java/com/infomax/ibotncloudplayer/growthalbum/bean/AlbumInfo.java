package com.infomax.ibotncloudplayer.growthalbum.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/7.
 * @Function:
 */

public class AlbumInfo implements Serializable{

    /**
     * Message : OK
     * Status : 200
     * data : [{"id":"1895","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303170456.jpg"},{"id":"1890","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303170155.jpg"},{"id":"1889","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303170135.jpg"},{"id":"1885","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303165755.jpg"},{"id":"1880","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303165155.jpg"},{"id":"1857","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303152631.jpg"},{"id":"1855","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303151430.jpg"},{"id":"1853","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303150950.jpg"},{"id":"1851","url":"http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303150450.jpg"}]
     * num : 9
     * page : 1
     * total : 9
     */

    private String Message;
    private int Status;
    private String num = "0";
    private String page;
    private String total;
    private String maxid;
    private List<DataBean> data;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getMaxid() {
        return maxid;
    }

    public void setMaxid(String maxid) {
        this.maxid = maxid;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * id : 1895
         * url : http://edu.ibotn.com/preview/kinduser/10028/1002800000586/kindpicture_011708160631B_20180303170456.jpg
         */

        private String id;
        private String url;
        private boolean isMark;
        private String thumbnail;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setMark(boolean mark) {
            isMark = mark;
        }

        public boolean isMark() {
            return isMark;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }
    }
}
