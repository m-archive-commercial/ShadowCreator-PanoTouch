package com.luci.pano_touch.screen_mirror;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.luci.pano_touch.Constants;
import com.luci.pano_touch.honghu_gamepad.HonghuKeysService;
import com.luci.pano_touch.BaseActivity;
import com.luci.pano_touch.InputCenter;
import com.luci.pano_touch.R;

/**
 * 该Activity用于向副屏发送触摸同步信号，其绑定的Service作用如下：
 * 1. ScreenMirrorService: 用于显示副屏悬浮技能框
 * 2. HonghuKeysService: 用于监听手柄按键输入，将其转为对应位置信号
 */
public class ScreenMirrorActivity extends BaseActivity {
    private final static String TAG = "ScreenMirrorActivity";

    /**
     * 主屏映射服务
     */
    ScreenMirrorService mScreenMirrorService;
    boolean isScreenMirrorServiceBound;

    ServiceConnection mScreenMirrorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.v(TAG, String.format("[ScreenMirrorService Connected] ComponentName: %s", componentName));
            ScreenMirrorService.ScreenMirrorBinder mBinder = (ScreenMirrorService.ScreenMirrorBinder) iBinder;
            mScreenMirrorService = mBinder.getService();
            isScreenMirrorServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v(TAG, String.format("[ScreenMirrorService Disconnected] ComponentName: %s", componentName));
            isScreenMirrorServiceBound = false;
        }
    };

    /**
     * 手柄输入服务
     */
    HonghuKeysService mHonghuKeysService;
    boolean isHonghuKeysServiceBound;

    ServiceConnection mHonghuKeysServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.v(TAG, String.format("[HonghuKeysService Connected] ComponentName: %s", componentName));
            HonghuKeysService.HonghuKeysBinder mBinder = (HonghuKeysService.HonghuKeysBinder) iBinder;
            mHonghuKeysService = mBinder.getService();
            isHonghuKeysServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v(TAG, String.format("[HonghuKeysService Disconnected] ComponentName: %s", componentName));
            isHonghuKeysServiceBound = false;
        }
    };

    /**
     * 屏幕相关：
     * 1. 副屏ID
     * 2. 主副屏宽高
     */
    private static int secondaryDisplayId;
    private static int primaryScreenWidth, primaryScreenHeight, secondaryScreenWidth, secondaryScreenHeight;

    private StatusBarManager mStatusBarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        劫持状态栏与导航栏
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        mStatusBarManager.disable(
                StatusBarManager.DISABLE_HOME
                        | StatusBarManager.DISABLE_RECENT
                        | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_MASK
                        | StatusBarManager.DISABLE_EXPAND
                        | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                        | StatusBarManager.DISABLE_NOTIFICATION_ICONS
        );

        setContentView(R.layout.primary_screen_mirror_layout);

        Log.v(TAG, "正在初始化【InputCenter】");
        InputCenter.init(this);
        Log.v(TAG, "正在绑定【ScreenMirrorService】");
        bindServiceAsUser(new Intent(this, ScreenMirrorService.class), mScreenMirrorServiceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
        Log.v(TAG, "正在绑定【HonghuKeysService】");
        bindServiceAsUser(new Intent(this, HonghuKeysService.class), mHonghuKeysServiceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
//        bindService(new Intent(this, HonghuKeysService.class), mHonghuKeysServiceConnection, Context.BIND_AUTO_CREATE);

        Log.v(TAG, "正在初始化屏幕相关信息");
        DisplayManager mSDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display[] mDisplays = mSDisplayManager.getDisplays();
        Point mPoint = new Point();
        Display primaryDisplay = mDisplays[0];
        primaryDisplay.getRealSize(mPoint);
        primaryScreenWidth = mPoint.x;
        primaryScreenHeight = mPoint.y;
        Display secondaryDisplay = mDisplays[mDisplays.length - 1];
        secondaryDisplay.getRealSize(mPoint);
        secondaryScreenWidth = mPoint.x;
        secondaryScreenHeight = mPoint.y;
        secondaryDisplayId = secondaryDisplay.getDisplayId();
        Log.v(TAG, String.format("[primary display] dim: (%d, %d), [second display] id: %d, dim: (%d, %d)",
                primaryScreenWidth, primaryScreenHeight, secondaryDisplayId, secondaryScreenWidth, secondaryScreenHeight));

        Log.v(TAG, "游戏模式初始化成功");
    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mScreenMirrorService == null) {
            Log.w(TAG, "ScreenMirrorService 暂未初始化，请稍后");
            return true;
        }
//        Log.v(TAG, event.toString());

        if (secondaryDisplayId == Display.DEFAULT_DISPLAY) {
            Log.v(TAG, "prevent to inject events into default display");
        } else {
//            映射位置，右下角四分之一块
            float newX = Constants.X1 + event.getRawX() / primaryScreenWidth * (Constants.X2 - Constants.X1);
            newX = Math.max(0, Math.min(secondaryScreenWidth - 1, newX));
            float newY = Constants.Y1 + event.getRawY() / primaryScreenHeight * (Constants.Y2 - Constants.Y1);
            newY = Math.max(0, Math.min(secondaryScreenHeight - 1, newY));
            event.setLocation(newX, newY);
//            todo：弄清为什么source（0x5002，类中都没有定义）为什么不等于SOURCE_TOUCHSCREEN（4098），是不是含义不等的原因？
//            assert event.getSource() == InputDevice.SOURCE_TOUCHSCREEN;
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            event.setDisplayId(secondaryDisplayId);

            InputCenter.unifiedDispatchMotionEvent(event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mScreenMirrorServiceConnection);
        isScreenMirrorServiceBound = false;
        unbindService(mHonghuKeysServiceConnection);
        isHonghuKeysServiceBound = false;
//        放开home键
        mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v(TAG, String.format("onKeyDown, keyCode: %d", keyCode));
        return true;
    }


}
