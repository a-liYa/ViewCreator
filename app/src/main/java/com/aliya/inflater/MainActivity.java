package com.aliya.inflater;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliya.view.ViewCreatorImpl;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ViewCreatorImpl mViewCreator;
    private ViewInflater mViewInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mViewCreator = new ViewCreatorImpl();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    long reflectTakeTime;
    long newTakeTime;
    int runCount;

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        if (view == null) {
            long uptimeMillis = SystemClock.uptimeMillis();
            view = mViewCreator.createView(name, context, attrs);
            newTakeTime += (SystemClock.uptimeMillis() - uptimeMillis);

//            uptimeMillis = SystemClock.uptimeMillis();
//            view = ViewInflater.createViewFromTag(context, name, attrs);
//            reflectTakeTime += (SystemClock.uptimeMillis() - uptimeMillis);

            runCount++;
        }
        return view;
    }

    public void onClick(View view) {
        LayoutInflater layoutInflater = getLayoutInflater();
        for (int i = 0; i < 10000; i++) {
            View inflate = layoutInflater
                    .inflate(R.layout.layout_single_view, (ViewGroup) view.getParent(), false);
        }
        Log.e("TAG", "创建次数: " + runCount + ", 相差时长：" + (reflectTakeTime - newTakeTime));
        Log.e("TAG", "反射耗时: " + reflectTakeTime);
        Log.e("TAG", "New耗时: " + newTakeTime);

    }
}