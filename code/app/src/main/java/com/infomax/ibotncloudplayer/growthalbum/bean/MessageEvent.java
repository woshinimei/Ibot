package com.infomax.ibotncloudplayer.growthalbum.bean;

/**
 * @Author:create by jinlong.zou
 * @Date: 2018/3/26.
 * @Function:
 */

public class MessageEvent {
    private static final String TAG = MessageEvent.class.getSimpleName();

    public static final  int GET_MARK_STATUS = 0;
    public static final  int SET_MARK_STATUS = 1;
    public static final  int GET_EDIT_STATUS = 2;


    public int type ;
    public Event event;

    public void sendMessageEvent(Event event, int type){
        this.type = type;
        this.event = event;
    }

    public static class Event{
        private boolean isMark;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        private int position;

        public boolean isMark() {
            return isMark;
        }

        public void setMark(boolean mark) {
            isMark = mark;
        }

        public boolean isEdit() {
            return isEdit;
        }

        public void setEdit(boolean edit) {
            isEdit = edit;
        }

        private boolean isEdit;

    }

}
