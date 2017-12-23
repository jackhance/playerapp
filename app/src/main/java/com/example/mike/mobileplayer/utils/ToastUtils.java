package com.example.mike.mobileplayer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by 国鑫 on 2017/12/16.
 * 作用：
 */

public class ToastUtils {

    private static Toast toast;

    public static void show(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}