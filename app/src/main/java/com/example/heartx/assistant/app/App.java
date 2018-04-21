package com.example.heartx.assistant.app;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;


public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter()/*{
                 @Override
                 public boolean isLoggable(int priority, String tag) {
                     return BuildConfig.DEBUG;
                 }
             }*/);
    }
}
