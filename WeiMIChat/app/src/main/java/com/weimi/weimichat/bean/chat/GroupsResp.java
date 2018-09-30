package com.weimi.weimichat.bean.chat;

import com.weimi.weimichat.bean.BaseResp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxl on 2018/9/19.
 */

public class GroupsResp extends BaseResp{

    List<Group> result = new ArrayList<>();

    public List<Group> getResult() {
        return result;
    }

    public void setResult(List<Group> result) {
        this.result = result;
    }
}
