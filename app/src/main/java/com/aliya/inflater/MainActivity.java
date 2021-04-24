package com.aliya.inflater;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), AppViewCreator.sInflaterFactory);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View view) {
        LayoutInflater layoutInflater = getLayoutInflater();
        for (int i = 0; i < 10000; i++) {
            View inflate = layoutInflater
                    .inflate(R.layout.layout_single_view, (ViewGroup) view.getParent(), false);
        }
    }
}