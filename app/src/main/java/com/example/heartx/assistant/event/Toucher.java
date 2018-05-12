package com.example.heartx.assistant.event;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.math.MathUtils;
import android.view.KeyEvent;
import android.view.View;

import com.example.heartx.assistant.ToucherService;

/**
 * 基于传感器的事件模拟
 * Created by HeartX on 2018/5/5.
 */

public abstract class Toucher implements SensorEventListener, View.OnKeyListener {

    MouseSprite mMouseSprite;
    boolean isStartTime = true;
    long mTimeMillis = System.currentTimeMillis() * 2;
    long timeEnd = 5000;

    private Toucher() {
    }

    Toucher(MouseSprite mouseSprite) {
        mMouseSprite = mouseSprite;

        changeMouseState(mouseSprite);
    }

    public abstract void changeMouseState(MouseSprite mouseSprite);

    public SensorEventListener getSensorListener() {
        return this;
    }

    public View.OnKeyListener getKeyListener() {
        return this;
    }

    void change(int type) {
        ToucherManager.getInstance().changeMode(type);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        checkToucherChange();
        move(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    void checkToucherChange() {

        if (mMouseSprite.getRawX() == ToucherService.screenWidth && mMouseSprite.getRawY() == 0) {

            if (isStartTime) {
                mTimeMillis = System.currentTimeMillis();
                isStartTime = false;
            }

            if ((System.currentTimeMillis() - mTimeMillis) > timeEnd) {
                mMouseSprite.toast("成功跳转到选择器！");
                change(ToucherManager.SELECTOR_TOUCHER_ID);
            }
            return;
        }

        isStartTime = true;
    }

    private void move(SensorEvent event) {
        mMouseSprite.getParams().x += event.values[0] * -1;

        if ((event.values[1] - 6) > 0) {
            mMouseSprite.getParams().y += (event.values[1] - 6) * 1.5;
        } else {
            mMouseSprite.getParams().y += event.values[1] - 6;
        }

        mMouseSprite.getParams().x = MathUtils.clamp(mMouseSprite.getRawX(), 0, ToucherService.screenWidth);
        mMouseSprite.getParams().y = MathUtils.clamp(mMouseSprite.getRawY(), 0, ToucherService.screenHeight);

        if (mMouseSprite.getWindowManager() != null) {
            mMouseSprite.getWindowManager().updateViewLayout(mMouseSprite.getMouse(), mMouseSprite.getParams());
        }
    }
}
