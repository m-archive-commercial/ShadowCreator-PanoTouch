package com.luci.pano_touch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

public abstract class BaseFloatService extends Service {
    private static final String TAG = "BaseFloatService";

    private final LocalBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BaseFloatService getService() {
            return BaseFloatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected WindowManager sWindowManager;
    protected final WindowManager.LayoutParams sLayoutParams = new WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
    );
    protected Display sDisplay;
    protected Context sContext;
    protected ViewGroup rootView;

    protected abstract void initRootView();

    @Override
    public void onCreate() {
        sDisplay = Utils.getSecondaryDisplay(this);
        if(sDisplay.getDisplayId() == Display.DEFAULT_DISPLAY) {
            Log.e(TAG, "该服务仅可在多屏幕模式下启动");
            return;
        }
        sContext = createDisplayContext(sDisplay);
        Log.v(TAG, sContext.toString());
        sWindowManager = (WindowManager) sContext.getSystemService(WINDOW_SERVICE);
        initRootView();
        sWindowManager.addView(rootView, sLayoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rootView.isAttachedToWindow()) {
            sWindowManager.removeView(rootView);
        }
    }
}
