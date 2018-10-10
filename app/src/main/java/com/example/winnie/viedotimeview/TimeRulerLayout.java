package com.example.winnie.viedotimeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author : winnie
 * @date : 2018/10/9
 * @desc
 */
public class TimeRulerLayout extends LinearLayout {
    TimeRulerView mTrvTimeRuler;
    TextView mTvTimeRuler;

    public TimeRulerLayout(Context context) {
        this(context, null);
    }

    public TimeRulerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_time_ruler, this);
        mTrvTimeRuler = findViewById(R.id.trv_time_ruler);
        mTvTimeRuler = findViewById(R.id.tv_time_ruler);

        List<TimeRulerView.TimePart> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TimeRulerView.TimePart part = new TimeRulerView.TimePart();
            part.startTime = i * 1000;
            part.endTime = part.startTime + new Random().nextInt(1000);
            list.add(part);
        }
        mTrvTimeRuler.setTimePartList(list);
        mTrvTimeRuler.setOnTimeChangedListener(new TimeRulerView.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {
                mTvTimeRuler.setText(TimeUtil.formatTimeHHmmss(newTime));
                if(mOnTimeChangeListener != null){
                    mOnTimeChangeListener.onTimeChanged(newTime);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把触摸事件交给TimeRulerView来处理
        if(mTrvTimeRuler != null){
            return mTrvTimeRuler.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private OnTimeChangeListener mOnTimeChangeListener;
    /**
     * 设置时间变化监听事件
     * @param onTimeChangeListener 监听回调
     */
    public void setOnTimeChangedListener(OnTimeChangeListener onTimeChangeListener) {
        this.mOnTimeChangeListener = onTimeChangeListener;
    }

    public interface OnTimeChangeListener{
        /**
         * 时间发生变化
         * @param newTime
         */
        void onTimeChanged(long newTime);
    }
}
