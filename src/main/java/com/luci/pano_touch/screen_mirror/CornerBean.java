package com.luci.pano_touch.screen_mirror;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableFloat;

import com.luci.pano_touch.Constants;

public class CornerBean {

    private final static String TAG = "CornerBean";

    private Context mContext;

    //    点的坐标
    public ObservableFloat x;
    public ObservableFloat y;

    //    方便用户控制可见
    public ObservableBoolean vis;

    public String name;

    public final int bgResourceId;

    //    点的邻接点，adjX表示x轴相同（同条纵线），adjY表示y轴相同（同条横线）
    private CornerBean adjX, adjY;

    //    用于移动跟踪
    public View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            /**
             *  x = touchX - [offsetX] + radius
             */
            float rawX = motionEvent.getRawX(), rawY = motionEvent.getRawY();
            Log.v(TAG, String.format("touch pos: (%.2f, %.2f)", rawX, rawY));

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    offsetX = motionEvent.getX();
                    offsetY = motionEvent.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = rawX - offsetX + Constants.CORNER_RADIUS;
                    float newY = rawY - offsetY + Constants.CORNER_RADIUS;
                    updateX(newX);
                    updateY(newY);
                    Log.v(TAG, String.format("move to: (%.2f, %.2f), new pos: (%.2f, %.2f)", newX, newY, x.get(), y.get()));
                    break;

                case MotionEvent.ACTION_UP:
//                    todo: update constants
                    break;

                default:
                    break;
            }
            return false;
        }
    };

    private float offsetX, offsetY;


    CornerBean(Context context, String name, float x, float y, int bgResourceId) {
        mContext = context;
        this.name = name;
        this.x = new ObservableFloat(x);
        this.y = new ObservableFloat(y);
        this.vis = new ObservableBoolean(true);
        this.bgResourceId = bgResourceId;
        /**
         *  x = touchX - [offsetX] + radius
         */
    }

    public void initAdj(CornerBean adjX, CornerBean adjY) {
        this.adjX = adjX;
        this.adjY = adjY;
    }

    public void updateX(float x) {
        this.x.set(x);
//        同步更新x轴相同的点的x轴坐标
        adjX.x.set(x);
    }

    public void updateY(float y) {
        this.y.set(y);
//        同步更新y轴相同的点的y轴坐标
        adjY.y.set(y);
    }
}
