package com.example.heartx.assistant.util;

import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * 执行shell命令
 * Created by HeartX on 2018/5/3.
 */

public class ShellCmdUtil {

    public static void execShellCmdTap(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        execShellCmdTap(location);
    }

    public static void execShellCmdTap(int[] location) {
        execShellCmd("input tap " + (location[0] + 50) + " " + (location[1] + 50));// TODO: 2018/5/3 把50改成动态获取
    }

    public static void execShellCmdTap(int x, int y) {
        execShellCmd("input tap " + x + " " + y);
    }

    public static void execShellCmdKeyEvent(int keyCode) {
        execShellCmd("input keyevent " + keyCode);
    }

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    private static void execShellCmd(String cmd) {

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

    public static void sendEvent0_116(){
        execShellCmd("sendevent /dev/input/event0 1 116 1\n" +
                "sendevent /dev/input/event0 0 0 0\n" +
                "sendevent /dev/input/event0 1 116 0\n" +
                "sendevent /dev/input/event0 0 0 0\n");
    }

}
