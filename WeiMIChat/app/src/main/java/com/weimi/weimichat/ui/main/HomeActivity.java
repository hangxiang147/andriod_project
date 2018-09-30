package com.weimi.weimichat.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.chat.ChatResp;
import com.weimi.weimichat.biz.home.ChatPresenter;
import com.weimi.weimichat.biz.home.ChatView;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.constant.Event;
import com.weimi.weimichat.ui.adaper.AppFragmentPageAdapter;
import com.weimi.weimichat.ui.adaper.ConversationListAdapterEx;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.ui.firstpage.FirstPageFrament;
import com.weimi.weimichat.ui.notice.NoticeFrament;
import com.weimi.weimichat.ui.personcenter.PersonalFragment;
import com.weimi.weimichat.ui.widget.MorePopWindow;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class HomeActivity extends BaseActivity implements ChatView{
/*    *//**
     * 返回
     *//*
    private Button back;

    *//**
     * 图片
     *//*
    private ImageButton image;*/
    private TextView navTabFirstPage;//首页

    private TextView navTabChat;//群聊

    private  TextView navTabNotice;//资讯

    private  TextView navTabPersonal;//个人中心

    private Fragment fragmentFirstPage, fragmentNotice, fragmentPersonal;

    /**
     * 会话列表的fragment
     */
    private ConversationListFragment conversationListFragment = null;

    private int tabId;//底部导航选中项的id

    private ViewPager mcContainer;

    //管理Fragment
    private FragmentManager fragmentManager;

    private LinearLayout moreOperation;

    private ImageView conversionSearchView;//会话搜索

    private ImageView moreOperationView;//更多操作

    private EBSharedPrefManager manager;

    private String rongToken;

    private ChatPresenter chatPresenter;

    private String userId;

    private String nickName;

    private String headerUrl;

    private Context context;

    private TextView titleSpace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        setContentView(R.layout.activity_home);
        super.onCreate(savedInstanceState);
        //ButterKnife.bind(this);
    }

    public void initViews() {
        //获取缓存的用户id
        manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
        rongToken = manager.getKDPreferenceTestSetting().getString(EBSharedPrefUser.RONG_TOKEN, "");
        userId = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.USER_ID, "");
        nickName = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.NICK_NAME, "");
        headerUrl = manager.getKDPreferenceUserInfo().getString(EBSharedPrefUser.HEADER_URL, "");
        //back = (Button) findViewById(R.id.back);
        //image = (ImageButton) findViewById(R.id.image);
        navTabFirstPage = (TextView) findViewById(R.id.tv_tab1);
        navTabChat = (TextView) findViewById(R.id.tv_tab2);
        navTabNotice = (TextView) findViewById(R.id.tv_tab3);
        navTabPersonal = (TextView) findViewById(R.id.tv_tab4);
        mcContainer = (ViewPager) findViewById(R.id.vp_container);
        conversionSearchView = (ImageView) findViewById(R.id.ac_iv_search);
        moreOperationView = (ImageView) findViewById(R.id.seal_more);
        moreOperation = (LinearLayout) findViewById(R.id.moreOperation);
        titleSpace = (TextView) findViewById(R.id.space_title);
        navTabFirstPage.setSelected(true);
       // if (fragmentFirstPage == null) {
        fragmentFirstPage = new FirstPageFrament();
        fragmentNotice = new NoticeFrament();
        fragmentPersonal = new PersonalFragment();
        initConversationList();
        fragmentManager = getSupportFragmentManager();
       // }
       // getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, fragmentFirstPage)
       //         .commitAllowingStateLoss();
        List<Fragment> fragmentList = new ArrayList<Fragment>(){{add(fragmentFirstPage);
            add(conversationListFragment);add(fragmentNotice);add(fragmentPersonal);}};
        //// 设置填充器
        mcContainer.setAdapter(new AppFragmentPageAdapter(fragmentManager,fragmentList));
        // 设置缓存页面数
        //mcContainer.setOffscreenPageLimit(2);
        presenter = chatPresenter = new ChatPresenter();
        chatPresenter.attachView(this);
    }
    @Override
    public void initListeners() {
        //back.setOnClickListener(this);
        navTabFirstPage.setOnClickListener(this);
        navTabChat.setOnClickListener(this);
        navTabNotice.setOnClickListener(this);
        navTabPersonal.setOnClickListener(this);
        mcContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do Nothing
            }
            @Override
            public void onPageSelected(int position) {
                setTab(position);
                mcContainer.setCurrentItem(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                //Do Nothing
            }
        });
        conversionSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        moreOperationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MorePopWindow morePopWindow = new MorePopWindow(HomeActivity.this);
                morePopWindow.showPopupWindow(moreOperationView);
            }
        });

    }

    @Override
    public void initData() {
        //Picasso.with(this).load("http://i.imgur.com/DvpvklR.png").resize(DensityUtil.dip2px(this,200), DensityUtil.dip2px(this,200)).centerCrop().into(image);
        //EventBus.getDefault().post(Event.IMAGE_LOADER_SUCCESS);
        if(TextUtils.isEmpty(rongToken)){
           chatPresenter.getRongToke(userId, nickName, headerUrl);
        }else{
            connectRong();
        }
    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
    }

    @Override
    public void onClick(View v) {
        tabId = v.getId();
        switch (tabId){
            case R.id.tv_tab1:
                //setCurrentTab(1);
                mcContainer.setCurrentItem(0);
                conversionSearchView.setVisibility(View.INVISIBLE);
                moreOperationView.setVisibility(View.INVISIBLE);
                break;
            case R.id.tv_tab2:
               // setCurrentTab(2);
                mcContainer.setCurrentItem(1);
                conversionSearchView.setVisibility(View.VISIBLE);
                moreOperationView.setVisibility(View.VISIBLE);
                title.setText("群聊");
                break;
            case R.id.tv_tab3:
              //  setCurrentTab(3);
                mcContainer.setCurrentItem(2);
                title.setText("资讯");
                conversionSearchView.setVisibility(View.INVISIBLE);
                moreOperationView.setVisibility(View.INVISIBLE);
                break;
            case R.id.tv_tab4:
               // setCurrentTab(4);
                mcContainer.setCurrentItem(3);
                title.setText("个人中心");
                conversionSearchView.setVisibility(View.INVISIBLE);
                moreOperationView.setVisibility(View.INVISIBLE);
                break;
        }
        super.onClick(v);
    }

    @Override
    public void setHeader() {
        super.setHeader();
        title.setText("主页");
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
        ChatResp chatResp = (ChatResp) resp;
        rongToken = chatResp.getToken();
        if(!TextUtils.isEmpty(rongToken)){
            connectRong();
        }else{
            ToastUtil.makeText(context, "获取用户融云token失败");
        }

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
private void setTab(int i)
{
    setHeader(i);
    resetImgs();
    // 设置图片为亮色
    // 切换内容区域
    switch (i)
    {
        case 0:
            Drawable nav_1 = getResources().getDrawable(R.drawable.main_nav_selected_1);
            nav_1.setBounds(0, 0, nav_1.getMinimumWidth(), nav_1.getMinimumHeight());
            navTabFirstPage.setCompoundDrawables(null,nav_1,null,null);
            navTabFirstPage.setTextColor(getResources().getColor(R.color.colorPrimary));
            moreOperation.setVisibility(View.GONE);
            titleSpace.setVisibility(View.GONE);
            break;
        case 1:
            Drawable nav_2 = getResources().getDrawable(R.drawable.main_nav_selected_2);
            nav_2.setBounds(0, 0, nav_2.getMinimumWidth(), nav_2.getMinimumHeight());
            navTabChat.setCompoundDrawables(null,nav_2,null,null);
            navTabChat.setTextColor(getResources().getColor(R.color.colorPrimary));
            moreOperation.setVisibility(View.VISIBLE);
            titleSpace.setVisibility(View.VISIBLE);
            break;
        case 2:
            Drawable nav_3 = getResources().getDrawable(R.drawable.main_nav_selected_3);
            nav_3.setBounds(0, 0, nav_3.getMinimumWidth(), nav_3.getMinimumHeight());
            navTabNotice.setCompoundDrawables(null,nav_3,null,null);
            navTabNotice.setTextColor(getResources().getColor(R.color.colorPrimary));
            moreOperation.setVisibility(View.GONE);
            titleSpace.setVisibility(View.GONE);
            break;
        case 3:
            Drawable nav_4 = getResources().getDrawable(R.drawable.main_nav_selected_4);
            nav_4.setBounds(0, 0, nav_4.getMinimumWidth(), nav_4.getMinimumHeight());
            navTabPersonal.setCompoundDrawables(null,nav_4,null,null);
            navTabPersonal.setTextColor(getResources().getColor(R.color.colorPrimary));
            moreOperation.setVisibility(View.GONE);
            titleSpace.setVisibility(View.GONE);
            break;
    }
}

    /**
     * 切换图片至暗色
     */
    private void resetImgs()
    {
        Drawable nav_1 = getResources().getDrawable(R.drawable.main_nav_1);
        nav_1.setBounds(0, 0, nav_1.getMinimumWidth(), nav_1.getMinimumHeight());
        navTabFirstPage.setCompoundDrawables(null,nav_1,null,null);
        Drawable nav_2 = getResources().getDrawable(R.drawable.main_nav_1);
        nav_2.setBounds(0, 0, nav_2.getMinimumWidth(), nav_2.getMinimumHeight());
        navTabChat.setCompoundDrawables(null,nav_2,null,null);
        Drawable nav_3 = getResources().getDrawable(R.drawable.main_nav_1);
        nav_3.setBounds(0, 0, nav_3.getMinimumWidth(), nav_3.getMinimumHeight());
        navTabNotice.setCompoundDrawables(null,nav_3,null,null);
        Drawable nav_4 = getResources().getDrawable(R.drawable.main_nav_1);
        nav_4.setBounds(0, 0, nav_4.getMinimumWidth(), nav_4.getMinimumHeight());
        navTabPersonal.setCompoundDrawables(null,nav_4,null,null);

        //文字
        navTabFirstPage.setTextColor(getResources().getColor(R.color.nav));
        navTabChat.setTextColor(getResources().getColor(R.color.nav));
        navTabNotice.setTextColor(getResources().getColor(R.color.nav));
        navTabPersonal.setTextColor(getResources().getColor(R.color.nav));
    }
    private void setHeader(int position){
        switch (position){
            case 0:
                title.setText("主页");
                break;
            case 1:
                title.setText("群聊");
                break;
            case 2:
                title.setText("资讯");
                break;
            case 3:
                title.setText("个人中心");
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragmentPersonal.onActivityResult(requestCode, resultCode, data);
    }
    public void connectRong(){
        RongIM.connect(rongToken, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                EBLog.e("connect", "onTokenIncorrect");
                //重新获取token
                //rongToken = chatPresenter.getRongToke(context, userId, nickName, headerUrl);
            }

            /**
             *  连接融云成功
             * @param userId 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String userId) {
                //connectResultId = s;
                EBLog.e("connect", "onSuccess userId:" + userId);
            }

            /**
             *
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                EBLog.e("connect", "onError errorcode:" + errorCode.getValue());
            }
        });
    }
    private void initConversationList() {
        if (conversationListFragment == null) {
            conversationListFragment = new ConversationListFragment();
            conversationListFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .build();
            conversationListFragment.setUri(uri);
        }
    }
}
