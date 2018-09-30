package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.home.RegisterResp;
import com.weimi.weimichat.biz.personcenter.IUserRegisterView;
import com.weimi.weimichat.biz.personcenter.RegisterPresenter;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;

/**
 * Created by yxl on 2018/8/29.
 */

public class RegisterActivity extends BaseActivity implements IUserRegisterView {
    private Context context = RegisterActivity.this;

    private RegisterPresenter registerPresenter;

    private EditText userName;

    private EditText password;

    private EditText email;//邮箱

    private EditText checkCode;//验证码

    private EditText inviteCode;//邀请码

    private EditText confirmPwd;//确认密码

    private RadioGroup sexRadioGroup;//性别

    private RadioButton maleRadioBtn;//男

    public static String REGISTER_SUCCESS = "注册成功，激活邮件已发送您的邮箱，激活成功后才可登录";

    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_register);
        super.onCreate(savedInstanceState);
        presenter = registerPresenter = new RegisterPresenter();
        registerPresenter.attachView(this);
    }
    @Override
       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(context)) {            showToast(getString(R.string.network_not_available));            return;        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(REGISTER_SUCCESS)
                .setCancelable(false)
                .setPositiveButton("明白了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(LoginActivity.class,null);
                    }
                })
                .show();
    }

    @Override
    public void onSuccess(BaseResp resp, String code) {

    }

    private LoadingDialog dialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("注册中...")
                .setCancelable(true)
                .setCancelOutside(true);
        dialog = loadBuilde.create();
        dialog.show();
    }
    @Override
    public void hideLoading() {
        dialog.dismiss();
    }

    @Override
    public void initViews() {
        userName = (EditText) findViewById(R.id.username_reg);
        password = (EditText) findViewById(R.id.passowrd_reg);
        email = (EditText) findViewById(R.id.email);
        confirmPwd = (EditText) findViewById(R.id.confirmPassowrd);
       // checkCode = (EditText) findViewById(R.id.checkCode);
        inviteCode = (EditText) findViewById(R.id.inviteCode);
        sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGroup);
        maleRadioBtn = (RadioButton) findViewById(R.id.male);
        register = (Button) findViewById(R.id.register);
        //过滤输入内容
        EditTextUtil.setEditTextInhibitInputSpeCharAndSpace(userName);
        EditTextUtil.setEditTextInhibitInputSpace(password);
        EditTextUtil.setEditTextInhibitInputSpace(email);
        EditTextUtil.setEditTextInhibitInputSpace(confirmPwd);
        EditTextUtil.setEditTextInhibitInputSpace(inviteCode);
        //控制注册图标大小
        Drawable drawableUser = getResources().getDrawable(R.drawable.user);
        Drawable drawablePwd = getResources().getDrawable(R.drawable.password);
        //Drawable drawableCheckCode = getResources().getDrawable(R.drawable.code);
        Drawable drawableInviteCode = getResources().getDrawable(R.drawable.invaite_code);
        Drawable drawableEmail = getResources().getDrawable(R.drawable.mail);

        drawableUser.setBounds(0, 0, 70, 70);//第一0是距左边距离，第二0是距上边距离，80分别是长宽
        drawablePwd.setBounds(0, 0, 70, 70);
       // drawableCheckCode.setBounds(0, 0, 70, 70);
        drawableInviteCode.setBounds(0, 0, 70, 70);
        drawableEmail.setBounds(0, 0, 70, 70);

        userName.setCompoundDrawables(drawableUser, null, null, null);//只放左边
        password.setCompoundDrawables(drawablePwd, null, null, null);
        confirmPwd.setCompoundDrawables(drawablePwd, null, null, null);
        email.setCompoundDrawables(drawableEmail, null, null, null);
       // checkCode.setCompoundDrawables(drawableCheckCode, null, null, null);
        inviteCode.setCompoundDrawables(drawableInviteCode, null, null, null);

    }

    @Override
    public void initListeners() {
        userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    registerPresenter.checkNickName(context, userName.getText().toString());
                }
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    registerPresenter.checkPwd(context, password.getText().toString(), confirmPwd.getText().toString());
                }
            }
        });
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    registerPresenter.checkMail(context, email.getText().toString());
                }
            }
        });
        confirmPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    registerPresenter.checkConfirmPwd(context, password.getText().toString(), confirmPwd.getText().toString());
                }
            }
        });
        inviteCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    registerPresenter.checkInviteCode(context, inviteCode.getText().toString());
                }
            }
        });
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                RegisterResp registerResp = new RegisterResp();
                registerResp.setEmail(email.getText().toString());
                int checkedRadioBtnId = sexRadioGroup.getCheckedRadioButtonId();
                if (checkedRadioBtnId == maleRadioBtn.getId()) {
                    registerResp.setGender("0");
                } else {
                    registerResp.setGender("1");
                }
                registerResp.setInviteCode(inviteCode.getText().toString());
                registerResp.setNickName(userName.getText().toString());
                registerResp.setPwd(password.getText().toString());
                registerResp.setConfirmPwd(confirmPwd.getText().toString());
                if (registerPresenter.checkInfo(context, registerResp)) {
                    if(AppUtil.isSingle()){
                        registerPresenter.register(registerResp);
                    }
                }
        }
        super.onClick(v);
    }

    @Override
    public void initData() {

    }
}
