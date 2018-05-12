package com.example.heartx.assistant;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.heartx.assistant.event.Toucher;
import com.example.heartx.assistant.event.ToucherManager;
import com.example.heartx.assistant.event.MouseSprite;
import com.orhanobut.logger.Logger;

import io.reactivex.functions.Consumer;

/**
 * 悬浮窗的服务
 * Created by HeartX on 2018/2/12.
 */

public class ToucherService extends Service {

    public static int screenWidth;
    public static int screenHeight;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mListener;

    private boolean isRegistered = false;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                isRegistered = mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                Logger.d("传感器注册成功");
            }
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mSensorManager.unregisterListener(mListener);
                Logger.d("传感器注销成功");
            }
        }

        @Override
        public String toString() {
            return super.toString();
        }
    };

    private Consumer<Integer> mConsumer = new Consumer<Integer>() {
        @Override
        public void accept(Integer type) throws Exception {
            setMode(type);
        }
    };

    private MouseSprite mMouseSprite;

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        mMouseSprite = new MouseSprite(this);

        mSensorManager = (SensorManager) getApplication().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ToucherManager.getInstance().init(mConsumer);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    public void setMode(int type) {
        if (isRegistered) {
            mSensorManager.unregisterListener(mListener);
        }
        Toucher toucher = ToucherManager.getInstance().getToucher(type, mMouseSprite);
        setMode(toucher.getSensorListener(), toucher.getKeyListener());
    }

    private void setMode(SensorEventListener sL, View.OnKeyListener kL) {
        isRegistered = mSensorManager.registerListener(mListener = sL, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mMouseSprite.getMouse().setOnKeyListener(kL);
    }

    @Override
    public void onDestroy() {

        mMouseSprite.destroy();

        if (isRegistered && mSensorManager != null) {
            mSensorManager.unregisterListener(mListener);
            mSensorManager = null;
            mSensor = null;
            mListener = null;
        }

        unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }


    private IBinder mIBinder = new MyBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }
    class MyBinder extends Binder {

        ToucherService getService() {
            return ToucherService.this;
        }
    }
}