package com.infomax.ibotncloudplayer.utils;
/**
 * Created by jy on 2017/2/8.
 *
 */
public class StringUtils {
    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
    /**
     * 过滤特殊字符并转换为16进制字符
     * 此时使用在：【云存储】-列表接口-字段【Originalurl : "UDlTc1MlCXdcMlRwUXgGYQFtUGtXelBmDzEFbFMoU2tRYQ8hAWcENgU2BGpVbwk3AW1WbVR9V3QHKlM8AW0NawtmBXsAFAVRURUIQQkRVX1QY1M3U2AJMFwnVG9RZQZ/AWpQald7UGQPNgV7Uz5TYFFjD2oBYgRhBWIENlUxCTQBYVZoVGFXMQdsU2kBLA1nC3IFZw=="】
     * 将 ‘/’ 转为 % + 16进制字符
     * @param str
     * @return
     */
    public static String filterCharToHexChar(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int ch = (int) c;
            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '.'
                    || c == '!' || c == '~' || c == '*' || c == '(' || c == ')')
            {
                result = result + c;
            }else
            {
                String hexString = Integer.toHexString(ch);
                result = result + "%" +  hexString;
            }
        }
        return result;
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

}
