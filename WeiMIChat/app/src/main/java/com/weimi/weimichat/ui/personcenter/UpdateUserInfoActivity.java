package com.weimi.weimichat.ui.personcenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.biz.personcenter.IUserLoginView;
import com.weimi.weimichat.biz.personcenter.LoginPresenter;
import com.weimi.weimichat.biz.personcenter.RegisterPresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

/**
 * Created by yxl on 2018/9/12.
 */

public class UpdateUserInfoActivity extends BaseActivity implements IUserLoginView {

    private EditText userInfoContentView;

    private TextView tootTipView;

    private Button buttonView;

    private LoginPresenter loginPresenter;

    private int type;//修改的字段类型 1：昵称；2：邮箱；3：邮箱验证码；4:性别

    private String userId;

    private  String email;

    private Context context;

    private String userinfoContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        type = bundle.getInt("type");
        userId = bundle.getString("userId");
        presenter = loginPresenter = new LoginPresenter();
        loginPresenter.attachView(this);
        context = UpdateUserInfoActivity.this;
        setContentView(R.layout.update_user_info);

        super.onCreate(savedInstanceState);
    }

    @Override
       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(context)) {            showToast(getString(R.string.network_not_available));            return;        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {

    }

    @Override
    public void onSuccess(BaseResp resp, String code) {
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        //数据是使用Intent返回
        Intent intent = new Intent();
        switch (type){
            case 1:
                manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.NICK_NAME, userinfoContent);
                //把返回数据存入Intent
                intent.putExtra("nickName", userinfoContent);
                //设置返回数据
                this.setResult(RESULT_OK, intent);
                finish();
                break;
            case 2:
                ToastUtil.makeText(context, context.getString(R.string.checkCode_send_success));
                tootTipView.setText(getString(R.string.checkCode_tooltip));
                buttonView.setText(getString(R.string.confirm));
                userInfoContentView.setText("");
                type = 3;
                email = userinfoContent;
                break;
            case 3:
                manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.EMAIL, email);
                //把返回数据存入Intent
                intent.putExtra("email", email);
                //设置返回数据
                this.setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    private LoadingDialog dialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("修改中...")
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
    public void setHeader() {
        super.setHeader();
        switch(type){
            case 1:
                title.setText(getString(R.string.modify_nickName));
                break;
            case 2:
                title.setText(getString(R.string.modify_email));
                break;
            default:
                break;

        }
    }
    @Override
    public void initViews() {
        userInfoContentView = (EditText) findViewById(R.id.userinfo_content);
        EditTextUtil.setEditTextInhibitInputSpace(userInfoContentView);
        tootTipView = (TextView) findViewById(R.id.tooltip);
        buttonView = (Button) findViewById(R.id.topBtn);
        switch (type){
            case 1:
                break;
            case 2:
                tootTipView.setText(getString(R.string.email_tooltip));
                buttonView.setText(getString(R.string.next));
                break;
            case 4:
                break;
        }
    }

    @Override
    public void initListeners() {
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppUtil.isSingle()){
                    userinfoContent = userInfoContentView.getText().toString();
                    LoginResp userInfo = new LoginResp();
                    userInfo.setUserId(userId);
                    switch (type){
                        case 1:
                            if(loginPresenter.checkUserInfo(type, userinfoContent, context)){
                                userInfo.setNickName(userinfoContent);
                                loginPresenter.updateUserInfo(userInfo);
                            }
                            break;
                        case 2:
                            if(loginPresenter.checkUserInfo(type, userinfoContent, context)){
                                userInfo.setEmail(userinfoContent);
                                loginPresenter.updateUserInfo(userInfo);
                            }
                            break;
                        case 3:
                            if(TextUtils.isEmpty(userinfoContent)){
                                ToastUtil.makeText(context, context.getString(R.string.checkCode_empty));
                            }else{
                                loginPresenter.checkCodeIsCorrect(userinfoContent, email, userId);
                            }
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void initData() {

    }

    @Override
    public void clearEditContent() {

    }
}
