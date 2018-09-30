package com.weimi.weimichat.server.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.weimi.weimichat.R;

/**
 * Created by AMing on 15/11/26.
 * Company RongCloud
 */
public class DialogWithYesOrNoUtils {

    private static DialogWithYesOrNoUtils instance = null;

    public static DialogWithYesOrNoUtils getInstance() {
        if (instance == null) {
            instance = new DialogWithYesOrNoUtils();
        }
        return instance;
    }

    private DialogWithYesOrNoUtils() {
    }

    public void showDialog(Context context, String titleInfo, final DialogWithYesOrNoUtils.DialogCallBack callBack) {
        AlertDialog.Builder alterDialog = new AlertDialog.Builder(context);
        alterDialog.setMessage(titleInfo);
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.executeEvent();
            }
        });
        alterDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alterDialog.show();
    }

    public interface DialogCallBack {
        void executeEvent();
    }
}
