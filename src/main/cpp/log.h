//
// Created by 南川 on 2021/6/13.
//

#ifndef SHADOWCREATOR_LOG_H
#define SHADOWCREATOR_LOG_H

#include <android/log.h>
// https://blog.csdn.net/fh400/article/details/5675285
#define LOGV(tag, ...) __android_log_print(ANDROID_LOG_VERBOSE, tag, __VA_ARGS__)
#define LOGD(tag, ...) __android_log_print(ANDROID_LOG_DEBUG  , tag, __VA_ARGS__)
#define LOGI(tag, ...) __android_log_print(ANDROID_LOG_INFO   , tag, __VA_ARGS__)
#define LOGW(tag, ...) __android_log_print(ANDROID_LOG_WARN   , tag, __VA_ARGS__)
#define LOGE(tag, ...) __android_log_print(ANDROID_LOG_ERROR  , tag, __VA_ARGS__)

#endif //SHADOWCREATOR_LOG_H
