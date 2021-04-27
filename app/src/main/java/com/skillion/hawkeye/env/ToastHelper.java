package com.skillion.hawkeye.env;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastHelper {

    private static Toast toast;

    public static void showToast(String text, Context context){
        if (toast==null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }else{
            View view = toast.getView();
            toast.cancel();
            toast= new Toast(context);
            toast.setView(view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setText(text);
        }
        toast.show();
    }

}