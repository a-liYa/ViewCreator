package com.aliya.inflater;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.aliya.view.ViewCreatorImpl;

import androidx.core.view.LayoutInflaterCompat;

/**
 * AppViewCreator
 *
 * @author a_liYa
 * @date 2021/4/24 16:33.
 */
public class AppViewCreator {

    private static boolean sInitialized;
    public static LayoutInflater.Factory2 sInflaterFactory;

    public static void init(Context context) {
        if (!sInitialized) {
            sInitialized = true;
            sInflaterFactory = new LayoutInflaterFactory();

            Application application = (Application) context.getApplicationContext();

            LayoutInflater.from(application).setFactory2(sInflaterFactory);
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    LayoutInflater layoutInflater = activity.getLayoutInflater();
                    if (layoutInflater.getFactory() == null) {
                        LayoutInflaterCompat.setFactory2(layoutInflater, sInflaterFactory);
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }
            });
        }
    }


    static class LayoutInflaterFactory implements LayoutInflater.Factory2 {

        ViewCreatorImpl mViewCreator = new ViewCreatorImpl();

        static long sTotalTime; // 单位 ns
        static long sCreateCount;
        static boolean sUseReflect = true;
        String msg_format = (sUseReflect ? "反射":"new ") + "创建View：%d 次，耗时：%d ms";

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return onCreateView(name, context, attrs);
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View view;
            long startNs = System.nanoTime();
            if (sUseReflect) {
                view = ViewInflater.createViewFromTag(context, name, attrs);
            } else {
                view = mViewCreator.createView(name, context, attrs);
            }
            sTotalTime += (System.nanoTime() - startNs);
            Log.e("TAG", String.format(msg_format, ++sCreateCount, sTotalTime / 1000000));
            return view;
        }
    }
}
