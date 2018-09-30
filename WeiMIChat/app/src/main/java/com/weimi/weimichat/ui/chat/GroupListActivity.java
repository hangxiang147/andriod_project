package com.weimi.weimichat.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weimi.weimichat.Constant;
import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Group;
import com.weimi.weimichat.bean.chat.GroupResp;
import com.weimi.weimichat.bean.chat.Groups;
import com.weimi.weimichat.bean.chat.GroupsResp;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.server.widget.SelectableRoundedImageView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.ui.main.LoginActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;

/**
 * Created by yxl on 2018/9/18.
 */

public class GroupListActivity extends BaseActivity implements ChatView {

    private Context context;

    private ListView mGroupListView;
    private GroupAdapter adapter;
    private TextView mNoGroups;
    private EditText mSearch;
    private List<Group> mList;
    private TextView mTextView;
    public static final String APPLY = "apply";
    private ChatPresenter chatPresenter;
    private String userId;
    //申请加入
    private boolean allGroup;
    //申请加入群组的弹框
    private AlertDialog applyDialog;
    //被点击的群组
    private Group clickedGroup;

    private EditText applyRemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        setContentView(R.layout.fr_group_list);
        allGroup = getIntent().getBooleanExtra("all", false);
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        super.onCreate(savedInstanceState);
        BroadcastManager.getInstance(context).addAction(Constant.GROUP_LIST_UPDATE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initData();
            }
        });
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
        switch (code){
            case APPLY:
                ToastUtil.makeText(context, getResources().getString(R.string.apply_success));
                break;
            default:
                GroupsResp groupsResp = (GroupsResp) resp;
                List<Group> groups = groupsResp.getResult();
                mList = groups;
                if (groups != null && groups.size() > 0) {
                    adapter = new GroupAdapter(context, mList);
                    mGroupListView.setAdapter(adapter);
                    mNoGroups.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText(getString(R.string.ac_group_list_group_number, groups.size()));
                    mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            clickedGroup = (Group) adapter.getItem(position);
                            if(allGroup){
                                applyDialog.show();
                            }else{
                                if(AppUtil.isSingle()){
                                    RongIM.getInstance().startGroupChat(context, clickedGroup.getGroupId(), clickedGroup.getGroupName());
                                }
                            }
                        }
                    });
                    mSearch.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            filterData(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                } else {
                    mNoGroups.setVisibility(View.VISIBLE);
                }
                break;
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
        //获取缓存的用户id
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        mGroupListView = (ListView) findViewById(R.id.group_listview);
        mNoGroups = (TextView) findViewById(R.id.show_no_group);
        mNoGroups.setText(allGroup ? getString(R.string.not_join_group):getString(R.string.fr_group_list_not_join_the_group));
        mSearch = (EditText) findViewById(R.id.group_search);
        mTextView = (TextView)findViewById(R.id.foot_group_size);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.apply_group,
                (ViewGroup) findViewById(R.id.apply_remark_layout));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.apply_join));
        builder.setView(layout);
        builder.setNegativeButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String remark = applyRemark.getText().toString();
                if(remark.length()>10){
                    ToastUtil.makeText(context, getString(R.string.not_limit_character, 10));
                    return;
                }
                chatPresenter.applyJoinGroup(clickedGroup.getGroupId(), userId, remark);
                applyRemark.setText("");
            }
        });
        builder.setNeutralButton(getString(R.string.cancel), null);
        applyDialog = builder.create();
        applyRemark = (EditText) layout.findViewById(R.id.apply_remark);
        EditTextUtil.setEditTextInhibitInputSpace(applyRemark);
        applyDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        if(allGroup){
            chatPresenter.getAllGroups(userId);
        }else{
            chatPresenter.getMyGroups(userId);
        }
    }

    @Override
    public void setHeader() {
        super.setHeader();
        if(allGroup){
            title.setText(R.string.all_group_list);
        }else{
            title.setText(R.string.my_group_list);
        }
    }
    private void filterData(String s) {
        List<Group> filterDataList = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            filterDataList = mList;
        } else {
            for (Group group : mList) {
                if (group.getGroupName().contains(s)) {
                    filterDataList.add(group);
                }
            }
        }
        adapter.updateListView(filterDataList);
        mTextView.setText(getString(R.string.ac_group_list_group_number, filterDataList.size()));
    }

    class GroupAdapter extends BaseAdapter {

        private Context context;

        private List<Group> list;

        public GroupAdapter(Context context, List<Group> list) {
            this.context = context;
            this.list = list;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Group> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (list != null) return list.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (list == null)
                return null;

            if (position >= list.size())
                return null;

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Group group = list.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.group_item_new, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.groupname);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.groupuri);
                viewHolder.groupId = (TextView) convertView.findViewById(R.id.group_id);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvTitle.setText(group.getGroupName());
            ImageLoader.getInstance().displayImage(group.getGroupImg(), viewHolder.mImageView, EBApplication.getOptions());
            return convertView;
        }


        class ViewHolder {
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userId
             */
            TextView groupId;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(context).destroy(Constant.GROUP_LIST_UPDATE);
    }
}
