//
// Created by 南川 on 2021/6/1.
//

#ifndef TOUCHPAD_UTILS_H
#define TOUCHPAD_UTILS_H

#include "linux/uinput.h"
#include "unistd.h"
#include "log.h"

#define DEBUG 0


void emit(int fd, int type, int code, int val) {
    struct input_event ie;

    ie.type = type;
    ie.code = code;
    ie.value = val;
    /* timestamp values below are ignored */
    ie.time.tv_sec = 0;
    ie.time.tv_usec = 0;

#if DEBUG
    LOGV("send event", "%d %d %d", type, code, val);
#endif
    write(fd, &ie, sizeof(ie));
}

#endif //TOUCHPAD_UTILS_H
