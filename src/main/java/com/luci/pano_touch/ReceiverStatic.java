package com.luci.pano_touch;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.luci.pano_touch.screen_mirror.ScreenMirrorActivity;
import com.luci.pano_touch.touchpad.TouchpadActivity;

import java.util.List;

import static android.content.Context.DISPLAY_SERVICE;
import static android.content.Context.UI_MODE_SERVICE;


public class ReceiverStatic extends BroadcastReceiver {
    private static final String TAG = "LuciRemote Main Receiver";
    private static DisplayManager sDisplayManager;
    private static UiModeManager sUiModeManager;
    private static boolean mMultiDisplay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        sDisplayManager = (DisplayManager) context.getSystemService(DISPLAY_SERVICE);
        mMultiDisplay = sDisplayManager.getDisplays().length > 1;
        sUiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);

        String action = intent.getAction();
        Intent mIntent;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                Log.v(TAG, "ACTION_BOOT_COMPLETED");

                if (Utils.isLuciImmers(context)) {
                    startTouchpad(context);
                }
                break;

            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                Log.v(TAG, "usb device attached");

                if (Utils.isLuciImmers(context)) {
                    startTouchpad(context);
                }
                break;

            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                Log.v(TAG, "usb detached");

                Log.v(TAG, "close all notification");
                Utils.cancelNotification();

                Log.v(TAG, "stop activity");
                mIntent = new Intent();
                mIntent.setAction(Constants.ACTION_CLOSE_ACTIVITY);
                context.sendBroadcast(mIntent);
                break;

//                todo: 目前因为是在framework中分发给活动应用的，所以这个应该监听不到
            case Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD:
                Log.v(TAG, String.format("[GLOBAL RECEIVER] received broadcast: MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD"));

                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                assert am != null;
                List<ActivityManager.AppTask> mTasks = am.getAppTasks();

                Class targetClass = TouchpadActivity.class;
                if (mTasks != null && !mTasks.isEmpty()) {
                    ComponentName mComponentName = mTasks.get(0).getTaskInfo().topActivity;
                    if (mComponentName != null) {
                        String mTopActivityClassName = mComponentName.getClassName();
                        Log.v(TAG, String.format("top activity name: %s", mTopActivityClassName));
                        if (mTopActivityClassName.equals(TouchpadActivity.class.getName())) {
                            if (mMultiDisplay) {
                                targetClass = ScreenMirrorActivity.class;
                            } else {
                                Toast.makeText(context, "连接LUCI眼镜后才能启动LUCI Touch游戏模式", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                }
//                for(ActivityManager.AppTask mAppTask: mTasks) {
//                    Log.v(TAG, mAppTask.toString());
//                }
//                if (mTasks != null && !mTasks.isEmpty()) {
//                    targetClass = mTasks.get(0).getTaskInfo().topActivity.getClass();
//                }
                Log.v(TAG, String.format("[GLOBAL RECEIVER] 正在启动 Activity: %s", targetClass.getName()));
                mIntent = new Intent(context, targetClass);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mIntent);
                break;

            case Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE:
                Log.v(TAG, String.format("[GLOBAL RECEIVER] received broadcast: MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE"));
                if (sUiModeManager.isDeskModeEnabled()) {
                    sUiModeManager.disableDeskMode(0);
                    Toast.makeText(context, "已切换至手机模式", Toast.LENGTH_SHORT).show();
                } else {
                    sUiModeManager.enableDeskMode(0);
                    Toast.makeText(context, "已切换至桌面模式", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.v(TAG, "default");
                break;
        }
    }

    public void startTouchpad(Context context) {
        Log.v(TAG, "showing notification");
        Utils.showNotification(context);

        UiModeManager mUiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        if (!mUiModeManager.isDeskModeEnabled()) {
            Log.v(TAG, "auto enable DESKTOP MODE");
            mUiModeManager.enableDeskMode(0);
        }

        Log.v(TAG, "auto start touchpad app");
        Intent i = new Intent();
        i.setClassName(context.getPackageName(), TouchpadActivity.class.getName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
