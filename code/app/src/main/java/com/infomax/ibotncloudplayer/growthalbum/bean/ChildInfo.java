package com.infomax.ibotncloudplayer.growthalbum.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/8.
 * @Function:
 */

public class ChildInfo implements Serializable{

    /**
     * Message : OK
     * Status : 200
     * data : [{"childname":"邹金龙","classid":"77","classname":"未知班级","faceid":"1001100000127","kindid":"10011","kindname":"深圳市鼎盛智能科技幼儿园","picture":"http://edu.ibotn.com/preview/work/1001100000127/zc151572297665293168.png"},{"childname":"邹金龙","classid":"79","classname":"未知班级","faceid":"1001100000553","kindid":"10011","kindname":"深圳市鼎盛智能科技幼儿园","picture":""},{"childname":"邹金龙","classid":"264","classname":"未知班级","faceid":"1002800000586","kindid":"10028","kindname":"深圳市鼎盛智能科技幼儿园","picture":"http://edu.ibotn.com/preview/work/1002800000586/zc152006968490870947.png"},{"childname":"邹金龙","classid":"283","classname":"未知班级","faceid":"1003000000623","kindid":"10030","kindname":"深圳市鼎盛智能科技幼儿园","picture":"http://edu.ibotn.com/preview/work/1003000000623/zc151996919353329978.png"}]
     * num : 4
     */

    private String Message;
    private int Status;
    private String num;
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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean implements Serializable{
        /**
         * childname : 邹金龙
         * classid : 77
         * classname : 未知班级
         * faceid : 1001100000127
         * kindid : 10011
         * kindname : 深圳市鼎盛智能科技幼儿园
         * picture : http://edu.ibotn.com/preview/work/1001100000127/zc151572297665293168.png
         */

        private String childname;
        private String classid;
        private String classname;
        private String faceid;
        private String kindid;
        private String kindname;
        private String picture;

        public String getChildname() {
            return childname;
        }

        public void setChildname(String childname) {
            this.childname = childname;
        }

        public String getClassid() {
            return classid;
        }

        public void setClassid(String classid) {
            this.classid = classid;
        }

        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }

        public String getFaceid() {
            return faceid;
        }

        public void setFaceid(String faceid) {
            this.faceid = faceid;
        }

        public String getKindid() {
            return kindid;
        }

        public void setKindid(String kindid) {
            this.kindid = kindid;
        }

        public String getKindname() {
            return kindname;
        }

        public void setKindname(String kindname) {
            this.kindname = kindname;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "childname='" + childname + '\'' +
                    ", classid='" + classid + '\'' +
                    ", classname='" + classname + '\'' +
                    ", faceid='" + faceid + '\'' +
                    ", kindid='" + kindid + '\'' +
                    ", kindname='" + kindname + '\'' +
                    ", picture='" + picture + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ChildInfo{" +
                "Message='" + Message + '\'' +
                ", Status=" + Status +
                ", num='" + num + '\'' +
                ", data=" + data +
                '}';
    }
}
