package com.example.heartx.assistant;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class MainActivity_Main extends AppCompatActivity {

    public TextView mTextView;
    public TextView x, y, z;

    private WeakReference<Object> mWeakReference;
    private ReferenceQueue<Object> q = new ReferenceQueue<>();
    private ToucherService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((ToucherService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_main);

        mTextView = findViewById(R.id.tv);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(MainActivity_Main.this)) {
                startupService();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity_Main.this, R.string.get_window_authority, Toast.LENGTH_LONG).show();
                startActivity(intent);
                finish();
            }
        } else {
            startupService();
        }
        testWeakReference();
    }

    private void startupService() {
        Intent intent = new Intent(MainActivity_Main.this, ToucherService.class);
        //bindService(intent, mConnection, BIND_AUTO_CREATE);
        startService(intent);
        //finish();
    }

    /**
     * 弱引用测试
     */
    private void testWeakReference(){
        Object mObject = new Object();
        mWeakReference = new WeakReference<Object>(mObject, q);
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
                Toast.makeText(MainActivity_Main.this, "退出应用", Toast.LENGTH_SHORT).show();
                MainActivity_Main.super.onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        //unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(false);
    }
}
