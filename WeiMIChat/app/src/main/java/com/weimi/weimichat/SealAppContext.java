package com.weimi.weimichat;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.capabilities.json.GsonHelper;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.ui.chat.GroupApplyActivity;
import com.weimi.weimichat.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.TextMessage;

/**
 * 融云相关监听 事件集合类
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class SealAppContext implements RongIMClient.OnReceiveMessageListener,
        RongIM.ConversationListBehaviorListener,
        RongIM.UserInfoProvider,
        RongIM.GroupInfoProvider,
        RongIM.GroupUserInfoProvider,
        RongIM.IGroupMembersProvider{

    private static SealAppContext mRongCloudInstance;

    public static final String UPDATE_GROUP_NAME = "update_group_name";
    public static final String UPDATE_GROUP_MEMBER = "update_group_member";
    public static final String QUIT_GROUP = "quit_group";
    public static final String GROUP_DISMISS = "group_dismiss";

    private final static String TAG = SealAppContext.class.getSimpleName();

    private Context mContext;

    public SealAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
    }
    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {
            synchronized (SealAppContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new SealAppContext(context);
                }
            }
        }

    }
    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setOnReceiveMessageListener(this);
        RongIM.getInstance().setGroupMembersProvider(this);
        RongIM.setConversationListBehaviorListener(this);
    }
    @Override
    public Group getGroupInfo(String s) {
        WeiMiUserInfoManager.getInstance().getGroupInfo(s);
        return null;
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String s, String s1) {
        return null;
    }

    @Override
    public UserInfo getUserInfo(String userId) {
        WeiMiUserInfoManager.getInstance().getUserInfo(userId);
        return null;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        //刷新成员头像
        WeiMiUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());//此处每次刷新不合理，后期会修改（头像修改后台发送自定义消息通知刷新）
        //刷新群头像
        WeiMiUserInfoManager.getInstance().getGroupInfo(message.getTargetId());//此处每次刷新不合理，后期会修改（头像修改后台发送自定义消息通知刷新）
        if(messageContent instanceof GroupNotificationMessage){
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            GroupNotificationMessageData data = jsonToBean(groupNotificationMessage.getData());
            final String groupId = message.getTargetId();
            //当前用户
            String currentID = RongIM.getInstance().getCurrentUserId();
            //群组重命名
            if(groupNotificationMessage.getOperation().equals("Rename")){
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_NAME, data.getTargetGroupName());
             //添加群成员
            }else if(groupNotificationMessage.getOperation().equals("Add")){
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_MEMBER, groupId);
            //移除群成员
            }else if(groupNotificationMessage.getOperation().equals("Kicked")){
                List<String> memberIdList = data.getTargetUserIds();
                if (memberIdList != null) {
                    for (String userId : memberIdList) {
                        if (currentID.equals(userId)) {
                            RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {

                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode e) {

                                }
                            });
                        }
                    }
                }
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_MEMBER, groupId);
            //退出群组
            }else if(groupNotificationMessage.getOperation().equals("Quit")){
                BroadcastManager.getInstance(mContext).sendBroadcast(QUIT_GROUP, groupId);

            }else if(groupNotificationMessage.getOperation().equals("Dismiss")){
                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, null);
                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        EBLog.d("解散群组", "删除会话成功，groupId="+groupId);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
                BroadcastManager.getInstance(mContext).sendBroadcast(Constant.GROUP_LIST_UPDATE);
                BroadcastManager.getInstance(mContext).sendBroadcast(GROUP_DISMISS, groupId);
            }
        }
        return false;
    }
    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }
            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }
            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }
            if (jsonObject.has("targetUserIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserIds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("targetUserDisplayNames")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }
            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }
            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }
            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEntity;
    }
    @Override
    public void getGroupMembers(String groupId, final RongIM.IGroupMemberCallback iGroupMemberCallback) {
        WeiMiUserInfoManager.getInstance().getGroupMembers(groupId, new WeiMiUserInfoManager.ResultCallback<List<UserInfo>>(){

            @Override
            public void onSuccess(List<UserInfo> userInfos) {
                iGroupMemberCallback.onGetGroupMembersResult(userInfos);
            }

            @Override
            public void onError(String errString) {
                iGroupMemberCallback.onGetGroupMembersResult(null);
            }
        });
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent message = uiConversation.getMessageContent();
        try {
            if(message instanceof  TextMessage){
                TextMessage tMsg = (TextMessage)message;
                String extra = tMsg.getExtra();
                if(null != extra){
                    JSONObject json = GsonHelper.toJsonObject(extra);
                    //申请加入群组的系统会话
                    if(json.has("type") && "applyGroup".equals(json.getString("type"))){
                        Intent intent = new Intent(context, GroupApplyActivity.class);
                        intent.putExtra("applyId", uiConversation.getConversationSenderId());
                        context.startActivity(intent);
                        //清除未读状态
                        RongIM.getInstance().clearMessagesUnreadStatus(uiConversation.getConversationType(), uiConversation.getConversationTargetId(), null);
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            EBLog.e(TAG, e.toString());
        }
        return false;
    }
}
