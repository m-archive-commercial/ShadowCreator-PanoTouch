package com.luci.pano_touch.screen_mirror;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;


import com.luci.pano_touch.Constants;
import com.luci.pano_touch.R;
import com.luci.pano_touch.databinding.SecondaryScreenMirrorLayoutBinding;

public class ScreenMirrorService extends Service {
    private static final String TAG = "ScreenMirrorService";

    private final ScreenMirrorBinder mBinder = new ScreenMirrorBinder();
    private WindowManager sWindowManager;
    private WindowManager.LayoutParams sLayoutParams;
    private DisplayManager sDisplayManager;
    private Display sDisplay;
    private Context sContext;
    private SecondaryScreenMirrorLayoutBinding sBinding;
    ViewGroup rootView;
    private Button btnSaveMirror;
    private Handler sHandler;


    public class ScreenMirrorBinder extends Binder {
        public ScreenMirrorService getService() {
            return ScreenMirrorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initRootView() {
        //        在服务中使用databinding: https://stackoverflow.com/a/24580870/9422455
        LayoutInflater mInflater = LayoutInflater.from(new ContextThemeWrapper(sContext, R.style.Theme_AppCompat_Translucent));
        sBinding = DataBindingUtil.inflate(mInflater, R.layout.secondary_screen_mirror_layout, null, false);
        rootView = (ViewGroup) sBinding.getRoot();
        btnSaveMirror = sBinding.btnSaveMirror;
        btnSaveMirror.setOnClickListener(v -> {
            saveMirror();
        });

        /**
         * 初始化四个角落的布局
         */
        sBinding.setLT(new CornerBean(this, "LT", Constants.X1, Constants.Y1, R.drawable.frame_left_top));
        sBinding.setLB(new CornerBean(this, "LB", Constants.X1, Constants.Y2, R.drawable.frame_left_bottom));
        sBinding.setRT(new CornerBean(this, "RT", Constants.X2, Constants.Y1, R.drawable.frame_right_top));
        sBinding.setRB(new CornerBean(this, "RB", Constants.X2, Constants.Y2, R.drawable.frame_right_bottom));

        CornerBean mCornerLT = sBinding.getLT();
        CornerBean mCornerLB = sBinding.getLB();
        CornerBean mCornerRT = sBinding.getRT();
        CornerBean mCornerRB = sBinding.getRB();

        mCornerLT.initAdj(mCornerLB, mCornerRT);
        mCornerLB.initAdj(mCornerLT, mCornerRB);
        mCornerRT.initAdj(mCornerRB, mCornerLT);
        mCornerRB.initAdj(mCornerRT, mCornerLB);

        /**
         * 创建布局，并展示在副屏
         */
        sWindowManager.addView(rootView, sLayoutParams);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display[] mDisplays = sDisplayManager.getDisplays();
        sDisplay = mDisplays[mDisplays.length - 1];
        Log.v(TAG, String.format("当前屏幕数量: %d, 副屏ID: %d", mDisplays.length, sDisplay.getDisplayId()));
        sContext = createDisplayContext(sDisplay);


        // 关键：要从副屏的context创建display，否则内容会显示在主屏，bug调了我半小时
        sWindowManager = (WindowManager) sContext.getSystemService(WINDOW_SERVICE);
        sLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        initRootView();
        saveMirror();
        Log.d(TAG, "[ScreenMirrorService] 初始化成功");
    }

    public void saveMirror() {
        Log.v(TAG, "saving mirror");
        sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        ;
        if (sWindowManager == null) return;
        sWindowManager.updateViewLayout(rootView, sLayoutParams);
        btnSaveMirror.setVisibility(View.INVISIBLE);
    }

    public void triggerUpdateMirror() {
        sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        sWindowManager.updateViewLayout(rootView, sLayoutParams);
        btnSaveMirror.setVisibility(View.VISIBLE);
    }

}
