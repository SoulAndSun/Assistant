package com.example.heartx.assistant;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

public class MainActivity_Main extends AppCompatActivity {

    private TextView mTextView;
    private WeakReference<Object> mWeakReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(MainActivity_Main.this)) {
                startupService();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        } else {
            startupService();
        }

        testWeakReference();
    }

    private void startupService() {
        Intent intent = new Intent(MainActivity_Main.this, WindowService.class);
        startService(intent);
        finish();
    }

    private void testWeakReference(){
        mTextView = findViewById(R.id.tv);
        Object mObject = new Object();
        mWeakReference = new WeakReference<Object>(mObject);
        mTextView.setOnClickListener(new View.OnClickListener() {
            private int mInt;
            @Override
            public void onClick(View v) {
                if (mWeakReference.get() != null) {
                    Logger.d(mWeakReference.get().toString());
                } else {
                    Logger.d("The Object is null");
                    //mObject = new Object();
                }
            }
        });
    }

}
