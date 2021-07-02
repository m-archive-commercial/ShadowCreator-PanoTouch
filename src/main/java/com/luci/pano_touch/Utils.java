package com.luci.pano_touch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.luci.pano_touch.R;
import com.luci.pano_touch.screen_mirror.ScreenMirrorActivity;
import com.luci.pano_touch.touchpad.TouchpadActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.content.Context.DISPLAY_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.USB_SERVICE;

public class Utils {

    private final static String TAG = "TouchpadUtils";
    private static NotificationManager sNotificationManager;
    private static AlertDialog.Builder sDialogBuilder;
    private static DisplayManager sDisplayManager;
    private static Intent sIntent;


    private static String createNotificationChannel(String channelID, String channelNAME, int level, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void showNotification(Context context) {
        //                todo：这里的判断需要优化，需要与甲方沟通确定一劳永逸的方案
        Log.v(TAG, "trying to detect displays info");
        sDisplayManager = (DisplayManager) context.getSystemService(DISPLAY_SERVICE);
        sNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        int displaysCount = sDisplayManager.getDisplays().length;
        Log.v(TAG, String.format("Displays Count: %d", displaysCount));
        if (displaysCount > 1) {
            Log.v(TAG, "try to start touchpad ui");
//                    refer： https://developer.android.com/training/notify-user/navigation

            Intent mTouchpadModeActivityIntent = new Intent(context, TouchpadActivity.class);
            mTouchpadModeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mTouchpadModeActivityIntent.putExtra("EXIT", true);
//
            String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH, context);
            assert channelId != null;
            NotificationCompat.Builder mNotification = new NotificationCompat
                    .Builder(context, channelId)
                    .setSmallIcon(R.drawable.mouse)
                    .setContentTitle("LUCI 远程控制")
                    .setContentText("检测到LUCI眼镜接入，点击确认进入触控板")
//                    设置默认进入触控板程序
                    .setContentIntent(PendingIntent.getActivity(context, 0, mTouchpadModeActivityIntent, 0))
                    .setPriority(1000)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setOngoing(true) // 防止用户关闭
                    .setAutoCancel(false); // Use a full-screen intent only for the highest-priority alerts where you // have an associated activity that you would like to launch after the user // interacts with the notification. Also, if your app targets Android 10 // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in // order for the platform to invoke this notification. .setFullScreenIntent(fullScreenPendingIntent, true);

//            甲方需要按返回键时关闭整个应用，而非单个activity，要配合manifest中的noHistory： https://stackoverflow.com/a/19109602/9422455
            mNotification.addAction(R.drawable.mouse, "进入触控板", PendingIntent.getActivity(context, 0, mTouchpadModeActivityIntent, 0));

            Intent mGameModeActivityIntent = new Intent(context, ScreenMirrorActivity.class);
            mTouchpadModeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mTouchpadModeActivityIntent.putExtra("EXIT", true);
            mNotification.addAction(R.drawable.mouse, "进入游戏模式", PendingIntent.getActivity(context, 0, mGameModeActivityIntent, 0));

//            if (((UiModeManager) context.getSystemService(UI_MODE_SERVICE)).isDeskModeEnabled()) {
//                mNotification.addAction(R.drawable.mouse, "手机/桌面模式切换", PendingIntent.getBroadcast(context, 0, new Intent(context,  TouchpadBroadcastReceiver.class).setAction(Constants.ACTION_EXIT_DESKTOP_MODE), 0));
//                synchronized (mNotification) {
//                    mNotification.notify();
//                }
//            } else {
//                mNotification.addAction(R.drawable.mouse, "进入桌面模式", PendingIntent.getBroadcast(context, 0, new Intent(context, TouchpadBroadcastReceiver.class).setAction(Constants.ACTION_ENTER_DESKTOP_MODE), 0));
//                synchronized (mNotification) {
//                    mNotification.notify();
//                }
//            }

            sNotificationManager = NotificationManager.from(context);
            sNotificationManager.notify(100, mNotification.build());
        }
    }


    public static void cancelNotification() {
        if (sNotificationManager == null) return;
        sNotificationManager.cancelAll();
    }

    public static Display getSecondaryDisplay(Context context) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        Log.v(TAG, String.format("displays count: %d", displays.length));
        return displays[displays.length - 1];
    }

    public static int getSecondaryDisplayId(Context context) {
        return getSecondaryDisplay(context).getDisplayId();
    }

    public static boolean isLuciImmers(Context context) {
        Log.v(TAG, "ACTION_BOOT_COMPLETED");
        UsbManager mUsbManager = (UsbManager) context.getSystemService(USB_SERVICE);
        for (UsbDevice mUsbDevice : mUsbManager.getDeviceList().values()) {
            if (mUsbDevice.getVendorId() == Constants.LUCI_VENDOR_ID) {
//                11312
                return true;
            }
        }
        return false;
    }

    public static float lerp(float a, float b, float alpha) {
        assert alpha >= 0 && alpha <= 1 :
                String.format("alpha必须在[0-1]内");
        return a + (b - a) * alpha;
    }

    public static void startTouchpadMode(Context context) {
        Log.v(TAG, "正在启动触控板应用");
        context.startActivity(new Intent(context, TouchpadActivity.class));
        Toast.makeText(context, "已切换至触控板模式", Toast.LENGTH_SHORT).show();
    }


    /**
     * https://blog.csdn.net/www1056481167/article/details/94436576
     * 为指定对象的指定属性动态赋予指定值
     *
     * @param obj       指定对象
     * @param fieldName 指定属性
     * @param value     指定值
     * @return obj      返回对象
     */
    public static Object dynamicSetValue(Object obj, String fieldName, Object value) {
        try {
            // 取属性首字母转大写
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            // set方法名
            String setMethodName = "set" + firstLetter + fieldName.substring(1);
            // 获取属性
            Field field = obj.getClass().getDeclaredField(fieldName);
            // 获取set方法
            Method setMethod = obj.getClass().getDeclaredMethod(setMethodName, field.getType());
            // 通过set方法动态赋值
            setMethod.invoke(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * https://blog.csdn.net/www1056481167/article/details/94436576
     * 动态获取指定对象指定属性的值
     *
     * @param obj       指定对象
     * @param fieldName 指定属性
     * @return 属性值
     */
    public static Object dynamicGetValueByGet(Object obj, String fieldName) {
        try {
            // 取属性首字母转大写
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            // get方法名
            String getMethodName = "get" + firstLetter + fieldName.substring(1);
            // 获取get方法
            Method getMethod = obj.getClass().getDeclaredMethod(getMethodName);
            // 动态取值
            return getMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object dynamicGetValue(Class cls, String fieldName) {
        try {
            // 获取get方法
            return cls.getField(fieldName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
