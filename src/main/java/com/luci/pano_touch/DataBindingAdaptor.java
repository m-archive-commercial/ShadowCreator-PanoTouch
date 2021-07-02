package com.luci.pano_touch;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.databinding.BindingAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DataBindingAdaptor {

    private final static String TAG = "DataBindingAdaptor";

//    @BindingAdapter("set_x")
//    public static void setX(FloatingActionButton view, float x) {
////        float dpRatio = view.getContext().getResources().getDisplayMetrics().density;
//        RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        mLayoutParams.leftMargin = (int) (x - (float) view.getWidth() / 2);
//        view.setLayoutParams(mLayoutParams);
//        Log.v(TAG, String.format("setting pos left: %d", mLayoutParams.leftMargin));
//    }
//
//    @BindingAdapter("set_y")
//    public static void setY(FloatingActionButton view, float y) {
////        float dpRatio = view.getContext().getResources().getDisplayMetrics().density;
//        RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        mLayoutParams.topMargin = (int) (y - (float) view.getHeight() / 2);
//        view.setLayoutParams(mLayoutParams);
//        Log.v(TAG, String.format("setting pos top: %d", mLayoutParams.topMargin));
//    }


    @BindingAdapter("android:set_icon")
    public static void setIcon(FloatingActionButton view, String icon) {
//        不加这个下面的容易报错
        if (icon == null) return;
//      如何从string获得drawable  https://stackoverflow.com/a/13351262/9422455
        Context mContext = view.getContext();
        int drawableId = mContext.getResources().getIdentifier(icon, "drawable", mContext.getPackageName());
//        Log.v(TAG, String.format("set bg icon of id: %d from string %s", drawableId, icon));
        view.setImageIcon(Icon.createWithResource(mContext, drawableId));
    }

//    @BindingAdapter("set_horizontal_bias")
//    public static void setHorizontalBias(View view, int left) {
//        ConstraintLayout.LayoutParams mLayoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
//        mLayoutParams.horizontalBias = (float) (left - Constants.KEY_BTN_WIDTH / 2) / Constants.SCREEN_RANGE_BASE;
////        Log.v(TAG, String.format("horizontal value: %d, bias: %.2f", left, mLayoutParams.horizontalBias));
//        view.setLayoutParams(mLayoutParams);
//    }
//
//    @BindingAdapter("set_vertical_bias")
//    public static void setVerticalBias(View view, int top) {
//        ConstraintLayout.LayoutParams mLayoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
//        mLayoutParams.verticalBias = (float) (top - Constants.KEY_BTN_HEIGHT / 2) / Constants.SCREEN_RANGE_BASE;
////        Log.v(TAG, String.format("vertical value: %d, bias: %.2f", top, mLayoutParams.verticalBias));
//        view.setLayoutParams(mLayoutParams);
//    }

    @BindingAdapter("android:listen_touch")
    public static void handleTouchMotionEvent(View self, View.OnTouchListener onTouchListener) {
        self.setOnTouchListener(onTouchListener);
    }

//    @BindingAdapter("listen_drag")
//    public static void handleDragMotionEvent(View self, View.OnDragListener onDragListener) {
//        self.setOnDragListener(onDragListener);
//    }

    @BindingAdapter(value = {"android:posX", "android:posY", "android:posOffset"}, requireAll = true)
    public static void setPos2(View view, float x, float y, boolean offset) {
        RelativeLayout.LayoutParams mParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (offset) {
            x -= Constants.CORNER_RADIUS;
            y -= Constants.CORNER_RADIUS;
        }
        mParams.leftMargin = (int) x;
        mParams.topMargin = (int) y;
        Log.v(TAG, String.format("set pos of view#%d: (%d, %d)", view.getId(), mParams.leftMargin, mParams.topMargin));
        view.setLayoutParams(mParams);

    }

    @BindingAdapter("android:set_visible")
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @BindingAdapter(value = {"android:x1", "android:x2", "android:y1", "android:y2"}, requireAll = true)
    public static void setPos4(View view, float x1, float x2, float y1, float y2) {
        RelativeLayout.LayoutParams mParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        int x = (int) x1, y = (int) y1, h = (int) (y2 - y1), w = (int) (x2 - x1);
        mParams.leftMargin = x;
        mParams.topMargin = y;
        mParams.width = w;
        mParams.height = h;
        view.setLayoutParams(mParams);
        Log.v(TAG, String.format("x: %d, y: %d, w: %d, h: %d", x, y, w, h));
    }

    @BindingAdapter("android:setOnTouchListener")
    public static void setOnTouchListener(View view, View.OnTouchListener onTouchListener) {
        view.setOnTouchListener(onTouchListener);
        Log.v(TAG, String.format("set onTouchListener on view#%d", view.getId()));
    }

    @BindingAdapter("android:setBgImage")
    public static void setBgImage(ImageView view, int resourceId) {
        view.setImageResource(resourceId);
    }

}
