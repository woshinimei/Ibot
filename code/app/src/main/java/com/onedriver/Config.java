package com.onedriver;

/**
 * Created by jy on 2016/12/24。<br/>
 * onedriver 相关的配置
 */
public class Config {
    /* Configurations */
    /*申请者 jy ；根据应用包名生成的下面配置*/
    public final static String CLIENT_ID = "9f6c6519-ea6e-4e84-a095-446a58aef4f4"; //This is your client ID
    public final static String REDIRECT_URI = "http://localhost"; //This is your redirect URI

    public final static String AUTHORITY_URL = "https://login.microsoftonline.com/common";  //COMMON OR YOUR TENANT ID

    private final static String AUTH_TAG = "auth"; // Search "auth" in your Android Monitor to see errors

}
