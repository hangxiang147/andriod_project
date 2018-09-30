package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

public class CheckCodeActivity extends BaseActivity implements IUserRegisterView {

    private Context context = CheckCodeActivity.this;

    private RegisterPresenter registerPresenter;

    private Button button;

    private EditText email;

    private  final static String SEND_SUCCESS = "验证码已发送至邮箱，请查收";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.modify_pwd_step_1);
        super.onCreate(savedInstanceState);
        presenter = registerPresenter = new RegisterPresenter();
        registerPresenter.attachView(this);
    }

       public void onError(String errorMsg, String code) {
        if (!GeneralUtils.isNetworkConnected(context)) {
            showToast(getString(R.string.network_not_available));
            return;
        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {
        final Bundle bundle = new Bundle();
        bundle.putString("email", email.getText().toString());
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(SEND_SUCCESS)
                .setCancelable(false)
                .setPositiveButton("明白了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(WriteCodeActivity.class,bundle);
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
        button = (Button) findViewById(R.id.sendEmail);
        email = (EditText) findViewById(R.id.email_for_checkCode);
        EditTextUtil.setEditTextInhibitInputSpace(email);
    }

    @Override
    public void initListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppUtil.isSingle()){
                    registerPresenter.checkIsEmail(email.getText().toString());
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
        title.setText("获取验证码");
    }
}
