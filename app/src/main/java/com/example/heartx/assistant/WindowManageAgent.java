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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.heartx.assistant.util.adaptive.AdaptiveScreen;
import com.orhanobut.logger.Logger;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HeartX on 2018/4/27.
 */

class WindowManageAgent {

    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowButtonParams;

    private Button mWindowButton;

    private Set<View> mWindowViews = new HashSet<>();
    private int mWidthPixels;
    private int mHeightPixels;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private SensorEventListener mAccelerometerListener;

    private Handler mHandler = new Handler() {
        private int mInt;

        @Override
        public void handleMessage(Message msg) {
            mWindowButtonParams.flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);
            Log.d("TAG", "恢复监听");
        }
    };

    private boolean isToast = false;

    WindowManageAgent(Context context) {
        Toast.makeText(mContext, "Create WindowManageAgent", Toast.LENGTH_LONG).show();
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

    void init() {

        mWindowButtonParams = new WindowManager.LayoutParams();

        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        //mWindowButtonParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowButtonParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //设置效果为背景透明.
        mWindowButtonParams.format = PixelFormat.RGBA_8888;

        mWindowButtonParams.flags =
                //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE//不获取焦点，会使按键监听失效，同时启动FLAG_NOT_TOUCH_MODAL
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |// 不拦截窗口之外的触摸事件。默认是拦截的
                        //WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;//额外接收窗口外触摸事件MotionEvent.ACTION_OUTSIDE
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//允许窗口在所有装饰之上
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;//不接收触摸事件

        //设置窗口初始停靠位置.
        mWindowButtonParams.gravity = Gravity.START | Gravity.TOP;

        mWindowButtonParams.x = 1080;
        mWindowButtonParams.y = 700;

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

        addWindowView(mWindowButton, mWindowButtonParams);
        addListener();
    }

    private void addListener() {

//--------------------------------------------------------------------------------------------------
        mWindowButton.setOnClickListener(new View.OnClickListener() {
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

        mWindowButton.setOnLongClickListener(new View.OnLongClickListener() {
            private int mInt;

            @Override
            public boolean onLongClick(View v) {

                ((Service) mContext).stopSelf();

                return true;
            }
        });

        mWindowButton.setOnTouchListener(new View.OnTouchListener() {

            private float offsetX = 0.0f;
            private float offsetY = 0.0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    offsetX = event.getX();
                    offsetY = event.getY();
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mWindowButtonParams.x = (int) (event.getRawX() - offsetX);
                    mWindowButtonParams.y = (int) (event.getRawY() - offsetY);

                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);

                    v.cancelLongPress();
                }

                return event.getAction() == MotionEvent.ACTION_UP && event.getEventTime() - event.getDownTime() > 200;
            }
        });

        mWindowButton.setOnKeyListener(new View.OnKeyListener() {
            private int[] location = new int[2];

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    mWindowButtonParams.flags =
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);

                    execShellCmdKeyEvent(4);

                    mHandler.sendEmptyMessageDelayed(0, 2000L);

                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == 79) {

                    mWindowButtonParams.flags =
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);

                    mWindowButton.getLocationOnScreen(location);
                    execShellCmdTap(location);

                    mHandler.sendEmptyMessageDelayed(0, 3000L);

                    return true;
                }

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode != 79 && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
                    //Toast.makeText(mContext, "onKey : " + keyCode, Toast.LENGTH_SHORT).show();
                    mWindowButtonParams.flags =
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);

                    execShellCmdKeyEvent(keyCode);

                    mHandler.sendEmptyMessageDelayed(0, 3000L);
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
                //float z = event.values[2];

                mWindowButtonParams.x += (int) (x + 1) * -1;
                mWindowButtonParams.y += (int) (y - 6);

                if (mWindowButtonParams.x < 0) {
                    mWindowButtonParams.x = 0;
                }

                if (mWindowButtonParams.x > mWidthPixels) {
                    mWindowButtonParams.x = mWidthPixels;
                }

                if (mWindowButtonParams.y < 0) {
                    mWindowButtonParams.y = 0;
                }

                if (mWindowButtonParams.y > mHeightPixels) {
                    mWindowButtonParams.y = mHeightPixels;
                }

                if (mWindowManager != null) {
                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);
                }

                if (isToast) {
                    Toast.makeText(mContext, "" + x, Toast.LENGTH_SHORT).show();
                    isToast = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

        }, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

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

    private void execShellCmdTap(int[] location) {
        execShellCmd("input tap " + location[0] + " " + location[1]);
    }

    private void execShellCmdTap(int x, int y) {
        execShellCmd("input tap " + x + " " + y);
    }

    private void execShellCmdKeyEvent(int keyCode) {
        execShellCmd("input keyevent " + keyCode);
    }

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void destroy() {

        if (mWindowManager != null) {
            for (View v : mWindowViews) {
                mWindowManager.removeView(v);
            }
            mWindowManager = null;
        }

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mAccelerometerListener);
        }
    }
}
