package com.weimi.weimichat.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by yxl on 2018/9/11.
 */

public class AppUtil {

    private static final int DEFAULT_TIME = 1000;

    private static long lastTime;
    /**
     * 获取应用名称
     * @param context
     * @return
     */
    public static String getAppName(Context context)
    {
        try
        {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本名
     * @param context
     * @return
     */
    public static String getVersionName(Context context)
    {
        try
        {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 防止用户重复点击
     * @return
     */
    public static boolean isSingle(){
        boolean isSingle ;
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastTime <= DEFAULT_TIME){
            isSingle = false;
        }else{
            isSingle = true;
        }
        lastTime = currentTime;
        return isSingle;
    }
}
