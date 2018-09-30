package com.weimi.weimichat.ui.chat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.weimi.weimichat.Constant;
import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.SealAppContext;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Friend;
import com.weimi.weimichat.bean.chat.GroupMember;
import com.weimi.weimichat.bean.chat.GroupResp;
import com.weimi.weimichat.bean.chat.Groups;
import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.capabilities.cache.FileUtil;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.server.pinyin.CharacterParser;
import com.weimi.weimichat.server.widget.SelectableRoundedImageView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.ui.main.LoginActivity;
import com.weimi.weimichat.ui.widget.BottomMenuDialog;
import com.weimi.weimichat.ui.widget.SwitchButton;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.DialogUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class GroupDetailActivity extends BaseActivity implements ChatView{

    private static final String TAG = GroupDetailActivity.class.getSimpleName();

    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final String UPDATE_GROUP_IMG = "update_group_img";
    private static final int ADD_MEMBER = 2;
    private static final int DELETE_MEMBER = 3;

    private boolean isCreated = false;//是否是创建者

    private Context context;
    private String userId;
    private List<GroupMember> mGroupMember;
    private LinearLayout liGroupClean;
    private LinearLayout liGroupHeader;
    private LinearLayout liGroupName;
    private GridView mGridView;
    private SelectableRoundedImageView mGroupHeader;
    private TextView mGroupName;
    private TextView all_group_member;
    private Button mDismissBtn;//解散群组
    private Button mQuitBtn;//退出群组
    private AlertDialog alertDialog;
    private EditText newGroupNameEdit;

    private SwitchButton messageNotification;
    private Conversation.ConversationType mConversationType;
    private boolean isFromConversation;
    private String newGroupName;
    private String groupId;
    private String groupImg;

    private ChatPresenter chatPresenter;
    private List<UserInfo> userList;//显示的群组成员列表
    private List<UserInfo> allUserList = new ArrayList<>();//全部群组成员列表



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getString("groupId");
        context = this;
        presenter = chatPresenter = new ChatPresenter();
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        chatPresenter.attachView(this);
        setContentView(R.layout.activity_detail_group);
        setGroupsInfoChangeListener();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onError(String errorMsg, String code) {
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
        switch (code){
            case "groupInfo":
                GroupResp groupResp = (GroupResp) resp;
                if(null != groupResp){
                    Glide.with(this).load(Uri.parse(groupResp.getGroupImg()))
                            .into(mGroupHeader);
                    newGroupName = groupResp.getGroupName();
                    groupImg = groupResp.getGroupImg();
                    mGroupName.setText(newGroupName);
                    //判断用户是不是该群的创建人
                    if(groupResp.getCreateId().equals(userId)){
                        isCreated = true;
                        mDismissBtn.setVisibility(View.VISIBLE);
                        mQuitBtn.setVisibility(View.GONE);
                    }
                }
                break;
            case UPDATE_GROUP_IMG:
                ToastUtil.makeText(context, getString(R.string.update_success));
                GroupResp groupImg = (GroupResp) resp;
                this.groupImg = groupImg.getGroupImg();
                RongIM.getInstance().refreshGroupInfoCache(new Group(groupId, groupImg.getGroupName(), Uri.parse(groupImg.getGroupImg())));
                break;
            case SealAppContext.UPDATE_GROUP_NAME:
                ToastUtil.makeText(context, getString(R.string.update_success));
                GroupResp groupName = (GroupResp) resp;
                newGroupName = groupName.getGroupName();
                mGroupName.setText(newGroupName);
                break;
            case SealAppContext.GROUP_DISMISS:
                RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, null);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {

                            }
                        });
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
                ToastUtil.makeText(context, getString(R.string.dismiss_success));
                setResult(2, new Intent());
                finish();
                break;
            case SealAppContext.QUIT_GROUP:
                RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, null);
                            }
                            @Override
                            public void onError(RongIMClient.ErrorCode e) {

                            }
                        });
                    }
                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
                BroadcastManager.getInstance(context).sendBroadcast(Constant.GROUP_LIST_UPDATE);
                ToastUtil.makeText(context, getString(R.string.quit_success));
                setResult(2, new Intent());
                finish();
                break;
        }
    }

    private LoadingDialog loadingDialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("加载中...")
                .setCancelable(true)
                .setCancelOutside(true);
        loadingDialog = loadBuilde.create();
        loadingDialog.show();
    }
    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
    }


    private class GridAdapter extends BaseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.group_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            // 最后一个item，减人按钮
            if (position == getCount() - 1 && isCreated) {
                tv_username.setText("");
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isDeleteGroupMember", true);
                        intent.putExtra("GroupId", groupId);
                        intent.putExtra("groupName", newGroupName);
                        startActivityForResult(intent, DELETE_MEMBER);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isAddGroupMember", true);
                        intent.putExtra("GroupId", groupId);
                        intent.putExtra("groupName", newGroupName);
                        startActivityForResult(intent, ADD_MEMBER);

                    }
                });
            } else { // 普通成员
                final UserInfo userInfo = userList.get(position);
                tv_username.setText(userInfo.getName());
                ImageLoader.getInstance().displayImage(userInfo.getPortraitUri().toString(), iv_avatar, EBApplication.getOptions());
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UserDetailActivity.class);
                        Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                        intent.putExtra("friend", friend);
                        intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
                        //Groups not Serializable,just need group name
                        intent.putExtra("groupName", newGroupName);
                        intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                        startActivity(intent, null);
                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (isCreated) {
                return userList.size() + 2;
            } else {
                return userList.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    //监听群组信息修改
    private void setGroupsInfoChangeListener() {
        BroadcastManager.getInstance(this).addAction(SealAppContext.UPDATE_GROUP_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (null != intent) {
                    String groupName = intent.getStringExtra("String");
                    if(!TextUtils.isEmpty(groupName)){
                        newGroupName = groupName;
                        mGroupName.setText(groupName);
                        RongIM.getInstance().refreshGroupInfoCache(new Group(groupId, newGroupName, Uri.parse(groupImg)));
                    }
                }
            }
        });
        BroadcastManager.getInstance(this).addAction(SealAppContext.UPDATE_GROUP_MEMBER, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(null != intent){
                    String groupIdForUpdate = intent.getStringExtra("String");
                    if(groupIdForUpdate.equals(groupId)){
                        getGroupMembers();
                    }
                }
            }
        });
        BroadcastManager.getInstance(this).addAction(SealAppContext.QUIT_GROUP, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(null != intent){
                    String groupIdForUpdate = intent.getStringExtra("String");
                    if(groupIdForUpdate.equals(groupId)){
                        getGroupMembers();
                    }
                }
            }
        });
        BroadcastManager.getInstance(this).addAction(SealAppContext.GROUP_DISMISS, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(null != intent){
                    String groupIdForDismiss = intent.getStringExtra("String");
                    if(groupIdForDismiss.equals(groupId)){
                        backAsGroupDismiss();
                        if(!isCreated){
                            ToastUtil.makeText(context, getString(R.string.group_dismissed));
                        }
                    }
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        BroadcastManager.getInstance(this).destroy(SealAppContext.UPDATE_GROUP_NAME);
        BroadcastManager.getInstance(this).destroy(SealAppContext.UPDATE_GROUP_MEMBER);
        BroadcastManager.getInstance(this).destroy(SealAppContext.GROUP_DISMISS);
        super.onDestroy();
    }
    private void backAsGroupDismiss() {
        this.setResult(2, new Intent());
        finish();
    }
    private BottomMenuDialog dialog;

    public static final int TAKE_PHOTO = 1;

    public static final int CHOOSE_PHOTO = 2;

    private Uri imageUri;
    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(context);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                File outputImage=new File(getExternalCacheDir(),"groupImg.jpg");
                try{
                    //判断outputImage是否文件存在
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                    EBLog.e(TAG, e.toString());
                }
                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(context,"com.weimi.chat",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if(ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(GroupDetailActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
        dialog.show();
    }
    //打开相册
    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    ToastUtil.makeText(context, context.getString(R.string.open_picture_failed));
                }
                break;
        }
    }
    //图片裁剪的方法
    private void startCrop(Uri uri){
        UCrop.Options options = new UCrop.Options();
        //裁剪后图片保存在文件夹中
        Uri destinationUri = Uri.fromFile(new File(getExternalCacheDir(), "header_uCrop.jpg"));
        UCrop uCrop = UCrop.of(uri, destinationUri);//第一个参数是裁剪前的uri,第二个参数是裁剪后的uri
        uCrop.withAspectRatio(1,1);//设置裁剪框的宽高比例
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setToolbarTitle("图片裁剪");//设置标题栏文字
        options.setCropGridStrokeWidth(2);//设置裁剪网格线的宽度(我这网格设置不显示，所以没效果)
        options.setCropFrameStrokeWidth(5);//设置裁剪框的宽度
        options.setMaxScaleMultiplier(3);//设置最大缩放比例
        options.setHideBottomControls(true);//隐藏下边控制栏
        options.setShowCropGrid(true);  //设置是否显示裁剪网格
        options.setOvalDimmedLayer(false);//设置是否为圆形裁剪框
        options.setShowCropFrame(true); //设置是否显示裁剪边框(true为方形边框)
        options.setToolbarWidgetColor(Color.parseColor("#ffffff"));//标题字的颜色以及按钮颜色
        options.setDimmedLayerColor(Color.parseColor("#AA000000"));//设置裁剪外颜色
        options.setToolbarColor(Color.parseColor("#000000")); // 设置标题栏颜色
        options.setStatusBarColor(Color.parseColor("#000000"));//设置状态栏颜色
        options.setCropGridColor(Color.parseColor("#ffffff"));//设置裁剪网格的颜色
        options.setCropFrameColor(Color.parseColor("#ffffff"));//设置裁剪框的颜色
        options.setCompressionQuality(80);
        uCrop.withOptions(options);
        uCrop.start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            //照相
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK ){
                    startCrop(imageUri);//照相完毕裁剪处理
                }
                break;
            //相册
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    //判断手机系统版本号
                    startCrop(data.getData());
                }
                break;
            //裁剪后的效果
            case UCrop.REQUEST_CROP:
                if(resultCode==RESULT_OK){
                    Uri resultUri=UCrop.getOutput(data);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        mGroupHeader.setImageBitmap(bitmap);
                        //上传至服务器
                        if(null != bitmap){
                            String result = Base64.encodeToString(FileUtil.bitmapToByteArray(bitmap), Base64.DEFAULT);
                            chatPresenter.updateGroupInfo(groupId, null, result, UPDATE_GROUP_IMG);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            //错误裁剪的结果
            case UCrop.RESULT_ERROR:
                if(resultCode==RESULT_OK){
                    final Throwable cropError = UCrop.getError(data);
                    handleCropError(cropError);
                }
                break;
        }
    }
    //处理剪切失败的返回值
    private void handleCropError(Throwable cropError) {
        deleteTempPhotoFile();
        if (cropError != null) {
            ToastUtil.makeTextLong(context, cropError.getMessage());
        } else {
            ToastUtil.makeTextLong(context, getString(R.string.cut_picture_failed));
        }
    }

    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(Environment.getExternalStorageDirectory() + File.separator + "header.jpg");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }
    @Override
    public void initViews() {
        messageNotification = (SwitchButton) findViewById(R.id.sw_group_notfaction);
        liGroupClean = (LinearLayout) findViewById(R.id.group_clean);
        liGroupHeader = (LinearLayout) findViewById(R.id.li_group_header);
        liGroupName = (LinearLayout) findViewById(R.id.li_group_name);
        mGridView = (GridView) findViewById(R.id.userGridView);
        mGroupHeader = (SelectableRoundedImageView) findViewById(R.id.group_header);
        mGroupName = (TextView) findViewById(R.id.group_name);
        mQuitBtn = (Button) findViewById(R.id.group_quit);
        mDismissBtn = (Button) findViewById(R.id.group_dismiss);
        all_group_member = (TextView) findViewById(R.id.all_group_member);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.update_group_name,
                (ViewGroup) findViewById(R.id.update_group_name_layout));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.update_group_name_title));
        builder.setView(layout);
        builder.setNegativeButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = newGroupNameEdit.getText().toString();
                if(TextUtils.isEmpty(name)){
                    ToastUtil.makeText(context, getString(R.string.group_name_not_is_null));
                    return;
                }
                if(name.length()<2){
                    ToastUtil.makeText(context, getString(R.string.group_name_size_is_one));
                    return;
                }
                if(name.length()>10){
                    ToastUtil.makeText(context, getString(R.string.group_name_size_limit));
                    return;
                }
                chatPresenter.updateGroupInfo(groupId, name, null, SealAppContext.UPDATE_GROUP_NAME);
                newGroupNameEdit.setText("");
            }
        });
        builder.setNeutralButton(getString(R.string.cancel), null);
        alertDialog = builder.create();
        newGroupNameEdit = (EditText) layout.findViewById(R.id.update_group_name);
        EditTextUtil.setEditTextInhibitInputSpace(newGroupNameEdit);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void initListeners() {
        messageNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!TextUtils.isEmpty(groupId)){
                    if (isChecked) {
                        setConverstionNotif(Conversation.ConversationType.GROUP, groupId, true);
                    } else {
                        setConverstionNotif(Conversation.ConversationType.GROUP, groupId, false);
                    }
                }
            }
        });
        all_group_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtil.isSingle()){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfoList", (Serializable) allUserList);
                    startActivity(SearchGroupMemberActivity.class, bundle);
                }
            }
        });
        liGroupClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtil.isSingle()){
                    DialogUtil.openConfirmDialog(context, "提示", getString(R.string.clean_group_chat_history), getString(R.string.confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            ToastUtil.makeText(context, getString(R.string.clear_success));
                                            //RongIMClient.getInstance().cleanRemoteHistoryMessages(Conversation.ConversationType.GROUP, groupId, System.currentTimeMillis(), null);
                                        }

                                        @Override
                                        public void onError(RongIMClient.ErrorCode errorCode) {
                                            ToastUtil.makeText(context, getString(R.string.clear_failed));;
                                        }
                                    });
                                }
                            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //do nothing

                                }
                            });
                }
            }
        });
        liGroupHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCreated){
                    showPhotoDialog();
                }
            }
        });
        liGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCreated){
                    alertDialog.show();
                }
            }
        });
        mDismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtil.isSingle()){
                    DialogUtil.openConfirmDialog(context, "提示", getString(R.string.confirm_dismiss_group), getString(R.string.confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    chatPresenter.dismissGroup(userId, groupId);
                                }
                            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //do nothing

                                }
                            });
                }
            }
        });
        mQuitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtil.isSingle()){
                    DialogUtil.openConfirmDialog(context, "提示", getString(R.string.confirm_quit_group), getString(R.string.confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    chatPresenter.quitGroup(userId, groupId);
                                }
                            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //do nothing

                                }
                            });
                }
            }
        });
    }
    private void setConverstionNotif(Conversation.ConversationType conversationType, String targetId, boolean state) {
        Conversation.ConversationNotificationStatus cns;
        if (state) {
            cns = Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        } else {
            cns = Conversation.ConversationNotificationStatus.NOTIFY;
        }
        RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId, cns, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
              //do nothing
            }
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                ToastUtil.makeText(context, getString(R.string.setting_failed));
            }
        });
    }
    @Override
    public void initData() {
        //获取群组信息
        getGroupInfo();
        //获取群组所有成员
        getGroupMembers();
    }

    private void getGroupMembers() {
        WeiMiUserInfoManager.getInstance().getGroupMembers(groupId, new WeiMiUserInfoManager.ResultCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(List<UserInfo> userInfos) {
                allUserList.clear();
                allUserList.addAll(userInfos);
                userList = userInfos;
                title.setText(getString(R.string.group_memeber_num, userInfos.size()));
                if(userList.size() > 45){
                    userList = userList.subList(0, 45);
                }
                GridAdapter gridAdapter = new GridAdapter();
                mGridView.setAdapter(gridAdapter);
                gridAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errString) {
                if (!GeneralUtils.isNetworkConnected(context)) {
                    showToast(getString(R.string.network_not_available));
                    return;
                }
                showToast(getString(R.string.server_error));
            }
        });
    }

    private void getGroupInfo() {
        chatPresenter.getGroupInfo(groupId);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                //数据是使用Intent返回
                Intent intent = new Intent();
                //把返回数据存入Intent
                intent.putExtra("groupName", newGroupName);
                //设置返回数据
                this.setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
