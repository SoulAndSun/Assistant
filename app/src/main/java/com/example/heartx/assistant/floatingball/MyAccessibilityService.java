package com.example.heartx.assistant.floatingball;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MyAccessibilityService extends AccessibilityService {

    public static final int BACK = 1;
    public static final int HOME = 2;
    private static final String TAG = "ICE";

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 点击了");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Subscribe
    public void onReceive(Integer action){
        switch (action){
            case BACK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                }
                break;
            case HOME:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                }
                break;
        }
    }
}