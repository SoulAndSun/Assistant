package com.example.heartx.assistant;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heartx.assistant.util.adaptive.AdaptiveScreen;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 悬浮窗的服务
 * Created by HeartX on 2018/2/12.
 */

public class WindowService extends Service {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowButtonParams;
    private WindowManager.LayoutParams mCoverViewParams;

    private Button mWindowButton;
    private TextView mCoverView;

    private BroadcastReceiver mReceiver;

    private float offsetX = 0.0f;
    private float offsetY = 0.0f;

    private boolean isCreatedCoverView = false;

    private Set<View> mWindowViews = new HashSet<>();
    private int mWidthPixels;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWindow();
        addListener();
        mWidthPixels = getResources().getDisplayMetrics().widthPixels;
    }

    private void initWindow() {
        //赋值WindowManager&LayoutParam.
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
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
//        mWindowButtonParams.gravity = Gravity.END | Gravity.TOP;
        mWindowButtonParams.gravity = Gravity.START | Gravity.TOP;

        mWindowButtonParams.x = 1080;
        mWindowButtonParams.y = 200;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        mWindowButtonParams.width = 150;
        mWindowButtonParams.height = 150;

        mWindowButton = new Button(this);
        mWindowButton.setText(R.string.Create);
        mWindowButton.setTextSize(7.0f);
        mWindowButton.setAlpha(0.3f);
        mWindowButton.setFocusableInTouchMode(true);

        AdaptiveScreen.adaptive(mWindowButton, mWindowButtonParams);
        addWindowView(mWindowButton, mWindowButtonParams);

//--------------------------------------------------------------------------------------------------
        mCoverViewParams = new WindowManager.LayoutParams();
        mCoverViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mCoverViewParams.format = PixelFormat.RGBA_8888;
        mCoverViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mCoverViewParams.gravity = Gravity.START | Gravity.TOP;
        mCoverViewParams.x = 20;
        mCoverViewParams.y = 890;
        mCoverViewParams.width = 1050;
        mCoverViewParams.height = 800;

        mCoverView = new TextView(WindowService.this);
        mCoverView.setBackgroundColor(getResources().getColor(R.color.coverViewColor));
        mCoverView.setFocusableInTouchMode(true);
    }

    private void addListener() {

        mReceiver = new BroadcastReceiver() {

            private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
            private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
            private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (TextUtils.equals(action, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                    if (TextUtils.equals(reason, SYSTEM_DIALOG_REASON_HOME_KEY)) {

                        if (isCreatedCoverView) {
                            removeCoverView();
                        }
                    }

                    if (TextUtils.equals(reason ,SYSTEM_DIALOG_REASON_RECENT_APPS)) {

                        if (isCreatedCoverView) {
                            removeCoverView();
                        }
                    }
                } else {
                    Toast.makeText(WindowService.this, action, Toast.LENGTH_SHORT).show();
                }

            }
        };
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

//--------------------------------------------------------------------------------------------------
        mWindowButton.setOnClickListener(new View.OnClickListener() {
            private int mInt;

            @Override
            public void onClick(View v) {

                if (isShow() && !isCreatedCoverView) {

                    AdaptiveScreen.adaptive(mCoverView, mCoverViewParams);
                    addWindowView(mCoverView, mCoverViewParams);
                    isCreatedCoverView = true;
                } else if (isCreatedCoverView){
                    removeCoverView();
                }
            }
        });

        mWindowButton.setOnLongClickListener(new View.OnLongClickListener() {
            private int mInt;
            @Override
            public boolean onLongClick(View v) {

                stopSelf();

                return true;
            }
        });

        mWindowButton.setOnTouchListener(new View.OnTouchListener() {
            private int mInt;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    if (mCoverView.getVisibility() == View.INVISIBLE ) {

                        mCoverView.setVisibility(View.VISIBLE);
                    }
                }

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

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (event.getRawX() > (mWidthPixels / 2)) {

                        mWindowButtonParams.x = mWidthPixels;
                    } else {
                        mWindowButtonParams.x = 0;
                    }
                    mWindowButtonParams.y = (int) (event.getRawY() - offsetY);
                    mWindowManager.updateViewLayout(mWindowButton, mWindowButtonParams);

                    if (event.getEventTime() - event.getDownTime() > 200){
                        return true;
                    } else {
                        return false;
                    }
                }

                return false;
            }
        });

//--------------------------------------------------------------------------------------------------
        mCoverView.setOnClickListener(new View.OnClickListener() {
            private int mInt;
            @Override
            public void onClick(View v) {

                mCoverView.setVisibility(View.INVISIBLE);
            }
        });

        mCoverView.setOnKeyListener(new View.OnKeyListener() {
            private int mInt;
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    removeCoverView();
                }
                return false;
            }
        });
    }

    private void addWindowView(View view, WindowManager.LayoutParams params) {
        mWindowManager.addView(view, params);
        mWindowViews.add(view);
    }

    private void removeCoverView() {
        mWindowManager.removeView(mCoverView);
        mWindowViews.remove(mCoverView);
        mCoverView.setVisibility(View.VISIBLE);
        isCreatedCoverView = false;
    }

    private boolean isRecentTask(){
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return "com.android.systemui".equals(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private boolean isShow(){
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return TextUtils.equals("com.jiongji.andriod.card", rti.get(0).topActivity.getPackageName());
    }

    @Override
    public void onDestroy() {

        for (View v: mWindowViews) {
            mWindowManager.removeView(v);
        }
        mWindowViews.clear();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        super.onDestroy();
    }
}

