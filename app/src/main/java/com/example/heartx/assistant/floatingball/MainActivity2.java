package com.example.heartx.assistant.floatingball;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.heartx.assistant.R;

public class MainActivity2 extends AppCompatActivity {

    Button btn_openSetting,btn_openFloatingBall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btn_openSetting = (Button) findViewById(R.id.btn_openSetting);
        btn_openFloatingBall = (Button) findViewById(R.id.btn_openFloatingBall);

        btn_openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开设置  打开服务才能实现返回功能
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        btn_openFloatingBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManager.getInstance(MainActivity2.this).showFloatBall();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (ViewManager.manager.floatBall != null) {
            ViewManager.manager.windowManager.removeView(ViewManager.manager.floatBall);
        }
        super.onDestroy();
    }
}
