//package com.markshawn.pano_touch.screen_mirror;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.PixelFormat;
//import android.os.Binder;
//import android.os.IBinder;
//import android.view.ContextThemeWrapper;
//import android.view.Display;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.Button;
//
//import androidx.databinding.DataBindingUtil;
//
//import com.markshawn.pano_touch.R;
//import Utils;
//
//public class ScreenMirrorServiceBackup extends Service {
//    private static final String TAG = "ScreenMirrorService";
//
//    private final ScreenMirrorBinder mBinder = new ScreenMirrorBinder();
//
//    public class ScreenMirrorBinder extends Binder {
//        public ScreenMirrorServiceBackup getService() {
//            return ScreenMirrorServiceBackup.this;
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//
//    private static WindowManager sWindowManager;
//    private static final WindowManager.LayoutParams sLayoutParams = new WindowManager.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT
//    );
//    private Button btnSaveMirror;
//    private View rootView;
//
//    @Override
//    public void onCreate() {
//        Display mSecondaryDisplay = Utils.getSecondaryDisplay(this);
//        Context mSecondaryContext = createDisplayContext(mSecondaryDisplay);
//        sWindowManager = (WindowManager) mSecondaryContext.getSystemService(WINDOW_SERVICE);
////        在服务中使用databinding: https://stackoverflow.com/a/24580870/9422455
//        LayoutInflater mInflater = LayoutInflater.from(new ContextThemeWrapper(mSecondaryContext, R.style.Theme_AppCompat_Translucent));
//        com.markshawn.pano_touch.databinding.FloatFrameBinding mSBinding = DataBindingUtil.inflate(mInflater, R.layout.screen_mirror_secndary_layout, null, false);
//        rootView = mSBinding.getRoot();
//        btnSaveMirror = mSBinding.btnSaveMirror;
//        btnSaveMirror.setOnClickListener(v -> {
//            saveMirror();
//        });
//
//        mSBinding.setLT(new CornerBean(this, "LT", Constants.X1, Constants.Y1, R.drawable.frame_left_top));
//        mSBinding.setLB(new CornerBean(this, "LB", Constants.X1, Constants.Y2, R.drawable.frame_left_bottom));
//        mSBinding.setRT(new CornerBean(this, "RT", Constants.X2, Constants.Y1, R.drawable.frame_right_top));
//        mSBinding.setRB(new CornerBean(this, "RB", Constants.X2, Constants.Y2, R.drawable.frame_right_bottom));
//
//        CornerBean mCornerLT = mSBinding.getLT();
//        CornerBean mCornerLB = mSBinding.getLB();
//        CornerBean mCornerRT = mSBinding.getRT();
//        CornerBean mCornerRB = mSBinding.getRB();
//
//        mCornerLT.initAdj(mCornerLB, mCornerRT);
//        mCornerLB.initAdj(mCornerLT, mCornerRB);
//        mCornerRT.initAdj(mCornerRB, mCornerLT);
//        mCornerRB.initAdj(mCornerRT, mCornerLB);
//
//        sWindowManager.addView(rootView, sLayoutParams);
//
////        todo: test
//        saveMirror();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        sWindowManager.removeView(rootView);
//    }
//
//    public void saveMirror() {
//        sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//        ;
//        sWindowManager.updateViewLayout(rootView, sLayoutParams);
//        btnSaveMirror.setVisibility(View.INVISIBLE);
//
////        Intent mIntent = new Intent(this, ScreenMirrorActivity.class);
////        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        Log.v(TAG, String.format("starting activity of SCREEN MIRROR"));
////        startActivity(mIntent);
//    }
//
//    public void triggerUpdateMirror() {
//        sLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        sWindowManager.updateViewLayout(rootView, sLayoutParams);
//        btnSaveMirror.setVisibility(View.VISIBLE);
//    }
//
//}
