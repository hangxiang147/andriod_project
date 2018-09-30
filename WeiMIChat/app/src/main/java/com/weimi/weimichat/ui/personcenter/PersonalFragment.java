package com.weimi.weimichat.ui.personcenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.ui.base.BaseFragment;
import com.weimi.weimichat.ui.main.LoginActivity;
import com.weimi.weimichat.ui.main.ModifyPwdActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.view.PersonalItemView;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by yxl on 2018/9/1.
 */

public class PersonalFragment extends BaseFragment {

    private ImageView blurImageView;

    private ImageView avatarImageView;

    private Context context;

    private  View root;

    private PersonalItemView exitLogin;

    private PersonalItemView versionView;

    private TextView nickNameView;

    private TextView inviteCodeView;

    private String userId;

    private String nickName;

    private String inviteCode;

    private String email;

    private String gender;

    private String headerUrl;

    private PersonalItemView modifyPwd;

    private PersonalItemView userInfoView;

    private final static int SHOW_USERINFO = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_personal, container, false);
        context = root.getContext();
        initViews();
        initListeners();
        return root;
    }

    @Override
    public void initViews() {
        //获取缓存的用户id
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        nickName = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.NICK_NAME, "");
        email = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.EMAIL, "");
        inviteCode = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.INVITE_CODE, "");
        gender = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.GENDER, "");
        headerUrl = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.HEADER_URL, "");

        blurImageView = (ImageView) root.findViewById(R.id.iv_blur);
        avatarImageView = (ImageView) root.findViewById(R.id.iv_avatar);
        exitLogin = (PersonalItemView) root.findViewById(R.id.exitLogin);
        versionView = (PersonalItemView) root.findViewById(R.id.about);
        nickNameView = (TextView) root.findViewById(R.id.user_name);
        inviteCodeView = (TextView) root.findViewById(R.id.checkCode);
        modifyPwd = (PersonalItemView) root.findViewById(R.id.pass);
        userInfoView = (PersonalItemView) root.findViewById(R.id.userInfo);

        nickNameView.setText(nickName);
        inviteCodeView.setText(inviteCode);
        versionView.setRightDesc(AppUtil.getVersionName(context));
        if(TextUtils.isEmpty(headerUrl)){
            Glide.with(this).load(R.drawable.head)
                    .bitmapTransform(new BlurTransformation(context, 25), new CenterCrop(context))
                    .into(blurImageView);

            Glide.with(this).load(R.drawable.head)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatarImageView);
        }else{
            Glide.with(this).load(Uri.parse(headerUrl))
                    .bitmapTransform(new BlurTransformation(context, 25), new CenterCrop(context))
                    .into(blurImageView);

            Glide.with(this).load(Uri.parse(headerUrl))
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatarImageView);
        }
    }

    @Override
    public void initListeners() {
        exitLogin.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                if(AppUtil.isSingle()){
                    DialogUtil.openConfirmDialog(context, "提示", "确认退出？", "确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //清除缓存userId
                                    EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
                                    manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.USER_ID, "");
                                    manager.getKDPreferenceTestSetting().saveString(EBSharedPrefUser.RONG_TOKEN, "");
                                    startActivity(new Intent(context, LoginActivity.class), null);
                                    getActivity().finish();
                                }
                            }, "取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //do nothing

                                }
                            });
                }
            }
        });
        modifyPwd.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                if(AppUtil.isSingle()){
                    startActivity(context, ModifyPwdActivity.class, bundle);
                }
            }
        });
        userInfoView.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                if(AppUtil.isSingle()){
                    //startActivity(context, ShowUserInfoActivity.class, null);
                    openActivityForResult(context, ShowUserInfoActivity.class, SHOW_USERINFO, null);

                }
            }
        });
        versionView.setItemClickListener(new PersonalItemView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                //do nothing
            }
        });
    }

    @Override
    public void initData() {

    }

    @Override
    public void setHeader() {

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHOW_USERINFO:
                if(null != data){
                    String nickName = data.getStringExtra("nickName");
                    String headerUrl = data.getStringExtra("headerUrl");
                    if(!TextUtils.isEmpty(nickName)){
                        nickNameView.setText(nickName);
                    }
                    if(!TextUtils.isEmpty(headerUrl)){
                        Glide.with(this).load(Uri.parse(headerUrl))
                                .bitmapTransform(new BlurTransformation(context, 25), new CenterCrop(context))
                                .into(blurImageView);

                        Glide.with(this).load(Uri.parse(headerUrl))
                                .bitmapTransform(new CropCircleTransformation(context))
                                .into(avatarImageView);
                    }
                }
                break;
        }
    }
}
