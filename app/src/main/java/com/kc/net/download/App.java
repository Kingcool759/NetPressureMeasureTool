package com.kc.net.download;

import android.app.Application;
import android.content.Context;

/**
 * created by zhaojianwei03
 * on 2021-11-18
 * at 2:40 PM
 */
public class App extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getContext() {
        return App.context;
    }
}
