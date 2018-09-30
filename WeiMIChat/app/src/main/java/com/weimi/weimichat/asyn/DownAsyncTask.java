package com.weimi.weimichat.asyn;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.weimi.weimichat.R;
import com.weimi.weimichat.adapter.NoticeAdapter;
import com.weimi.weimichat.bean.notice.NoticeBean;
import com.weimi.weimichat.bean.notice.NoticeBean.Notice;
import com.weimi.weimichat.capabilities.http.NetUtil;
import com.weimi.weimichat.capabilities.json.GsonHelper;
import com.weimi.weimichat.capabilities.log.EBLog;

import java.util.ArrayList;

/**
 * Created by yxl on 2018/9/10.
 */

public class DownAsyncTask extends AsyncTask<String, Integer, byte[]> {

    ArrayList<Notice> data;
    NoticeAdapter adapter;
    Context context;
    View root;
    View footView;

    public DownAsyncTask(ArrayList<Notice> data, NoticeAdapter adapter, Context context, View root, View footView) {
        super();
        this.data = data;
        this.adapter = adapter;
        this.context = context;
        this.root = root;
        this.footView = footView;
    }

    /*
     * 当主线程中调用executeOnExecutor方法或execute方法时，会调用此方法
     */
    @Override
    protected byte[] doInBackground(String... params) {
        //下载网络数据
        return NetUtil.getNetData(params[0]);
    }

    /*
     * doInBackground方法执行之后会执行此方法，并把结果传过来
     */
    @Override
    protected void onPostExecute(byte[] result){
        super.onPostExecute(result);
        if (result != null) {
            //把从网络上获取的byte类型的数据转换为String字符串
            String jsonString = new String(result);
            try {
                //用json解析工具来解析该字符串数据
                NoticeBean cb = GsonHelper.toType(jsonString, NoticeBean.class);
                //取出data数据，并保存到集合中
                if(cb!=null && cb.result!=null){
                    data.addAll(cb.result);
                    //刷新数据
                    adapter.notifyDataSetChanged();
                }
                ListView lv = (ListView) root.findViewById(R.id.lv);
                if(lv.getFooterViewsCount()>0){
                    lv.removeFooterView(footView);
                }
            }catch (Exception e){
                e.printStackTrace();
                EBLog.e(DownAsyncTask.class.getSimpleName(), "获取消息列表失败:" + e.toString());
            }
        }else {
            Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
    }
}
