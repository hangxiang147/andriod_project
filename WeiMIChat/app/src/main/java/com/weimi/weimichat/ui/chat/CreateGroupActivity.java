package com.weimi.weimichat.ui.chat;

import android.Manifest;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.Friend;
import com.weimi.weimichat.bean.chat.GroupResp;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.capabilities.cache.FileUtil;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.ui.widget.BottomMenuDialog;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.EditTextUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;

/**
 * Created by yxl on 2018/9/18.
 */

public class CreateGroupActivity extends BaseActivity implements ChatView {

    public static final String REFRESH_GROUP_UI = "REFRESH_GROUP_UI";

    private Context context;

    private String userId;

    private AsyncImageView asyncImageView;

    private Button mButton;

    private EditText mGroupNameEdit;

    private String mGroupName,  mGroupId;

    private List<String> groupIds = new ArrayList<>();//群人员userId集合

    private ChatPresenter chatPresenter;

    private static String tag = CreateGroupActivity.class.getName();

    private  Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_create_group);
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        super.onCreate(savedInstanceState);
        context = this;
        List<Friend> memberList = (List<Friend>) getIntent().getSerializableExtra("GroupMember");
        if (memberList != null && memberList.size() > 0) {
            groupIds.add(userId);
            for (Friend f : memberList) {
                groupIds.add(f.getUserId());
            }
        }
    }

    @Override
    public void setHeader() {
        super.setHeader();
        title.setText(R.string.rc_item_create_group);
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
        GroupResp groupResp = (GroupResp) resp;
        mGroupId = groupResp.getGroupId();
        BroadcastManager.getInstance(context).sendBroadcast(REFRESH_GROUP_UI);
        ToastUtil.makeText(context, getString(R.string.create_group_success));
        RongIM.getInstance().startConversation(context, Conversation.ConversationType.GROUP, mGroupId, mGroupName);
        finish();
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

    @Override
    public void initViews() {
        //获取缓存的用户id
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        asyncImageView = (AsyncImageView) findViewById(R.id.img_Group_portrait);
        mButton = (Button) findViewById(R.id.create_ok);
        mGroupNameEdit = (EditText) findViewById(R.id.create_groupname);
        EditTextUtil.setEditTextInhibitInputSpace(mGroupNameEdit);
    }

    @Override
    public void initListeners() {
        asyncImageView.setOnClickListener(this);
        mButton.setOnClickListener(this);
        asyncImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(AppUtil.isSingle()){
            super.onClick(v);
            switch (v.getId()) {
                case R.id.img_Group_portrait:
                    showPhotoDialog();
                    break;
                case R.id.create_ok:
                    mGroupName = mGroupNameEdit.getText().toString().trim();
                    if (TextUtils.isEmpty(mGroupName)) {
                        ToastUtil.makeText(context, getString(R.string.group_name_not_is_null));
                        return;
                    }
                    if (mGroupName.length() == 1) {
                        ToastUtil.makeText(context, getString(R.string.group_name_size_is_one));
                        return;
                    }
                    if (mGroupName.length() >10) {
                        ToastUtil.makeText(context, getString(R.string.group_name_size_limit));
                        return;
                    }
                    if (groupIds.size() > 1) {
                        String result = "";
                        if(null != bitmap){
                            result = Base64.encodeToString(FileUtil.bitmapToByteArray(bitmap), Base64.DEFAULT);
                        }
                        chatPresenter.createGroup(mGroupName, groupIds, result, userId);
                    }
                    break;
            }
        }
    }

    @Override
    public void initData() {

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
                    EBLog.e(tag, e.toString());
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
                    ActivityCompat.requestPermissions(CreateGroupActivity.this,
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
                        bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        asyncImageView.setImageBitmap(bitmap);
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
}
