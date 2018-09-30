package com.weimi.weimichat.bean.chat;

import com.weimi.weimichat.bean.BaseResp;

import java.util.List;

/**
 * Created by yxl on 2018/9/18.
 */

public class GroupResp extends BaseResp {

    private String groupId;

    private String groupName;

    private String groupImg;

    private String createId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(String groupImg) {
        this.groupImg = groupImg;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }
}
