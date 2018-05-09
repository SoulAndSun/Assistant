package com.example.heartx.assistant.event;

import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.heartx.assistant.ToucherService;
import com.example.heartx.assistant.util.ShellCmdUtil;

/**
 * 支持物理按键操作的事件模拟类
 * Created by HeartX on 2018/5/6.
 */

class KeyToucher extends Toucher {

    private long delay = 1000;

    private boolean isFirstDown = true;
    private boolean isBreakUp = false;
    private long mT;

    KeyToucher(MouseSprite mouseSprite) {
        super(mouseSprite);
        mMouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        int action = event.getAction();
        //任意键长按检测
        if (checkKeyLongPress(action)) {
            return true;
        }

        //声量键“-”弹起检测
        if (checkVolumeDownKey(action, keyCode)) {
            return true;
        }

        //声量键“+”弹起检测
        if (checkVolumeUpKey(action, keyCode)) {
            return true;
        }


        //声量键以外的键弹起时，直接模拟该键
        if (action == KeyEvent.ACTION_UP) {
            mMouseSprite.toast("onKey : " + keyCode);

            mMouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            ShellCmdUtil.execShellCmdKeyEvent(keyCode);

            mMouseSprite.sendMessage(0, delay);

            return true;
        }

        return true;
    }

    /**
     * 检测长按的方法
     *
     * @param action
     * @return 如果是长按操作并且长按时间大于等于1秒将返回true拦截其他操作
     */
    private boolean checkKeyLongPress(int action) {

        if (action == KeyEvent.ACTION_DOWN && isFirstDown) {
            isBreakUp = false;
            isFirstDown = false;
            mT = System.currentTimeMillis();
        }

        if (action == KeyEvent.ACTION_DOWN && !isFirstDown && (System.currentTimeMillis() - mT) > 1000) {

            mMouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            isBreakUp = true;
            mT = System.currentTimeMillis() * 2;
            ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.LONG_CLICK, mMouseSprite.getX(), mMouseSprite.getY());
            mMouseSprite.sendMessage(0, delay);
            return true;
        }

        if (action == KeyEvent.ACTION_UP) {
            isFirstDown = true;
        }

        if (action == KeyEvent.ACTION_UP && isBreakUp) {
            isFirstDown = true;
            return true;
        }
        return false;
    }

    private boolean checkVolumeDownKey(int action, int keyCode) {

        if (action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //mMouse.toast("onKey : " + keyCode);

            //左上 + 声量减键 = home键
            if (mMouseSprite.getParams().y <= 0 && mMouseSprite.getParams().x <= 0) {
                ShellCmdUtil.execShellCmdKeyEvent(KeyEvent.KEYCODE_HOME);
                return true;
            }

            //最左 + 声量减键 = 滑出左边内容
            if (mMouseSprite.getParams().x <= 0) {
                ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.SWIPE_TO_LEFT, mMouseSprite.getX(), mMouseSprite.getY());
                return true;
            }

            //最上 + 声量减键 = 屏幕向下滚动
            if (mMouseSprite.getParams().y <= 0) {
                ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.SWIPE_TO_TOP, mMouseSprite.getX(), mMouseSprite.getY());
                return true;
            }

            //最右 + 声量减键 = 滑出右边内容
            if (mMouseSprite.getParams().x >= ToucherService.screenW) {
                ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.SWIPE_TO_RIGHT, mMouseSprite.getX(), mMouseSprite.getY());
                return true;
            }

            //最下 + 声量减键 = 屏幕向上滚动
            if (mMouseSprite.getParams().y >= ToucherService.screenH) {
                ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.SWIPE_TO_BOTTOM, mMouseSprite.getX(), mMouseSprite.getY());
                return true;
            }

            mMouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            ShellCmdUtil.execShellCmdKeyEvent(KeyEvent.KEYCODE_BACK);

            mMouseSprite.sendMessage(0, delay);

            return true;
        }

        return false;
    }

    private boolean checkVolumeUpKey(int action, int keyCode) {

        if (action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            //mMouse.toast("onKey : " + keyCode);

            // 最上 + 声量键加 = 滑出状态栏
            if (mMouseSprite.getParams().y <= 0) {
                ShellCmdUtil.execShellCmdSwipe(ShellCmdUtil.SWIPE_STATUS_BAR, mMouseSprite.getX(), mMouseSprite.getRawY());
                return true;
            }

            mMouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            ShellCmdUtil.execShellCmdTap(mMouseSprite.getX(), mMouseSprite.getY());

            mMouseSprite.sendMessage(0, delay);

            return true;
        }

        return false;
    }
}
