package com.luci.pano_touch.honghu_gamepad;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.luci.pano_touch.Constants;
import com.luci.pano_touch.Utils;


public class HonghuJoystickBean extends HonghuKeyBean {
    private final static String TAG = "HonghuTouchBean";

    //    控制是否按下，基于此决定每次事件的行为
    public boolean isPressed = false;
    private long downTime;
    private int lastJoystickX, lastJoystickY;
    private final static int JOYSTICK_SCREEN_RADIUS = 100;
    private final MotionEvent.PointerCoords[] mCoords = MotionEvent.PointerCoords.createArray(1);
    private final MotionEvent.PointerProperties[] mProperties = MotionEvent.PointerProperties.createArray(1);

    private final static int deviceId = 0;      // deviceId 可以不用管
    private final static int displayId = 0;     // displayId 在 dispatch 的时候会自动处理，所以也不用管

    HonghuJoystickBean(Context context, String spName, String s, int code, int id, int x, int y, String icon, boolean reset) {
        super(context, spName, s, code, id, x, y, icon, reset);
        mProperties[0].id = id;
        mProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
        Log.v(TAG, String.format("初始化 JoystickBean of [%s] 完成", s));
    }

    public void dispatchJoystickMotionEvent(int x, int y) {
        Log.d(TAG, String.format("joystick %s received (%d, %d)", s, x, y));
        if (isFinal(x, y)) {
            if (isPressed) {
                Log.v(TAG, "终态且压下，则抬起");
                dispatchMotionEvent(x, y, MotionEvent.ACTION_UP);
                isPressed = false;
            } else {
                Log.v(TAG, " 终态且未压下（可能是抖动），则忽略");
            }
        } else {
//            非终态
            if (!isPressed) {
                Log.v(TAG, " 非终态，且未按下，则按下");
                downTime = SystemClock.uptimeMillis();
                dispatchMotionEvent(x, y, MotionEvent.ACTION_DOWN);
                isPressed = true;
            }
            Log.v(TAG, "已经按下，则移动");
            int N = 5;
            for (int i = 1; i <= N; i++) {
                float x1 = Utils.lerp(lastJoystickX, x, (float) i / N);
                float y1 = Utils.lerp(lastJoystickY, y, (float) i / N);
                dispatchMotionEvent(x1, y1, MotionEvent.ACTION_MOVE);
            }
        }
        lastJoystickX = x;
        lastJoystickY = y;
    }


    private boolean isFinal(int x, int y) {
        return (x == 7 || x == 8) && (y == 7 || y == 8);
    }

    private void dispatchMotionEvent(float x, float y, int action) {
        float N = 15;
        mCoords[0].x = (float) (this.x.get() + (x - N / 2) / N * 2 * JOYSTICK_SCREEN_RADIUS);
        mCoords[0].y = (float) (this.y.get() + -(y - N / 2) / N * 2 * JOYSTICK_SCREEN_RADIUS);
        MotionEvent mEvent = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), action,
                1, mProperties, mCoords, 0, 0,
                Constants.X_PRECISION, Constants.Y_PRECISION,
                deviceId, 0, InputDevice.SOURCE_TOUCHSCREEN, displayId, 0
        );
        dispatch(mEvent);
    }
}
