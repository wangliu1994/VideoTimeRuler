package com.example.winnie.viedotimeview;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author : winnie
 * @date : 2018/10/9
 * @desc
 */
public class PxUtil {

    public static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
