package com.weimi.weimichat.biz;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.weimi.weimichat.asyn.AsyncTaskManager;
import com.weimi.weimichat.bean.chat.Group;
import com.weimi.weimichat.bean.chat.User;
import com.weimi.weimichat.bean.chat.UserResp;
import com.weimi.weimichat.bean.home.LoginResp;
import com.weimi.weimichat.capabilities.http.NetUtil;
import com.weimi.weimichat.capabilities.json.GsonHelper;
import com.weimi.weimichat.capabilities.log.EBLog;
import com.weimi.weimichat.constant.URLUtil;
import java.util.ArrayList;
import java.util.List;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by yxl on 2018/9/21.
 */

public class WeiMiUserInfoManager {

    private final static String TAG = WeiMiUserInfoManager.class.getSimpleName();

    private static WeiMiUserInfoManager wInstance;

    private final Context mContext;

    static Handler mHandler;

    private final AsyncTaskManager mAsyncTaskManager;

    public static WeiMiUserInfoManager getInstance() {
        return wInstance;
    }

    public WeiMiUserInfoManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mAsyncTaskManager = AsyncTaskManager.getInstance(mContext);
    }

    public static void init(Context context) {
        EBLog.d(TAG, "WeiMiUserInfoManager init");
        wInstance = new WeiMiUserInfoManager(context);
    }

    public void getUserInfo(final String userId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] result = NetUtil.getNetData(URLUtil.GET_USER_INFO+"?userId="+userId);
                if (result != null) {
                    //把从网络上获取的byte类型的数据转换为String字符串
                    String jsonString = new String(result);
                    try {
                        //用json解析工具来解析该字符串数据
                        LoginResp userInfo = GsonHelper.toTypeForResult(jsonString, LoginResp.class);
                        if(null != userInfo){
                            String imgUri = userInfo.getUserImg();
                            UserInfo userInfoRong = new UserInfo(userInfo.getUserId(), userInfo.getNickName(), imgUri==null ? null:Uri.parse(imgUri));
                            RongIM.getInstance().refreshUserInfoCache(userInfoRong);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void getGroupInfo(final String groupId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] result = NetUtil.getNetData(URLUtil.GET_GROUP_INFO+"?groupId="+groupId);
                if (result != null) {
                    //把从网络上获取的byte类型的数据转换为String字符串
                    String jsonString = new String(result);
                    try {
                        //用json解析工具来解析该字符串数据
                        Group group = GsonHelper.toTypeForResult(jsonString, Group.class);
                        if(null != group){
                            String imgUri = group.getGroupImg();
                            io.rong.imlib.model.Group groupRong = new io.rong.imlib.model.Group(group.getGroupId(), group.getGroupName(), imgUri==null ? null:Uri.parse(imgUri));
                            RongIM.getInstance().refreshGroupInfoCache(groupRong);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void getGroupMembers(final String groupId, final ResultCallback<List<UserInfo>> callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<UserInfo> users = new ArrayList<>();
                byte[] result = NetUtil.getNetData(URLUtil.GET_GROUP_MEMBER+"?groupId="+groupId);
                if (result != null) {
                    //把从网络上获取的byte类型的数据转换为String字符串
                    String jsonString = new String(result);
                    try {
                        //用json解析工具来解析该字符串数据
                        UserResp userResp = GsonHelper.toType(jsonString, UserResp.class);
                        for(User user: userResp.getResult()){
                            users.add(new UserInfo(user.getUserId(), user.getNickName(), Uri.parse(user.getUserImg())));
                        }
                        callback.onCallback(users);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    onCallBackFail(callback);
                }
            }
        }).start();
    }
    /**
     * 泛型类，主要用于 API 中功能的回调处理。
     *
     * @param <T> 声明一个泛型 T。
     */
    public static abstract class ResultCallback<T> {

        public static class Result<T> {
            public T t;
        }

        public ResultCallback() {

        }

        /**
         * 成功时回调。
         *
         * @param t 已声明的类型。
         */
        public abstract void onSuccess(T t);

        /**
         * 错误时回调。
         *
         * @param errString 错误提示
         */
        public abstract void onError(String errString);


        public void onFail(final String errString) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onError(errString);
                }
            });
        }

        public void onCallback(final T t) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(t);
                }
            });
        }
    }
    private void onCallBackFail(ResultCallback<?> callback) {
        if (callback != null) {
            callback.onFail(null);
        }
    }
}
