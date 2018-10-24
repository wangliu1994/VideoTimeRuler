package com.example.winnie.viedotimeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * - 参考：https://github.com/zjun615/RulerView
 * <p>
 * - 时间缩放，采用缩放手势检测器 ScaleGestureDetector
 * - 缩放的等级估算方式：进入默认比例为1，根据每隔所占的秒数与宽度，可估算出每个等级的宽度范围，再与默认等级对应的宽度相除，即可算出缩放比例
 * - 惯性滑动，使用速度追踪器 VelocityTracker
 * - 缩放与滑动之间的连续操作，ScaleGestureDetector 开始与结束的条件是第二个手指按下与松开，
 * - 所以onTouchEvent()中应该使用 getActionMasked()来监听第二个手指的 DOWN(ACTION_POINTER_DOWN) 与 UP(ACTION_POINTER_UP) 事件，MOVE 都是一样的
 *
 * @author : winnie
 * @date : 2018/10/9
 * @desc 当前时间轴尺子
 */
public class CurrentTimeRulerView extends View {
    private final static String TAG = CurrentTimeRulerView.class.getSimpleName();
    /**
     * 绘制时间为当前时间的前后格数
     */
    private int mMaxRuleCount = 60;

    /**
     * 背景色
     */
    private int bgColor;
    /**
     * 刻度颜色
     */
    private int gradationColor;
    /**
     * 时间块的高度
     */
    private float partHeight;
    /**
     * 时间块的颜色
     */
    private int partColor;
    /**
     * 时间块背景色
     */
    private int partBgColor;
    /**
     * 裁剪时间块的颜色
     */
    private int clipColor;
    /**
     * 裁剪时间指针颜色
     */
    private int clipIndicatorColor;
    /**
     * 裁剪时间指针宽度
     */
    private float clipIndicatorWidth;
    /**
     * 时间快与刻度之间的距离
     */
    private float partGradationGap;
    /**
     * 刻度宽度
     */
    private float gradationWidth;
    /**
     * 秒、分、时刻度的长度
     */
    private float secondLen;
    private float minuteLen;
    private float hourLen;
    /**
     * 刻度数值颜色、大小、与时刻度的距离
     */
    private int gradationTextColor;
    private float gradationTextSize;
    private float gradationTextGap;

    /**
     * 指针颜色
     */
    private int indicatorColor;
    /**
     * 指针的宽度
     */
    private float indicatorWidth;

    /**
     * 最小单位对应的单位秒数值，一共四级: 10s、1min、5min、15min
     * 与 {@link #mPerTextCounts} 和 {@link #mPerCountScaleThresholds} 对应的索引值
     */
    private static int[] mUnitSeconds = {
            10, 10, 10, 10,
            60, 60,
            5 * 60, 5 * 60,
            15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60
    };

    /**
     * 数值显示间隔。一共13级，第一级最大值，不包括
     */
    @SuppressWarnings("all")
    private static int[] mPerTextCounts = {
            60, 60, 2 * 60, 4 * 60, // 10s/unit: 最大值, 1min, 2min, 4min
            5 * 60, 10 * 60, // 1min/unit: 5min, 10min
            20 * 60, 30 * 60, // 5min/unit: 20min, 30min
            3600, 2 * 3600, 3 * 3600, 4 * 3600, 5 * 3600, 6 * 3600 // 15min/unit
    };
    /**
     * 与 {@link #mPerTextCounts} 对应的阈值，在此阈值与前一个阈值之间，则使用此阈值对应的间隔数值
     * 如：1.5f 代表 4*60 对应的阈值，如果 mScale >= 1.5f && mScale < 1.8f，则使用 4*60
     */
    @SuppressWarnings("all")
    private float[] mPerCountScaleThresholds = {
            6f, 3.6f, 1.8f, 1.5f, // 10s/unit: 最大值, 1min, 2min, 4min
            0.8f, 0.4f,   // 1min/unit: 5min, 10min
            0.25f, 0.125f, // 5min/unit: 20min, 30min
            0.07f, 0.04f, 0.03f, 0.025f, 0.02f, 0.015f // 15min/unit: 1h, 2h, 3h, 4h, 5h, 6h
    };
    /**
     * 默认mScale为1
     */
    private float mScale = 1;
    /**
     * 1s对应的间隔，比较好估算
     */
    private final float mOneSecondGap = PxUtil.dp2px(getContext(), 20) / 60f;
    /**
     * 当前最小单位秒数值对应的间隔
     */
    private float mUnitGap = mOneSecondGap * 60;
    /**
     * 默认索引值
     */
    private int mPerTextCountIndex = 4;
    /**
     * 一格代表的秒数。默认1min
     */
    private int mUnitSecond = mUnitSeconds[mPerTextCountIndex];

    /**
     * 数值文字宽度的一半：时间格式为“00:00”，所以长度固定
     */
    private final float mTextHalfWidth;

    private final int SCROLL_SLOP;
    private final int MIN_VELOCITY;
    private final int MAX_VELOCITY;

    /**
     * 当前时间，单位：s
     */
    private long mCurrentTime;
    private long mMaxTime;
    private long mMinTime;

    /**
     * 裁剪开始时间
     */
    private long mClipStartTime;
    /**
     * 裁剪结束时间
     */
    private long mClipEndTime;


    /**
     * 初始化时的基准时间，作为时间原点
     */
    private long mInitialTime;
    /**
     * 距离当前时间移动的值
     */
    private float mMoveDistance;

    private Paint mPaint;
    private TextPaint mTextPaint;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private int mWidth, mHeight, mHalfWidth, mHalfHeight;
    private int mInitialX;
    private int mLastX, mLastY;
    private boolean isMoving;
    private boolean isScaling;

    private List<TimePart> mTimePartList;
    private OnTimeChangeListener mOnTimeChangeListener;

    /**
     * 时间片段
     */
    public static class TimePart {
        /**
         * 时间段开始时间戳，单位：秒
         */
        private long startTime;

        /**
         * 时间段结束时间戳，必须大于{@link #startTime}单位：秒
         */
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        /**
         * @param currentTime     基准时间 单位：秒
         * @param startTimeOffset 基准时间差值  单位：秒
         */
        public void setStartTime(long currentTime, long startTimeOffset) {
            this.startTime = currentTime + startTimeOffset;
        }

        public long getEndTime() {
            return endTime;
        }

        /**
         * @param endTimeOffset 与开始时间的差值 单位：毫秒
         */
        public void setEndTime(long endTimeOffset) {
            this.endTime = startTime + endTimeOffset;
        }
    }

    public interface OnTimeChangeListener {
        /**
         * 时间发生变化
         *
         * @param newTime 单位，毫秒
         */
        void onTimeChanged(long newTime);
    }


    public CurrentTimeRulerView(Context context) {
        this(context, null);
    }

    public CurrentTimeRulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurrentTimeRulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);
        init(context);
        initScaleGesture(context);

        mTextHalfWidth = mTextPaint.measureText("00:00") * 0.5f;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        SCROLL_SLOP = viewConfiguration.getScaledTouchSlop();
        MIN_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();

        calculateDistance();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerView);
        bgColor = ta.getColor(R.styleable.TimeRulerView_bgColor, Color.parseColor("#F4F4F4"));
        gradationColor = ta.getColor(R.styleable.TimeRulerView_gradationColor, Color.parseColor("#333333"));
        partHeight = ta.getDimension(R.styleable.TimeRulerView_partHeight, PxUtil.dp2px(context, 5));
        partColor = ta.getColor(R.styleable.TimeRulerView_partColor, Color.parseColor("#605FBC"));
        partBgColor = ta.getColor(R.styleable.TimeRulerView_partBgColor, Color.parseColor("#C4C4C4"));
        clipColor = ta.getColor(R.styleable.TimeRulerView_clipColor, Color.parseColor("#E28A8A"));
        clipIndicatorColor = ta.getColor(R.styleable.TimeRulerView_clipIndicatorColor, Color.parseColor("#FF5150"));
        clipIndicatorWidth = ta.getDimension(R.styleable.TimeRulerView_clipIndicatorWidth, PxUtil.dp2px(context, 1));
        partGradationGap = ta.getDimension(R.styleable.TimeRulerView_partGradationGap, PxUtil.dp2px(getContext(), 5));
        gradationWidth = ta.getDimension(R.styleable.TimeRulerView_gradationWidth, PxUtil.dp2px(context, 1));
        secondLen = ta.getDimension(R.styleable.TimeRulerView_secondLen, PxUtil.dp2px(context, 3));
        minuteLen = ta.getDimension(R.styleable.TimeRulerView_minuteLen, PxUtil.dp2px(context, 5));
        hourLen = ta.getDimension(R.styleable.TimeRulerView_hourLen, PxUtil.dp2px(context, 8));
        gradationTextColor = ta.getColor(R.styleable.TimeRulerView_gradationTextColor, Color.parseColor("#333333"));
        gradationTextSize = ta.getDimension(R.styleable.TimeRulerView_gradationTextSize, PxUtil.dp2px(context, 10));
        gradationTextGap = ta.getDimension(R.styleable.TimeRulerView_gradationTextGap, PxUtil.dp2px(context, 4));
        indicatorWidth = ta.getDimension(R.styleable.TimeRulerView_indicatorLineWidth, PxUtil.dp2px(context, 1));
        indicatorColor = ta.getColor(R.styleable.TimeRulerView_indicatorLineColor, Color.parseColor("#FAD500"));
        ta.recycle();
    }

    private void initScaleGesture(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            /**
             * 缩放被触发(会调用0次或者多次)，
             * 如果返回 true 则表示当前缩放事件已经被处理，检测器会重新积累缩放因子
             * 返回 false 则会继续积累缩放因子。
             */
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                LogUtil.logD(TAG, "onScale...focusX=%f, focusY=%f, scaleFactor=%f",
                        detector.getFocusX(), detector.getFocusY(), scaleFactor);

                float maxScale = mPerCountScaleThresholds[0];
                float minScale = mPerCountScaleThresholds[mPerCountScaleThresholds.length - 1];
                if (scaleFactor > 1 && mScale >= maxScale) {
                    //已经放到至最大值
                    return true;
                } else if (scaleFactor < 1 && mScale <= minScale) {
                    //已经缩小至最小值
                }

                mScale = mScale * scaleFactor;
                mScale = Math.max(minScale, Math.min(maxScale, mScale));
                mPerTextCountIndex = findScaleIndex(mScale);

                mUnitSecond = mUnitSeconds[mPerTextCountIndex];
                mUnitGap = mScale * mOneSecondGap * mUnitSecond;
                mMaxRuleCount = (int) (mWidth / mUnitGap + 2);
                LogUtil.logD(TAG, "onScale: mScale=%f, mPerTextCountIndex=%d, mUnitSecond=%d, mUnitGap=%f",
                        mScale, mPerTextCountIndex, mUnitSecond, mUnitGap);

                mMoveDistance = (mCurrentTime - mInitialTime) / (float) mUnitSecond * mUnitGap;
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
        });
        // 调整最小跨度值。默认值27mm(>=sw600dp的32mm)，太大了，效果不好
        Class clazz = ScaleGestureDetector.class;
        int newMinSpan = ViewConfiguration.get(context).getScaledTouchSlop();
        try {
            Field mMinSpanField = clazz.getDeclaredField("mMinSpan");
            mMinSpanField.setAccessible(true);
            mMinSpanField.set(mScaleGestureDetector, newMinSpan);
            mMinSpanField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void calculateDistance() {
        mMoveDistance = (mCurrentTime - mInitialTime) / (float) mUnitSecond * mUnitGap;
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(gradationTextSize);
        mTextPaint.setColor(gradationTextColor);

        mScroller = new Scroller(context);
    }

    /**
     * 二分法查找缩放值对应的索引值
     */
    private int findScaleIndex(float scale) {
        final int size = mPerCountScaleThresholds.length;
        int min = 0;
        int max = size - 1;
        int mid = (min + max) >> 1;
        while (!(scale >= mPerCountScaleThresholds[mid] && scale < mPerCountScaleThresholds[mid - 1])) {
            if (scale >= mPerCountScaleThresholds[mid - 1]) {
                // 因为值往小取，index往大取，所以不能为mid -1
                max = mid;
            } else {
                min = mid + 1;
            }
            mid = (min + max) >> 1;
            if (min >= max) {
                break;
            }
            if (mid == 0) {
                break;
            }
        }
        return mid;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 处理wrap_content的高度，设置为60dp
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            mHeight = PxUtil.dp2px(getContext(), 60);
        }

        mHalfHeight = mHeight >> 1;
        mHalfWidth = mWidth >> 1;
        mMaxRuleCount = (int) (mWidth / mUnitGap + 2);

        setMeasuredDimension(mWidth, mHeight);
        Log.i(TAG, "onMeasure");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        final int actionMasked = event.getActionMasked();
        final int action = event.getAction();
        final int pointerCount = event.getPointerCount();
        LogUtil.logD(TAG, "onTouchEvent: isScaling=%b, actionIndex=%d, pointerId=%d, actionMasked=%d, action=%d, pointerCount=%d",
                isScaling, actionIndex, pointerId, actionMasked, action, pointerCount);
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        mScaleGestureDetector.onTouchEvent(event);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                isMoving = false;
                mInitialX = x;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 只要第二手指按下，就禁止滑动
                isScaling = true;
                isMoving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isScaling) {
                    break;
                }
                int dx = x - mLastX;
                if (!isMoving) {
                    final int dy = y - mLastY;
                    //达到惯性滑动的阈值，且水平方向的滑动距离大于数值方向，才可执行惯性滑动
                    if (Math.abs(x - mInitialX) <= SCROLL_SLOP || Math.abs(dx) <= Math.abs(dy)) {
                        break;
                    }
                    isMoving = true;
                }
                mMoveDistance -= dx;
                //根据滑动距离计算时间
                computeTime();
                break;
            case MotionEvent.ACTION_UP:
                if (isScaling || !isMoving) {
                    break;
                }
                mVelocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
                final int xVelocity = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= MIN_VELOCITY) {
                    // 惯性滑动,阈值是向左向右300个刻度
                    final int maxDistance = (int) (mMoveDistance + (mUnitSecond * mMaxRuleCount / (float) mUnitSecond * mUnitGap));
                    final int minDistance = (int) (mMoveDistance + (mUnitSecond * -mMaxRuleCount / (float) mUnitSecond * mUnitGap));
                    mScroller.fling((int) mMoveDistance, 0, -xVelocity, 0, minDistance, maxDistance, 0, 0);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 两个中的有一个手指被抬起，允许滑动。同时把未抬起的手机当前位置赋给初始X
                isScaling = false;
                int restIndex = actionIndex == 0 ? 1 : 0;
                mInitialX = (int) event.getX(restIndex);
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    private void computeTime() {
        mCurrentTime = mInitialTime + (int) (mMoveDistance / mUnitGap * mUnitSecond);
        mMinTime = mCurrentTime + mUnitSecond * -mMaxRuleCount;
        mMaxTime = mCurrentTime + mUnitSecond * mMaxRuleCount;

        if (mOnTimeChangeListener != null) {
            mOnTimeChangeListener.onTimeChanged(mCurrentTime);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 背景
        canvas.drawColor(bgColor);

        // 刻度
        drawRule(canvas);

        // 时间段
        drawTimeParts(canvas);

        //裁剪时间段
        drawClipPart(canvas);

        // 当前时间指针
        drawTimeIndicator(canvas);
        Log.i(TAG, "onDraw");
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mMoveDistance = mScroller.getCurrX();
            computeTime();
            Log.i(TAG, "computeScroll");
        } else {
            isMoving = false;
        }
    }

    /**
     * 绘制刻度
     */
    private void drawRule(Canvas canvas) {
        // 移动画布坐标系
        canvas.save();
        canvas.translate(0, mHeight - partHeight - partGradationGap);
        mPaint.setColor(gradationColor);
        mPaint.setStrokeWidth(gradationWidth);

        //绘制尺子刻度, 从min到max
        final float secondGap = mUnitGap / (float) mUnitSecond;
        final int perTextCount = mPerTextCounts[mPerTextCountIndex];

        //mCurrentTime与其最邻近的左边刻度尺的时间差值
        long dxTime = mCurrentTime % perTextCount;
        long centerTime = mCurrentTime - mInitialTime - dxTime;

        //绘制centerTime左右两边MAX_RULE_COUNT个尺子刻度
        long start = centerTime + mUnitSecond * -mMaxRuleCount;
        float startX = mHalfWidth - mMoveDistance + start * secondGap;

//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (start <= centerTime + mUnitSecond * mMaxRuleCount) {
            long timeTemp = start + mInitialTime;
            // 刻度
            if (timeTemp % 3600 == 0) {
                // 时刻度
                canvas.drawLine(startX, 0, startX, -hourLen, mPaint);
            } else if (timeTemp % 60 == 0) {
                // 分刻度
                canvas.drawLine(startX, 0, startX, -minuteLen, mPaint);
            } else {
                // 秒刻度
                canvas.drawLine(startX, 0, startX, -secondLen, mPaint);
            }

            // 时间数值
            if (timeTemp % perTextCount == 0) {
                String text = TimeUtil.formatTimeHHmm(timeTemp);
                //String text = format.format(timeTemp * 1000);
                canvas.drawText(text, startX - mTextHalfWidth, -hourLen - gradationTextGap - gradationTextSize, mTextPaint);
            }

            start += mUnitSecond;
            startX += mUnitGap;
        }
        canvas.restore();
    }

    /**
     * 绘制当前时间指针
     */
    private void drawTimeIndicator(Canvas canvas) {
        // 指针
        mPaint.setColor(indicatorColor);
        mPaint.setStrokeWidth(indicatorWidth);
        canvas.drawLine(mHalfWidth, 0, mHalfWidth, mHeight - partHeight / 2, mPaint);

        //实心圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(partColor);
        canvas.drawCircle(mHalfWidth, mHeight - partHeight, partHeight, mPaint);
    }

    /**
     * 绘制裁剪时间轴部分
     *
     * @param canvas
     */
    public void drawClipPart(Canvas canvas) {
        //mClipStartTime>0表示开始裁剪了
        //mClipEndTime<=0表示裁剪还没有结束
        if (mClipStartTime > 0 && mClipEndTime <= 0) {
            // 裁剪开始指针
            mPaint.setColor(clipIndicatorColor);
            mPaint.setStrokeWidth(clipIndicatorWidth);
            final float secondGap = mUnitGap / (float) mUnitSecond;
            //只绘制区间内的裁剪时间段
            long startTime = Math.min(Math.max(mClipStartTime, mMinTime), mMaxTime);
            float startX = mHalfWidth - mMoveDistance + (startTime - mInitialTime) * secondGap;
            float endX = mHalfWidth - mMoveDistance + (mCurrentTime - mInitialTime) * secondGap;
            canvas.drawLine(startX, 0, startX, mHeight - partHeight / 2, mPaint);

            mPaint.setColor(clipColor);
            mPaint.setStrokeWidth(partHeight);
            float partY = mHeight - partHeight;
            canvas.drawLine(startX, partY, endX, partY, mPaint);
        }
    }

    /**
     * 绘制时间段
     */
    private void drawTimeParts(Canvas canvas) {
        if (mTimePartList == null) {
            return;
        }
        // 不用矩形，直接使用直线绘制
        mPaint.setStrokeWidth(partHeight);
        mPaint.setColor(partBgColor);
        float partY = mHeight - partHeight;
        canvas.drawLine(0, partY, mWidth, partY, mPaint);

        mPaint.setColor(partColor);
        float startX, endX;
        final float secondGap = mUnitGap / (float) mUnitSecond;
        for (int i = 0, size = mTimePartList.size(); i < size; i++) {
            TimePart timePart = mTimePartList.get(i);
            //只绘制区间内的时间段
            long startTime = Math.min(Math.max(timePart.getStartTime(), mMinTime), mMaxTime);
            long endTime = Math.min(Math.max(timePart.getEndTime(), mMinTime), mMaxTime);
            startX = mHalfWidth - mMoveDistance + (startTime - mInitialTime) * secondGap;
            endX = mHalfWidth - mMoveDistance + (endTime - mInitialTime) * secondGap;
            canvas.drawLine(startX, partY, endX, partY, mPaint);
        }
    }


    /**
     * 设置时间变化监听事件
     *
     * @param onTimeChangeListener 监听回调
     */
    public void setOnTimeChangedListener(OnTimeChangeListener onTimeChangeListener) {
        this.mOnTimeChangeListener = onTimeChangeListener;
    }

    /**
     * 设置时间块（段）集合
     *
     * @param timePartList 时间块集合
     */
    public void setTimePartList(List<TimePart> timePartList) {
        this.mTimePartList = timePartList;
        postInvalidate();
    }

    /**
     * 设置当前时间
     *
     * @param currentTime 当前时间
     */
    public void setCurrentTime(long currentTime) {
        //取currentTime最近的整点时间
        long time = currentTime;
        this.mCurrentTime = time;
        this.mMaxTime = time + mUnitSecond * mMaxRuleCount;
        this.mMinTime = time + mUnitSecond * -mMaxRuleCount;
        this.mInitialTime = time;
        if (mOnTimeChangeListener != null) {
            mOnTimeChangeListener.onTimeChanged(mInitialTime);
        }
        calculateDistance();
        postInvalidate();
        startRun();
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间秒数
     */
    public long getCurrentTime() {
        return mCurrentTime;
    }

    /**
     * 开始裁剪
     */
    public void startClip() {
        this.mClipStartTime = mCurrentTime;
        this.mClipEndTime = 0;
        postInvalidate();
    }

    /**
     * 结束裁剪
     */
    public void stopClip() {
        if (mClipStartTime > mCurrentTime) {
            //处理结束时间小于开始时间的情况
            this.mClipEndTime = mClipStartTime;
            this.mClipStartTime = mCurrentTime;
        } else {
            this.mClipEndTime = mCurrentTime;
        }
        postInvalidate();
    }

    public long getClipStartTime() {
        return mClipStartTime;
    }

    public long getClipEndTime() {
        return mClipEndTime;
    }

    private void startRun() {
        Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                while (true) {
                    if (isMoving || isScaling) {
                        continue;
                    }
                    Thread.sleep(1000);
                    emitter.onNext(1L);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mCurrentTime = mCurrentTime + aLong;
                        calculateDistance();
                        invalidate();
                        if (mOnTimeChangeListener != null) {
                            mOnTimeChangeListener.onTimeChanged(mCurrentTime);
                        }
                    }
                });
    }
}
