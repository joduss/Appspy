package com.epfl.appspy;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jonathan Duss on 10.04.15.
 *
 * Custom Toast used for debug
 */
public class ToastDebug {

    Toast t;

    private ToastDebug(Toast t){
        this.t = t;
    }

    public static ToastDebug makeText(Context context, String message, int duration){
        return new ToastDebug(Toast.makeText(context, message, duration));
    }

    public void show(){
        if(GlobalConstant.DEBUG_TOAST) {
            t.show();
        }
    }



}
