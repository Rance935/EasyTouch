package com.rance.easypoint.easypoint.common;

/**
 * Created by Administrator on 2016/9/19.
 */
public class Constants {
    public static final String TAG = "EasyTouch";

    public static final int INSTALLED = 0; // 表示已经安装，且跟现在这个apk文件是一个版本
    public static final int UNINSTALLED = 1; // 表示未安装
    public static final int INSTALLED_UPDATE = 2; // 表示已经安装，版本比现在这个版本要低，可以点击按钮更新

    public static final int SYSTEM_APPLICATION = 0; // 表示系统应用
    public static final int PERSONAL_APPLICATION = 1; // 表示个人应用

    public static final String APPLICATION_URL = "http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.mm";

    //应用管理页面标识
    public static final int UPDATE = 0;
    public static final int UNINSTALL = 1;
    public static final int INSTALLATION_PACKAGE = 2;
    public static final int ADD_APPLICATION = 3;

    public static final String WINDOWSWITCH = "WINDOWSWITCH";
}
