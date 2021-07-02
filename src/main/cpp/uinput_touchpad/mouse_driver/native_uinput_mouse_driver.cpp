#include <jni.h>
#include <string>
#include "android/log.h"
#include "mouse_driver.h"

MouseDriver mouseDriver = MouseDriver();

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_nativeMouseMove(JNIEnv *env, jobject thiz, jint x, jint y) {
    mouseDriver.move(x, y);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_leftClick(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "touchpad jni", "left click");
    mouseDriver.leftClick();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_rightClick(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "touchpad jni", "right click");
    mouseDriver.rightClick();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_nativeMouseLeftDown(JNIEnv *env, jobject thiz) {
    mouseDriver.leftDown();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_nativeMouseLeftUp(JNIEnv *env, jobject thiz) {
    mouseDriver.leftUp();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_luci_pano_1touch_touchpad_TouchpadActivity_nativeMouseScroll(JNIEnv *env, jobject thiz, int x, int y) {
    mouseDriver.scroll(x, y);
    return 0;
}
