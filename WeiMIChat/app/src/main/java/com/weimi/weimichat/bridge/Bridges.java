
package com.weimi.weimichat.bridge;

/**
 * <与Bridge模块一一对应，用以在BridgeFactory获取某个Bridge对应的Key>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class Bridges {
    /**
     * 本地缓存(sd卡存储和手机内部存储)
     */
    public static final String LOCAL_FILE_STORAGE = "com.weimi.weimichat.LOCAL_FILE_STORAGE";

    /**
     * SharedPreference缓存
     */
    public static final String SHARED_PREFERENCE = "com.weimi.weimichat.SHARED_PREFERENCE";

    /**
     * 安全
     */
    public static final String SECURITY = "com.weimi.weimichat.SECURITY";

    /**
     * 用户Session
     */
    public static final String USER_SESSION = "com.weimi.weimichat.USER_SESSION";

    /**
     * CoreService，主要维护功能模块
     */
    public static final String CORE_SERVICE = "com.weimi.weimichat.CORE_SERVICE";


    /**
     * 数据库模块
     */
    public static final String DATABASE = "com.weimi.weimichat.DATABASE";

    /**
     * http请求
     */
    public static final String HTTP = "com.weimi.weimichat.HTTP";

}
