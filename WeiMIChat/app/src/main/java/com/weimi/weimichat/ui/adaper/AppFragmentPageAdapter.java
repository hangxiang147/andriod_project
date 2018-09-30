package com.weimi.weimichat.ui.adaper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by yxl on 2018/9/3.
 */

public class AppFragmentPageAdapter extends FragmentPagerAdapter {
    public List<Fragment> mFragmentList;

    public AppFragmentPageAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        mFragmentList=fragmentList;
    }

    @Override
    public Fragment getItem(int position) {

        return mFragmentList==null?null:mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList==null?0:mFragmentList.size();
    }
}
