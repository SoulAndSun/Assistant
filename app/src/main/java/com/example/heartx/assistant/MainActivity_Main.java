package com.example.heartx.assistant;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class MainActivity_Main extends AppCompatActivity {

    private TextView mTextView;
//    private Button mButton;
//    private EditText mEditText;

    private WeakReference<Object> mWeakReference;
    private ReferenceQueue<Object> q = new ReferenceQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_main);
        //setContentView(new CircleView(this));

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

//        mButton = findViewById(R.id.btn);
//        mButton.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Toast.makeText(MainActivity_Main.this, "key press in Button: " + keyCode, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//
//        mEditText = findViewById(R.id.et);
//        mEditText.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Toast.makeText(MainActivity_Main.this, "key press in EditText: " + keyCode, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
    }

    private void startupService() {
        Intent intent = new Intent(MainActivity_Main.this, WindowService.class);
        startService(intent);
        //finish();
    }

    /**
     * 弱引用测试
     */
    private void testWeakReference(){
        mTextView = findViewById(R.id.tv);
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
                Toast.makeText(MainActivity_Main.this, "点击了TextView", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
