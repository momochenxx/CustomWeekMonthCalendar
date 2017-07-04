package com.momochen.customweekmonthcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.momochen.customweekmonthcalendar.calendar.schedule.ScheduleLayout;

public class MainActivity extends AppCompatActivity {
    ScheduleLayout scheduleLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleLayout = (ScheduleLayout) findViewById(R.id.slSchedule);

        findViewById(R.id.btn_extend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleLayout.changeCalendarState();
            }
        });
    }
}
