package com.aliya.inflater;

import android.app.Application;

/**
 * App
 *
 * @author a_liYa
 * @date 2020/9/23 16:14.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppViewCreator.init(this);
    }
}
