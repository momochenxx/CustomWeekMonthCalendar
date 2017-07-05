package com.momochen.customweekmonthcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.momochen.customweekmonthcalendar.adapter.MainAdapter;
import com.momochen.customweekmonthcalendar.calendar.OnCalendarClickListener;
import com.momochen.customweekmonthcalendar.calendar.schedule.ScheduleLayout;
import com.momochen.customweekmonthcalendar.calendar.schedule.ScheduleRecyclerView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ScheduleLayout scheduleLayout;

    TextView tvYear;
    TextView tvDay;

    private String[] mMonthText;

    ScheduleRecyclerView recyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMonthText = getResources().getStringArray(R.array.calendar_month);
        recyclerView = (ScheduleRecyclerView) findViewById(R.id.rvScheduleList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new MainAdapter(this));

        scheduleLayout = (ScheduleLayout) findViewById(R.id.slSchedule);

        tvYear = (TextView) findViewById(R.id.tv_year);
        tvDay = (TextView) findViewById(R.id.tv_day);

        findViewById(R.id.btn_extend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleLayout.toggleState();
            }
        });

        scheduleLayout.setOnCalendarClickListener(new OnCalendarClickListener() {
            @Override
            public void onClickDate(int year, int month, int day) {
                resetMainTitleDate(year, month, day);
            }

            @Override
            public void onPageChange(int year, int month, int day) {
                resetMainTitleDate(year, month, day);
            }
        });

        scheduleLayout.post(new Runnable() {
            @Override
            public void run() {
                resetMainTitleDate(scheduleLayout.getCurrentSelectYear(), scheduleLayout.getCurrentSelectMonth(), scheduleLayout.getCurrentSelectDay());
            }
        });
    }

    public void resetMainTitleDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) &&
                day == calendar.get(Calendar.DAY_OF_MONTH)) {
            tvYear.setText(mMonthText[month]);
            tvDay.setText(getString(R.string.calendar_today));
        } else {
            if (year == calendar.get(Calendar.YEAR)) {
                tvYear.setText(mMonthText[month]);
            } else {
                tvYear.setText(String.format("%s%s", String.format(getString(R.string.calendar_year), year),
                        mMonthText[month]));
            }
            tvDay.setText(String.format(getString(R.string.calendar_day), day));
        }
    }
}
