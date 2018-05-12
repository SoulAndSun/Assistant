package com.example.heartx.assistant.event;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
    public int offsetY;

    private SoundPool soundPool;

    public MouseSprite(Context context) {
        mContext = context;
        mHandler = new MyHandle(this);

        if (context instanceof Activity) {
            mWindowManager = (WindowManager) ((Activity) context).getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        if (context instanceof Service) {
            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        generateMouse();

        offsetX = mSensorMouseParams.width / 2;
        offsetY = mSensorMouseParams.height / 2;

    }

    public void generateMouse() {

        if (mSensorMouseParams == null) {
            mSensorMouseParams = generateParam(100, 100);
        }

        if (mSensorMouse == null) {
            mSensorMouse = new Button(mContext);
            mSensorMouse.setAlpha(0.5f);
        }

        if (!mWindowViews.contains(mSensorMouse)) {
            addWindowView(mSensorMouse, mSensorMouseParams);
        }
    }

    /**
     * 限制触屏和获取焦点
     * @param type
     * @return
     */
    public WindowManager.LayoutParams generateParam(int type) {

        return generateParam(
                type,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);
    }

    /**
     * 唯一限制：不能触屏
     * @param width
     * @param height
     * @return
     */
    public WindowManager.LayoutParams generateParam(int width, int height) {

        return generateParam(width, height, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public WindowManager.LayoutParams generateParam(int width, int height, int flags) {

        return generateParam(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, width, height, flags, PixelFormat.RGBA_8888);
    }

    public WindowManager.LayoutParams generateParam(int type, int width, int height, int flags, int format) {

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        //系统报错提示窗口
        //params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        //params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.type = type;

        // 设置背景透明度
        //params.format = PixelFormat.TRANSPARENT;
        //params.format = PixelFormat.RGBA_8888;
        params.format = format;
        //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//允许窗口在所有装饰之上
        //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;// 不拦截窗口之外的触摸事件。默认是拦截的
        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//不获取焦点，会使按键监听失效，同时启动FLAG_NOT_TOUCH_MODAL
        //WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;//具有焦点能力并且不会影响输入法的使用
        //WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;//额外接收窗口外触摸事件MotionEvent.ACTION_OUTSIDE
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;//不接收触摸事件
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | flags;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.START | Gravity.TOP;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = width;
        params.height = height;

        return params;
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
     *
     * @param flag
     */
    void changeMouseState(int flag, float alpha) {
        mSensorMouse.setAlpha(alpha);
        mSensorMouseParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | flag;
        mWindowManager.updateViewLayout(mSensorMouse, mSensorMouseParams);
    }

    void sendMessage(int what, long delay) {
        if (mHandler.isHandled) {
            mHandler.isHandled = false;
            mHandler.sendEmptyMessageDelayed(what, delay);
        }
    }

    public void addWindowView(View view, WindowManager.LayoutParams params) {
        if (mWindowManager != null && !mWindowViews.contains(view)) {
            AdaptiveScreen.adaptive(view, params);
            mWindowManager.addView(view, params);
            mWindowViews.add(view);
        }
    }

    public void removeWindowView(View view) {
        if (mWindowManager != null && mWindowViews.contains(view)) {
            mWindowManager.removeView(view);
            mWindowViews.remove(view);
        }
    }

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public WindowManager.LayoutParams getParams() {
        return mSensorMouseParams;
    }

    public Context getContext() {
        return mContext;
    }

    public Button getMouse() {
        return mSensorMouse;
    }

    public Set<View> getWindowViews() {
        return mWindowViews;
    }

    public int getX() {
        if (mSensorMouseParams.x >= ToucherService.screenWidth) {
            return mSensorMouseParams.x - offsetX;
        }
        return mSensorMouseParams.x + offsetX;
    }

    public int getY() {

        if (mSensorMouseParams.y >= ToucherService.screenHeight) {
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
                removeWindowView(v);
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
                mouseSprite.changeMouseState(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
                isHandled = true;
                Log.d("TAG", "恢复监听");
            }
        }
    }

    public static class Mouse extends View {

        private Paint mPaint;

        public Mouse(Context context) {
            this(context, null);
        }

        public Mouse(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public Mouse(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            mPaint = new Paint();
            mPaint.setColor(Color.BLUE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.parseColor("#70b9b7b5"));

            canvas.drawCircle(200, 200, 50, mPaint);
        }
    }
}