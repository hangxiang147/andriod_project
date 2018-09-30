package com.weimi.weimichat.bridge.cache.sharePref;

import android.content.Context;

import com.weimi.weimichat.capabilities.cache.BaseSharedPreference;


/**
 * <用户信息缓存>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class EBSharedPrefUser extends BaseSharedPreference {
    /**
     * 用户userId
     */
    public static final String USER_ID = "userId";

    public static final String EMAIL = "email";

    public static final String NICK_NAME = "nickName";

    public static final String GENDER = "gender";

    public static final String INVITE_CODE = "inviteCode";

    public static final String HEADER_URL = "headerUrl";

    public static final String RONG_TOKEN = "rongToken";

    public EBSharedPrefUser(Context context, String fileName) {
        super(context,fileName);
    }
}
