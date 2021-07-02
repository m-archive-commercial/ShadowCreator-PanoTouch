package com.luci.pano_touch.touchpad;

import androidx.databinding.DataBindingUtil;

import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.luci.pano_touch.Constants;
import com.luci.pano_touch.screen_mirror.ScreenMirrorActivity;
import com.luci.pano_touch.BaseActivity;
import com.luci.pano_touch.R;
import com.luci.pano_touch.databinding.PrimaryTouchpadLayoutBinding;


/**
 * 触控板程序
 * 1. 对scrolling的判定是没有必要的，可以直接通过手指数目以及动作进行监控
 */
public class TouchpadActivity extends BaseActivity {

    private static final String TAG = "Touchpad";

    static {
        System.loadLibrary("native_pano_touch_mouse_driver");
    }

    //    单根手指的坐标记录
    private float x, y;
    //    两根手指的x、y坐标记录
    private float x1, y1, x2, y2;
    //    上一次下压时间与抬起时间，抬起时间用于计算是否双击
    private long lastDownTime, lastUpTime;
    //    是否正在移动，用于判定鼠标移动或拖动
    private boolean isPressing;
    private boolean isMoving;

    private static UiModeManager sUiModeManager;
    private static DisplayManager sDisplayManager;
    private static Handler sHandler = new Handler();
    private Button btnSwitchBinding, btnForceUseWithLUCIGlasses;
    private Runnable sKeepMouseActiveRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarManager mStatusBarManager = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
        mStatusBarManager.disable(
                StatusBarManager.DISABLE_NOTIFICATION_ALERTS
        );
        super.onCreate(savedInstanceState);

        sDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Log.v(TAG, "onCreate");


        PrimaryTouchpadLayoutBinding binding = DataBindingUtil.setContentView(this, R.layout.primary_touchpad_layout);

        sUiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);

        binding.btnToGameMode.setOnClickListener(v -> {
            startActivity(new Intent(this, ScreenMirrorActivity.class));
        });


//        初始化切换模式的按钮监听
        btnSwitchBinding = binding.btnSwitchMode;
        btnSwitchBinding.setVisibility(View.INVISIBLE);
        btnSwitchBinding.setText(sUiModeManager.isDeskModeEnabled() ? R.string.switchToMobileMode : R.string.switchToDesktopMode);

//        初始化基于屏幕个数的阻挡可见
        btnForceUseWithLUCIGlasses = binding.btnForceUseWithLUCIGlasses;
        if (sDisplayManager.getDisplays().length > 1) {
            btnForceUseWithLUCIGlasses.setVisibility(View.INVISIBLE);
        }

        btnSwitchBinding.setOnClickListener(view -> {
            if (sUiModeManager.isDeskModeEnabled()) {
//                切换回手机模式
                sUiModeManager.disableDeskMode(0);
                btnSwitchBinding.setText(R.string.switchToDesktopMode);
                Toast.makeText(this, R.string.switchedMobileMode, Toast.LENGTH_SHORT).show();
            } else {
//                切换回桌面模式
                sUiModeManager.enableDeskMode(0);
                btnSwitchBinding.setText(R.string.switchToMobileMode);
                Toast.makeText(this, R.string.switchedDesktopMode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        Log.v(TAG, String.format("display count: %d", sDisplayManager.getDisplays().length));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (sDisplayManager.getDisplays().length == 1) {
            btnForceUseWithLUCIGlasses.setVisibility(View.VISIBLE);
            return true;
        } else if (btnForceUseWithLUCIGlasses.getVisibility() == View.VISIBLE) {
            btnForceUseWithLUCIGlasses.setVisibility(View.INVISIBLE);
        }


        int n = event.getPointerCount();
        int action = event.getAction();
        long now = SystemClock.uptimeMillis();


        /**
         * 三指
         */
        if (n > 2) {
            showModeSwitchDialog();
            return true;
        }

        //
        // 双指
        //
        //  如果是主屏，则直接劫持后续的鼠标点击
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) return false;

        if (n == 2) {
            if (MotionEvent.ACTION_POINTER_DOWN == (action & MotionEvent.ACTION_POINTER_DOWN)) {
                x1 = event.getRawX(0);
                y1 = event.getRawY(0);
                x2 = event.getRawX(1);
                y2 = event.getRawY(1);
                // todo: test forcing mouse shown on the secondary display even when long press
//                nativeMouseMove(0, 0);
//                nativeMouseLeftDown();
            } else if (MotionEvent.ACTION_POINTER_UP == (action & MotionEvent.ACTION_POINTER_UP)) {
                nativeMouseScroll(0, 0);
            } else {
                float newX1 = event.getRawX(0), newY1 = event.getRawY(0);
                float newX2 = event.getRawX(1), newY2 = event.getRawY(1);
                int dx = (int) ((newX1 + newX2) - (x1 + x2));
                int dy = (int) ((newY1 + newY2) - (y1 + y2));
                x1 = newX1;
                x2 = newX2;
                y1 = newY1;
                y2 = newY2;
                int ddx = 0, ddy = 0;
                if (dx > Constants.SCROLL_DISTANCE_THROTTLE) ddx = 1;
                else if (dx < -Constants.SCROLL_DISTANCE_THROTTLE) ddx = -1;
                if (dy > Constants.SCROLL_DISTANCE_THROTTLE) ddy = 1;
                else if (dy < -Constants.SCROLL_DISTANCE_THROTTLE) ddy = -1;
                if (ddx != 0 || ddy != 0) {
//                    Log.v(TAG, String.format("dispatching scroll of (%d, %d)", ddx, ddy));
                    nativeMouseScroll(ddx, ddy);
                }
            }
            return false;
        }


        //
        // 单指
        //
        Log.v(TAG, event.toString());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastDownTime = SystemClock.uptimeMillis();
                isPressing = true;

                //
                // 为防止长按手指后鼠标消失，刷新鼠标位置
                //
                sKeepMouseActiveRunnable = () -> {
                    nativeMouseMove(0, 1);
                };
                sHandler.postDelayed(sKeepMouseActiveRunnable, 300);
                break;

            case MotionEvent.ACTION_MOVE:
                if (sKeepMouseActiveRunnable != null) {
                    sHandler.removeCallbacks(sKeepMouseActiveRunnable);
                    sKeepMouseActiveRunnable = null;
                }

                float newX = event.getRawX(), newY = event.getRawY();
                if (!isMoving) {
                    // 若开始移动时间超过SHORT_PRESS，则为拖动效果，否则为移动效果
                    isMoving = true;
                    if (now - lastDownTime > Constants.SHORT_PRESS && now - lastDownTime < Constants.LONG_PRESS) {
                        nativeMouseLeftDown();
                        isPressing = true;
                    }
                } else {
//                    开始移动
                    nativeMouseMove((int) (newX - x), (int) (newY - y));
                }
                x = newX;
                y = newY;
                break;
            case MotionEvent.ACTION_UP:
                if (sKeepMouseActiveRunnable != null) {
                    sHandler.removeCallbacks(sKeepMouseActiveRunnable);
                    sKeepMouseActiveRunnable = null;
                }

                lastUpTime = SystemClock.uptimeMillis();
//                Log.v(TAG, String.format("click time length: %s", lastUpTime - lastDownTime));

                if (isMoving) {
//                反正在移动过程中，抬起必然导致抬起
                    nativeMouseLeftUp();
                } else if (lastUpTime - lastDownTime < Constants.SHORT_PRESS) {
//                    如果没有在移动，并且下压时间过短，则视为单击
                    nativeMouseLeftDown();
                    nativeMouseLeftUp();
//                    todo: 复杂的双击转单击实现
//                    clickedCount++;
//                    if(clickedCount > 1 || isMoving) {
////                        双击屏幕或者移动后抬起，触发鼠标抬起
////                        nativeMouseLeftDown();
//                        Log.v(TAG, "dispatch click up");
//                        nativeMouseLeftUp();
//                        clickedCount = 0;
//                    } else {
//                        Log.v(TAG, "dispatch click down");
//                        nativeMouseLeftDown();
//                    }
                } else {
//                    todo: 否则也视为单击
                    nativeMouseLeftUp();

                }

                isPressing = false;
                isMoving = false;
                break;

            case MotionEvent.ACTION_CANCEL:
            default:
                if (sKeepMouseActiveRunnable != null) {
                    sHandler.removeCallbacks(sKeepMouseActiveRunnable);
                    sKeepMouseActiveRunnable = null;
                }
                break;
        }

        return false;
    }

    /**
     * 模拟副屏的鼠标操作只需要三个事件：点击、弹起、移动即可。
     */
    public native int nativeMouseLeftDown();

    public native int nativeMouseLeftUp();

    public native int nativeMouseMove(int x, int y);

    public native int nativeMouseScroll(int x, int y);
}