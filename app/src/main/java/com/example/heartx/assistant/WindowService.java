package com.example.heartx.assistant;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import io.reactivex.functions.Consumer;

/**
 * 悬浮窗的服务
 * Created by HeartX on 2018/2/12.
 */

public class WindowService extends Service {

    private IBinder mIBinder = new MyBinder();

    private WindowManageAgent mWindowManageAgent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();
        mWindowManageAgent = new WindowManageAgent(this);
        mWindowManageAgent.init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    public void bindView(View... view) {
        for (View v: view) {
            switch (v.getId()) {
                case R.id.x:
                    mWindowManageAgent.mX = (TextView) v;
                    break;
                case R.id.y:
                    mWindowManageAgent.mY = (TextView) v;
                    break;
                case R.id.z:
                    mWindowManageAgent.mZ = (TextView) v;
                    break;
                case R.id.tv:
                    mWindowManageAgent.mTv = (TextView) v;
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {

        mWindowManageAgent.destroy();
        mWindowManageAgent = null;

        super.onDestroy();
    }

    class MyBinder extends Binder {

        WindowService getService() {
            return WindowService.this;
        }
    }
}

