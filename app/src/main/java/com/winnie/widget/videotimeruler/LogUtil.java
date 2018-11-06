package com.winnie.widget.videotimeruler;

import android.util.Log;

import com.example.winnie.viedotimeview.BuildConfig;

/**
 * @author : winnie
 * @date : 2018/10/9
 * @desc
 */
public class LogUtil {
    public static void logV(String tag, String format, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, String.format(format, args));
        }
    }

    public static void logD(String tag, String format, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, String.format(format, args));
        }
    }

    public static void logI(String tag, String format, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, String.format(format, args));
        }
    }
}
