package com.oakraw.facies.custom;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by oakraw90 on 5/16/2014.
 */
public class util {

    public static int pxToDip(Context context ,int px){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, metrics);
    }
}
