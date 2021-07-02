package com.luci.pano_touch;

public class Constants {
    public static final int MAX_POINTERS_COUNT = 32; // do not edit
    public static final int LUCI_VENDOR_ID = 11312;
    public static int SHORT_PRESS = 200;
    public static int LONG_PRESS = 3000;
    public static int SCROLL_DISTANCE_THROTTLE = 30;

    public static final String ACTION_CLOSE_ACTIVITY = "com.markshawn.touchpad.finish";

    public static String[] MODES = {
            "切换到桌面模式",
            "切换到手机模式",
//            "切换到鼠标模式",
            "切换到游戏模式"};


    //    action
    public final static String ACTION_ENTER_DESKTOP_MODE = "ACTION_ENTER_DESKTOP_MODE";
    public final static String ACTION_EXIT_DESKTOP_MODE = "ACTION_EXIT_DESKTOP_MODE";
    public final static String MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD = "MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD";
    public final static String MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE = "MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE";

    //    输入控制
    public final static float X_PRECISION = 1.000803f;
    public final static float Y_PRECISION = 1.000926f;
    public static int JOYSTICK_SCREEN_RADIUS = 200; // px

    //    副屏映射时Trigger键组合偏移
    public final static int KEY_COMPOSITE_OFFSET_X = -150;
    public final static int KEY_COMPOSITE_OFFSET_Y = -150;
    //    副屏中矩形区域四角的半径
    public static float CORNER_RADIUS = 50;

    //    副屏中矩形区域的四角位置
    public static int X1 = 950, X2 = 1800, Y1 = 200, Y2 = 1000;
}
