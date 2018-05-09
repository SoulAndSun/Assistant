package com.example.heartx.assistant.event;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.heartx.assistant.R;
import com.example.heartx.assistant.ToucherService;
import com.example.heartx.assistant.util.adaptive.AdaptiveScreen;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * 悬浮窗相关类
 * Created by HeartX on 2018/4/27.
 */

public class MouseSprite {

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mSensorMouseParams;
    private Button mSensorMouse;
    private Set<View> mWindowViews = new HashSet<>();
    private MyHandle mHandler;

    public int offsetX;
    public  int offsetY;

    public static final int OFFSET_TO_LEFT = 0;
    public static final int OFFSET_TO_RIGHT = 1;
    
    private SoundPool soundPool;

    public MouseSprite(Context context) {
        mContext = context;
        mHandler = new MyHandle(this);

        if (context instanceof Activity) {
            mWindowManager = (WindowManager) ((Activity) context).getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        if (context instanceof Service) {
            mWindowManager = (WindowManager) ((Service) context).getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        CreateMouse();

        offsetX = mSensorMouseParams.width / 2;
        offsetY = mSensorMouseParams.height / 2;

    }

    private void CreateMouse() {
        mSensorMouseParams = new WindowManager.LayoutParams();

        //系统报错提示窗口
        mSensorMouseParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        //设置效果为背景透明.
        mSensorMouseParams.format = PixelFormat.RGBA_8888;
        //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//允许窗口在所有装饰之上
        //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;// 不拦截窗口之外的触摸事件。默认是拦截的
        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//不获取焦点，会使按键监听失效，同时启动FLAG_NOT_TOUCH_MODAL
        //WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;//额外接收窗口外触摸事件MotionEvent.ACTION_OUTSIDE
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;//不接收触摸事件
        mSensorMouseParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//允许窗口在所有装饰之上
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;//不接收触摸事件

        //设置窗口初始停靠位置.
        mSensorMouseParams.gravity = Gravity.START | Gravity.TOP;

        mSensorMouseParams.x = 1080;
        mSensorMouseParams.y = 700;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        mSensorMouseParams.width = 150;
        mSensorMouseParams.height = 150;

        mSensorMouse = new Button(mContext);
        mSensorMouse.setText(R.string.Create);
        mSensorMouse.setTextSize(7.0f);
        mSensorMouse.setAlpha(0.5f);

        addWindowView(mSensorMouse, mSensorMouseParams);
    }

    //提示音
    public void soundPlay() {

        if (soundPool == null) {
            //1 最多同时放出的声音数，2声音类型，3声音质量越高越耗费资源
            soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
            soundPool.load(mContext, R.raw.fu, 1);//context id 级别
        }

        //第1个参数 ID(放入 soundpool的顺序 第一个放入)
        //2,3 左声道 右声道的控制量
        //4 优先级
        //5 是否循环     0 - 不循环    -1 -  循环
        //6 播放比例     0.5-2 一般为1 表示正常播放
        soundPool.play(1, 1, 1, 1, 0, 1);
    }

    /**
     * {@link #changeMouseState(int, float)}.
     */
    void changeMouseState(int flag) {
        changeMouseState(flag, 1f);
    }

    /**
     * 转换鼠标监听状态
     * @param flag
     */
    void changeMouseState(int flag, float alpha) {
        mSensorMouse.setAlpha(alpha);
        mSensorMouseParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | flag;
        mWindowManager.updateViewLayout(mSensorMouse, mSensorMouseParams);
    }

    void sendMessage(int what, long delay){
        if (mHandler.isHandled) {
            mHandler.isHandled = false;
            mHandler.sendEmptyMessageDelayed(what, delay);
        }
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

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public WindowManager.LayoutParams getParams() {
        return mSensorMouseParams;
    }

    public Button getMouse() {
        return mSensorMouse;
    }

    public int getX() {
        if (mSensorMouseParams.x >= ToucherService.screenW ) {
            return mSensorMouseParams.x - offsetX;
        }
        return mSensorMouseParams.x + offsetX;
    }

    public int getY() {

        if (mSensorMouseParams.y >= ToucherService.screenH) {
            return mSensorMouseParams.y - offsetY;
        }

        return mSensorMouseParams.y + offsetY;
    }

    public int getRawX() {

        return mSensorMouseParams.x;
    }
    public int getRawY() {

        return mSensorMouseParams.y;
    }

    public void toast(String s) {
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
    }

    public void destroy() {

        if (mWindowManager != null) {
            for (View v : mWindowViews) {
                mWindowManager.removeView(v);
            }
            mWindowManager = null;
            mWindowViews = null;
        }
        
        mContext = null;

        mHandler = null;

        mSensorMouse = null;
        mSensorMouseParams = null;
    }

    private static class MyHandle extends Handler {

        private boolean isHandled = true;

        private WeakReference<MouseSprite> mWeakReference;

        private MyHandle(MouseSprite mouseSprite) {
            mWeakReference = new WeakReference<MouseSprite>(mouseSprite);
        }

        @Override
        public void handleMessage(Message msg) {

            MouseSprite mouseSprite = mWeakReference.get();

            if (mouseSprite != null) {
                mouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
                isHandled = true;
                Log.d("TAG", "恢复监听");
            }
        }
    }
}
