package com.weimi.weimichat.biz;

import com.weimi.weimichat.bean.BaseResp;

/**
 * <功能详细描述>
 *
 * @author yxl
 * @version [版本号, 2016/5/4]
 * @see [相关类/方法]
 * @since [V1]
 */
public interface IMvpView {
    void onError(String errorMsg, String code);

    void onSuccess(String code);

    void onSuccess(BaseResp resp, String code);

    void showLoading();

    void hideLoading();
}
