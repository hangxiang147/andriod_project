package com.weimi.weimichat.bean.chat;

import com.weimi.weimichat.bean.BaseResp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxl on 2018/9/17.
 */

public class UserResp extends BaseResp{

    public List<User> result = new ArrayList<>();

    public List<User> getResult() {
        return result;
    }

    public void setResult(List<User> result) {
        this.result = result;
    }
}
