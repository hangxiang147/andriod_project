package com.weimi.weimichat;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.BridgeLifeCycleSetKeeper;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.localstorage.LocalFileStorageManager;
import com.weimi.weimichat.provider.WeiMiMessage;
import com.weimi.weimichat.provider.WeiMiMessageProvider;
import com.weimi.weimichat.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.display.FadeInBitmapDisplayer;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;

import static io.rong.imkit.utils.SystemUtils.getCurProcessName;

/**
 * 
 * <应用初始化> <功能详细描述>
 * 
 * @author cyf
 * @version [版本号, 2014-3-24]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class EBApplication extends MultiDexApplication
{
    /**
     * app实例
     */
    public static EBApplication ebApplication = null;
    
    /**
     * 本地activity栈
     */
    public static List<Activity> activitys = new ArrayList<Activity>();
    
    /**
     * 当前avtivity名称
     */
    public static String currentActivityName = "";

    private static DisplayImageOptions options;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        ebApplication = this;
        BridgeFactory.init(this);
        BridgeLifeCycleSetKeeper.getInstance().initOnApplicationCreate(this);
        LocalFileStorageManager manager = BridgeFactory.getBridge(Bridges.LOCAL_FILE_STORAGE);
        Picasso picasso = new Picasso.Builder(this).downloader(
                new OkHttpDownloader(new File(manager.getCacheImgFilePath(this)))).build();
        Picasso.setSingletonInstance(picasso);
        /**
         *
         * OnCreate 会被多个进程重入，这段保护代码，确保只有您需要使用 RongIM 的进程和 Push 进程执行了 init。
         * io.rong.push 为融云 push 进程名称，不可修改。
         */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            /**
             * IMKit SDK调用第一步 初始化
             */
            RongIM.init(this);
            SealAppContext.init(this);
            WeiMiUserInfoManager.init(this);
            RongIM.registerMessageType(WeiMiMessage.class);
            RongIM.registerMessageTemplate(new WeiMiMessageProvider());
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.de_default_portrait)
                    .showImageOnFail(R.drawable.de_default_portrait)
                    .showImageOnLoading(R.drawable.de_default_portrait)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
    }


    @Override
    public void onTerminate()
    {
        super.onTerminate();
        onDestory();
    }

    /**
     * 退出应用，清理内存
     */
    private void onDestory() {
        BridgeLifeCycleSetKeeper.getInstance().clearOnApplicationQuit();
        ToastUtil.destory();
    }


    /**
     * 
     * <添加> <功能详细描述>
     * 
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void addActivity(Activity activity)
    {
        activitys.add(activity);
    }
    
    /**
     * 
     * <删除>
     * <功能详细描述>
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void deleteActivity(Activity activity)
    {
        if (activity != null)
        {
            activitys.remove(activity);
            activity.finish();
            activity = null;
        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public static DisplayImageOptions getOptions() {
        return options;
    }
}
