package com.infomax.ibotncloudplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jy on 2017/2/6 ;15:02.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 */
public class DateUtils {
    /**
     *
     * @param date
     * @param formatType  if formatType is 1 ："yyyyMMddHHmmss" ;2: "MM-dd HH:mm:ss"
     * @return
     */
    public static String formatDate(Date date,int formatType){
        SimpleDateFormat dateFormat = null;
        if (formatType == 1)
        {
            dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        }else if (formatType == 2)
        {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        }
        if (dateFormat == null)
        {
            return null;
        }
        return  dateFormat.format(date);
    }
}
