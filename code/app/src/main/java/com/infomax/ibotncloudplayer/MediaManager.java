package com.infomax.ibotncloudplayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.infomax.ibotncloudplayer.utils.Constant;
import com.infomax.ibotncloudplayer.utils.DevicePath;
import com.infomax.ibotncloudplayer.utils.FileUtils;
import com.infomax.ibotncloudplayer.utils.MyLog;
import com.infomax.ibotncloudplayer.utils.SDUtils;
import com.infomax.ibotncloudplayer.utils.ThreadUtils;

/**
 * modify by jy // TODO: 2017/3/14
 * 跟踪代码添加注释；发现问题。373 行if(equals)后面有 分号，造成
 * ibotn上面的sd卡放到其它如手机上后，再放回到ibotn上面。【教育内容】-【成长照片】【成长视频不能显示】
 * storage/sdcard/DCIM/100ANDRO
 * storage/sdcard/DCIM/Camera
 */
public class MediaManager {

    private static final String TAG = "MediaManager";

    public static final String KEY_MEDIA_TYPE = "key_media_type";
    public static final String KEY_IMAGE_PATH = "key_image_path";

    /**
     * 成长记录-成长相片,类型
     */
    public static final int MEDIA_TYPE_LOCAL_PHOTO = 0;
    /**
     * 成长记录-成长视频,类型
     */
    public static final int MEDIA_TYPE_LOCAL_VIDEO = 1;
    /**
     * 云端视频，使用腾讯云
     */
    public static final int MEDIA_TYPE_CLOUD_VIDEO = 2;

    public static final int MESSAGE_DELETE_CHECKED = 1;
    public static final int MESSAGE_DELETE_CHECKED_DONE = 2;
    public static final int MESSAGE_UPDATE_LIST = 3;
    public static final int MESSAGE_UPLOAD_PROGRESS = 4;

    public static final String ACTION_OPEN_EDIT_MODE = "com.infomax.ibotncloudplayer.open_edit_mode";
    public static final String ACTION_CLOSE_EDIT_MODE = "com.infomax.ibotncloudplayer.close_edit_mode";
    public static final String ACTION_TOGGLE_EDIT_MODE = "com.infomax.ibotncloudplayer.toggle_edit_mode";
    public static final String ACTION_OPEN_FULLSCREEN = "com.infomax.ibotncloudplayer.open_fullscreen";

    public static final String THUMBNAILS_FOLDER = "thumbnails";
    public static final String SCREENSHOTS_FOLDER = "Screenshots";

    /**
     * 系统/storage/sdcard/DCIM/下面 Camera文件夹
     */
    public static final String CAMERA_FOLDER = "Camera";
    private Context mContext = null;
    private int mMediaType = MEDIA_TYPE_LOCAL_PHOTO;

    private int mIdColumnIndex = -1;
    private int mNameColumnIndex = -1;
    private int mSizeColumnIndex = -1;
    private int mDataColumnIndex = -1;
    private int mDateTmeColumnIndex = -1;
    private int mDurationTmeColumnIndex = -1;
    private int mDateTmeAddColumnIndex = -1;

    private ArrayList<DayMediaGroup> mDayMediaGroupList = null;
    private Handler mParentHandler = null;
    /**
     * 分组完成标志
     */
    private boolean mGroupingDone = false;
    public static final int THUMBNAILS_COLUMNS = 4;

    /**
     * default false ,can load ;true ,cancel all load
     */
    private boolean isCancel;

    public MediaManager(Context c, Handler parentHandler) {
        mContext = c;
        mParentHandler = parentHandler;
        mDayMediaGroupList = new ArrayList<DayMediaGroup>();
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void clearItemGroups() {
        mGroupingDone = false;
        if (mDayMediaGroupList != null) {
            for (DayMediaGroup dg : mDayMediaGroupList) {
                dg.clearItems();
            }

            mDayMediaGroupList.clear();
        }
    }

    private Runnable mstartGetDataRunnable = new Runnable() {

        @Override
        public void run() {

            MyLog.d(TAG, "mstartGetDataRunnable>>>run()>>>start");
            //注意：这种写法，是主线程。
            MyLog.d(TAG, ">>>>mstartGetDataRunnable>>>>>>>>>thread-name:" + Thread.currentThread().getName());

            //检查是否有u盘
            String usbStoragePath = DevicePath.getInstance().getUsbStoragePath();
            MyLog.d(TAG, "mstartGetDataRunnable>>>run()>>>start>>usbStoragePath:" + usbStoragePath);
            if (TextUtils.isEmpty(usbStoragePath)) {
                //没有优盘时
                String cameraPath = getSingleCameraPath(false);
                getDayMediaItems(cameraPath);
            } else {
                //有优盘，将优盘/storage/uhost/DCIM/Camera/下的文件也添加进来,内置卡的也加入进来
                String usbStoragePathCamera = usbStoragePath + File.separator + "DCIM" + File.separator + "Camera";
                getDayMediaItems(usbStoragePathCamera);
                String cameraPath = getSingleCameraPath(false);
                getDayMediaItems(cameraPath);
            }

            mGroupingDone = true;
            MyLog.d(TAG, "getDayMediaItems() done>>>isCancel:" + isCancel);
            if (!isCancel) {
                if (mParentHandler != null) {
                    mParentHandler.sendEmptyMessage(MESSAGE_UPDATE_LIST);
                }
            }
        }
    };

    public ArrayList<DayMediaGroup> getGroupList() {
        return mDayMediaGroupList;
    }

    /**
     * 获取【拍照】【开始录像】存储路径
     * 1.之前存在内置SD卡中的路径为 ，
     * 照片: /storage/sdcard/DCIM/Camera/
     * 录像文件：/storage/sdcard/DCIM/Camera/
     * 2.以用户的优盘存储优先，如果优盘不存在，就使用之前的内置sd卡 /storage/sdcard/DCIM/Camera/
     * 3.优盘路径为：/storage/uhost/DCIM/Camera/
     *
     * @param mediaType
     */
    public void startGetData(int mediaType) {
        Log.d(TAG, "startGetData()>>>>>>>>>mediaType:" + mediaType);
        mMediaType = mediaType;
        mGroupingDone = false;

        ThreadUtils.runOnBackThread(mstartGetDataRunnable);
    }

    /**
     * getDayMediaItems
     *
     * @param fileRootPath
     */
    private void getDayMediaItems(String fileRootPath) {
        MyLog.d(TAG, TAG + ">>>>getDayMediaItems()>>>>fileRootPath:" + fileRootPath);
        Cursor cr = initCursor(fileRootPath);
        if (cr != null) {
            MyLog.d(TAG, TAG + ">>>Total media count:" + cr.getCount());
            if (cr.getCount() > 0) {
                cr.moveToFirst();
                try {
                    if (mMediaType == MEDIA_TYPE_LOCAL_VIDEO) {
                        mIdColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                        mNameColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                        mSizeColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                        mDataColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        mDateTmeColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);
//                        mDurationTmeColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    } else if (mMediaType == MEDIA_TYPE_LOCAL_PHOTO) {
                        mIdColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                        mNameColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                        mSizeColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                        mDataColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        mDateTmeColumnIndex = cr.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                    }

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                do {
                    long id = 0;
                    long date = 0;
                    String path = null;
                    String name = null;
                    long size = 0;
                    long duration = 0;

                    if (mIdColumnIndex != -1) {
                        id = Long.parseLong(cr.getString(mIdColumnIndex));
                    }
                    if (mDateTmeColumnIndex != -1) {
                        date = Long.parseLong(cr.getString(mDateTmeColumnIndex));
                    }
                    if (mSizeColumnIndex != -1) {
                        size = Long.parseLong(cr.getString(mSizeColumnIndex));
                    }
                    if (mDataColumnIndex != -1) {
                        path = cr.getString(mDataColumnIndex);
                    }
                    if (mNameColumnIndex != -1) {
                        name = cr.getString(mNameColumnIndex);
                    }
                    if (mDurationTmeColumnIndex != -1) {
                        duration = cr.getInt(mDurationTmeColumnIndex);

                    }
                    Log.e("--ColumnIndex-", mDurationTmeColumnIndex + "");
                    Log.e("--duration-", duration + "");
                    MyLog.d(TAG, TAG + ">>>getDayMediaItems()>>>>>>>>>id:" + id + ",path:" + path + ",name:" + name);

                    DayMediaItem item = new DayMediaItem(mMediaType, id, date, path, name, size, duration);
                    mDayMediaGroupList = groupingDayMediaItems(mDayMediaGroupList, item);
                } while (cr.moveToNext() && !isCancel);
            }
            cr.close();
        }
    }

    /**
     * 组合每一天的Media文件
     *
     * @param itemGroupList
     * @param item
     * @return
     */
    private ArrayList<DayMediaGroup> groupingDayMediaItems(ArrayList<DayMediaGroup> itemGroupList, DayMediaItem item) {
        boolean isSameDay = false;
        for (int i = 0; i < itemGroupList.size(); i++) {
            if (isSameDate(itemGroupList.get(i).getDate(), item.getDate())) {//比较当前文件日期，在itemGroupList中是否有同一天日期的，如果有直接添加到该组
                itemGroupList.get(i).getItemList().add(item);
                isSameDay = true;
                break;
            }
        }

        if (!isSameDay) {
            //使用当前文件的时间作为 该组的时间
            DayMediaGroup dg = new DayMediaGroup(mMediaType, item.getDate());
            dg.getItemList().add(item);
            itemGroupList.add(dg);
        }

        return itemGroupList;
    }

    /**
     * 是否是同一天的文件
     *
     * @param date1
     * @param date2
     * @return
     */
    private boolean isSameDate(long date1, long date2) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String year1 = df.format(new Date(date1));
        String year2 = df.format(new Date(date2));
        if (!year1.equals(year2)) {
            return false;
        }

        df = new SimpleDateFormat("MM");
        String month1 = df.format(new Date(date1));
        String month2 = df.format(new Date(date2));
        if (!month1.equals(month2)) {
            return false;
        }

        df = new SimpleDateFormat("dd");
        String day1 = df.format(new Date(date1));
        String day2 = df.format(new Date(date2));
        if (!day1.equals(day2)) {
            return false;
        }

        return true;
    }

    /**
     * init cursor
     *
     * @param path
     * @return
     */
    private Cursor initCursor(String path) {
        MyLog.d(TAG, ">>>>>initCursor()>>>>>:" + path);
        Cursor cursor = null;
        if (!TextUtils.isEmpty(path)) {
            cursor = queryFiles(path);
        }
        return cursor;
    }

    public static ArrayList<String> getCameraPaths(boolean cloudType) {
        String DCIM_Path;
        if (cloudType) {
            DCIM_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        } else {
            DCIM_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

            MyLog.d(TAG, ">>>>getCameraPaths>>>>Environment.DIRECTORY_DCIM:" + Environment.DIRECTORY_DCIM);//DCIM
        }
        MyLog.d(TAG, ">>>>getCameraPaths()>>>>DCIM_Path:" + DCIM_Path);//DCIM
        ArrayList<String> CameraPaths = new ArrayList<String>();
        //String DCIM_Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File CameraDirectory = new File(DCIM_Path);
        File[] files = CameraDirectory.listFiles();
        if (files != null) {
            for (File CurFile : files) {
                Log.d(TAG, ">>>>getCameraPaths()>>>CurFile:" + CurFile.getName());
                if (CurFile.isDirectory()) {
                    if (!CurFile.getName().contains(MediaManager.THUMBNAILS_FOLDER) &&
                            !CurFile.getName().contains(MediaManager.SCREENSHOTS_FOLDER)) {
                        Log.d(TAG, ">>>>getCameraPaths()>>>CurFile:" + CurFile.getAbsolutePath());
                        CameraPaths.add(CurFile.getAbsolutePath());
                    }
                }
            }
        }
        Log.d(TAG, ">>>>getCameraPaths()>>>CameraPaths:" + CameraPaths);
        return CameraPaths;
    }

    /**
     * 1.【成长照片】【成长视频】调用时。获取camera路径。 即为：/storage/sdcard/DCIM/Camera/ <br/>
     * 2.如果是云端播放的视频时对应，路径在内置sd卡。现切换到外置sd卡[TODO: 2017/3/20  ] <br/>
     *
     * @param cloudType true 供腾讯云使用。false，供本地使用【成长视频】【成长照片】使用。
     * @return 没有外置sd卡就返回null。
     */
    public static String getSingleCameraPath(boolean cloudType) {
        String rootPath = null;
        //if(mediaType == MediaManager.MEDIA_TYPE_LOCAL_PHOTO || mediaType == MediaManager.MEDIA_TYPE_LOCAL_VIDEO) {
        MyLog.d(TAG, ">>>>getSingleCameraPath()>>>>cloudType:" + cloudType
                + ",Environment.getExternalStorageState():" + Environment.getExternalStorageState());

        if (cloudType) {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return rootPath;
            }

            if (Constant.TengXunYun.PATH_STORAGE_TYPE_EXTERNALSD_OR_INTERNALDS == 0) {
                rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                return rootPath;

            } else if (Constant.TengXunYun.PATH_STORAGE_TYPE_EXTERNALSD_OR_INTERNALDS == 1) {
                //////////////新的方式，外置sd卡，但不能使用系统downloadmanager下载了。
                boolean createOrExistsDir = FileUtils.createOrExistsDir(Constant.TengXunYun.ROOT_PATH_FOR_TENGXUYUN_VIDEO_STORAGE);
                if (createOrExistsDir) {

                    long sDFreeSize = SDUtils.getSDFreeSize(Constant.Config.ROOT_PATH_FIRST_EXTERNAL_SD_SD_EXT);
                    MyLog.d(TAG, ">>>>getSingleCameraPath()>>>>ROOT_PATH_FOR_TENGXUYUN_VIDEO_STORAGE:" + Constant.TengXunYun.ROOT_PATH_FOR_TENGXUYUN_VIDEO_STORAGE
                            + ",sDFreeSize:" + sDFreeSize);
                    if (sDFreeSize >= Constant.Config.MAX_WRITE_SPACE_FOR_SD_MB) {
                        return Constant.TengXunYun.ROOT_PATH_FOR_TENGXUYUN_VIDEO_STORAGE;
                    } else {
                        return rootPath;
                    }

                } else {

                    return rootPath;
                }
                ////////////
            }

        } else {
            rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            MyLog.d(TAG, ">>>>getSingleCameraPath()>>>>Environment.DIRECTORY_DCIM:" + Environment.DIRECTORY_DCIM);//DCIM
        }
        MyLog.d(TAG, ">>>>getSingleCameraPath()>>>>DCIM_Path:" + rootPath);

        File CameraDirectory = new File(rootPath);
        File[] files = CameraDirectory.listFiles();
        if (files != null && files.length != 0) {
            //Log.d(TAG, "files length: " + files.length + ", " + files[0].getAbsolutePath());
            MyLog.d(TAG, ">>getSingleCameraPath()>>>>>CurFile:" + files.length);

            for (File CurFile : files) {
                /**
                 * : >>getSingleCameraPath()>>>001>>CurFile.getAbsolutePath:/storage/sdcard/DCIM/100ANDRO
                 *: >>getSingleCameraPath()>>>001>>CurFile.getAbsolutePath:/storage/sdcard/DCIM/Camera
                 */
                MyLog.d(TAG, ">>getSingleCameraPath()>>>(total-path)>>CurFile.getAbsolutePath:" + CurFile.getAbsolutePath());
            }

            for (File CurFile : files) {
                MyLog.d(TAG, ">>>>getSingleCameraPath()>>>> CurFile.getAbsolutePath():" + CurFile.getAbsolutePath()
                        + ",CurFile.isDirectory():" + CurFile.isDirectory());
                if (CurFile.isDirectory()) {
                    String fileName = CurFile.getName();
                    boolean equals = CurFile.getName().equals(MediaManager.CAMERA_FOLDER);
                    Log.d(TAG, ">>getSingleCameraPath()>>>>>fileName:" + fileName
                            + ",equal:" + equals);
                    if (equals) {
                        Log.d(TAG, ">>getSingleCameraPath()>>>>>CurFile:" + CurFile.getAbsolutePath());
                        return CurFile.getAbsolutePath();
                    }
                }
            }
        } else {
            Log.w(TAG, ">>getSingleCameraPath()>>>>DCIM_Path:" + rootPath);
        }
        MyLog.d(TAG, ">>>>getSingleCameraPath()>>>end>>>>DCIM_Path:" + rootPath);
        return rootPath;
    }

    /**
     * query files
     *
     * @param path
     * @return
     */
    private Cursor queryFiles(String path) {
        MyLog.d(TAG, TAG + ">>>>queryFiles()>>>>>path:" + path);

        if (mMediaType == MEDIA_TYPE_LOCAL_VIDEO) {
            String[] proj = {MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DATE_TAKEN};

            if (path != null) {
                StringBuilder selection = new StringBuilder();
                selection.append("(" + MediaStore.Video.Media.DATA + " LIKE '" + path + "/%')");
                return mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, selection.toString(), null, MediaStore.Video.Media.DATE_MODIFIED + " DESC");
            } else {
                return mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj, null, null, MediaStore.Video.Media.DATE_MODIFIED + " DESC");
            }
        } else if (mMediaType == MEDIA_TYPE_LOCAL_PHOTO) {
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_TAKEN};

            if (path != null) {
                StringBuilder selection = new StringBuilder();
                selection.append("(" + MediaStore.Images.Media.DATA + " LIKE '" + path + "/%')");
                return mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, selection.toString(), null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            } else {
                return mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            }
        }

        return null;
    }
}
