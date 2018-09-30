package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.biz.personcenter.IUserLoginView;
import com.weimi.weimichat.biz.personcenter.LoginPresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.constant.Event;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;


public class LoginActivity extends BaseActivity implements IUserLoginView {

    private Context context = LoginActivity.this;
    /**
     * 用户名
     */
    private EditText userName;

    /**
     * 用户密码
     */
    private EditText password;

    /**
     * 登录
     */
    private Button login;

    private LoginPresenter mUserLoginPresenter;

    private TextView goToRegister;

    private TextView forgetPwd;

    private String userId;//用户userId
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //获取缓存的用户id
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        //若不为空,则回到主页面
        if(!TextUtils.isEmpty(userId)){
            startActivity(HomeActivity.class, null);
            super.onCreate(savedInstanceState);
            finish();
        }else{
            setContentView(R.layout.activity_login);
            presenter = mUserLoginPresenter = new LoginPresenter();
            mUserLoginPresenter.attachView(this);
            super.onCreate(savedInstanceState);
        }
    }

    @Override
    public void initViews() {
        if(TextUtils.isEmpty(userId)){
            userName = (EditText) findViewById(R.id.username);
            EditTextUtil.setEditTextInhibitInputSpace(userName);
            password = (EditText) findViewById(R.id.passowrd);
            EditTextUtil.setEditTextInhibitInputSpace(password);
            login = (Button) findViewById(R.id.login);
            goToRegister = (TextView) findViewById(R.id.goToRegister);
            forgetPwd = (TextView) findViewById(R.id.forgetPwd);
        }
    }

    @Override
    public void initListeners() {
        if(TextUtils.isEmpty(userId)){
            login.setOnClickListener(this);
            goToRegister.setOnClickListener(this);
            forgetPwd.setOnClickListener(this);
        }
    }

    @Override
    public void initData() {

    }

    @Override
    public void setHeader() {
        //super.setHeader();
        //title.setText("登录");
    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        switch (event){
            case IMAGE_LOADER_SUCCESS:
                clearEditContent();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
                case R.id.login:
                String user = userName.getText().toString();
                String pwd = password.getText().toString();
                //用户名或者密码是否为空
                if(TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)){
                    DialogUtil.alertMsg(context,"提示","用户名或者密码不能为空","明白了");
                    return;
                }
                if(AppUtil.isSingle()){
                    mUserLoginPresenter.login(user, pwd);
                }
                break;
            case R.id.goToRegister:
                if(AppUtil.isSingle()){
                    startActivity(RegisterActivity.class, null);
                }
                break;
            case R.id.forgetPwd:
                if(AppUtil.isSingle()){
                    startActivity(CheckCodeActivity.class, null);
                }
                break;
        }
        super.onClick(v);
    }

    @Override
    public void clearEditContent() {
        userName.setText("");
        password.setText("");
    }

    @Override
       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(context)) {            showToast(getString(R.string.network_not_available));            return;        }
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
        startActivity(HomeActivity.class,null);
        finish();
    }
    private LoadingDialog dialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("登录中...")
                .setCancelable(true)
                .setCancelOutside(true);
        dialog = loadBuilde.create();
        dialog.show();
    }
    @Override
    public void hideLoading() {
        dialog.dismiss();
    }

}
