package com.weimi.weimichat.capabilities.json;

import android.util.Log;

import com.google.gson.Gson;
import com.weimi.weimichat.bridge.http.HttpException;
import com.weimi.weimichat.capabilities.log.EBLog;

import org.json.JSONObject;


/**
 * <json公共解析库>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class GsonHelper {

    private static String TAG = GsonHelper.class.getName();

    private static Gson gson = new Gson();

    /**
     * 把json string 转化成类对象
     *
     * @param str
     * @param t
     * @return
     */
    public static <T> T toType(String str, Class<T> t) {
        try {
            if (str != null && !"".equals(str.trim())) {
                T res = gson.fromJson(str.trim(), t);
                return res;
            }
        } catch (Exception e) {
            EBLog   .e(TAG, "exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * json 转 jsonObject
     *
     * @param t
     * @return
     */
    public static <T> String toJson(T t) {
        return gson.toJson(t);
    }

    public static JSONObject toJsonObject(String str) throws Exception{
        return new JSONObject(str);

    }
    /**
     * 将json中result转化为对象
     *
     * @param str
     * @param t
     * @return
     */
    public static <T> T toTypeForResult(String str, Class<T> t) {
        try {
            if (str != null && !"".equals(str.trim())) {
                JSONObject json = toJsonObject(str);
                T res = gson.fromJson(json.getString("result"), t);
                return res;
            }
        } catch (Exception e) {
            EBLog.e(TAG, "exception:" + e.getMessage());
        }
        return null;
    }
    /**
     * 将bean对象转化成json字符串
     * @param obj
     * @return
     * @throws HttpException
     */
    public static String beanToJson(Object obj) throws HttpException {
        String result = gson.toJson(obj);
        EBLog.e(TAG, "beanToJson: " + result);
        return result;
    }
}
