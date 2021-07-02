package com.luci.pano_touch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;

import com.android.internal.util.ArrayUtils;
import com.luci.pano_touch.screen_mirror.TrackBallBean;
import com.luci.pano_touch.R;
import com.luci.pano_touch.databinding.SecondaryInputCenterLayoutBinding;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * ACTION_DOWN: 0, ACTION_UP: 1, ACTION_POINTER_DOWN: 5, ACTION_POINTER_UP: 6
 * ACTION_MOVE: 2
 */
public class InputCenter {
    private final static String TAG = "InputCenter";

    /**
     * 每一个事件都要修改
     */
//    第一个触摸点的时间
    static long downTime = 0;
    //    当前分发的动作
    static int action = MotionEvent.ACTION_DOWN;
    //    指针个数
    static int sPointerCount = 0;
    //    指针属性数组
    static ArrayList<PointerProperties> sPropertiesList = new ArrayList<>();

    //    指针坐标数组
    static ArrayList<PointerCoords> sCoordsList = new ArrayList<>();

    //    记录每个id的指针是否按下，默认都为否
    static boolean[] touchIds = new boolean[Constants.MAX_POINTERS_COUNT];
    static boolean isFinished = true;

    // track ball
    static int mTrackBallCount = 5;
    static TrackBallBean[] mTrackBallBeans;
    static Runnable[] mTrackBallRunnableList;
    static Handler sHandler;
    static SecondaryInputCenterLayoutBinding sBinding;
    /**
     * 初始化需要修改
     */
//    副屏ID（初始化为主屏）
    static int displayId = 0;
    //    todo: 设备id暂记为0，应该没问题
    static int deviceId = 0;

    /**
     * 不用修改
     */
    static final int metaState = 0;
    static final int buttonState = 0;
    static final float xPrecision = Constants.X_PRECISION;
    static final float yPrecision = Constants.Y_PRECISION;
    static final int edgeFlags = 0;
    static int source = InputDevice.SOURCE_TOUCHSCREEN;
    static final int flags = 0;

    static InputManager sInputManager;
    static DisplayManager sDisplayManager;
    static WindowManager sWindowManager;
    static ViewGroup rootView;
    static ImageView sImageView;
    public static Context sContext;

    public static boolean STRICT_MODE = false;

    public static void init(Context context) {
        if (sContext == null) {
            sContext = context;
        }
        if (sInputManager == null) {
            sInputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        }
        if (sDisplayManager == null) {
            sDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] mDisplays = sDisplayManager.getDisplays();
            Display mDisplay = mDisplays[mDisplays.length - 1];
            // todo: 验证sContext 和 context关系
            sContext = sContext.createDisplayContext(mDisplay);
            sWindowManager = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
            sHandler = new Handler(sContext.getMainLooper());
            mTrackBallRunnableList = new Runnable[mTrackBallCount];

            // 关键：要从副屏的context创建display，否则内容会显示在主屏，bug调了我半小时
            // InputCenter 只是显示轨迹，所以必定不需要交互，一定要加上NOT_FOCUSABLE 和 NOT_TOUCHABLE
            WindowManager.LayoutParams sLayoutParams = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT
            );

            //        在服务中使用databinding: https://stackoverflow.com/a/24580870/9422455
            LayoutInflater mInflater = LayoutInflater.from(new ContextThemeWrapper(sContext, R.style.Theme_AppCompat_Translucent));
            sBinding = DataBindingUtil.inflate(mInflater, R.layout.secondary_input_center_layout, null, false);
            rootView = (ViewGroup) sBinding.getRoot();
            sWindowManager.addView(rootView, sLayoutParams);

            mTrackBallBeans = new TrackBallBean[mTrackBallCount];
            for (int i = 0; i < mTrackBallCount; i++) {
                float x = 300 * (i + 1);
                float y = 300 * (i + 1);
                mTrackBallBeans[i] = new TrackBallBean(x, y);
                String trackBallName = "trackBall" + (i + 1);
                int trackBallId = sContext.getResources().getIdentifier(trackBallName, "id", sContext.getPackageName());
                Log.v(TAG, String.format("[trackBall] id: %d, name: %s, x: %.2f, y: %.2f",
                        trackBallId, trackBallName, x, y));
                /**
                 可以通过BR序号进行递推
                 **/
                boolean setResult = sBinding.setVariable(BR.trackBall + i + 1, mTrackBallBeans[i]);
                assert setResult;
            }
        }
    }

    private static void dispatch() {
        if (sInputManager == null) {
            Log.e(TAG, "InputCenter 还未初始化，请传入Context");
            return;
        }
        Display[] mDisplays = sDisplayManager.getDisplays();
        displayId = mDisplays[mDisplays.length - 1].getDisplayId();

//        source = InputDevice.SOURCE_TRACKBALL; // test track ball failed

        long now = SystemClock.uptimeMillis();
        MotionEvent mEvent = MotionEvent.obtain(
                downTime, now, action,
                sPointerCount,
                sPropertiesList.toArray(new PointerProperties[sPointerCount]),
                sCoordsList.toArray(new PointerCoords[sPointerCount]),
                metaState, buttonState, xPrecision, yPrecision,
                deviceId, edgeFlags, source, displayId, flags
        );

        sInputManager.injectInputEvent(mEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
//        sInputManager.injectInputEvent(mEvent, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
        Log.v(TAG, ">>> INJECT: " + mEvent.toString());
    }

    /**
     * 这个函数必须加锁
     *
     * @param motionEvent
     */
    @SuppressLint("DefaultLocale")
    public static synchronized void unifiedDispatchMotionEvent(MotionEvent motionEvent) {
        assert sContext != null :
                "InputCenter Not Initialized or Died";

        if (STRICT_MODE) {
            assert isFinished == true :
                    String.format("[NOT FINISHED] time: %d, motion: %s",
                            SystemClock.uptimeClock(), motionEvent.toString());
        }
        isFinished = false;

        action = motionEvent.getAction();

        if (action == 0 || (action & 5) == 5) {
            /**
             * action down
             */
            if (action == 0 && sPointerCount == 0) {
//                经更新后的action如果还是0，说明是第一次下压
                downTime = motionEvent.getDownTime();
            }

            int targetPointerSeq = action == 0 ? 0 : (action >> 8);
            int targetPointerId = motionEvent.getPointerId(targetPointerSeq);
            Log.v(TAG, String.format("action: %d, targetPointerSeq: %d, targetPointerId: %d",
                    action, targetPointerSeq, targetPointerId));
            if (STRICT_MODE) {
//                由于游戏模式下会自动将摇杆指针按下，所以再次重启时该无法通过按下无重复检测
                assert !touchIds[targetPointerId] : String.format("指针[%d]已按下", targetPointerId);
            }
            touchIds[targetPointerId] = true;

            action = sPointerCount == 0 ? 0 : sPointerCount * (1 << 8) + 5;  // 更改action行为
            sPointerCount++;

            PointerProperties mProperties = new PointerProperties();
            motionEvent.getPointerProperties(targetPointerSeq, mProperties);
            ArrayUtils.add(sPropertiesList, mProperties);

            PointerCoords mCoords = new PointerCoords();
            motionEvent.getPointerCoords(targetPointerSeq, mCoords);
            ArrayUtils.add(sCoordsList, mCoords);

            Log.v(TAG, String.format("[ADDED FINGER] pointerCount: (%d -> %d), actonId: %d, actionName: %s, targetPointerSeq: %d, targetPointerId: %d",
                    sPointerCount - 1, sPointerCount, action, MotionEvent.actionToString(action), targetPointerSeq, targetPointerId));
            dispatch();
        } else if (action == 1 || action == MotionEvent.ACTION_CANCEL || (action & 6) == 6) {
            /**
             * action up
             */
//            todo: 为什么现在主屏的抬起都变成CANCEL了？
            int targetPointerSeq = action == 1 ? 0 : action >> 8; //    额外的手指数目
            int targetPointerId = motionEvent.getPointerId(targetPointerSeq);
            if (STRICT_MODE) {
//                由于上述按下的原因，可能导致无法通过抬起非空检测
                assert touchIds[targetPointerId] :
                        String.format("指针[%d]当前并未按下", targetPointerId);
            }

            int targetPointerSeqInCenter =
                    IntStream.range(0, sPropertiesList.size())
                            .filter(i -> sPropertiesList.get(i).id == targetPointerId)
                            .findFirst().orElse(-1);
            assert targetPointerSeqInCenter != -1 :
                    String.format("要抬起的手指的id必须在`InputCenter`中");

            // 十分关键，需要基于id找到InputCenter中的seq，然后再抬起
            // 否则会出现：左键、下键同时按，然后抬起时，ACTION_PINTER_UP错误，导致方向盘失效
            action = sPointerCount == 1 ? 1 : targetPointerSeqInCenter * (1 << 8) + 6;
//            抬起时，肯定是`ACTION_UP`，所以不需要额外判断

            dispatch();

            Log.v(TAG, String.format("[REMOVING FINGER] pointerCount: (%d -> %d), actonId: %d, actionName: %s, targetPointerSeq: %d, targetPointerId: %d, targetPointerSeqInCenter: %d",
                    sPointerCount, sPointerCount - 1, action, MotionEvent.actionToString(action), targetPointerSeq, targetPointerId, targetPointerSeqInCenter));
            sPointerCount--;
            touchIds[targetPointerId] = false;
            sPropertiesList.remove(targetPointerSeqInCenter);
            sCoordsList.remove(targetPointerSeqInCenter);
        } else if (action == 2) {
            /**
             * action move
             */
            for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                boolean found = false;
                int targetPointerId = motionEvent.getPointerId(i);
                for (int j = 0; j < sPointerCount; j++) {
                    if (sPropertiesList.get(j).id == targetPointerId) {
                        motionEvent.getPointerCoords(i, sCoordsList.get(j));
                        found = true;
                        break;
                    }
                }
                if (STRICT_MODE) {
                    assert found :
                            String.format("未找到`id=%d`的指针进行移动，\nmotion_from: %s\nmotion_to  : %s",
                                    targetPointerId, motionEvent.toString(), dumpState());
                } else {

                }

            }
            dispatch();
        } else {

            /**
             * others
             */
            Log.e(TAG, "未定义行为");
        }

        /**
         * 显示轨迹
         */
        showMotionOnSecondaryDisplay(motionEvent);

        isFinished = true;
    }

    public static boolean isActionDown(int action) {
        return action == 0 || (action & 5) == 5;
    }

    public static boolean isActionUp(int action) {
        return action == 1 || (action & 6) == 6;
    }

    @SuppressLint("DefaultLocale")
    public static String dumpState() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("downTime: %d, ", downTime));
        s.append(String.format("pointerCount: %d, ", sPointerCount));
        s.append(String.format("action: (%d, %s), ", action, MotionEvent.actionToString(action)));
        for (int i = 0; i < sPointerCount; i++) {
            s.append(String.format("id[%d], x: %.2f, y: %.2f, ",
                    sPropertiesList.get(i).id, sCoordsList.get(i).x, sCoordsList.get(i).y));
        }
        return s.toString();
    }

    public static void clear() {
        sPointerCount = 0;
        sPropertiesList.clear();
        sCoordsList.clear();
        touchIds = new boolean[Constants.MAX_POINTERS_COUNT];
    }

    public static void showMotionOnSecondaryDisplay(MotionEvent motionEvent) {
        for (int i = 0; i < Math.min(motionEvent.getPointerCount(), mTrackBallCount); i++) {
//            重要：这里不可以使用rawX、rawY，要用coords
//            float x = motionEvent.getRawX(i);
//            float y = motionEvent.getRawY(i);
            MotionEvent.PointerCoords mCoords = new MotionEvent.PointerCoords();
            motionEvent.getPointerCoords(i, mCoords);
            mTrackBallBeans[i].x.set(mCoords.x);
            mTrackBallBeans[i].y.set(mCoords.y);
            mTrackBallBeans[i].vis.set(true);
            if (mTrackBallRunnableList[i] != null) {
                sHandler.removeCallbacks(mTrackBallRunnableList[i]);
            }
            int finalI = i;
            mTrackBallRunnableList[i] = () -> {
                mTrackBallBeans[finalI].vis.set(false);
            };
            sHandler.postDelayed(mTrackBallRunnableList[i], 1000);
//            Log.v(TAG, String.format("[TOUCH TRACK] i: %d, pos: (%.2f, %.2f), real: (%.2f, %.2f)",
//                    i, x, y, mCoords.x, mCoords.y));
        }
    }

}
