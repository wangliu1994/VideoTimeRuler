package com.example.winnie.viedotimeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author : winnie
 * @date : 2018/10/10
 * @desc
 */
public class CurrentTimeRulerLayout extends LinearLayout {
    CurrentTimeRulerView mTrvTimeRuler;
//    TextView mTvTimeRuler;

    public CurrentTimeRulerLayout(Context context) {
        this(context, null);
    }

    public CurrentTimeRulerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_current_time_ruler, this);
        mTrvTimeRuler = findViewById(R.id.trv_time_ruler);
//        mTvTimeRuler = findViewById(R.id.tv_time_ruler);

        long current = System.currentTimeMillis() / 1000;
        List<CurrentTimeRulerView.TimePart> list = new ArrayList<>();
        for (int i = -10; i < 10; i++) {
            CurrentTimeRulerView.TimePart part = new CurrentTimeRulerView.TimePart();
            part.setStartTime(current,i * 1000);
            part.setEndTime(new Random().nextInt(1000));
            list.add(part);
        }
        mTrvTimeRuler.setTimePartList(list);
        mTrvTimeRuler.setOnTimeChangedListener(new CurrentTimeRulerView.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {
                if(mOnTimeChangeListener != null){
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
        if(mTrvTimeRuler != null){
            return mTrvTimeRuler.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    public CurrentTimeRulerView getTimeRuler() {
        return mTrvTimeRuler;
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
         * @param newTime 单位，毫秒
         */
        void onTimeChanged(long newTime);
    }
}
