package com.weimi.weimichat.biz.personcenter;


import android.content.Context;
import android.text.TextUtils;

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

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yxl on 2018/8/29.
 */

public class RegisterPresenter extends BasePresenter<IUserRegisterView> {
    public RegisterPresenter() {

    }

    /**
     * 昵称不超过10位
     * @param context
     * @param nickName 昵称
     */
    public boolean checkNickName(Context context, String nickName){
        if(nickName.length()>10){
            DialogUtil.alertMsg(context,"提示","昵称长度不超过10位","明白了");
            return  false;
        }
/*        String reg = "^[a-zA-Z0-9\\x01-\\x7f]+$";
        //只能包含字母中文数字
        if(!TextUtils.isEmpty(nickName) && !nickName.matches(reg)){
            DialogUtil.alertMsg(context,"提示","昵称不可包含特殊字符","明白了");
            return  false;
        }*/
        return  true;
    }

    /**
     * 检查密码，密码必须包含字母和数字
     * @param context
     * @param pwd
     */
    public boolean checkPwd(Context context, String pwd, String confirmPwd){
        if(!TextUtils.isEmpty(pwd)){
            if(pwd.length()<6 || pwd.length()>16){
                DialogUtil.alertMsg(context,"提示","密码长度必须大于6位，小于16位","明白了");
                return false;
            }
            //检查是否包含数字
            Pattern p = Pattern.compile("[\\d]");
            Matcher m = p.matcher(pwd);
            boolean number = m.find();
            //检查是否包含字母
            p = Pattern.compile("[a-zA-Z]");
            m = p.matcher(pwd);
            boolean character = m.find();
            //必须包含字母数字
            if(!number || !character){
                DialogUtil.alertMsg(context,"提示","密码必须包含字母和数字","明白了");
                return  false;
            }
            //限制不可为中文
            String reg = "^[\\x01-\\x7f]*$";
            if(!pwd.matches(reg)){
                DialogUtil.alertMsg(context,"提示","密码不能包含中文和中文字符","明白了");
                return  false;
            }
            if(!checkConfirmPwd(context, pwd, confirmPwd)){
                return  false;
            }
        }else{
            DialogUtil.alertMsg(context,"提示","密码不能为空","明白了");
        }
        return  true;
    }

    /**
     * 验证邮箱的格式
     */
    public boolean checkMail(Context context, String mail){
        String reg =  "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        if(!TextUtils.isEmpty(mail) && !mail.matches(reg)){
            DialogUtil.alertMsg(context,"提示","邮箱格式不正确","明白了");
            return  false;
        }
        return  true;
    }

    /**
     * 验证两次密码输入是否一致
     * @param context
     * @param pwd
     * @param confirmPwd
     * @return
     */
    public boolean checkConfirmPwd(Context context, String pwd, String confirmPwd){
        if(!TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(confirmPwd) && !pwd.equals(confirmPwd)){
            DialogUtil.alertMsg(context,"提示","两次输入的密码不一致","明白了");
            return  false;
        }
        return  true;
    }

    /**
     * 邀请码只能是数字和字母，校验格式
     * @param context
     * @param inviteCode
     * @return
     */
    public boolean checkInviteCode(Context context, String inviteCode){
        String reg = "^[0-9A-Za-z]{6}";
        if(!TextUtils.isEmpty(inviteCode)  && !inviteCode.matches(reg)){
            DialogUtil.alertMsg(context,"提示","验证码格式不正确","明白了");
            return  false;
        }
        return  true;
    }

    /**
     * 检查注册信息
     * 1、除了邀请码，其它信息不可为空
     * 2、检查所填写的信息格式是否正确
     * @return
     */
    public boolean checkInfo(Context context, RegisterResp register) {

        if(TextUtils.isEmpty(register.getNickName())){
            DialogUtil.alertMsg(context,"提示","昵称不能为空","明白了");
            return false;
        }else if(!checkNickName(context, register.getNickName())) return false;
        if(TextUtils.isEmpty(register.getEmail())){
            DialogUtil.alertMsg(context,"提示","邮箱不能为空","明白了");
            return false;
        }else if(!checkMail(context, register.getEmail())) return  false;
        if(TextUtils.isEmpty(register.getPwd())){
            DialogUtil.alertMsg(context,"提示","密码不能为空","明白了");
            return false;
        }
        if(TextUtils.isEmpty(register.getConfirmPwd())){
            DialogUtil.alertMsg(context,"提示","请确认密码不能为空","明白了");
            return false;
        }else if(!checkPwd(context, register.getPwd(), register.getConfirmPwd())) return  false;
        return true;
    }
    public void register(RegisterResp registerResp) {
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.USER_REGISTER, getName(), new ITRequestResult<RegisterResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(RegisterResp entity) {
                        mvpView.onSuccess("");
                    }
                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, RegisterResp.class, new Param("nickName", registerResp.getNickName()),
                new Param("pwd", securityManager.get32MD5Str(registerResp.getPwd())),
                new Param("email", registerResp.getEmail()),
                new Param("gender", registerResp.getGender()),
                new Param("inviteCode", registerResp.getInviteCode()));
    }
    public void checkIsEmail(String mail){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.CHECK_EMAIL_EXIST, getName(), new ITRequestResult<RegisterResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(RegisterResp entity) {
                mvpView.onSuccess("");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, RegisterResp.class, new Param("email", mail));
    }
    public void checkCodeIsCorrect(String checkCode, String email){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.CHECK_EMAIL_CODE, getName(), new ITRequestResult<RegisterResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(RegisterResp entity) {
                mvpView.onSuccess("");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, RegisterResp.class, new Param("emailCode", checkCode), new Param("email", email));
    }
    public void startModifyPwd(String pwd, String email){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.UPDATE_PWD, getName(), new ITRequestResult<RegisterResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(RegisterResp entity) {
                mvpView.onSuccess("");
            }
            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, RegisterResp.class, new Param("email", email), new Param("newpassword", securityManager.get32MD5Str(pwd)));
    }
}
