package com.luci.pano_touch;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.luci.pano_touch.screen_mirror.ScreenMirrorActivity;
import com.luci.pano_touch.touchpad.TouchpadActivity;

/**
 * 用于接收关闭程序的intent，参考：https://stackoverflow.com/a/9413702/9422455
 */
public class BaseActivity extends Activity {
    private final static String TAG = "BaseActivity";
    protected static UiModeManager sUiModeManager;
    protected static InputManager sInputManager;
    protected static DisplayManager sDisplayManager;
    protected static AlertDialog.Builder sDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sUiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        sInputManager = (InputManager) getSystemService(INPUT_SERVICE);
        sDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        Log.v(TAG, "注册广播：强制关闭、双击电源、三击电源");
        IntentFilter mIntentFilterToFinish = new IntentFilter();
        mIntentFilterToFinish.addAction(Constants.ACTION_CLOSE_ACTIVITY);
//        mIntentFilterToFinish.addAction(Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD);
//        mIntentFilterToFinish.addAction(Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE);
        registerReceiver(mReceiver, mIntentFilterToFinish);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.v(TAG, String.format("intent: %s", action));
            switch (intent.getAction()) {
                case Constants.ACTION_CLOSE_ACTIVITY:
                    finish();
                    break;
//                case Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD:
//                    Log.v(TAG, String.format("received broadcast of %s", Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_POINTER_AND_GAMEPAD));
//                    switchModeBetweenMouseAndGamepad();
//                    break;
//                case Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE:
//                    Log.v(TAG, String.format("received broadcast of %s", Constants.MULTI_PRESS_POWER_SWITCH_MODE_BETWEEN_DESKTOP_AND_MOBILE));
//                    switchModeBetweenDesktopAndMobile();
//                    break;
                default:
                    Log.e(TAG, "undefined broadcast");
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "注销广播");
        unregisterReceiver(mReceiver);
    }

    public void showModeSwitchDialog() {
        //                                    关闭对话窗：https://stackoverflow.com/a/18136387/9422455
        //                                https://stackoverflow.com/a/21453290/9422455
        if (sDialogBuilder != null) return;
        String[] modes = {
                sUiModeManager.isDeskModeEnabled() ? "退出桌面模式" : "进入桌面模式",
                "进入游戏模式"
        };
//        Display[] mDisplays = sDisplayManager.getDisplays();
//        Display mDisplay = mDisplays[mDisplays.length - 1];
//        Context context = createDisplayContext(mDisplay);
//        todo: 想要在副屏弹窗，可能需要service或者popup window……
        Context context = this;
        sDialogBuilder = new AlertDialog.Builder(context)
                .setTitle("LUCI Touch 模式切换")
                .setSingleChoiceItems(modes, 0, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if(sUiModeManager.isDeskModeEnabled()) {
                                sUiModeManager.disableDeskMode(0);
                                Toast.makeText(context, "已切换至手机模式", Toast.LENGTH_SHORT).show();
                            } else {
                                sUiModeManager.enableDeskMode(0);
                                Toast.makeText(context, "已切换至桌面模式", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1:
                            if (BaseActivity.this instanceof ScreenMirrorActivity) {
                                Toast.makeText(context, "您正处于游戏模式", Toast.LENGTH_SHORT).show();
                            } else {
                                startGameMode(context);
                            }
                            break;
                        default:
                            Log.e(TAG, "未定义模式");
                            break;
                    }

                    Log.v(TAG, String.format("dismissing dialog: %s", dialog.toString()));
                    dialog.dismiss();
                    sDialogBuilder = null;
                })
                .setOnCancelListener(dialogInterface -> {
                    Log.v(TAG, String.format("dismissing dialog since cancelled"));
                    dialogInterface.dismiss();
                    sDialogBuilder = null;
                });
        Log.v(TAG, "showing builder");
        sDialogBuilder.show();
    }


    protected void startGameMode(Context context) {
        Log.v(TAG, String.format("starting activity of SCREEN MIRROR"));
        Intent mIntent = new Intent(context, ScreenMirrorActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);

        Toast.makeText(context, "已切换至游戏模式", Toast.LENGTH_SHORT).show();
    }

    protected void switchModeBetweenMouseAndGamepad() {
        Intent mIntent = new Intent();
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        if (this instanceof TouchpadActivity) {
            if (sDisplayManager.getDisplays().length == 1) {
                Log.e(TAG, "游戏模式仅可在多屏幕模式下启动");
                Toast.makeText(this, "游戏模式仅可在多屏幕模式下启动", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.v(TAG, "starting [GAME MODE] ScreenMirrorActivity");
            mIntent.setClass(this, ScreenMirrorActivity.class);
            startActivity(mIntent);
            Toast.makeText(this, "已切换至游戏模式", Toast.LENGTH_SHORT).show();
        } else if (this instanceof ScreenMirrorActivity) {
            Log.v(TAG, "starting [TOUCHPAD MODE] TouchpadActivity");
            mIntent.setClass(this, TouchpadActivity.class);
            startActivity(mIntent);
            Toast.makeText(this, "已切换至触控板模式", Toast.LENGTH_SHORT).show();
        } else {
            throw new RuntimeException("未定义Activity");
        }
        finish();
    }

    protected void switchModeBetweenDesktopAndMobile() {
        if (sUiModeManager.isDeskModeEnabled()) {
            Log.v(TAG, "disabling desktop mode");
            sUiModeManager.disableDeskMode(0);
            Toast.makeText(this, "已切换至手机模式", Toast.LENGTH_SHORT).show();
        } else {
            Log.v(TAG, "enabling desktop mode");
            sUiModeManager.enableDeskMode(0);
            Toast.makeText(this, "已切换至桌面模式", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        Log.v(TAG, String.format("onWindowFocusChange, hasFocus: %B", hasFocus));
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
