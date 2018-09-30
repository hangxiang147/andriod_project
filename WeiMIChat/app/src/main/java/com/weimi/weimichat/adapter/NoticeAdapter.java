package com.weimi.weimichat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.notice.NoticeBean;
import com.weimi.weimichat.bean.notice.NoticeBean.Notice;

import java.util.ArrayList;

/**
 * Created by yxl on 2018/9/10.
 */

public class NoticeAdapter extends BaseAdapter {
    ArrayList<Notice> data;
    Context context;

    public NoticeAdapter(ArrayList<Notice> data, Context context) {
        super();
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //初始化holder对象
        ViewHold holder;
        if(convertView == null){
            //把条目布局转化为view对象
            convertView = View.inflate(context, R.layout.notice_item, null);
            //初始化holder对象，并初始化holder中的控件
            holder = new ViewHold();
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.iv = (SmartImageView) convertView.findViewById(R.id.iv);
            //给当前view做个标记，并把数据存到该tag中
            convertView.setTag(holder);

        }else {
            //如果当前view存在，则直接从中取出其保存的控件及数据
            holder = (ViewHold) convertView.getTag();
        }
        //通过position获取当前item的car数据，从car数据中取出title、pubDate和image
        Notice notice = data.get(position);
        holder.tv_title.setText(notice.title);
        holder.tv_date.setText(notice.publishTime);

        //使用SmartImageView的setImageUrl方法下载图片
        holder.iv.setImageUrl(notice.titleImg);

        return convertView;
    }

    /*
     * 用来存放item布局中控件的holder类
     */
    class ViewHold{
        SmartImageView iv; //显示图片的控件，注意是SmartImageView
        TextView tv_title; //显示标题的控件
        TextView tv_date;  //显示日期的控件
    }
}
