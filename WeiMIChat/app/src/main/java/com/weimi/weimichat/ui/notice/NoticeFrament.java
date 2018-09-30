package com.weimi.weimichat.ui.notice;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.weimi.weimichat.R;
import com.weimi.weimichat.adapter.NoticeAdapter;
import com.weimi.weimichat.asyn.DownAsyncTask;
import com.weimi.weimichat.bean.notice.NoticeBean.Notice;
import com.weimi.weimichat.constant.URLUtil;
import com.weimi.weimichat.ui.base.BaseFragment;
import com.weimi.weimichat.util.AppUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yxl on 2018/9/1.
 */

public class NoticeFrament extends BaseFragment{

    private ListView lv;
    private ArrayList<Notice> data;
    private NoticeAdapter adapter;
    private boolean flag = false;
    private int pageNo = 1;
    private  View root;private ExecutorService es;
    private  View footView;
    private Context context;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_notice, container, false);
        context = root.getContext();
        initViews();
        initListeners();
        return root;
    }

    @Override
    public void initViews() {
        //初始化listview控件
        lv = (ListView) root.findViewById(R.id.lv);
        //存放json解析的数据的集合
        data = new ArrayList<Notice>();
        //自定义适配器
        adapter = new NoticeAdapter(data,context);

        //给listview设置一个底部view(必须在设置数据之前)
        footView = View.inflate(context, R.layout.notice_footer, null);

        //给listview设置适配器
        lv.setAdapter(adapter);
        //使用线程池来实现异步任务的多线程下载
        es = Executors.newFixedThreadPool(10);
        new DownAsyncTask(data,adapter,context, root, footView).executeOnExecutor(es, URLUtil.NOTICE_LIST+"?pageNum="+pageNo+"&pageSize=20");

        /*
         * 对listview设置滚动监听事件，实现分页加载数据
         */
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //如果停止了滑动且滑动到了结尾，则更新数据,加载下一页
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && flag == true) {
                    if(lv.getFooterViewsCount() == 0){
                        lv.addFooterView(footView);
                    }
                    pageNo += 1;
                    new DownAsyncTask(data,adapter,context, root, footView).executeOnExecutor(es, URLUtil.NOTICE_LIST+"?pageNum="+pageNo+"&pageSize=20");
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //判断是否滑动到了结尾
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    flag = true;
                }else {
                    flag = false;
                }
            }
        });
    }

    @Override
    public void initListeners() {
        //设置listview点击事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Notice notice = data.get(i);
                Bundle bundle = new Bundle();
                bundle.putInt("noticeId", notice.id);
                if(AppUtil.isSingle()){
                    startActivity(context, NoticeShowActivity.class, bundle);
                }
            }
        });

    }

    @Override
    public void initData() {

    }

    @Override
    public void setHeader() {

    }
}
