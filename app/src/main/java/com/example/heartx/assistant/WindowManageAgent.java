package com.example.heartx.assistant;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heartx.assistant.util.ShellCmdUtil;
import com.example.heartx.assistant.util.adaptive.AdaptiveScreen;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

/**
 * Created by HeartX on 2018/4/27.
 */

class WindowManageAgent {

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mSensorMouseParams;

    private Button mSensorMouse;

    private Set<View> mWindowViews = new HashSet<>();
    private int mWidthPixels;
    private int mHeightPixels;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private SensorEventListener mAccelerometerListener;

    private MyHandle mHandler;

    private boolean isToast = false;

    public TextView mTv, mX, mY, mZ;

    WindowManageAgent(Context context) {
        mContext = context;
        mHandler = new MyHandle(this);

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

        Toast.makeText(mContext, "Create WindowManageAgent", Toast.LENGTH_LONG).show();
    }

    void init() {

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
        addListener();
    }

    private void addListener() {

//--------------------------------------------------------------------------------------------------
        mSensorMouse.setOnClickListener(new View.OnClickListener() {
            private int mInt;

            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, "点击了Button", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext,
                        "WM: " + mWindowManager.toString() +
                                "AS: " + mAccelerometerSensor.toString() +
                                "AL: " + mAccelerometerListener.toString()
                        , Toast.LENGTH_SHORT).show();
                isToast = true;
            }
        });

        mSensorMouse.setOnLongClickListener(new View.OnLongClickListener() {
            private int mInt;

            @Override
            public boolean onLongClick(View v) {

                ((Service) mContext).stopSelf();

                return true;
            }
        });

        mSensorMouse.setOnTouchListener(new View.OnTouchListener() {

            private float offsetX = 0.0f;
            private float offsetY = 0.0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    offsetX = event.getX();
                    offsetY = event.getY();
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mSensorMouseParams.x = (int) (event.getRawX() - offsetX);
                    mSensorMouseParams.y = (int) (event.getRawY() - offsetY);

                    mWindowManager.updateViewLayout(mSensorMouse, mSensorMouseParams);

                    v.cancelLongPress();
                }

                return event.getAction() == MotionEvent.ACTION_UP && event.getEventTime() - event.getDownTime() > 200;
            }
        });

        mSensorMouse.setOnKeyListener(new View.OnKeyListener() {
            private int mInt;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    //Toast.makeText(mContext, "onKey : " + keyCode, Toast.LENGTH_SHORT).show();

                    changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                    ShellCmdUtil.execShellCmdKeyEvent(4);

                    sendMessage(0, 1000L);

                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == 79 || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
                    //Toast.makeText(mContext, "onKey : " + keyCode, Toast.LENGTH_SHORT).show();

                    changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    ShellCmdUtil.execShellCmdTap(mSensorMouse);

                    sendMessage(0, 3000L);

                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode != 79 && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
                    Toast.makeText(mContext, "onKey : " + keyCode, Toast.LENGTH_SHORT).show();

                    changeMouseState(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                    ShellCmdUtil.execShellCmdKeyEvent(keyCode);

                    sendMessage(0, 1000L);

                    return true;
                }

                return true;
            }
        });

//--------------------------------------------------------------------------------------------------
        mSensorManager.registerListener(mAccelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if (mX != null && mY != null && mZ != null) {
                    mX.setText("" + ((x +1) * -1));
                    mY.setText("" + (y -7));
                    mZ.setText("" + z);
                }

                mSensorMouseParams.x += (int) (x + 1) * -1;
                mSensorMouseParams.y += (int) (y - 7);
//                mSensorMouseParams.x += (int) x;
//                mSensorMouseParams.y += (int) y * -1;

                if (mSensorMouseParams.x < 0) {
                    mSensorMouseParams.x = 0;
                }

                if (mSensorMouseParams.x > mWidthPixels) {
                    mSensorMouseParams.x = mWidthPixels;
                }

                if (mSensorMouseParams.y < 0) {
                    mSensorMouseParams.y = 0;
                }

                if (mSensorMouseParams.y > mHeightPixels) {
                    mSensorMouseParams.y = mHeightPixels;
                }

                if (mWindowManager != null) {
                    mWindowManager.updateViewLayout(mSensorMouse, mSensorMouseParams);
                }

                if (isToast) {
                    Toast.makeText(mContext, "" + x, Toast.LENGTH_SHORT).show();
                    isToast = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                if (mTv != null) {
                    mTv.setText("" + accuracy);
                }
                Log.d("TAG", "onAccuracyChanged: ///////////////////////////////////////" + accuracy);
            }

        }, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * {@link #changeMouseState(int, float)}.
     */
    private void changeMouseState(int flag) {
        changeMouseState(flag, 1f);
    }

    /**
     * 转换鼠标监听状态
     * @param flag
     */
    private void changeMouseState(int flag, float alpha) {
        mSensorMouse.setAlpha(alpha);
        mSensorMouseParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | flag;
        mWindowManager.updateViewLayout(mSensorMouse, mSensorMouseParams);
    }

    private void sendMessage(int what, long delay){
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

    void destroy() {

        if (mWindowManager != null) {
            for (View v : mWindowViews) {
                mWindowManager.removeView(v);
            }
            mWindowManager = null;
            mWindowViews = null;
        }

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccelerometerListener);
            mSensorManager = null;
            mAccelerometerSensor = null;
            mAccelerometerListener = null;
        }
        mContext = null;

        mHandler = null;

        mSensorMouse = null;
        mSensorMouseParams = null;
    }

    private static class MyHandle extends Handler {

        private boolean isHandled = true;

        private WeakReference<WindowManageAgent> mWeakReference;

        private MyHandle(WindowManageAgent agent) {
            mWeakReference = new WeakReference<WindowManageAgent>(agent);
        }

        @Override
        public void handleMessage(Message msg) {

            WindowManageAgent agent = mWeakReference.get();

            if (agent != null) {
                agent.changeMouseState(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, 0.5f);
                isHandled = true;
                Log.d("TAG", "恢复监听");
            }
        }
    }
}
