package com.example.heartx.assistant.toucher;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //当AndroidSDK>=23及Android版本6.0及以上时，需要获取OVERLAY_PERMISSION.
        //使用canDrawOverlays用于检查，下面为其源码。其中也提醒了需要在manifest文件中添加权限.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (Settings.canDrawOverlays(MainActivity1.this)) {
                startupService();
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity1.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }
        } else {
            startupService();
        }
    }

    private void startupService() {
        Intent intent = new Intent(MainActivity1.this, MainService.class);
        Toast.makeText(MainActivity1.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
        startService(intent);
        finish();
    }

}
