package com.weimi.weimichat.bean;

/**
 * <网络请求返回体>
 *
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class BaseResp
{
    /**
     * 返回状态码(200（正常）、400（错误提示）、500（代码异常）)
     */
    protected int code;

    /**
     * 返回信息描述
     */
    protected String massege;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMassege() {
        return massege;
    }

    public void setMassege(String massege) {
        this.massege = massege;
    }
}
