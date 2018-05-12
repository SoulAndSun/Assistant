package com.example.heartx.assistant.util;

import android.view.View;

import com.example.heartx.assistant.ToucherService;
import com.orhanobut.logger.Logger;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * 执行shell命令
 * Created by HeartX on 2018/5/3.
 */

public class ShellCmdUtil {

    public static final int SWIPE_TO_TOP = 1;
    public static final int SWIPE_TO_BOTTOM = 2;
    public static final int SWIPE_TO_RIGHT = 3;
    public static final int SWIPE_TO_LEFT = 4;
    public static final int SWIPE_STATUS_BAR = 5;
    public static final int LONG_CLICK = 6;

    private static int delay = 1000;
    private static int offset = 300;

    public static void execShellCmdTap(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        execShellCmdTap(location);
    }

    public static void execShellCmdTap(int[] location) {
        execShellCmd("input tap " + location[0] + " " + location[1]);
    }

    public static void execShellCmdTap(int x, int y) {
        execShellCmd("input tap " + x + " " + y);
    }

    public static void execShellCmdKeyEvent(int keyCode) {
        execShellCmd("input keyevent " + keyCode);
    }

    public static void execShellCmdSwipe(int num) {

        switch (num) {
            case SWIPE_TO_TOP:
                execShellCmd("input swipe 333 333 333 1111 500");
                break;
            case SWIPE_TO_BOTTOM:
                execShellCmd("input swipe 333 1111 333 333 500");
                break;
            case SWIPE_TO_RIGHT:
                execShellCmd("input swipe 666 666 000 666 500");
                break;
            case SWIPE_TO_LEFT:
                execShellCmd("input swipe 000 666 666 666 500");
                break;

            case SWIPE_STATUS_BAR:
                execShellCmd("input swipe 666 000 666 666 500");
                break;

            case LONG_CLICK:
                //execShellCmd();
                break;

            default:
                break;
        }
    }

    public static void execShellCmdSwipe(int num, int x, int y) {

        switch (num) {
            case SWIPE_TO_TOP:
                execShellCmd("input swipe " + x + " " + (y + offset) + " " + x + " " + ToucherService.screenHeight + " " + delay);
                break;
            case SWIPE_TO_BOTTOM:
                execShellCmd("input swipe " + x + " " + (y - offset) + " " + x + " 000 " + delay);
                break;
            case SWIPE_TO_RIGHT:
                execShellCmd("input swipe " + (x - offset) + " " + y + " 000 " + y + " " + delay);
                break;
            case SWIPE_TO_LEFT:
                execShellCmd("input swipe " + (x + offset) + " " + y + " " + ToucherService.screenWidth + " " + y + " " + delay);
                break;

            case SWIPE_STATUS_BAR:
                execShellCmd("input swipe " + x + " " + y + " " + x + " " + ToucherService.screenHeight + " " + delay);
                break;

            case LONG_CLICK:
                execShellCmd("input swipe " + x + " " + y + " " + (x + 1) + " " + (y + 1) + " " + 1000);
                break;

            default:
                break;
        }
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

    public static void sendEvent0_116() {
        execShellCmd("sendevent /dev/input/event0 1 116 1\n" +
                "sendevent /dev/input/event0 0 0 0\n" +
                "sendevent /dev/input/event0 1 116 0\n" +
                "sendevent /dev/input/event0 0 0 0\n");
    }

}
