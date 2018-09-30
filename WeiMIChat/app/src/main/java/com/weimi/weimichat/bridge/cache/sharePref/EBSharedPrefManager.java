
package com.weimi.weimichat.bridge.cache.sharePref;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.weimi.weimichat.bridge.BridgeLifeCycleListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * <管理SharedPreference存储、读取>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class EBSharedPrefManager implements BridgeLifeCycleListener {

    /** SharedPreference文件名列表 */
    private static final String PREF_NAME_USERINFO = "userinfo";

    private static final String PREF_NAME_SETTING = "setting";

    /**
     * 用户信息缓存
     */
    private static EBSharedPrefUser mEBSharedPrefUser;

    /**
     * 设置信息缓存
     */
    private EBSharedPrefSetting mEBSharedPrefSetting;

    private static Context mApplicationContext;

    @Override
    public void initOnApplicationCreate(Context context) {
        mApplicationContext = context;
    }

    @Override
    public void clearOnApplicationQuit() {
    }

    /**
     * 用户信息缓存
     * @return
     */
    public EBSharedPrefUser getKDPreferenceUserInfo() {
        return mEBSharedPrefUser == null ? mEBSharedPrefUser = new EBSharedPrefUser(
                mApplicationContext, PREF_NAME_USERINFO) : mEBSharedPrefUser;
    }

    /**
     * 设置信息缓存
     */
    public EBSharedPrefSetting getKDPreferenceTestSetting() {
        return mEBSharedPrefSetting == null ? mEBSharedPrefSetting = new EBSharedPrefSetting(
                mApplicationContext, PREF_NAME_SETTING)
                : mEBSharedPrefSetting;
    }
    /**
     * writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
     * 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
     *
     * @param object 待加密的转换为String的对象
     * @return String   加密后的String
     */
    private static String Object2String(Object object) throws Exception{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        String string = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
        objectOutputStream.close();
        return string;
    }

    /**
     * 使用Base64解密String，返回Object对象
     *
     * @param objectString 待解密的String
     * @return object      解密后的object
     */
    private static Object String2Object(String objectString) throws Exception{
        byte[] mobileBytes = Base64.decode(objectString.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
        ObjectInputStream objectInputStream = null;
        objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

    /**
     * 使用SharedPreference保存对象
     *
     * @param fileKey    储存文件的key
     * @param key        储存对象的key
     * @param saveObject 储存的对象
     */
    public static void save(String fileKey, String key, Object saveObject) throws Exception{
        SharedPreferences sharedPreferences = mApplicationContext.getApplicationContext().getSharedPreferences(fileKey, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String string = Object2String(saveObject);
        editor.putString(key, string);
        editor.commit();
    }

    /**
     * 获取SharedPreference保存的对象
     *
     * @param fileKey 储存文件的key
     * @param key     储存对象的key
     * @return object 返回根据key得到的对象
     */
    public static Object get(String fileKey, String key) throws Exception{
        SharedPreferences sharedPreferences =  mApplicationContext.getApplicationContext().getSharedPreferences(fileKey, Activity.MODE_PRIVATE);
        String string = sharedPreferences.getString(key, null);
        if (string != null) {
            Object object = String2Object(string);
            return object;
        } else {
            return null;
        }
    }
}
