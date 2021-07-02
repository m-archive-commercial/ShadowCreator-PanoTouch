#include <jni.h>
#include <string>
#include "android/log.h"
#include "touchpad_driver.h"

TouchpadDriver touchpadDriver = TouchpadDriver();


extern "C"
JNIEXPORT jint JNICALL
Java_com_markshawn_touchpad_TouchpadModeActivity_nativeTouchpadSingleTouch(JNIEnv *env, jobject thiz) {
    touchpadDriver.testSingleTouch();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_markshawn_touchpad_TouchpadModeActivity_nativeTouchpadFingerDrag(JNIEnv *env, jobject thiz) {
    touchpadDriver.testFingerDrag();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_markshawn_touchpad_TouchpadModeActivity_nativeTouchpadInit(JNIEnv *env, jobject thiz) {
    return touchpadDriver.init();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_markshawn_touchpad_TouchpadModeActivity_nativeTouchpadDestroy(JNIEnv *env, jobject thiz) {
    touchpadDriver.destroy();
    return 0;
}

