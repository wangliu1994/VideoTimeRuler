package com.example.winnie.viedotimeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * @author winnie
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimeRulerLayout rulerLayout = findViewById(R.id.time_ruler_layout);
        rulerLayout.setOnTimeChangedListener(new TimeRulerLayout.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {

            }
        });

        CurrentTimeRulerLayout currentTimeRulerLayout = findViewById(R.id.current_time_ruler_layout);
       currentTimeRulerLayout.setOnTimeChangedListener(new CurrentTimeRulerLayout.OnTimeChangeListener() {
           @Override
           public void onTimeChanged(long newTime) {

           }
       });
    }
}
