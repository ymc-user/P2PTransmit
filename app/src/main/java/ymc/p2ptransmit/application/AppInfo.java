package ymc.p2ptransmit.application;

import android.graphics.drawable.Drawable;

/**
 * AppInfo类，保存了app的信息
 * @author Group
 * @Time 2017-2-16
 *
 */

public class AppInfo {
    private Drawable appIcon;               //图标
    private String packageName;             //包名
    private String appVersion;              //版本号
    private String appLabel;                //app标签
    private String apkPath;

    //函数定义
    public AppInfo(){};


    public AppInfo(String appLabel, String packageName)
    {
        this.appLabel = appLabel;
        this.packageName = packageName;
    }


    public AppInfo(String appLabel, String packageName, Drawable appIcon)
    {
        this.appLabel = appLabel;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }


    public String getPackageName()
    {
        return this.packageName;
    }


    public String getAppVersion()
    {
        return this.appVersion;
    }


    public void setAppIcon(Drawable icon)
    {
        this.appIcon = icon;
    }


    public void setAppVersion(String version)
    {
        this.appVersion = version;
    }


    public Drawable getAppIcon()
    {
        return this.appIcon;
    }


    public String getAppLabel()
    {
        return this.appLabel;
    }


    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }


    public void setAppLabel(String label)
    {
        this.appLabel = label;
    }


    public void setApkPath(String value)
    {
        this.apkPath = value;
    }


    public String getApkPath()
    {
        return this.apkPath;
    }
}
