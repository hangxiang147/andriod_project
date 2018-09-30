package com.weimi.weimichat.ui.chat;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weimi.weimichat.EBApplication;
import com.weimi.weimichat.R;
import com.weimi.weimichat.SealAppContext;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.GroupResp;
import com.weimi.weimichat.biz.WeiMiUserInfoManager;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.http.OkHttpManager;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.server.broadcast.BroadcastManager;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import com.weimi.weimichat.util.ToastUtil;

import org.w3c.dom.Text;

import de.greenrobot.event.EventBus;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.UriFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

/**
 * Created by yxl on 2018/9/19.
 */

public class ConversationActivity extends BaseActivity implements ChatView{

    private Context context;

    private String TAG = ConversationActivity.class.getSimpleName();

    private RelativeLayout layout_announce;
    private TextView tv_announce;
    private ImageView iv_arrow;
    private String groupId;
    private ImageView right;
    private LinearLayout back;

    private ChatPresenter chatPresenter;

    private static final int GROUP_SETTING = 1;

    private static final int DELETE_GROUP = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
        setContentView(R.layout.conversation);
        super.onCreate(savedInstanceState);
        enterFragment();
        setGroupsInfoChangeListener();
    }
    private ConversationFragment fragment;

    /**
     * 加载会话页面
     */
    private void enterFragment() {

        fragment = new ConversationFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.conversation, fragment);
        transaction.commitAllowingStateLoss();
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
        title.setText(groupResp.getGroupName());
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
        layout_announce = (RelativeLayout) findViewById(R.id.ll_annouce);
        iv_arrow = (ImageView) findViewById(R.id.iv_announce_arrow);
        layout_announce.setVisibility(View.GONE);
        tv_announce = (TextView) findViewById(R.id.tv_announce_msg);
        back = (LinearLayout) findViewById(R.id.ll_back);
        title = (TextView) findViewById(R.id.tv_title);
        right = (ImageView) findViewById(R.id.tv_right);
    }

    @Override
    public void setHeader() {

    }

    @Override
    public void initListeners() {
        back.setOnClickListener(this);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupId);
                //进入群组设置界面
                openActivityForResult(GroupDetailActivity.class, GROUP_SETTING, bundle);
            }
        });
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) return;
        groupId = intent.getData().getQueryParameter("targetId");
        chatPresenter.getGroupInfo(groupId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case GROUP_SETTING:
                if(null != data){
                    String groupName = data.getStringExtra("groupName");
                    if(TextUtils.isEmpty(groupName)){
                        title.setText(groupName);
                    }
                }
                break;
            case DELETE_GROUP:
                finish();
                break;
        }
    }
    //监听群名称修改
    private void setGroupsInfoChangeListener() {
        BroadcastManager.getInstance(this).addAction(SealAppContext.UPDATE_GROUP_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String groupName = intent.getStringExtra("String");
                    if(!TextUtils.isEmpty(groupName)){
                        title.setText(groupName);
                    }
                }
            }
        });
    }
}
