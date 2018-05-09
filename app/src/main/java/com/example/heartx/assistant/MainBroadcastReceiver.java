package com.example.heartx.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 监听开机广播
 * Created by HeartX on 2018/4/27.
 */

public class MainBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            startupService(context);
        }
    }

    private void startupService(Context context) {
        Intent intent = new Intent(context, ToucherService.class);
        context.startService(intent);
        //finish();
    }

    private void startupActivity(Context context) {
        Intent intent = new Intent(context, MainActivity_Main.class);
        context.startActivity(intent);
        //finish();
    }
}
