package com.example.winnie.viedotimeview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author winnie
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimeRulerView rulerView = findViewById(R.id.time_ruler_view);
        // 模拟时间段数据
        List<TimeRulerView.TimePart> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TimeRulerView.TimePart part = new TimeRulerView.TimePart();
            part.startTime = i * 1000;
            part.endTime = part.startTime + new Random().nextInt(1000);
            list.add(part);
        }
        rulerView.setTimePartList(list);
    }
}
