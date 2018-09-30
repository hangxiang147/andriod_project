package com.weimi.weimichat.biz.personcenter;


import android.content.Context;
import android.text.TextUtils;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.bean.home.RegisterResp;
import com.weimi.weimichat.biz.BasePresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.bridge.http.OkHttpManager;
import com.weimi.weimichat.capabilities.http.ITRequestResult;
import com.weimi.weimichat.capabilities.http.Param;
import com.weimi.weimichat.constant.URLUtil;
import com.weimi.weimichat.bridge.security.SecurityManager;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.util.ToastUtil;

/**
 * <功能详细描述>
 *
 * @author yxl
 * @version [版本号, 2016/5/4]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class LoginPresenter extends BasePresenter<IUserLoginView> {

    public LoginPresenter() {

    }

    public void login(final String useName, String password) {
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.USER_LOGIN, getName(), new ITRequestResult<LoginResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(LoginResp entity) {
                        mvpView.onSuccess(entity, "");
                        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
                        EBSharedPrefUser ebSharedPrefUser = manager.getKDPreferenceUserInfo();
                        ebSharedPrefUser.saveString(EBSharedPrefUser.USER_ID, entity.getUserId());
                        ebSharedPrefUser.saveString(EBSharedPrefUser.EMAIL, entity.getEmail());
                        ebSharedPrefUser.saveString(EBSharedPrefUser.GENDER, entity.getGender());
                        ebSharedPrefUser.saveString(EBSharedPrefUser.NICK_NAME, entity.getNickName());
                        ebSharedPrefUser.saveString(EBSharedPrefUser.INVITE_CODE, entity.getInviteCode());
                        ebSharedPrefUser.saveString(EBSharedPrefUser.HEADER_URL, entity.getUserImg());
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, LoginResp.class, new Param("nickName", useName),
                new Param("pwd", securityManager.get32MD5Str(password)));
    }
    public void updateUserInfo(LoginResp userInfo){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.UPDATE_USERINFO, getName(), new ITRequestResult<LoginResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(LoginResp entity) {
                        mvpView.onSuccess(entity, "");
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, LoginResp.class, new Param("nickName", userInfo.getNickName()),
                new Param("gender", userInfo.getGender()),
                new Param("email", userInfo.getEmail()),
                new Param("userId", userInfo.getUserId()),
                new Param("userImg", userInfo.getUserImg()));
    }
    public boolean checkUserInfo(int type, String userInfoContent, Context context){
        switch (type){
            case 1:
                if(TextUtils.isEmpty(userInfoContent)){
                    ToastUtil.makeText(context, context.getString(R.string.update_userInfo_nickName_empty));
                    return false;
                }
                if(userInfoContent.length()>10){
                    ToastUtil.makeText(context, context.getString(R.string.nickName_length_error));
                    return false;
                }
                break;
            case 2:
                if(TextUtils.isEmpty(userInfoContent)){
                    ToastUtil.makeText(context, context.getString(R.string.update_userInfo_email_empty));
                    return false;
                }
                String reg =  "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
                if(!TextUtils.isEmpty(userInfoContent) && !userInfoContent.matches(reg)){
                    ToastUtil.makeText(context, context.getString(R.string.email_format_error));
                    return  false;
                }
                break;
            case 3:
                break;
        }
        return true;
    }
    public void checkCodeIsCorrect(String checkCode, String email, String userId){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.CHECK_EMAIL_CODE, getName(), new ITRequestResult<LoginResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(LoginResp entity) {
                mvpView.onSuccess(entity, "");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, LoginResp.class, new Param("emailCode", checkCode), new Param("email", email), new Param("userId", userId));
    }

}
