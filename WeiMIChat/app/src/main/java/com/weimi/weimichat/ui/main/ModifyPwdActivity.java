package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.biz.personcenter.IUserRegisterView;
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

/**
 * Created by yxl on 2018/8/30.
 */

public class ModifyPwdActivity extends BaseActivity implements IUserRegisterView {

    private Context context = ModifyPwdActivity.this;

    private RegisterPresenter registerPresenter;

    private Button button;

    private EditText newPassword;

    private String email;

    private final static String MODIFY_PWD_CORRECT = "密码修改成功";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent =getIntent();
        email = intent.getStringExtra("email");
        setContentView(R.layout.modify_pwd_step_3);
        super.onCreate(savedInstanceState);
        presenter = registerPresenter = new RegisterPresenter();
        registerPresenter.attachView(this);
    }

       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(context)) {            showToast(getString(R.string.network_not_available));            return;        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {
        //去除缓存的userId,回到登录界面
        //清除缓存userId
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.USER_ID, "");
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(MODIFY_PWD_CORRECT)
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
                .setMessage("加载中...")
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
        newPassword = (EditText) findViewById(R.id.modifyPwd);
        EditTextUtil.setEditTextInhibitInputSpace(newPassword);
        button = (Button) findViewById(R.id.startModifyPwd);
    }

    @Override
    public void initListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registerPresenter.checkPwd(context, newPassword.getText().toString(), "")){
                    if(AppUtil.isSingle()){
                        registerPresenter.startModifyPwd(newPassword.getText().toString(), email);
                    }
                }
            }
        });
    }

    @Override
    public void initData() {

    }
    @Override
    public void setHeader() {
        super.setHeader();
        title.setText("修改密码");
    }
}
