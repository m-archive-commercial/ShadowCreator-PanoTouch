//
// Created by 南川 on 2021/6/1.
//

#include "string"
#include "fcntl.h"
#include "../../utils.h"
#include "mouse_driver.h"

const char *DRIVER_NAME = "mark_mouse";
const char *UINPUT_PATH = "/dev/uinput";

int DRIVER_INITIALIZED_OK = 1;
int DRIVER_INITIALIZE_FAILED_FOR_CANT_OPEN_UINPUT = -1;
int DRIVER_INITIALIZE_FAILED_FOR_CANT_WRITE_UINPUT = -2;
int DRIVER_INITIALIZE_NOT_YET = -10;

#define DEBUG 1

/**
 * 核心参考：https://www.kernel.org/doc/html/v4.12/input/uinput.html#examples
 */


MouseDriver::~MouseDriver() {
    if (fd <= 0) return;
//        释放驱动，否则在 /sys/devices/virtual/input 、 /sys/class/input 、 /dev/input 下会有越来越多的驱动信息
    ioctl(fd, UI_DEV_DESTROY);
    close(fd);
}

MouseDriver::MouseDriver() {
    init();
}

int MouseDriver::init() {
    if (fd > 0) {
//        方便无限调用
        return DRIVER_INITIALIZED_OK;
    }
    LOGV(DRIVER_NAME, "try to initialize mouse driver…");
//        打开uinput文件，如果失败，一般都是没有设置打开权限
    fd = open(UINPUT_PATH, O_WRONLY | O_NDELAY);
    if (fd < 0) {
        LOGE(DRIVER_NAME, "failed to open %s", UINPUT_PATH);
        LOGE(DRIVER_NAME, "please grant permission under root using: `chmod 777 %s`", UINPUT_PATH);
        return DRIVER_INITIALIZE_FAILED_FOR_CANT_OPEN_UINPUT;
    }

//        设置驱动信息
    struct uinput_user_dev driver{};
    strncpy(driver.name, DRIVER_NAME, strlen(DRIVER_NAME));
    driver.id.version = 1;
    driver.id.bustype = BUS_USB;

//        注册 event
    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    ioctl(fd, UI_SET_EVBIT, EV_REL);

//    注册扫描码
//    ioctl(fd, UI_SET_EVBIT, EV_MSC);

    ioctl(fd, UI_SET_KEYBIT, BTN_MIDDLE);
    ioctl(fd, UI_SET_KEYBIT, BTN_LEFT);
    ioctl(fd, UI_SET_KEYBIT, BTN_RIGHT);
    ioctl(fd, UI_SET_RELBIT, REL_X);
    ioctl(fd, UI_SET_RELBIT, REL_Y);
    ioctl(fd, UI_SET_RELBIT, REL_WHEEL);
    ioctl(fd, UI_SET_RELBIT, REL_HWHEEL);   //    横向滚动

    if (write(fd, &driver, sizeof(driver)) != sizeof(driver)) {
        close(fd);
        LOGE(DRIVER_NAME, "failed to write to %s", UINPUT_PATH);
        return DRIVER_INITIALIZE_FAILED_FOR_CANT_WRITE_UINPUT;
    }

//        setup and create, https://www.kernel.org/doc/html/v4.12/input/uinput.html
    ioctl(fd, UI_DEV_SETUP, &driver);
    ioctl(fd, UI_DEV_CREATE);
    LOGI(DRIVER_NAME, "created mouse driver successfully");
    return DRIVER_INITIALIZED_OK;
}


int MouseDriver::leftDown() {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }


    emit(fd, EV_MSC, MSC_SCAN, 589825);
    emit(fd, EV_KEY, BTN_LEFT, 1);
    emit(fd, 0, 0, 0);
    isLeftDown = true;
#if DEBUG
    LOGV(DRIVER_NAME, "left click down");
#endif
    return 0;
}

int MouseDriver::leftUp() {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }

    emit(fd, EV_MSC, MSC_SCAN, 589825);
    emit(fd, EV_KEY, BTN_LEFT, 0);
    emit(fd, EV_SYN, SYN_REPORT, 0);
    isLeftDown = false;
#if DEBUG
    LOGV(DRIVER_NAME, "left click up");
#endif
    return 0;
}

int MouseDriver::leftClick() {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }

    MouseDriver::leftDown();
    MouseDriver::leftUp();
    return 0;
}

int MouseDriver::rightClick() {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }

    emit(fd, EV_MSC, MSC_SCAN, 589825);
    emit(fd, EV_KEY, BTN_RIGHT, 1);
    emit(fd, EV_SYN, SYN_REPORT, 0);
#if DEBUG
    LOGV(DRIVER_NAME, "left click down");
#endif

    emit(fd, EV_MSC, MSC_SCAN, 589825);
    emit(fd, EV_KEY, BTN_RIGHT, 0);
    emit(fd, EV_SYN, SYN_REPORT, 0);
#if DEBUG
    LOGV(DRIVER_NAME, "left click up");
#endif
    return 0;
}

int MouseDriver::move(int x, int y) {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }

    emit(fd, EV_REL, REL_X, x);
    emit(fd, EV_REL, REL_Y, y);
    emit(fd, 0, 0, 0);
#if DEBUG
    LOGV(DRIVER_NAME, "moved relative (%d, %d)", x, y);
#endif
    return 0;
}

int MouseDriver::scroll(int x, int y) {
    if (init() < 0) {
        LOGE(DRIVER_NAME, "mouse driver not yet initialized");
        return DRIVER_INITIALIZE_NOT_YET;
    }

    if (x == 0 && y == 0 && isLeftDown) {
//        抬起
        leftUp();
    }

    if (y != 0) {
        int unitH = 1;
        int d = (y > 0 ? unitH : -unitH);
        LOGV(DRIVER_NAME, "scroll vertical: %d", d);
        emit(fd, EV_REL, REL_WHEEL, d);
        emit(fd, 0, 0, 0);
    }
    if (x != 0) {
//        if (!isLeftDown) {
//            leftDown();
//        }
//        int unitW = 20;
//        move(x > 0 ? unitW : -unitW, 0);
        int d = (x > 0 ? 1 : -1);
        LOGV(DRIVER_NAME, "scroll horizontal: %d", d);
        emit(fd, EV_REL, REL_HWHEEL, d);
        emit(fd, 0, 0, 0);
    }
    return 0;
}
