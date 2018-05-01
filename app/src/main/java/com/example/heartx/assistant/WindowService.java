package com.example.heartx.assistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * 悬浮窗的服务
 * Created by HeartX on 2018/2/12.
 */

public class WindowService extends Service {

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private WindowManageAgent mWindowManageAgent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    @Override
    public void onDestroy() {

        mWindowManageAgent.destroy();

        super.onDestroy();
    }
}

