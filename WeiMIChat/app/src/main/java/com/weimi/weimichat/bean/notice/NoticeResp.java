package com.weimi.weimichat.bean.notice;

import com.weimi.weimichat.bean.BaseResp;

/**
 * Created by yxl on 2018/9/10.
 */

public class NoticeResp extends BaseResp {
    private int id;
    private String title;
    private String content;
    private String publishTime;
    private String titleImg;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getTitleImg() {
        return titleImg;
    }

    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }
}
