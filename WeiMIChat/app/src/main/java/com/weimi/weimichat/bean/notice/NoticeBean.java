package com.weimi.weimichat.bean.notice;

import java.util.ArrayList;

/**
 * Created by yxl on 2018/9/10.
 */

public class NoticeBean{

    public ArrayList<Notice> result = new ArrayList<>();

    public static class Notice{
        public int id;
        public String title;
        public String publishTime;
        public String titleImg;
    }
}
