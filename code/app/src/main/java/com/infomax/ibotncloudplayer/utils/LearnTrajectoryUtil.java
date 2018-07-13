package com.infomax.ibotncloudplayer.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.bean.LearnTrajectoryBean;

import java.util.ArrayList;

import static com.infomax.ibotncloudplayer.utils.LearnTrajectoryUtil.Constant.ACTION_LEARN_TRAJECTORY;

/**
 * Created by phc on 2017/7/14 0014.
 * 发送学习轨迹广播的工具类
 */

public class LearnTrajectoryUtil {
    private static final String TAG = "LearnTrajectoryUtil";

    /**
     * 发送广播到launcher 利用launcher的udp通信给手机发消息 统一出口
     *
     * @param context
     * @param type    类型
     * @param bean 轨迹实体类
     */
    public static void sendBro(Context context, @NonNull LearnTrajectoryBean bean) {
        sendBro(context, "[" + bean.toString() + "]");

    }

    /**
     * 发送广播到launcher 利用launcher的udp通信给手机发消息 统一出口
     *
     * @param context
     * @param holder  轨迹holder
     */
    public static void sendBro(Context context, @NonNull LearnTrajectoryHolder holder) {

        if (holder.getTrajectory() == null || holder.getTrajectory().isEmpty()) {
            Log.i(TAG, "rajectoryList is null or empty,return ");
            return;
        }
        sendBro(context, holder.getTrajectory().toString());

    }

    /**
     * 发送广播到launcher 利用launcher的udp通信给手机发消息 统一出口
     *
     * @param context
     * @param strJson  轨迹对应的json字符串
     */
    private static void sendBro(Context context, @NonNull String strJson) {
        try {
            Log.i(TAG, "sendBro strJson is : " + strJson);
            Intent intent = new Intent();
            intent.setAction(ACTION_LEARN_TRAJECTORY)
                    .putExtra("message_type", "track")
                    .putExtra("message_description", strJson)
                    .putExtra("message_name", "track");
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断app是否在前台
     *
     * @param packName 包名
     * @return true在前台 反之
     */
    public static boolean isRunFore(Context context, String packName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info :
                am.getRunningAppProcesses()) {
            if (TextUtils.equals(packName, info.processName)
                    && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @IntDef({Constant.TYPE_CALL,
            Constant.TYPE_GROW_VIDEO,
            Constant.TYPE_GROW_PHOTO,
            Constant.TYPE_EDU_AUDIO,
            Constant.TYPE_EDU_VIDEO,
            Constant.TYPE_EDU_WAWALU,
            Constant.TYPE_PUZZLE,
            Constant.TYPE_BROWSER,
            Constant.TYPE_IDENTIFY_FOLLOW,
            Constant.TYPE_SCENE_LEARNING,
            Constant.TYPE_SMART_REMINDER,
            Constant.TYPE_FACE_RECOGNITION,
            Constant.TYPE_LOCATION_INFO,
            Constant.TYPE_BOOT_INFO,
            Constant.TYPE_SHUTDOWN_INFO})
    public @interface TypeConstant {
    }

 /*   *//**
     * 记录学习轨迹的接口
     *//*
    public interface LearnTrajectoryImpl {
        *//**
     * 添加学习轨迹到list中
     *
     * @param bean
     *//*
        void addTrajectory(LearnTrajectoryBean bean);

        */

    /**
     * 获取储存轨迹的list
     *
     * @return
     *//*
        ArrayList getTrajectory();
    }
*/
    public static class LearnTrajectoryHolder {
        private ArrayList<LearnTrajectoryBean> trajectoryList = new ArrayList<>();
        /**
         * 是否进入学习
         */
        private boolean isStart = false;
        /**
         * 间隔时间
         */
        private int TIME_SPACE = 5000;
        /**
         * 开始时间
         */
        private long startTime;
        /**
         * 结束时间
         */
        private long endTime;

        private LearnTrajectoryBean tempBean;

        /**
         * 添加学习轨迹到list中
         *
         * @param bean
         */
        private void addTrajectory(LearnTrajectoryBean bean) {
            if (bean != null && endTime - startTime >= TIME_SPACE) {
                bean.setEndTime(endTime);
                trajectoryList.add(bean);
                tempBean = null;
            }
        }

        /**
         * 获取储存轨迹的list
         *
         * @return
         */
        public ArrayList getTrajectory() {
            return trajectoryList;
        }

        /**
         * 设置进入某项学习
         */
        public void startLearn(LearnTrajectoryBean bean) {
            isStart = true;
            startTime = System.currentTimeMillis();
            tempBean = bean;
        }

        /**
         * 设置结束某项学习
         */
        public void endLearn() {
            isStart = false;
            endTime = System.currentTimeMillis();
            addTrajectory(tempBean);
        }

    }

    public class Constant {
        /**
         * 通话
         */
        public static final int TYPE_CALL = 0;
        /**
         * 成长视频
         */
        public static final int TYPE_GROW_VIDEO = 1;
        /**
         * 成长相片
         */
        public static final int TYPE_GROW_PHOTO = 2;
        /**
         * 教育音乐
         */
        public static final int TYPE_EDU_AUDIO = 3;
        /**
         * 教育视频
         */
        public static final int TYPE_EDU_VIDEO = 4;
        /**
         * 娃娃路
         */
        public static final int TYPE_EDU_WAWALU = 5;

        /**
         * 益智游戏
         */
        public static final int TYPE_PUZZLE = 6;
        /**
         * 浏览器
         */
        public static final int TYPE_BROWSER = 7;
        /**
         * 识别跟随
         */
        public static final int TYPE_IDENTIFY_FOLLOW = 8;
        /**
         * 场景学习
         */
        public static final int TYPE_SCENE_LEARNING = 9;
        /**
         * 智能提醒
         */
        public static final int TYPE_SMART_REMINDER = 10;
        /**
         * 人脸识别
         */
        public static final int TYPE_FACE_RECOGNITION = 11;

        /**
         * 位置信息
         */
        public static final int TYPE_LOCATION_INFO = 12;
        /**
         * 开机
         */
        public static final int TYPE_BOOT_INFO = 13;

        /**
         * 关机
         */
        public static final int TYPE_SHUTDOWN_INFO = 14;

        /**
         * 向launcher发送的广播名
         */
        protected static final String ACTION_LEARN_TRAJECTORY = "MessageDefine.LEARN_TRAJECTORY";

    }
}
