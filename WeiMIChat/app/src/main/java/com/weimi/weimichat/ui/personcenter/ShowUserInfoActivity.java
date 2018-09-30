package com.weimi.weimichat.ui.personcenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.biz.personcenter.IUserLoginView;
import com.weimi.weimichat.biz.personcenter.LoginPresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.ui.main.LoginActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.view.PersonalItemView;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * Created by yxl on 2018/9/11.
 */

public class ShowUserInfoActivity extends BaseActivity implements IUserLoginView{

    private  String userId;

    private String nickName;

    private String email;

    private String gender;

    private String headerUrl;

    private LoginPresenter loginPresenter;

    private PersonalItemView nickNameView;

    private PersonalItemView genderView;

    private PersonalItemView emailView;

    private ImageView headerView;

    private Context context;

    private AlertDialog genderAlert;

    private RadioGroup sexRadioGroup;//性别

    private RadioButton maleRadioBtn;//男

    private RadioButton femaleRadioBtn;//女

    private static final int MDY_NICK_NAME = 1;

    private static final int MDY_EMAIL = 2;

    private static final int MDY_HEADER = 3;

    private EBSharedPrefManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        nickName = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.NICK_NAME, "");
        email = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.EMAIL, "");
        gender = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.GENDER, "");
        headerUrl = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.HEADER_URL, "");
        presenter = loginPresenter = new LoginPresenter();
        loginPresenter.attachView(this);
        context = ShowUserInfoActivity.this;
        setContentView(R.layout.user_info);
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
        genderView.setRightDesc(gender.equals("0") ? "男":"女");
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
        title.setText("个人信息");


    }
    @Override
    public void initViews() {
        headerView = (ImageView) findViewById(R.id.userinfo_header);
        nickNameView = (PersonalItemView) findViewById(R.id.userInfo_nickName);
        genderView = (PersonalItemView) findViewById(R.id.userInfo_gender);
        emailView = (PersonalItemView) findViewById(R.id.userInfo_email);
        nickNameView.setRightDesc(nickName);
        genderView.setRightDesc(gender.equals("0") ? "男":"女");
        emailView.setRightDesc(email);
        if(!TextUtils.isEmpty(headerUrl)){
            Glide.with(this).load(Uri.parse(headerUrl))
                    .into(headerView);
        }
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.select_gender,
                (ViewGroup) findViewById(R.id.sexUpdate));
        genderAlert = new AlertDialog.Builder(context)
                .setTitle("性别")
                .setView(layout)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LoginResp userInfo = new LoginResp();
                        userInfo.setUserId(userId);
                        int checkedRadioBtnId = sexRadioGroup.getCheckedRadioButtonId();
                        if (checkedRadioBtnId == maleRadioBtn.getId()) {
                            gender = "0";
                            userInfo.setGender("0");
                        } else {
                            userInfo.setGender("1");
                            gender = "1";
                        }
                        loginPresenter.updateUserInfo(userInfo);
                    }
                }).create();
        sexRadioGroup = (RadioGroup) layout.findViewById(R.id.sexRadioGroupUpdate);
        maleRadioBtn = (RadioButton) layout.findViewById(R.id.maleUpdate);
        femaleRadioBtn = (RadioButton) layout.findViewById(R.id.femleUpdate);
        if(gender.equals("0")){
            maleRadioBtn.setChecked(true);
            femaleRadioBtn.setChecked(false);
        }else{
            femaleRadioBtn.setChecked(true);
            maleRadioBtn.setChecked(false);
        }
    }

    @Override
    public void initListeners() {
        nickNameView.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putInt("type",1);
                if(AppUtil.isSingle()){
                    //startActivity(UpdateUserInfoActivity.class, bundle);
                    openActivityForResult(UpdateUserInfoActivity.class, MDY_NICK_NAME, bundle);
                }
            }
        });
        genderView.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                if(AppUtil.isSingle()){
                    genderAlert.show();
                }
            }
        });
        emailView.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putInt("type",2);
                if(AppUtil.isSingle()){
                    //startActivity(UpdateUserInfoActivity.class, bundle);
                    openActivityForResult(UpdateUserInfoActivity.class, MDY_EMAIL, bundle);
                }
            }
        });
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppUtil.isSingle()){
                    //startActivity(UploadHeaderActivity.class, null);
                    openActivityForResult(UploadHeaderActivity.class, MDY_HEADER, null);
                }
            }
        });

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                //数据是使用Intent返回
                Intent intent = new Intent();
                //把返回数据存入Intent
                intent.putExtra("nickName", nickName);
                intent.putExtra("headerUrl", headerUrl);
                //设置返回数据
                this.setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
    @Override
    public void initData() {

    }

    @Override
    public void clearEditContent() {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MDY_NICK_NAME:
                if (null != data) {
                    String nickName = data.getStringExtra("nickName");
                    if (!TextUtils.isEmpty(nickName)) {
                        nickNameView.setRightDesc(nickName);
                    }
                }
                break;
            case MDY_EMAIL:
                if (null != data) {
                    String email = data.getStringExtra("email");
                    if (!TextUtils.isEmpty(email)) {
                        emailView.setRightDesc(email);
                    }
                    break;
                }
            case MDY_HEADER:
                if (null != data) {
                    headerUrl = data.getStringExtra("headerUrl");
                    if (!TextUtils.isEmpty(headerUrl)) {
                        Glide.with(this).load(Uri.parse(headerUrl))
                                .into(headerView);
                    }
                    break;
                }
        }
    }
}
