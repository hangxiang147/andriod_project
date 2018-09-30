package com.weimi.weimichat.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.weimi.weimichat.biz.BasePresenter;
import com.weimi.weimichat.biz.IMvpView;

/**
 * Created by yxl on 2018/9/3.
 */

public abstract class BaseFragment extends Fragment {

    /**
     * 初始化布局组件
     */
    public abstract void initViews();

    /**
     * 增加按钮点击事件
     */
    public abstract void initListeners();

    /**
     * 初始化数据
     */
    public abstract void initData();

    /**
     * 初始化公共头部
     */
    public abstract void setHeader();

    public void startActivity(Context context, Class<?> openClass, Bundle bundle) {
        Intent intent = new Intent(context, openClass);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivity(intent);
    }
    public void openActivityForResult(Context context, Class<?> openClass, int requestCode, Bundle bundle) {
        Intent intent = new Intent(context, openClass);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }
}
