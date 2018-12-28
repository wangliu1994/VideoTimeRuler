package com.winnie.widget.videotimeruler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winnie.widget.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * @author : winnie
 * @date : 2018/10/10
 * @desc
 */
public class RunTimeRulerLayout extends LinearLayout {
    RunTimeRulerView mTrvTimeRuler;
    TextView mTvTimeRuler;

    public RunTimeRulerLayout(Context context) {
        this(context, null);
    }

    public RunTimeRulerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_run_time_ruler, this);
        mTrvTimeRuler = findViewById(R.id.trv_time_ruler);
        mTvTimeRuler = findViewById(R.id.tv_time_ruler);

        long current = System.currentTimeMillis() / 1000 % (24 * 3600);
        List<RunTimeRulerView.TimePart> list = new ArrayList<>();
        for (int i = -10; i < 10; i++) {
            RunTimeRulerView.TimePart part = new RunTimeRulerView.TimePart();
            part.setStartTime(current, i * 1000);
            part.setEndTime(new Random().nextInt(1000));
            list.add(part);
        }
        mTrvTimeRuler.setTimePartList(list);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        mTrvTimeRuler.setOnTimeChangedListener(new RunTimeRulerView.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {
                mTvTimeRuler.setText(format.format(newTime * 1000));
                if (mOnTimeChangeListener != null) {
                    mOnTimeChangeListener.onTimeChanged(newTime);
                }
            }
        });

        mTrvTimeRuler.setCurrentTime(current);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把触摸事件交给TimeRulerView来处理
        if (mTrvTimeRuler != null) {
            return mTrvTimeRuler.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    public RunTimeRulerView getTimeRuler() {
        return mTrvTimeRuler;
    }

    private OnTimeChangeListener mOnTimeChangeListener;

    /**
     * 设置时间变化监听事件
     *
     * @param onTimeChangeListener 监听回调
     */
    public void setOnTimeChangedListener(OnTimeChangeListener onTimeChangeListener) {
        this.mOnTimeChangeListener = onTimeChangeListener;
    }

    public interface OnTimeChangeListener {
        /**
         * 时间发生变化
         *
         * @param newTime 单位，毫秒
         */
        void onTimeChanged(long newTime);
    }
}
