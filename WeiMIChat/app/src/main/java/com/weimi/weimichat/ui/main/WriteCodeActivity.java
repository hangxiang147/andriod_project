package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.biz.personcenter.IUserRegisterView;
import com.weimi.weimichat.biz.personcenter.RegisterPresenter;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;

/**
 * Created by yxl on 2018/8/30.
 */

public class WriteCodeActivity extends BaseActivity implements IUserRegisterView {

    private Context context = WriteCodeActivity.this;

    private RegisterPresenter registerPresenter;

    private Button button;

    private EditText checkCode;

    private String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent =getIntent();
        email = intent.getStringExtra("email");
        setContentView(R.layout.modify_pwd_step_2);
        super.onCreate(savedInstanceState);
        presenter = registerPresenter = new RegisterPresenter();
        registerPresenter.attachView(this);
    }

       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(context)) {            showToast(getString(R.string.network_not_available));            return;        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {
        final Bundle bundle = new Bundle();
        bundle.putString("email", email);
        startActivity(ModifyPwdActivity.class,bundle);
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
        button = (Button) findViewById(R.id.goToModifyPwd);
        checkCode = (EditText) findViewById(R.id.checkCode);
        EditTextUtil.setEditTextInhibitInputSpace(checkCode);
    }

    @Override
    public void initListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppUtil.isSingle()){
                    registerPresenter.checkCodeIsCorrect(checkCode.getText().toString(), email);
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
        title.setText("填写验证码");
    }
}
