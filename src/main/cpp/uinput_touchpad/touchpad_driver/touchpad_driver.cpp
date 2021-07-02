//
// Created by 南川 on 2021/6/13.
//

#include "touchpad_driver.h"
#include <linux/uinput.h>
#include <fcntl.h>
#include <unistd.h>
#include <cstring>
#include <cstdio>
#include <cmath>
#include "../../log.h"

#define TAG_TOUCHPAD_DRIVER "touchpad_driver"
#define DEBUG 0


int TouchpadDriver::init() {
    uinp_fd = open("/dev/uinput", O_WRONLY | O_NDELAY);
    if (uinp_fd <= 0) {
        LOGE(TAG_TOUCHPAD_DRIVER, "can not open uinput\n");
        return -1;
    }

    // configure touch device event properties
    memset(&uinp, 0, sizeof(uinp));
    strncpy(uinp.name, "uTouchpad", UINPUT_MAX_NAME_SIZE);
    uinp.id.version = 4;
    uinp.id.bustype = BUS_USB;
    uinp.absmin[ABS_MT_SLOT] = 0;
    uinp.absmax[ABS_MT_SLOT] = 3; // track up to 3 fingers
    uinp.absmin[ABS_MT_TOUCH_MAJOR] = 0;
    uinp.absmax[ABS_MT_TOUCH_MAJOR] = 15;
    uinp.absmin[ABS_MT_POSITION_X] = 0; // screen dimension
    uinp.absmax[ABS_MT_POSITION_X] = 1080; // screen dimension (根据panogo mini，x是纵坐标）
    uinp.absmin[ABS_MT_POSITION_Y] = 0; // screen dimension
    uinp.absmax[ABS_MT_POSITION_Y] = 1200; // screen dimension  （根据panogo mini，y是横坐标）
    uinp.absmin[ABS_MT_TRACKING_ID] = 0;
    uinp.absmax[ABS_MT_TRACKING_ID] = 65535;
    uinp.absmin[ABS_MT_PRESSURE] = 0;
    uinp.absmax[ABS_MT_PRESSURE] = 255;

// Setup the uinput device
    ioctl(uinp_fd, UI_SET_EVBIT, EV_KEY);
    ioctl(uinp_fd, UI_SET_EVBIT, EV_REL);

// Touch
    ioctl(uinp_fd, UI_SET_EVBIT, EV_ABS);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_SLOT);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_TOUCH_MAJOR);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_POSITION_X);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_POSITION_Y);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_TRACKING_ID);
    ioctl(uinp_fd, UI_SET_ABSBIT, ABS_MT_PRESSURE);
    ioctl(uinp_fd, UI_SET_PROPBIT, INPUT_PROP_DIRECT);
//    added
    ioctl(uinp_fd, UI_SET_EVBIT, BTN_TOUCH);

    if (write(uinp_fd, &uinp, sizeof(uinp)) != sizeof(uinp)) {
        close(uinp_fd);
        uinp_fd = -1;
        return uinp_fd;
    }

    if (ioctl(uinp_fd, UI_DEV_CREATE)) {
        close(uinp_fd);
        uinp_fd = -1;
        return uinp_fd;
    }
    return uinp_fd;

}

void TouchpadDriver::send_event(int type, int code, int value) {
    if (uinp_fd <= 0) {
        init();
        if (uinp_fd <= 0) {
            return;
        }
    }
    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);
    event.type = type;
    event.code = code;
    event.value = value;
    write(uinp_fd, &event, sizeof(event));
    usleep(500);
#if DEBUG
    LOGV(TAG_TOUCHPAD_DRIVER, "hhh test you shouldn't see it");
    LOGV(TAG_TOUCHPAD_DRIVER, "send event (%d, %d, %d)", type, code, value);
#endif
}

void TouchpadDriver::destroy() {
    LOGV(TAG_TOUCHPAD_DRIVER, "destroyed\n");
    if (uinp_fd > 0) {
        ioctl(uinp_fd, UI_DEV_DESTROY);
        close(uinp_fd);
        uinp_fd = -1;
    }
}

