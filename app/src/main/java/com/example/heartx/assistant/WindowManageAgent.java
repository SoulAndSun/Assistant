package com.example.heartx.assistant;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.heartx.assistant.util.adaptive.AdaptiveScreen;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by HeartX on 2018/4/27.
 */

public class WindowManageAgent {

    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowButtonParams;

    private Button mWindowButton;

    private BroadcastReceiver mReceiver;

    private Set<View> mWindowViews = new HashSet<>();
    private int mWidthPixels;
    private int mHeightPixels;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private SensorEventListener mAccelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public WindowManageAgent(Context context) {
        mContext = context;

        mWidthPixels = context.getResources().getDisplayMetrics().widthPixels;
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;

        if (context instanceof Activity) {
            mWindowManager = (WindowManager) ((Activity) context).getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        if (context instanceof Service) {
            mWindowManager = (WindowManager) ((Service) context).getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void init() {

        mWindowButtonParams = new WindowManager.LayoutParams();

        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        //mWindowButtonParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowButtonParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //设置效果为背景透明.
        mWindowButtonParams.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        mWindowButtonParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
//        mWindowButtonParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        //设置窗口初始停靠位置.
        mWindowButtonParams.gravity = Gravity.START | Gravity.TOP;

        mWindowButtonParams.x = 1080;
        mWindowButtonParams.y = 200;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        mWindowButtonParams.width = 150;
        mWindowButtonParams.height = 150;

        mWindowButton = new Button(mContext);
        mWindowButton.setText(R.string.Create);
        mWindowButton.setTextSize(7.0f);
        mWindowButton.setAlpha(0.3f);
        mWindowButton.setFocusableInTouchMode(true);

        addWindowView(mWindowButton, mWindowButtonParams);
        addListener();
    }

    private void addListener() {

        mReceiver = new BroadcastReceiver() {
            private int mInt;

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                Toast.makeText(mContext, action, Toast.LENGTH_SHORT).show();
            }
        };
        mContext.registerReceiver(mReceiver, new IntentFilter(VOLUME_CHANGED_ACTION));

//--------------------------------------------------------------------------------------------------
        mWindowButton.setOnLongClickListener(new View.OnLongClickListener() {
            private int mInt;
            @Override
            public boolean onLongClick(View v) {

                ((Service)mContext).stopSelf();

                return true;
            }
        });

//--------------------------------------------------------------------------------------------------
        new View(mContext).setOnKeyListener(new View.OnKeyListener() {
            private int mInt;
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){

                }
                return false;
            }
        });

        mSensorManager.registerListener(mAccelerometerListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void addWindowView(View view, WindowManager.LayoutParams params) {
        AdaptiveScreen.adaptive(view, params);
        mWindowManager.addView(view, params);
        mWindowViews.add(view);
    }

    private void removeWindowView(View view) {
        mWindowManager.removeView(view);
        mWindowViews.remove(view);

    }

    private void destroy(){

        mContext.unregisterReceiver(mReceiver);

        mSensorManager.unregisterListener(mAccelerometerListener);

    }
}
