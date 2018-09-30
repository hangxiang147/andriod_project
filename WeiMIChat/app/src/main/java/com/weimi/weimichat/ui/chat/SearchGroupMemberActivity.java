package com.weimi.weimichat.ui.chat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Group;
import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.server.widget.SelectableRoundedImageView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yxl on 2018/9/28.
 */

public class SearchGroupMemberActivity extends BaseActivity implements ChatView{
    private Context context;

    private GridView userGridView;

    private EditText group_member_search;

    private List<UserInfo> userList;

    private ChatPresenter chatPresenter;

    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        userList = (List<UserInfo>) bundle.getSerializable("userInfoList");
        context = this;
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        setContentView(R.layout.group_member_list);
        super.onCreate(savedInstanceState);
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
        userGridView = (GridView) findViewById(R.id.userGridView);
        group_member_search = (EditText) findViewById(R.id.group_member_search);
    }

    @Override
    public void initListeners() {
        group_member_search.addTextChangedListener(new TextWatcher() {
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
    }
    private void filterData(String s) {
        List<UserInfo> filterDataList = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            filterDataList = userList;
        } else {
            for (UserInfo userInfo : userList) {
                if (userInfo.getName().contains(s)) {
                    filterDataList.add(userInfo);
                }
            }
        }
        gridAdapter.updateListView(filterDataList);
    }
    @Override
    public void initData() {
        //获取群组所有成员
        getGroupMembers();
    }
    private void getGroupMembers() {
        title.setText(getString(R.string.all_group_member_num, userList.size()));
        gridAdapter = new GridAdapter(userList);
        userGridView.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
    }
    private class GridAdapter extends BaseAdapter {

        private List<UserInfo> list;

        GridAdapter(List<UserInfo> list){
            this.list = list;
        }
        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<UserInfo> list) {
            this.list = list;
            notifyDataSetChanged();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.group_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            final UserInfo userInfo = list.get(position);
            tv_username.setText(userInfo.getName());
            //ImageLoader.getInstance().displayImage(userInfo.getPortraitUri().toString(), iv_avatar, EBApplication.getOptions());
            Uri headerUri = userInfo.getPortraitUri();
            EBLog.e(userInfo.getName(), headerUri.toString());
            if(headerUri.toString().length()<10){
                iv_avatar.setImageResource(R.drawable.de_default_portrait);
            }else{
                Glide.with(context).load(headerUri)
                        .into(iv_avatar);
            }
            iv_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                        Intent intent = new Intent(context, UserDetailActivity.class);
//                        Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
//                        intent.putExtra("friend", friend);
//                        intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
//                        //Groups not Serializable,just need group name
//                        intent.putExtra("groupName", mGroup.getName());
//                        intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
//                        startActivity(intent, null);
                }

            });

            return convertView;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }
}
