#include <jni.h>
#include <string>
#include <iostream>
#include "HandShankHelper.h"

//#include <android/log.h>
//#define LOG_TAG "HandShankHelper.h"
//#define  ALOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

handshank_client_helper_t *me;


jobject mObject;
static JavaVM *g_vm = nullptr;

jint JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

JNIEnv *GetJniEnv() {
    JNIEnv *env;
    jint result = g_vm->AttachCurrentThread(&env, nullptr);
    return result == JNI_OK ? env : nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_wtk_factory_handle_JINHandleManager_create(
        JNIEnv *env,
        jobject obj/* this */) {
    me = HandShankClient_Create();
    mObject = env->NewGlobalRef(obj);
}

extern "C" JNIEXPORT void JNICALL
Java_com_wtk_factory_handle_JINHandleManager_start(
        JNIEnv *env,
        jobject /* this */) {
    HandShankClient_Start(me);
//setCallback();
}

extern "C" JNIEXPORT void JNICALL
Java_com_wtk_factory_handle_JINHandleManager_stop(
        JNIEnv *env,
        jobject /* this */) {
    HandShankClient_Stop(me);
}

extern "C" JNIEXPORT void JNICALL
Java_com_wtk_factory_handle_JINHandleManager_destroy(
        JNIEnv *env,
        jobject /* this */) {
    HandShankClient_Destroy(me);
    env->DeleteGlobalRef(mObject);
    mObject = nullptr;
    me = nullptr;

}

//HandShankClient_RegisterKeyEventCB  ------------------按键回调方法

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeCreateServer(JNIEnv *env,
                                                                                    jobject thiz) {
    ALOGD("creating joystick server…");
    Java_com_wtk_factory_handle_JINHandleManager_create(env, thiz);
    ALOGD("created joystick server…");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeStartServer(JNIEnv *env,
                                                                                    jobject thiz) {
    ALOGD("starting joystick server…");
    Java_com_wtk_factory_handle_JINHandleManager_start(env, thiz);
    ALOGD("started joystick server.");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeStopServer(JNIEnv *env,
                                                                                                jobject thiz) {
    ALOGD("stopping joystick server…");
    Java_com_wtk_factory_handle_JINHandleManager_stop(env, thiz);
    ALOGD("stopped joystick server…");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeDestroyServer(JNIEnv *env,
                                                                                                jobject thiz) {
    ALOGD("destroying joystick server…");
    Java_com_wtk_factory_handle_JINHandleManager_destroy(env, thiz);
    ALOGD("destroyed joystick server.");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeRegisterKeyEvent(JNIEnv *env,
                                                                                                jobject thiz) {

    jclass cls = env->GetObjectClass(thiz);
    jmethodID callback = env->GetMethodID(cls, "handleKeyEvent", "(III)V");

    HandShankClient_RegisterKeyEventCB(
            me, [callback](int a, int b, u_int8_t c) -> void {
                if (mObject) {
                    JNIEnv *jniEnv = GetJniEnv();
                    jniEnv->CallVoidMethod(mObject, callback, a, b, c);
                }
            });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_luci_pano_1touch_honghu_1gamepad_HonghuKeysService_nativeRegisterTouchEvent(JNIEnv *env,
                                                                                                  jobject thiz) {

    jclass cls = env->GetObjectClass(thiz);
    jmethodID callback = env->GetMethodID(cls, "handleTouchEvent", "(III)V");

    HandShankClient_RegisterTouchEventCB(
            me, [callback](int a, int b, u_int8_t c) -> void {
                if (mObject) {
                    JNIEnv *jniEnv = GetJniEnv();
                    jniEnv->CallVoidMethod(mObject, callback, a, b, c);
                }
            });
}
