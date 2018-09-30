package com.weimi.weimichat.biz.home;

import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

import com.weimi.weimichat.R;
import com.weimi.weimichat.SealAppContext;
import com.weimi.weimichat.asyn.DownAsyncTask;
import com.weimi.weimichat.bean.chat.ChatResp;
import com.weimi.weimichat.bean.chat.Group;
import com.weimi.weimichat.bean.chat.GroupResp;
import com.weimi.weimichat.bean.chat.GroupsResp;
import com.weimi.weimichat.bean.chat.UserResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.bean.home.RegisterResp;
import com.weimi.weimichat.bean.notice.NoticeBean;
import com.weimi.weimichat.biz.BasePresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.http.OkHttpManager;
import com.weimi.weimichat.bridge.security.SecurityManager;
import com.weimi.weimichat.capabilities.http.ITRequestResult;
import com.weimi.weimichat.capabilities.http.NetUtil;
import com.weimi.weimichat.capabilities.http.Param;
import com.weimi.weimichat.capabilities.json.GsonHelper;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.constant.URLUtil;

import org.jsoup.helper.StringUtil;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;

/**
 * Created by yxl on 2018/9/14.
 */

public class ChatPresenter extends BasePresenter<ChatView> {
    /**
     * 获取用户token
     * @param userId  用户id
     * @param nickName 用户名称
     * @param userImg  用户头像
     */
    public void getRongToke(String userId, String nickName, String userImg){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.RONG_TOKEN, getName(), new ITRequestResult<ChatResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(ChatResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, ChatResp.class, new Param("userId", userId), new Param("nickName", nickName), new Param("userImg", userImg));
    }
    public void getAllUsers(){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.GET_ALL_USER, getName(), new ITRequestResult<UserResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(UserResp entity) {
                        mvpView.onSuccess(entity, "getAllUser");
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, UserResp.class);
    }

    /**
     * 创建者将群组成员踢出群组
     * @param groupId 群组Id
     * @param memberIds 成员集合
     */
    public void deleteGroupMembers(String operatorId, String groupId,List<String> memberIds){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.KICK_MEMBER, getName(), new ITRequestResult<GroupResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(GroupResp entity) {
                        mvpView.onSuccess(entity, SealAppContext.UPDATE_GROUP_MEMBER);
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg,  "");
                    }

                }, GroupResp.class, new Param("userId", StringUtil.join(memberIds, ",")),
                new Param("groupId", groupId), new Param("operatorId", operatorId));
    }
    /**
     * 退出群组
     * @param groupId 群组Id
     * @param operatorId 操作人
     */
    public void quitGroup(String operatorId, String groupId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.QUIT_GROUP, getName(), new ITRequestResult<GroupResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(GroupResp entity) {
                        mvpView.onSuccess(entity, SealAppContext.QUIT_GROUP);
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, GroupResp.class, new Param("userId", operatorId),
                new Param("groupId", groupId));
    }
    public void dismissGroup(String operatorId, String groupId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.DISMISS_GROUP, getName(), new ITRequestResult<GroupResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(GroupResp entity) {
                        mvpView.onSuccess(entity, SealAppContext.GROUP_DISMISS);
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, GroupResp.class, new Param("groupId", groupId), new Param("userId", operatorId));
    }
    /**
     * 当前用户添加群组成员
     * @param groupId  群组Id
     * @param groupName
     * @param memberIds 成员集合
     */
    public void addGroupMembers(String operatorId, String groupId, String groupName, List<String> memberIds){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.ADD_MEMBER, getName(), new ITRequestResult<GroupResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(GroupResp entity) {
                        mvpView.onSuccess(entity, SealAppContext.UPDATE_GROUP_MEMBER);
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, GroupResp.class, new Param("userId", StringUtil.join(memberIds, ",")),
                new Param("groupId", groupId), new Param("groupName", groupName), new Param("operatorId", operatorId));
    }
    /**
     * 创建群组
     * @param name  群组名
     * @param memberIds 群组成员id
     * @param groupImg 群头像
     */
    public void createGroup(String name, List<String> memberIds, String groupImg, String creatorId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.CREATE_GROUP, getName(), new ITRequestResult<GroupResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupResp.class, new Param("userId", StringUtil.join(memberIds, ",")),
                new Param("groupName", name), new Param("groupImg", groupImg), new Param("createId", creatorId));
    }

    /**
     * 获取当前用户的所有群组
     * @param userId
     */
    public void getMyGroups(String userId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.GET_MY_GROUP, getName(), new ITRequestResult<GroupsResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupsResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupsResp.class, new Param("userId",userId));
    }
    /**
     * 获取当前用户的未加入的所有群组
     * @param userId
     */
    public void getAllGroups(String userId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.GET_ALL_GROUP, getName(), new ITRequestResult<GroupsResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupsResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupsResp.class, new Param("userId",userId));
    }

    /**
     * 申请加入群组
     * @param groupId 群组id
     * @param userId 用户id
     * @param remark 备注信息
     */
    public void applyJoinGroup(String groupId, String userId, String remark){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.APPLY_JOIN_GROUP, getName(), new ITRequestResult<GroupsResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupsResp entity) {
                mvpView.onSuccess(entity, "apply");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupsResp.class, new Param("userId",userId), new Param("groupId", groupId),
                new Param("remark", remark));
    }

    /**
     * 获取申请的群组（未处理）
     * @param creatorId 群组创建人
     * @param userId 群组申请人
     */
    public void getApplyGroups(String creatorId, String userId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.GET_APPLY_GROUP, getName(), new ITRequestResult<GroupsResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupsResp entity) {
                mvpView.onSuccess(entity, "init");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupsResp.class, new Param("creatorId",creatorId), new Param("applyId", userId));
    }

    /**
     * 审批群组申请
     * @param creatorId 创建人
     * @param applyId 申请人
     * @param groupId 群组id
     * @param status 审批状态 1：不同意；2：同意
     */
    public void auditGroupApply(String creatorId, String applyId, String groupId, String status){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        httpManager.requestAsyncPostByTag(URLUtil.AUDIT_GROUP_APPLY, getName(), new ITRequestResult<GroupResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupResp.class, new Param("userId",applyId), new Param("groupId", groupId),
                new Param("status", status), new Param("creatorId", creatorId));
    }
    public void getGroupInfo(String groupId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        httpManager.requestAsyncPostByTag(URLUtil.GET_GROUP_INFO, getName(), new ITRequestResult<GroupResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(GroupResp entity) {
                        mvpView.onSuccess(entity, "groupInfo");
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, GroupResp.class, new Param("groupId", groupId));
    }
    public void getUserInfo(String userId){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        httpManager.requestAsyncPostByTag(URLUtil.GET_USER_INFO, getName(), new ITRequestResult<GroupResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupResp entity) {
                mvpView.onSuccess(entity, "userInfo");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupResp.class, new Param("userId", userId));
    }
    public void updateGroupInfo(String groupId, String groupName, String groupImg, final  String code){
        //网络层
        mvpView.showLoading();
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        httpManager.requestAsyncPostByTag(URLUtil.UPDATE_GROUP_INFO, getName(), new ITRequestResult<GroupResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(GroupResp entity) {
                mvpView.onSuccess(entity, code);
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, GroupResp.class, new Param("groupId", groupId), new Param("groupName", groupName), new Param("groupImg", groupImg));
    }
}
