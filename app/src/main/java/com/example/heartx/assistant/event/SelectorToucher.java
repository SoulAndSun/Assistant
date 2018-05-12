package com.example.heartx.assistant.event;

import android.view.WindowManager;

import com.example.heartx.assistant.ToucherService;

/**
 * 选择器，用于选择不同的模拟事件的模式
 * Created by HeartX on 2018/5/6.
 */
class SelectorToucher extends Toucher {

    private final int NULL = -1;//NULL表示没设定toucher模式
//    private int leftTop = ToucherManager.STANDARD_TOUCHER_ID;//左上角的选项，表示设定toucher模式
//    private int rightTop = NULL;//右上角的选项，表示设定toucher模式
//    private int rightBottom = NULL;//右下角的选项，表示设定toucher模式
//    private int leftBottom = NULL;//左下角的选项，表示设定toucher模式

    private Location leftTop;
    private Location rightTop;
    private Location leftBottom;
    private Location rightBottom;
    private final MouseSprite.Mouse mMouse;

    SelectorToucher(MouseSprite mouseSprite) {
        super(mouseSprite);

        leftTop = new Location(ToucherManager.STANDARD_TOUCHER_ID, 0, 0);
        rightTop = new Location(ToucherManager.INDUCE_TOUCHER_ID, ToucherService.screenWidth, 0);
        leftBottom = new Location(NULL, 0, ToucherService.screenHeight);
        rightBottom = new Location(NULL, ToucherService.screenWidth, ToucherService.screenHeight);

        WindowManager.LayoutParams params = mouseSprite.generateParam(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mMouse = new MouseSprite.Mouse(mouseSprite.getContext());
        mouseSprite.addWindowView(mMouse, params);
    }

    @Override
    public void changeMouseState(MouseSprite mouseSprite) {
        mouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
    }

    @Override
    void checkToucherChange() {
        if (checkOn(leftTop)) return;//检测左上手势

        if (checkOn(rightTop)) return;//检测右上手势

        if (checkOn(leftBottom)) return;//检测左下手势

        if (checkOn(rightBottom)) return;//检测右下手势

        isStartTime = true;
    }

    /**
     * {@link #checkOn(int, int, int)}
     */
    private boolean checkOn(Location target) {
        return checkOn(target.ID, target.x, target.y);
    }

    /**
     * 鼠标在指定的posX和posY的位置上停留{@link #timeEnd}毫秒后，将转换到指定模式
     *
     * @param posX     指定的x
     * @param posY     指定的y
     * @param targetID
     * @return 如果检查的位置是正确的，返回true打断其他位置的检查，否则返回false
     */
    private boolean checkOn(int targetID, int posX, int posY) {
        if (mMouseSprite.getRawX() == posX && mMouseSprite.getRawY() == posY) {

            if (isStartTime) {
                mTimeMillis = System.currentTimeMillis();
                isStartTime = false;
            }

            if ((System.currentTimeMillis() - mTimeMillis) > timeEnd) {

                if (targetID != NULL) {
                    mMouseSprite.toast("转换成功,ToucherID：" + targetID);
                    mMouseSprite.removeWindowView(mMouse);
                    change(targetID);
                } else {
                    mMouseSprite.toast("转换失败,ToucherID：" + targetID);
                    mTimeMillis = System.currentTimeMillis() * 2;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 用于记录特定位置,
     */
    private class Location {

        int ID, x, y;

        Location(int ID, int x, int y) {
            this.ID = ID;
            this.x = x;
            this.y = y;
        }
    }
}
