package com.luci.pano_touch.honghu_gamepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;

import com.luci.pano_touch.Constants;

public class HonghuKeyBean {
    private final static String TAG = "HonghuKeyBean";
    //    整个按键系统持久化的文件名（SharedPreference）
    public String spName;
    // 键名
    public String s;
    //    按键码
    public int code;
    //    按键映射到副屏的id，一定要与目前触屏系统的id区分开（用panogo mini默认就可）
    public int id;
    //    按键映射到副屏的图标
    public ObservableField<String> icon;
    //    按键映射到副屏对应的坐标
    public ObservableFloat x, y;
    //    按键在副屏是否可见
    public ObservableBoolean visible;
    //    按键在副屏的大小
    public float keyWidth, keyHeight;

    protected Context mContext;
    protected SharedPreferences sp;

    public View.OnTouchListener onTouchListener;


    HonghuKeyBean(Context context, String spName, String s, int code, int id, int x, int y, String icon, boolean reset) {
        init(context, spName, s, code, id, x, y, Constants. JOYSTICK_SCREEN_RADIUS, Constants.JOYSTICK_SCREEN_RADIUS, icon, reset);
    }

    private void init(Context context, String spName, String s, int code, int id, int x, int y, float w, float h, String icon, boolean reset) {
        mContext = context;
        this.spName = spName;
        this.code = code;
        this.id = id;
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mContext.getDisplay().getMetrics(mDisplayMetrics);

//        dp -> pixel
        float dpRatio = context.getResources().getDisplayMetrics().density;
        keyWidth = w * dpRatio;
        keyHeight = h * dpRatio;
        sp = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        this.s = s;
        this.x = new ObservableFloat(reset ? x : sp.getInt(s + "_x", x));
        this.y = new ObservableFloat(reset ? y : sp.getInt(s + "_y", y));
        this.icon = new ObservableField<>(reset ? icon : sp.getString(s + "_icon", icon));
        this.visible = new ObservableBoolean(reset || sp.getBoolean(s + "_visible", true));
        save();
        this.registerOnTouchListener();
    }

    class HonghuKeyOnTouchListener implements View.OnTouchListener {
        private float lastRealX, lastRealY;
        private float lastRawX, lastRawY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            String es = MotionEvent.actionToString(e.getAction());
            Log.v(TAG, String.format("key: %s, event type: %s", s, es));

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastRealX = x.get();
                    lastRealY = y.get();
                    lastRawX = e.getRawX();
                    lastRawY = e.getRawY();
                    Log.v(TAG, String.format("key: (%s, %.2f, %.2f), touch: (%.2f, %.2f), dim: (%d, %d)",
                            s, x.get(), y.get(), lastRawX, lastRawY, (int) keyWidth, (int) keyHeight));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float newRealX = e.getRawX() - lastRawX + lastRealX;
                    float newRealY = e.getRawY() - lastRawY + lastRealY;
                    x.set(newRealX);
                    y.set(newRealY);
                    SharedPreferences.Editor mEditor = sp.edit();
                    mEditor.putInt(s + "_x", (int) newRealX);
                    mEditor.putInt(s + "_y", (int) newRealY);
                    mEditor.apply();
                    Log.i(TAG, String.format("key: %s, pos: (%.2f, %.2f) -> (%.2f, %.2f), touch: (%.2f, %.2f) -> (%.2f, %.2f)",
                            s, lastRawX, lastRawY, e.getRawX(), e.getRawY(), lastRealX, lastRealY, newRealX, newRealY));
                    lastRealX = newRealX;
                    lastRealY = newRealY;
                    lastRawX = e.getRawX();
                    lastRawY = e.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public void registerOnTouchListener() {
        this.onTouchListener = new HonghuKeyOnTouchListener();
    }

    public void unregisterOnTouchListener() {
        this.onTouchListener = (e, v) -> false;
    }

    private void save() {
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putInt(s + "_x", (int) x.get());
        mEditor.putInt(s + "_y", (int) y.get());
        mEditor.putBoolean(s + "_visible", visible.get());
        mEditor.apply();
    }

    public void dump() {
        Log.v(TAG, String.format("preference of %s:" +
                        "position: (%.2f, %.2f), dimension: (%.2f, %.2f), visible: %b",
                s, x.get(), y.get(), keyWidth, keyHeight, visible.get()));
    }

    public void dispatch(MotionEvent motionEvent) {
        ((HonghuKeysService) mContext).unifiedDispatchMotionEvent(motionEvent);
    }

}



