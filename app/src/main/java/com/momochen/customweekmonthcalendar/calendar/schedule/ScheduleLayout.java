package com.momochen.customweekmonthcalendar.calendar.schedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.momochen.customweekmonthcalendar.R;
import com.momochen.customweekmonthcalendar.calendar.CalendarUtils;
import com.momochen.customweekmonthcalendar.calendar.OnCalendarClickListener;
import com.momochen.customweekmonthcalendar.calendar.month.MonthCalendarView;
import com.momochen.customweekmonthcalendar.calendar.month.MonthView;
import com.momochen.customweekmonthcalendar.calendar.week.WeekCalendarView;
import com.momochen.customweekmonthcalendar.calendar.week.WeekView;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

/**
 * Created by momochen on 2017/7/4.
 */
public class ScheduleLayout extends FrameLayout {

    private final int DEFAULT_MONTH = 0;
    private final int DEFAULT_WEEK = 1;

    private MonthCalendarView mcvCalendar;//月视图
    private WeekCalendarView wcvCalendar;//周视图
    private RelativeLayout rlMonthCalendar;//月视图  外层布局
    private RelativeLayout rlScheduleList;
    private ScheduleRecyclerView rvScheduleList;//底部列表视图

    private int mCurrentSelectYear;//当前选中的  年
    private int mCurrentSelectMonth;//当前选中的  月
    private int mCurrentSelectDay;//当前选中的  日
    private int mRowSize;//日历行高
    private int mMinDistance;
    private int mAutoScrollDistance;
    private int mDefaultView;//首次默认视图
    private float mDownPosition[] = new float[2];
    private boolean mIsScrolling = false;//当前是否在滚动
    private boolean mIsAutoChangeMonthRow;//日历布局有时6行、有时5行  是否自动根据行数改变布局高度
    private boolean mCurrentRowsIsSix = true;//当前是否是有6行  mIsAutoChangeMonthRow为true  功能才有效果个

    private ScheduleState mState;
    private OnCalendarClickListener mOnCalendarClickListener;
    private GestureDetector mGestureDetector;

    public ScheduleLayout(Context context) {
        this(context, null);
    }

    public ScheduleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.ScheduleLayout));
        initDate();
        initGestureDetector();
    }

    private void initAttrs(TypedArray array) {
        //首次默认视图  默认值月视图
        mDefaultView = array.getInt(R.styleable.ScheduleLayout_default_view, DEFAULT_MONTH);
        //是否自动根据行数改变布局高度  默认false
        mIsAutoChangeMonthRow = array.getBoolean(R.styleable.ScheduleLayout_auto_change_month_row, false);
        array.recycle();
//        mState = ScheduleState.OPEN;
        mRowSize = getResources().getDimensionPixelSize(R.dimen.week_calendar_height);
        mMinDistance = getResources().getDimensionPixelSize(R.dimen.calendar_min_distance);
        mAutoScrollDistance = getResources().getDimensionPixelSize(R.dimen.auto_scroll_distance);
    }

    /**
     * 初始化手势
     */
    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new OnScheduleScrollListener(this));
    }

    /**
     * 初始化数据  第一次初始化
     */
    private void initDate() {
        Calendar calendar = Calendar.getInstance();
        resetCurrentSelectDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mcvCalendar = findViewById(R.id.mcvCalendar);
        wcvCalendar = findViewById(R.id.wcvCalendar);
        rlMonthCalendar = findViewById(R.id.rlMonthCalendar);
        rlScheduleList = findViewById(R.id.rlScheduleList);
        rvScheduleList = findViewById(R.id.rvScheduleList);
        bindingMonthAndWeekCalendar();
    }

    /**
     * 初始化 周月视图
     */
    private void bindingMonthAndWeekCalendar() {
        mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
        wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        // 初始化视图
        Calendar calendar = Calendar.getInstance();
        if (mIsAutoChangeMonthRow) {
            mCurrentRowsIsSix = CalendarUtils.getMonthRows(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) == 6;
        }
        //默认月视图  初始化
        if (mDefaultView == DEFAULT_MONTH) {
            //隐藏周视图
            wcvCalendar.setVisibility(INVISIBLE);
            //状态  开启
            mState = ScheduleState.OPEN;
            if (!mCurrentRowsIsSix) {
                rlScheduleList.setY(rlScheduleList.getY() - mRowSize);
            }
            //默认周视图  初始化
        } else if (mDefaultView == DEFAULT_WEEK) {
            //隐藏月视图
            wcvCalendar.setVisibility(VISIBLE);
            //状态  关闭
            mState = ScheduleState.CLOSE;
            //获取当前日期所在的周  在第几行
            int row = CalendarUtils.getWeekRow(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            //月视图定位到当前日期所在的行
            rlMonthCalendar.setY(-row * mRowSize);
            //滚动布局  向上滚动（其实就是滚动到顶部  只显示一行）
            rlScheduleList.setY(rlScheduleList.getY() - 5 * mRowSize);
        }
    }

    /**
     * 设置当前选中的日期
     */
    private void resetCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
    }

    private OnCalendarClickListener mMonthCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            wcvCalendar.setOnCalendarClickListener(null);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, day);
            resetCurrentSelectDate(year, month, day);
            int position = wcvCalendar.getCurrentItem() + weeks;
            if (weeks != 0) {
                wcvCalendar.setCurrentItem(position, false);
            }
            resetWeekView(position);
            wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            computeCurrentRowsIsSix(year, month);
        }
    };

    private void computeCurrentRowsIsSix(int year, int month) {
        if (mIsAutoChangeMonthRow) {
            boolean isSixRow = CalendarUtils.getMonthRows(year, month) == 6;
            if (mCurrentRowsIsSix != isSixRow) {
                mCurrentRowsIsSix = isSixRow;
                if (mState == ScheduleState.OPEN) {
                    if (mCurrentRowsIsSix) {
                        AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, mRowSize);
                        rlScheduleList.startAnimation(animation);
                    } else {
                        AutoMoveAnimation animation = new AutoMoveAnimation(rlScheduleList, -mRowSize);
                        rlScheduleList.startAnimation(animation);
                    }
                }
            }
        }
    }

    private void resetWeekView(int position) {
        WeekView weekView = wcvCalendar.getCurrentWeekView();
        if (weekView != null) {
            weekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            weekView.invalidate();
        } else {
            WeekView newWeekView = wcvCalendar.getWeekAdapter().instanceWeekView(position);
            newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            newWeekView.invalidate();
            wcvCalendar.setCurrentItem(position);
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }

    private OnCalendarClickListener mWeekCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            mcvCalendar.setOnCalendarClickListener(null);
            int months = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
            resetCurrentSelectDate(year, month, day);
            if (months != 0) {
                int position = mcvCalendar.getCurrentItem() + months;
                mcvCalendar.setCurrentItem(position, false);
            }
            resetMonthView();
            mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
            if (mIsAutoChangeMonthRow) {
                mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6;
            }
        }

        @Override
        public void onPageChange(int year, int month, int day) {
            if (mIsAutoChangeMonthRow) {
                if (mCurrentSelectMonth != month) {
                    mCurrentRowsIsSix = CalendarUtils.getMonthRows(year, month) == 6;
                }
            }
        }
    };

    private void resetMonthView() {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        if (monthView != null) {
            monthView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            monthView.invalidate();
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
        resetCalendarPosition();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //设置 底部列表 高度
        resetViewHeight(rlScheduleList, height - mRowSize);
//        resetViewHeight(this, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置  视图高度
     */
    private void resetViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = ev.getRawX();
                mDownPosition[1] = ev.getRawY();
                mGestureDetector.onTouchEvent(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsScrolling) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float distanceX = Math.abs(x - mDownPosition[0]);
                float distanceY = Math.abs(y - mDownPosition[1]);
                if (distanceY > mMinDistance && distanceY > distanceX * 2.0f) {
                    return (y > mDownPosition[1] && isRecyclerViewTouch()) || (y < mDownPosition[1] && mState == ScheduleState.OPEN);
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isRecyclerViewTouch() {
        return mState == ScheduleState.CLOSE && (rvScheduleList.getChildCount() == 0 || rvScheduleList.isScrollTop());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = event.getRawX();
                mDownPosition[1] = event.getRawY();
                resetCalendarPosition();
                return true;
            case MotionEvent.ACTION_MOVE:
                transferEvent(event);
                mIsScrolling = true;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                transferEvent(event);
                changeCalendarState();
                resetScrollingState();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void transferEvent(MotionEvent event) {
        if (mState == ScheduleState.CLOSE) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            mGestureDetector.onTouchEvent(event);
        } else {
            mGestureDetector.onTouchEvent(event);
        }
    }

    /**
     * 切换  日历视图 动画
     */
    private void changeCalendarState() {
        if (rlScheduleList.getY() > mRowSize * 2 &&
                rlScheduleList.getY() < mcvCalendar.getHeight()){// - mRowSize) { // 位于中间
            ScheduleAnimation animation = new ScheduleAnimation(this, mState, mAutoScrollDistance);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    changeState();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else if (rlScheduleList.getY() <= mRowSize * 2) { // 位于顶部
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.OPEN, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.OPEN) {
                        changeState();
                    } else {
                        resetCalendar();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else {
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.CLOSE, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.CLOSE) {
                        mState = ScheduleState.OPEN;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        }
    }

    /**
     * 切换月历，周历开关
     */
    public void toggleState() {
        if (mState == ScheduleState.CLOSE) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
        }

        ScheduleAnimation changeStateAnimation = new ScheduleAnimation(
                this, mState, mAutoScrollDistance);
        changeStateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mState == ScheduleState.CLOSE) {
                    mState = ScheduleState.OPEN;
//                    setmState(ScheduleState.OPEN);
                } else {
                    if (mState == ScheduleState.OPEN) {
                        changeState();
                    } else {
                        resetCalendar();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        rlScheduleList.startAnimation(changeStateAnimation);
        resetScrollingState();
    }


    private void resetCalendarPosition() {
        if (mState == ScheduleState.OPEN) {
            rlMonthCalendar.setY(0);
            if (mCurrentRowsIsSix) {
                rlScheduleList.setY(mcvCalendar.getHeight());
            } else {
                rlScheduleList.setY(mcvCalendar.getHeight() - mRowSize);
            }
        } else {
            rlMonthCalendar.setY(-CalendarUtils.getWeekRow(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay) * mRowSize);
            rlScheduleList.setY(mRowSize);
        }
    }

    private void resetCalendar() {
        if (mState == ScheduleState.OPEN) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
        } else {
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
        }
    }

    /**
     * 切换视图状态
     */
    private void changeState() {
        if (mState == ScheduleState.OPEN) {
            mState = ScheduleState.CLOSE;
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
            rlMonthCalendar.setY((1 - mcvCalendar.getCurrentMonthView().getWeekRow()) * mRowSize);
            checkWeekCalendar();
        } else {
            mState = ScheduleState.OPEN;
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            rlMonthCalendar.setY(0);
        }
    }

    private void checkWeekCalendar() {
        WeekView weekView = wcvCalendar.getCurrentWeekView();
        DateTime start = weekView.getStartDate();
        DateTime end = weekView.getEndDate();
        DateTime current = new DateTime(mCurrentSelectYear, mCurrentSelectMonth + 1, mCurrentSelectDay, 23, 59, 59);
        int week = 0;
        while (current.getMillis() < start.getMillis()) {
            week--;
            start = start.plusDays(-7);
        }
        current = new DateTime(mCurrentSelectYear, mCurrentSelectMonth + 1, mCurrentSelectDay, 0, 0, 0);
        if (week == 0) {
            while (current.getMillis() > end.getMillis()) {
                week++;
                end = end.plusDays(7);
            }
        }
        if (week != 0) {
            int position = wcvCalendar.getCurrentItem() + week;
            if (wcvCalendar.getWeekViews().get(position) != null) {
                wcvCalendar.getWeekViews().get(position).setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
                wcvCalendar.getWeekViews().get(position).invalidate();
            } else {
                WeekView newWeekView = wcvCalendar.getWeekAdapter().instanceWeekView(position);
                newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
                newWeekView.invalidate();
            }
            wcvCalendar.setCurrentItem(position, false);
        }
    }

    private void resetScrollingState() {
        mDownPosition[0] = 0;
        mDownPosition[1] = 0;
        mIsScrolling = false;
    }

    protected void onCalendarScroll(float distanceY) {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        distanceY = Math.min(distanceY, mAutoScrollDistance);
        float calendarDistanceY = distanceY / (mCurrentRowsIsSix ? 5.0f : 4.0f);
        int row = monthView.getWeekRow() - 1;
        int calendarTop = -row * mRowSize;
        int scheduleTop = mRowSize;
        float calendarY = rlMonthCalendar.getY() - calendarDistanceY * row;
        calendarY = Math.min(calendarY, 0);
        calendarY = Math.max(calendarY, calendarTop);
        rlMonthCalendar.setY(calendarY);
        float scheduleY = rlScheduleList.getY() - distanceY;
        if (mCurrentRowsIsSix) {
            scheduleY = Math.min(scheduleY, mcvCalendar.getHeight());
        } else {
            scheduleY = Math.min(scheduleY, mcvCalendar.getHeight() - mRowSize);
        }
        scheduleY = Math.max(scheduleY, scheduleTop);
        rlScheduleList.setY(scheduleY);
    }

    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    private void resetMonthViewDate(final int year, final int month, final int day, final int position) {
        if (mcvCalendar.getMonthViews().get(position) == null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetMonthViewDate(year, month, day, position);
                }
            }, 50);
        } else {
            mcvCalendar.getMonthViews().get(position).clickThisMonth(year, month, day);
        }
    }

    /**
     * 初始化年月日
     *
     * @param year
     * @param month (0-11)
     * @param day   (1-31)
     */
    public void initData(int year, int month, int day) {
        int monthDis = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
        int position = mcvCalendar.getCurrentItem() + monthDis;
        mcvCalendar.setCurrentItem(position);
        resetMonthViewDate(year, month, day, position);
    }

    /**
     * 添加多个圆点提示
     *
     * @param hints
     */
    public void addTaskHints(List<Integer> hints) {
        CalendarUtils.getInstance(getContext()).addTaskHints(mCurrentSelectYear, mCurrentSelectMonth, hints);
        if (mcvCalendar.getCurrentMonthView() != null) {
            mcvCalendar.getCurrentMonthView().invalidate();
        }
        if (wcvCalendar.getCurrentWeekView() != null) {
            wcvCalendar.getCurrentWeekView().invalidate();
        }
    }

    /**
     * 删除多个圆点提示
     *
     * @param hints
     */
    public void removeTaskHints(List<Integer> hints) {
        CalendarUtils.getInstance(getContext()).removeTaskHints(mCurrentSelectYear, mCurrentSelectMonth, hints);
        if (mcvCalendar.getCurrentMonthView() != null) {
            mcvCalendar.getCurrentMonthView().invalidate();
        }
        if (wcvCalendar.getCurrentWeekView() != null) {
            wcvCalendar.getCurrentWeekView().invalidate();
        }
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    public void addTaskHint(Integer day) {
        if (mcvCalendar.getCurrentMonthView() != null) {
            if (mcvCalendar.getCurrentMonthView().addTaskHint(day)) {
                if (wcvCalendar.getCurrentWeekView() != null) {
                    wcvCalendar.getCurrentWeekView().invalidate();
                }
            }
        }
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    public void removeTaskHint(Integer day) {
        if (mcvCalendar.getCurrentMonthView() != null) {
            if (mcvCalendar.getCurrentMonthView().removeTaskHint(day)) {
                if (wcvCalendar.getCurrentWeekView() != null) {
                    wcvCalendar.getCurrentWeekView().invalidate();
                }
            }
        }
    }

    public ScheduleRecyclerView getSchedulerRecyclerView() {
        return rvScheduleList;
    }

    public MonthCalendarView getMonthCalendar() {
        return mcvCalendar;
    }

    public WeekCalendarView getWeekCalendar() {
        return wcvCalendar;
    }

    public int getCurrentSelectYear() {
        return mCurrentSelectYear;
    }

    public int getCurrentSelectMonth() {
        return mCurrentSelectMonth;
    }

    public int getCurrentSelectDay() {
        return mCurrentSelectDay;
    }

}
