package com.example.winnie.viedotimeview;

/**
 * @author : winnie
 * @date : 2018/10/9
 * @desc 时间戳转换时间工具
 */
public class TimeUtil {
    public static final int MAX_TIME_VALUE = 24 * 3600;
    public final static long SECOND_UNIT = 1;
    public final static long MINUTE_UNIT = SECOND_UNIT * 60;
    public final static long HOUR_UNIT = MINUTE_UNIT * 60;
    public final static long DAY_UNIT = HOUR_UNIT *24;

    /**
     * 格式化时间 HH:mm
     * @param timeValue 距离 1970/1/1 8:0:0 的差值
     * @return 格式化后的字符串，eg：3600 to 01:00
     */
    public static String formatTimeHHmm(long timeValue) {
        if (timeValue < 0){
            timeValue=0;
        }

        long hour = (timeValue % DAY_UNIT / HOUR_UNIT + 8) % 24;
        long minute = timeValue % HOUR_UNIT / MINUTE_UNIT;
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour).append(':');
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute);
        return sb.toString();
    }

    /**
     * 格式化时间 HH:mm
     * @param timeValue 距离 1970/1/1 8:0:0 的差值
     * @return 格式化后的字符串，eg：3600 to 01:00:00
     */
    public static String formatTimeHHmmss(long timeValue) {
        if (timeValue < 0){
            timeValue=0;
        }

        long hour = (timeValue % DAY_UNIT / HOUR_UNIT + 8) % 24;
        long minute = timeValue % HOUR_UNIT / MINUTE_UNIT;
        long second = timeValue % MINUTE_UNIT / SECOND_UNIT;
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour).append(':');
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute).append(':');
        if (second < 10) {
            sb.append('0');
        }
        sb.append(second);
        return sb.toString();
    }
}
