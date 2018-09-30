package com.weimi.weimichat.ui.personcenter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.weimi.weimichat.capabilities.cache.FileUtil;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.AppUtil;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yxl on 2018/9/12.
 */

public class UploadHeaderActivity extends BaseActivity implements IUserLoginView {

    private LoginPresenter loginPresenter;

    private String userId;

    private String nickName;

    private ImageView headerView;

    private Button cameraBtn;

    private Button pictureBtn;

    private String headerUrl;

    private Context context;

    public static final int TAKE_PHOTO = 1;

    public static final int CHOOSE_PHOTO = 2;

    private Uri imageUri;

    private static String tag = UploadHeaderActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        nickName = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.NICK_NAME, "");
        headerUrl = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.HEADER_URL, "");
        setContentView(R.layout.update_header);
        presenter = loginPresenter = new LoginPresenter();
        loginPresenter.attachView(this);
        context = UploadHeaderActivity.this;
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
        LoginResp userInfo = (LoginResp)resp;
        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.HEADER_URL, userInfo.getUserImg());
        headerUrl = userInfo.getUserImg();
        //刷新用户头像
        RongIM.getInstance().refreshUserInfoCache(new UserInfo(userId, nickName, Uri.parse(headerUrl)));
    }

    private LoadingDialog dialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("上传中...")
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
        title.setText("修改头像");
    }
    @Override
    public void initViews() {
        headerView = (ImageView) findViewById(R.id.headImage);
        cameraBtn = (Button) findViewById(R.id.camera);
        pictureBtn = (Button) findViewById(R.id.picture);
        if(!TextUtils.isEmpty(headerUrl)){
            Glide.with(this).load(Uri.parse(headerUrl))
                    .into(headerView);
        }

    }

    @Override
    public void initListeners() {
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File(路径,文件名字)对象,用于储存拍照后的图片
                /*
                getExternalCacheDir获取SDCard/Android/data/你的应用包名/cache/目录,一般存放临时缓存数据
                getExternalFilesDir()获取SDCard/Android/data/你的应用的包名/files/ 目录,一般放一些长时间保存的数据
                */
                File outputImage=new File(getExternalCacheDir(),"header.jpg");
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

                //不同Android版本进行处理
                /*
                * Android 7.0开始，直接读取本地Uri被认为是不安全的.
                * 低于 Android 7.0只需要将File对象转换成Uri对象,而Uri就是标识output_iamge.jpg的路径
                * 高于等于Android 7.0需要通过特定的内容容器FileProvider的getUriForFile()将File对象转换成封装的Uri对象
                * getUriForFile()需要三个参数,第一个Context对象,第二个任意且唯一的字符串(自己随便取)但必须保持与你注册里表一致,第三个就是创建的File对象
                * 别忘了在AdnroidManifest.xml进行内容容器FileProvider的注册,以及SD卡访问权限
                * */

                if(Build.VERSION.SDK_INT>=24){
                    imageUri = FileProvider.getUriForFile(context,"com.weimi.chat",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }

                //启动照相机
                /*通过隐式Intent启动,并且给上个界面返回一个结果码TAKE_PHOTO=1
                * 传递时最好传路径,直接传File,内存太大,并且Intent传值是有内存限制大小的*/
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(UploadHeaderActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    openAlbum();
                }
            }
        });
    }

    //打开相册
    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }
    @Override
    public void initData() {

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
    @Override
    public void clearEditContent() {

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
                        Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        headerView.setImageBitmap(bitmap);
                        if(AppUtil.isSingle()){
                            //上传图片到服务器
                            String result = Base64.encodeToString(FileUtil.bitmapToByteArray(bitmap), Base64.DEFAULT);
                            LoginResp userInfo = new LoginResp();
                            userInfo.setUserId(userId);
                            userInfo.setUserImg(result);
                            loginPresenter.updateUserInfo(userInfo);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                //数据是使用Intent返回
                Intent intent = new Intent();
                //把返回数据存入Intent
                intent.putExtra("headerUrl", headerUrl);
                //设置返回数据
                this.setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
