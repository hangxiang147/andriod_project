package com.weimi.weimichat.constant;

/**
 * <网络请求url地址>
 * <功能详细描述>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class URLUtil {

    /**
     * 服务器地址
     */
    //public static final String SERVER = "http://192.168.1.6:8080/weimi/";

    public static final String SERVER = "http://weimi.free.idcfengye.com/weimi/";

    /**
     * 用户登陆
     */
    public static final String USER_LOGIN = SERVER + "index/login";
    /**
     * 用户注册
     */
    public static final String USER_REGISTER = SERVER + "index/register";
    /**
     * 验证邮箱是否存在，存在发送验证码邮件
     */
    public static final String CHECK_EMAIL_EXIST = SERVER + "index/checkIsEmail";
    /**
     * 验证邮箱验证码是否存在
     */
    public static final String CHECK_EMAIL_CODE = SERVER + "index/checkEmailCode";

    public static final String UPDATE_PWD = SERVER + "index/updatePwd";
    /**
     * 获取消息列表
     */
    public static final String NOTICE_LIST = SERVER + "index/getAllNewsContent";

    public static final String NOTICE_DETAIL = SERVER + "index/selectNewsById";

    /**
     * 更新用户信息（昵称、邮箱、性别）
     */
    public static final String UPDATE_USERINFO = SERVER + "index/updateUserInfo";

    public static final String RONG_TOKEN = SERVER + "userToken/insertUserAppToken";
    /**
     * 获取用户列表
     */
    public static final String GET_ALL_USER = SERVER + "index/getAllUser";
    /**
     * 创建群组
     */
    public static final String CREATE_GROUP = SERVER + "userToken/createGroup";
    /**
     * 加入群组
     */
    public static final String ADD_MEMBER = SERVER + "userToken/addGroup";
    /**
     * 退出群组
     */
    public static final String QUIT_GROUP= SERVER + "userToken/quitGroup";

    public static final String DISMISS_GROUP = SERVER + "userToken/dismissGroup";
    /**
     * 移除群成员
     */
    public static final String KICK_MEMBER = SERVER + "userToken/kickGroupMember";
    /**
     * 我的群组
     */
    public static final String GET_MY_GROUP = SERVER + "userToken/getAllUserGroup";

    public static final String GET_USER_INFO = SERVER + "userToken/getUserInfo";

    public static final String GET_GROUP_INFO = SERVER + "userToken/getGroupInfo";
    /**
     * 我未加入的群组
     */
    public static final String GET_ALL_GROUP = SERVER + "userToken/getAllGroup";
    /**
     * 申请加入群组
     */
    public static final String APPLY_JOIN_GROUP = SERVER + "userToken/applyJoinGroup";
    /**
     * 获取群成员
     */
    public static final String GET_GROUP_MEMBER = SERVER + "userToken/getGroupUser";
    /**
     * 获取群组个人申请列表
     */
    public static final String GET_APPLY_GROUP = SERVER + "userToken/getApplyGroupsByUserId";
    /**
     * 审批群组申请
     */
    public static final String AUDIT_GROUP_APPLY = SERVER + "userToken/auditGroupApply";
    /**
     * 更新群组信息
     */
    public static final String UPDATE_GROUP_INFO = SERVER + "userToken/updateGroupInfo";

}
