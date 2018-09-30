package com.weimi.weimichat.ui.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.SealAppContext;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Friend;
import com.weimi.weimichat.bean.chat.GroupMember;
import com.weimi.weimichat.bean.chat.User;
import com.weimi.weimichat.bean.chat.UserResp;
import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.server.pinyin.CharacterParser;
import com.weimi.weimichat.server.pinyin.PinyinComparator;
import com.weimi.weimichat.server.pinyin.SideBar;
import com.weimi.weimichat.server.widget.DialogWithYesOrNoUtils;
import com.weimi.weimichat.server.widget.SelectableRoundedImageView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yxl on 2018/9/14.
 */

public class SelectFriendsActivity extends BaseActivity implements ChatView {

    private String userId;

    private Context context;
    /**
     * 好友列表的 ListView
     */
    private ListView mListView;
    /**
     * 发起讨论组的 adapter
     */
    private StartDiscussionAdapter adapter;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser mCharacterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private TextView mNoFriends;
    /**
     * 中部展示的字母提示
     */
    public TextView dialog;

    private List<Friend> mSelectedFriend;

    private LinearLayout mSelectedFriendsLinearLayout;

    private List<Friend> sourceDataList = new ArrayList<>();//展示的用户数据

    private List<Friend> data_list = new ArrayList<>();

    private ArrayList<UserInfo> addDisList, deleDisList;

    private boolean isAddGroupMember;
    private boolean isDeleteGroupMember;
    private List<UserInfo> addGroupMemberList;
    private List<UserInfo> deleteGroupMemberList;
    private String groupId;
    private String groupName;
    private String conversationStartId;
    private String conversationStartType = "null";
    private ArrayList<String> discListMember;
    private boolean isCrateGroup;
    private ChatPresenter chatPresenter;
    private static final String GET_GROUP_MEMBER= "getAllUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_start_disc);
        context = this;
        mSelectedFriend = new ArrayList<>();
        mSelectedFriendsLinearLayout = (LinearLayout) findViewById(R.id.ll_selected_friends);
        Intent intent = getIntent();
        isCrateGroup = intent.getBooleanExtra("createGroup", false);
        groupId = intent.getStringExtra("GroupId");
        groupName = intent.getStringExtra("groupName");
        isAddGroupMember = intent.getBooleanExtra("isAddGroupMember", false);
        isDeleteGroupMember = intent.getBooleanExtra("isDeleteGroupMember", false);
        if (isAddGroupMember || isDeleteGroupMember) {
            initGroupMemberList();
        }
        addDisList = (ArrayList<UserInfo>) getIntent().getSerializableExtra("AddDiscuMember");
        deleDisList = (ArrayList<UserInfo>) getIntent().getSerializableExtra("DeleteDiscuMember");
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        super.onCreate(savedInstanceState);
    }

    /**
     * 初始化群组成员
     */
    private void initGroupMemberList() {
        WeiMiUserInfoManager.getInstance().getGroupMembers(groupId, new WeiMiUserInfoManager.ResultCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(List<UserInfo> userInfos) {
                if (isAddGroupMember) {
                    addGroupMemberList = userInfos;
                    fillSourceDataListWithFriendsInfo();
                } else {
                    deleteGroupMemberList = userInfos;
                    fillSourceDataListForDeleteGroupMember();
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
    }
    private void fillSourceDataListForDeleteGroupMember() {
        if (deleteGroupMemberList != null && deleteGroupMemberList.size() > 0) {
            for (UserInfo deleteMember : deleteGroupMemberList) {
                if (deleteMember.getUserId().contains(userId)) {
                    continue;
                }
                data_list.add(new Friend(deleteMember.getUserId(),
                        deleteMember.getName(), deleteMember.getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            fillSourceDataList();
            updateAdapter();
        }
    }
    @Override
    public void setHeader() {
        super.setHeader();
        if(isAddGroupMember){
            title.setText(getString(R.string.add_group_member));
        }else if(isDeleteGroupMember){
            title.setText(getString(R.string.remove_group_member));
        }else{
            title.setText(getString(R.string.select_group_member));
        }
        right.setText("确定");
        right.setOnClickListener(this);
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
            case GET_GROUP_MEMBER:
                UserResp userResp = (UserResp) resp;
                if (mListView != null) {
                    if (userResp != null && userResp.result != null) {
                        for (User friend : userResp.result) {
                            if(!friend.getUserId().equals(userId)){
                                data_list.add(new Friend(friend.getUserId(), friend.getNickName(), Uri.parse(friend.getUserImg()), friend.getNickName(), null, null));
                            }
                        }
                        if (isAddGroupMember) {
                            for (UserInfo groupMember : addGroupMemberList) {
                                for (int i = 0; i < data_list.size(); i++) {
                                    if (groupMember.getUserId().equals(data_list.get(i).getUserId())) {
                                        data_list.remove(i);
                                    }
                                }
                            }
                        }
                        fillSourceDataList();
                        filterSourceDataList();
                        updateAdapter();
                    }
                }
                break;
            case SealAppContext.UPDATE_GROUP_MEMBER:
                finish();
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
        mListView = (ListView) findViewById(R.id.dis_friendlistview);
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        pinyinComparator = PinyinComparator.getInstance();
        mListView = (ListView) findViewById(R.id.dis_friendlistview);
        mNoFriends = (TextView) findViewById(R.id.dis_show_no_friend);
        SideBar mSidBar = (SideBar) findViewById(R.id.dis_sidrbar);
        dialog = (TextView) findViewById(R.id.dis_dialog);
        right = (TextView
                ) findViewById(R.id.tv_right);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        adapter = new StartDiscussionAdapter(context, sourceDataList);
        mListView.setAdapter(adapter);
    }
    //用于存储CheckBox选中状态
    public Map<Integer, Boolean> mCBFlag;

    public List<Friend> adapterList;


    class StartDiscussionAdapter extends BaseAdapter implements SectionIndexer {

        private Context context;
        private ArrayList<CheckBox> checkBoxList = new ArrayList<>();

        public StartDiscussionAdapter(Context context, List<Friend> list) {
            this.context = context;
            adapterList = list;
            mCBFlag = new HashMap<>();
            init();
        }

        public void setData(List<Friend> friends) {
            adapterList = friends;
            init();
        }

        void init() {
            for (int i = 0; i < adapterList.size(); i++) {
                mCBFlag.put(i, false);
            }
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Friend> list) {
            adapterList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final Friend friend = adapterList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_start_discussion, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.dis_friendname);
                viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.dis_catalog);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.dis_frienduri);
                viewHolder.isSelect = (CheckBox) convertView.findViewById(R.id.dis_select);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(friend.getLetters());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }

            viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCBFlag.put(position, viewHolder.isSelect.isChecked());
                    updateSelectedSizeView(mCBFlag);
                    if (mSelectedFriend.contains(friend)) {
                        int index = mSelectedFriend.indexOf(friend);
                        if (index > -1) {
                            mSelectedFriendsLinearLayout.removeViewAt(index);
                        }
                        mSelectedFriend.remove(friend);
                    } else {
                        mSelectedFriend.add(friend);
                        LinearLayout view = (LinearLayout) View.inflate(SelectFriendsActivity.this, R.layout.item_selected_friends, null);
                        SelectableRoundedImageView asyncImageView = (SelectableRoundedImageView) view.findViewById(R.id.iv_selected_friends);
                        String portraitUri = friend.getPortraitUri().toString();
                        ImageLoader.getInstance().displayImage(portraitUri, asyncImageView);
                        view.removeView(asyncImageView);
                        mSelectedFriendsLinearLayout.addView(asyncImageView);
                    }
                }
            });

            viewHolder.isSelect.setChecked(mCBFlag.get(position));

            if (TextUtils.isEmpty(adapterList.get(position).getDisplayName())) {
                viewHolder.tvTitle.setText(adapterList.get(position).getName());
            } else {
                viewHolder.tvTitle.setText(adapterList.get(position).getDisplayName());
            }

            String portraitUri = adapterList.get(position).getPortraitUri().toString();
            ImageLoader.getInstance().displayImage(portraitUri, viewHolder.mImageView, EBApplication.getOptions());
            return convertView;
        }

        private void updateSelectedSizeView(Map<Integer, Boolean> mCBFlag) {
            if (mCBFlag != null) {
                int size = 0;
                for (int i = 0; i < mCBFlag.size(); i++) {
                    if (mCBFlag.get(i)) {
                        size++;
                    }
                }
                if (size == 0) {
                    right.setText("确定");
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                } else {
                    right.setText("确定(" + size + ")");
                    List<Friend> selectedList = new ArrayList<>();
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (mCBFlag.get(i)) {
                            selectedList.add(sourceDataList.get(i));
                        }
                    }
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = adapterList.get(i).getLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return adapterList.get(position).getLetters().charAt(0);
        }


        final class ViewHolder {
            /**
             * 首字母
             */
            TextView tvLetter;
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userid
             */
//            TextView tvUserId;
            /**
             * 是否被选中的checkbox
             */
            CheckBox isSelect;
        }

    }
    @Override
    public void initListeners() {
        right.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(AppUtil.isSingle()){
            super.onClick(v);
            switch (v.getId()) {
                case R.id.tv_right:
                    if (mCBFlag != null && sourceDataList != null && sourceDataList.size() > 0) {
                        startDisList = new ArrayList<>();
                        List<String> disNameList = new ArrayList<>();
                        createGroupList = new ArrayList<>();
                        for (int i = 0; i < sourceDataList.size(); i++) {
                            if (mCBFlag.get(i)) {
                                startDisList.add(sourceDataList.get(i).getUserId());
                                disNameList.add(sourceDataList.get(i).getName());
                                createGroupList.add(sourceDataList.get(i));
                            }
                        }
                        if (deleteGroupMemberList != null && startDisList != null && sourceDataList.size() > 0) {
                            right.setClickable(true);
                            DialogUtil.openConfirmDialog(context, "提示", getString(R.string.remove_group_members), getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //删除群组成员
                                        chatPresenter.deleteGroupMembers(userId, groupId, startDisList);
                                    }
                                }, getString(R.string.cancel), new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //do nothing
                                    }
                                }
                            );
                        } else if (addGroupMemberList != null && startDisList != null && startDisList.size() > 0) {
                            chatPresenter.addGroupMembers(userId, groupId, groupName, startDisList);
                        }  else if (isCrateGroup) {
                            if (createGroupList.size() > 1) {
                                right.setClickable(true);
                                Intent intent = new Intent(SelectFriendsActivity.this, CreateGroupActivity.class);
                                intent.putExtra("GroupMember", (Serializable) createGroupList);
                                startActivity(intent);
                                finish();
                            } else {
                                ToastUtil.makeText(context, getString(R.string.must_select_friend));
                                right.setClickable(true);
                            }
                        } else {
                            Toast.makeText(SelectFriendsActivity.this, "无数据", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
            }
        }
    }
    @Override
    public void initData() {
        if (deleDisList != null && deleDisList.size() > 0) {
            for (int i = 0; i < deleDisList.size(); i++) {
                if (deleDisList.get(i).getUserId().contains(userId)) {
                    continue;
                }
                data_list.add(new Friend(deleDisList.get(i).getUserId(),
                        deleDisList.get(i).getName(),
                        deleDisList.get(i).getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            /**
             * 以下3步是标准流程
             * 1.填充数据sourceDataList
             * 2.过滤数据,邀请新成员时需要过滤掉已经是成员的用户,但做删除操作时不需要这一步
             * 3.设置adapter显示
             */
            fillSourceDataList();
            filterSourceDataList();
            updateAdapter();
        } else if (!isDeleteGroupMember && !isAddGroupMember) {
            fillSourceDataListWithFriendsInfo();
        }
    }
    private void fillSourceDataList() {
        if (data_list != null && data_list.size() > 0) {
            sourceDataList = filledData(data_list); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < data_list.size(); i++) {
            sourceDataList.get(i).setName(data_list.get(i).getName());
            sourceDataList.get(i).setUserId(data_list.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(data_list.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(data_list.get(i).getDisplayName());
        }
        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);
    }

    //讨论组群组邀请新成员时需要过滤掉已经是成员的用户
    private void filterSourceDataList() {
        if (addDisList != null && addDisList.size() > 0) {
            for (UserInfo u : addDisList) {
                for (int i = 0; i < sourceDataList.size(); i++) {
                    if (sourceDataList.get(i).getUserId().contains(u.getUserId())) {
                        sourceDataList.remove(sourceDataList.get(i));
                    }
                }
            }
        } else if (addGroupMemberList != null && addGroupMemberList.size() > 0) {
            for (UserInfo addMember : addGroupMemberList) {
                for (int i = 0; i < sourceDataList.size(); i++) {
                    if (sourceDataList.get(i).getUserId().contains(addMember.getUserId())) {
                        sourceDataList.remove(sourceDataList.get(i));
                    }
                }
            }
        } else if (conversationStartType.equals("DISCUSSION")) {
            if (discListMember != null && discListMember.size() > 1) {
                for (String s : discListMember) {
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (sourceDataList.get(i).getUserId().contains(s)) {
                            sourceDataList.remove(sourceDataList.get(i));
                        }
                    }
                }
            }
        } else if (conversationStartType.equals("PRIVATE")) {
            for (int i = 0; i < sourceDataList.size(); i++) {
                if (sourceDataList.get(i).getUserId().contains(conversationStartId)) {
                    sourceDataList.remove(sourceDataList.get(i));
                }
            }
        }
    }

    private void updateAdapter() {
        adapter.setData(sourceDataList);
        adapter.notifyDataSetChanged();
    }

    /**
     * 由于没有私聊，不存在好友的概念。所以，获取所有注册激活成功的朋友
     */
    private void fillSourceDataListWithFriendsInfo() {
        chatPresenter.getAllUsers();
    }
    private List<String> startDisList;
    private List<Friend> createGroupList;
    /**
     * 为ListView填充数据
     */
    private List<Friend> filledData(List<Friend> list) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Friend friendModel = new Friend(list.get(i).getUserId(), list.get(i).getName(), list.get(i).getPortraitUri());
            //汉字转换成拼音
            String pinyin = null;
            if (!TextUtils.isEmpty(list.get(i).getDisplayName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getDisplayName());
            } else if (!TextUtils.isEmpty(list.get(i).getName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getName());
            } else {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(list.get(i).getUserId());
                if (userInfo != null) {
                    pinyin = mCharacterParser.getSpelling(userInfo.getName());
                }
            }
            String sortString;
            if (!TextUtils.isEmpty(pinyin)) {
                sortString = pinyin.substring(0, 1).toUpperCase();
            } else {
                sortString = "#";
            }

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString);
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;
    }
}
