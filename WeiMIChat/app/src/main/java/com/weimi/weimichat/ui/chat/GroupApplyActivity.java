package com.weimi.weimichat.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Group;
import com.weimi.weimichat.bean.chat.GroupsResp;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.server.widget.SelectableRoundedImageView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yxl on 2018/9/26.
 */

public class GroupApplyActivity extends BaseActivity implements ChatView {

    private Context context;

    private ChatPresenter chatPresenter;

    private List<Group> applyGroups;

    private String applyId;

    private String userId;

    private ListView listView;

    private GroupApplyListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.group_apply_list);
        context = this;
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        Intent intent = getIntent();
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        applyId = intent.getStringExtra("applyId");
        super.onCreate(savedInstanceState);
    }
    @Override
    public void setHeader() {
        super.setHeader();
         title.setText(getString(R.string.group_apply));
    }
    @Override
    public void onError(String errorMsg, String code) {
        if (!GeneralUtils.isNetworkConnected(context)) {
            showToast(getString(R.string.network_not_available));
            return;
        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {

    }

    @Override
    public void onSuccess(BaseResp resp, String code) {
        if("init".equals(code)){
            GroupsResp groupsResp = (GroupsResp) resp;
            applyGroups = groupsResp.getResult();
            adapter = new GroupApplyListAdapter();
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            chatPresenter.getApplyGroups(userId, applyId);
        }
    }

    private LoadingDialog loadingDialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("加载中...")
                .setCancelable(true)
                .setCancelOutside(true);
        loadingDialog = loadBuilde.create();
        loadingDialog.show();
    }
    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
    }

    @Override
    public void initViews() {
        listView = (ListView) findViewById(R.id.group_apply_list);
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        chatPresenter.getApplyGroups(userId, applyId);
    }
    class GroupApplyListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return applyGroups.size();
        }

        @Override
        public Object getItem(int position) {
            return applyGroups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.group_apply_item, parent, false);
                holder.mName = (TextView) convertView.findViewById(R.id.ship_name);
                holder.mMessage = (TextView) convertView.findViewById(R.id.ship_message);
                holder.mHead = (SelectableRoundedImageView) convertView.findViewById(R.id.new_header);
                holder.agree = (Button) convertView.findViewById(R.id.apply_agree);
                holder.refuse = (Button) convertView.findViewById(R.id.apply_refuse);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Group group = applyGroups.get(position);
            holder.mName.setText(group.getGroupName());
            ImageLoader.getInstance().displayImage(group.getGroupImg(), holder.mHead, EBApplication.getOptions());
            String remark = group.getRemark();
            if(!TextUtils.isEmpty(remark)){
                holder.mMessage.setText(group.getRemark());
                holder.mMessage.setVisibility(View.VISIBLE);
            }
            holder.agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatPresenter.auditGroupApply(userId, applyId, group.getGroupId(), "2");
                }
            });
            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatPresenter.auditGroupApply(userId, applyId, group.getGroupId(), "1");
                }
            });
            return convertView;
        }
        class ViewHolder {
            SelectableRoundedImageView mHead;
            TextView mName;
            TextView mMessage;
            Button agree;
            Button refuse;
        }
    }
}
