package com.example.heartx.assistant.event;

import android.hardware.SensorEvent;
import android.view.WindowManager;

import com.example.heartx.assistant.ToucherService;

/**
 * 感应鼠标的位置和行为的事件模拟类
 * Created by HeartX on 2018/5/11.
 */

public class InduceToucher extends Toucher{


    private boolean isToBorder;
    private long mT;

    InduceToucher(MouseSprite mouseSprite) {
        super(mouseSprite);
    }

    @Override
    public void changeMouseState(MouseSprite mouseSprite) {
        mouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event);

        if (checkBorder(mMouseSprite.getRawY(), 0)) return;

        if (checkBorder(mMouseSprite.getRawX(), ToucherService.screenWidth)) return;

        if (checkBorder(mMouseSprite.getRawY(), ToucherService.screenHeight)) return;

        if (checkBorder(mMouseSprite.getRawX(), 0)) return;

        isToBorder = true;
    }

    private boolean checkBorder(int pos, int targetPos) {

        if (pos == targetPos) {

            if (isToBorder) {
                mT = System.currentTimeMillis();
                isToBorder = false;
            }

            if ((System.currentTimeMillis() - mT) > 1000) {
                mMouseSprite.toast(targetPos +" : 1000 Millis");
                mT = System.currentTimeMillis() * 2;
            }
            return true;
        }
        return false;
    }
}
