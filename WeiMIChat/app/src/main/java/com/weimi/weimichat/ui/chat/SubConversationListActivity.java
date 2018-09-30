package com.weimi.weimichat.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;
import io.rong.imkit.RongContext;
import io.rong.imkit.fragment.SubConversationListFragment;
import io.rong.imkit.widget.adapter.SubConversationListAdapter;

/**
 * Created by Bob on 15/11/3.
 * 聚合会话列表
 */
public class SubConversationListActivity extends BaseActivity implements ChatView {
    //聚合会话参数
    private String type;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        setContentView(R.layout.sub_conversation_list);
        super.onCreate(savedInstanceState);
        SubConversationListFragment fragment = new SubConversationListFragment();
        fragment.setAdapter(new SubConversationListAdapter(RongContext.getInstance()));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();
        Intent intent = getIntent();
        if (intent.getData() == null) {
            return;
        }
        type = intent.getData().getQueryParameter("type");
    }
    @Override
    public void setHeader() {
        super.setHeader();
        title.setText(getString(R.string.system_notice));
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

    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {

    }
}
