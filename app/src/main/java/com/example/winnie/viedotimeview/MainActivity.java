package com.example.winnie.viedotimeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * @author winnie
 */
public class MainActivity extends AppCompatActivity {

    TimeRulerLayout rulerLayout;
    CurrentTimeRulerLayout currentTimeRulerLayout;
    TextView clipTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rulerLayout = findViewById(R.id.time_ruler_layout);
        rulerLayout.setOnTimeChangedListener(new TimeRulerLayout.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {

            }
        });

        currentTimeRulerLayout = findViewById(R.id.current_time_ruler_layout);
        currentTimeRulerLayout.setOnTimeChangedListener(new CurrentTimeRulerLayout.OnTimeChangeListener() {
            @Override
            public void onTimeChanged(long newTime) {

            }
        });

        clipTimeView = findViewById(R.id.clip_time);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTimeRulerLayout.getTimeRuler().startClip();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTimeRulerLayout.getTimeRuler().stopClip();
                String sData1 = "开始时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTimeRulerLayout.getTimeRuler().getClipStartTime());
                String sData2 = " 结束时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTimeRulerLayout.getTimeRuler().getClipEndTime());
                clipTimeView.setText(sData1 + sData2);
            }
        });
    }
}
