package com.weimi.weimichat.biz.home;

import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.bean.notice.NoticeBean;
import com.weimi.weimichat.bean.notice.NoticeResp;
import com.weimi.weimichat.biz.BasePresenter;
import com.weimi.weimichat.bridge.BridgeFactory;
import com.weimi.weimichat.bridge.Bridges;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefManager;
import com.weimi.weimichat.bridge.cache.sharePref.EBSharedPrefUser;
import com.weimi.weimichat.bridge.http.OkHttpManager;
import com.weimi.weimichat.bridge.security.SecurityManager;
import com.weimi.weimichat.capabilities.http.ITRequestResult;
import com.weimi.weimichat.capabilities.http.Param;
import com.weimi.weimichat.constant.URLUtil;

import java.lang.ref.SoftReference;

/**
 * Created by yxl on 2018/9/10.
 */

public class NoticePresenter extends BasePresenter<NoticeView> {

    public void showNoticeContent(int noticeId){
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncGetByTag(URLUtil.NOTICE_DETAIL, getName(), new ITRequestResult<NoticeResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(NoticeResp entity) {
                        mvpView.onSuccess(entity, "");
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, NoticeResp.class, new Param("id", noticeId));
    }
}
