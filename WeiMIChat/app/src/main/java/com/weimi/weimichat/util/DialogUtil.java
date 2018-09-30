package com.weimi.weimichat.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;

/**
 * 
 * <弹出框公共类>
 * @author yxl
 * @version [版本号, 2016/6/6]
 * @see [相关类/方法]
 * @since [V1]
 */
public class DialogUtil
{
    /**
     * 提示信息
     * @param context
     * @param title
     * @param msg
     * @param btnName
     */
    public static void alertMsg(Context context, String title, String msg, String btnName) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(btnName, null)
                .show();
    }
    /**
     * 弹出一个带确认和取消的dialog
     * @param context
     * @param title
     * @param msg
     * @param okButton
     * @param ok 点击确定事件
     * @param noButton
     * @param no 点击取消事件
     * @return
     */
    public static AlertDialog openConfirmDialog(Context context, String title,
                                                String msg, String okButton, DialogInterface.OnClickListener ok, String noButton,
                                                DialogInterface.OnClickListener no) {
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNegativeButton(okButton, ok);
        builder.setNeutralButton(noButton, no);
        AlertDialog loadWaitDialog = builder.create();
        loadWaitDialog.setCanceledOnTouchOutside(false);
        loadWaitDialog.show();
        return loadWaitDialog;
    }
}
