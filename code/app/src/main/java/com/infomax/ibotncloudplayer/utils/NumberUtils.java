package com.infomax.ibotncloudplayer.utils;

import android.text.TextUtils;

/**
 * Created by jy on 2017/3/13 ;17:50.<br/>
 * ibotnCloudPlayer_Studio <br/>
 *
 * @description:
 * 号码工具类：
 */
public class NumberUtils {

    /**
     *
     * @param src
     * @param maxLength  字符串最大长度
     * @return
     * src如果长度小于指定长度maxLength，前面补0。缺几位补几个0。
     */
    public static String completeString(String src,int maxLength){
        String result = "";

        if (TextUtils.isEmpty(src))
        {
            return result;
        }else {

            result = src;
            if (result.length() < maxLength)
            {
               result = "0" + src;

                //递归，如果还不够长度，继续添加
                return  completeString(result,maxLength);
            }
        }

        return result;
    }
}
