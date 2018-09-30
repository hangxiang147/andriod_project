package com.weimi.weimichat.bean.chat;

import com.weimi.weimichat.bean.BaseResp;

/**
 * Created by yxl on 2018/9/14.
 */

public class ChatResp extends BaseResp{

    private String userId;

    private String token;

    private String groupImgUrl;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getGroupImgUrl() {
        return groupImgUrl;
    }

    public void setGroupImgUrl(String groupImgUrl) {
        this.groupImgUrl = groupImgUrl;
    }
}
