package com.momochen.customweekmonthcalendar.calendar.month;

/**
 * Created by momochen on 2017/7/4.
 */
public interface OnMonthClickListener {
    void onClickThisMonth(int year, int month, int day);
    void onClickLastMonth(int year, int month, int day);
    void onClickNextMonth(int year, int month, int day);
}
