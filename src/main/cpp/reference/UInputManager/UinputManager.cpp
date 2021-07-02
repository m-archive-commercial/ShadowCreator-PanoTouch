#include <android/log.h>
#include "./UinputManager.h"

const char * MOUSE_DRIVER_NAME = "mouse driver";
const char * UINUPUT_PATH = "/dev/uinput";


UinputManager::UinputManager() : fd_mouse(-1), fd_keyboard(-1), width(-1), height(-1) {}

UinputManager::~UinputManager() = default;

int UinputManager::createKeyboardDevice() {
    struct uinput_user_dev uinp;
    fd_keyboard = open("/dev/uinput", O_WRONLY | O_NDELAY);
    if (fd_keyboard < 0) {
        return -1;
    }
    memset(&uinp, 0x00, sizeof(uinp));
    strncpy(uinp.name, "myKeyboard", strlen("myKeyboard"));
    uinp.id.version = 1;
    uinp.id.bustype = BUS_USB;
    /**
     * todo: vendor, product
     */
//	uinp.id.vendor = ***;
//	uinp.id.product = ***;

    uinp.absmin[ABS_HAT0X] = -1;
    uinp.absmax[ABS_HAT0X] = 1;
    uinp.absfuzz[ABS_HAT0X] = 0;
    uinp.absflat[ABS_HAT0X] = 0;

    uinp.absmin[ABS_HAT0Y] = -1;
    uinp.absmax[ABS_HAT0Y] = 1;
    uinp.absfuzz[ABS_HAT0Y] = 0;
    uinp.absflat[ABS_HAT0Y] = 0;
    ioctl(fd_keyboard, UI_SET_EVBIT, EV_KEY);
    ioctl(fd_keyboard, UI_SET_EVBIT, EV_ABS);


    for (int i = 1; i <= 11; i++) {
        ioctl(fd_keyboard, UI_SET_KEYBIT, i);
    }
    for (int i = 59; i < 255; i++) {
        ioctl(fd_keyboard, UI_SET_KEYBIT, i);
    }

    ioctl(fd_keyboard, UI_SET_KEYBIT, KEY_ENTER);
    ioctl(fd_keyboard, UI_SET_KEYBIT, 0x192);
    ioctl(fd_keyboard, UI_SET_KEYBIT, 0x193);

    ioctl(fd_keyboard, UI_SET_KEYBIT, KEY_SELECT);
    ioctl(fd_keyboard, UI_SET_ABSBIT, ABS_HAT0X);
    ioctl(fd_keyboard, UI_SET_ABSBIT, ABS_HAT0Y);

    if (write(fd_keyboard, &uinp, sizeof(uinp)) != sizeof(uinp)) {
        close(fd_keyboard);
        fd_keyboard = -1;
        return -1;
    }
    if (ioctl(fd_keyboard, UI_DEV_CREATE)) {
        close(fd_keyboard);
        fd_keyboard = -1;
        return -1;
    }
    return fd_keyboard;
}

int UinputManager::handleKey(int keycode, int status) {
    struct input_event event;
    int ret = -1;
    if (fd_keyboard < 0) {
        return -1;
    }
    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);
    switch (status) {
        case KEYUP:
            event.type = EV_KEY;
            event.code = keycode;
            event.value = 0;
            write(fd_keyboard, &event, sizeof(event));
            event.type = EV_SYN;
            event.code = 0;
            event.value = 0;
            ret = write(fd_keyboard, &event, sizeof(event));
            break;
        case KEYDOWN:
            event.type = EV_KEY;
            event.code = keycode;
            event.value = 1;
            write(fd_keyboard, &event, sizeof(event));
            event.type = EV_SYN;
            event.code = 0;
            event.value = 0;
            ret = write(fd_keyboard, &event, sizeof(event));
            break;
        default:
            break;

    }
    if (ret < 0) {
        close(fd_keyboard);
        fd_keyboard = -1;
        return -1;
    }
    return ret;
}

int UinputManager::createMouseDevice() {
    /**
     * finished
     */
    struct uinput_user_dev uinp{};
    fd_mouse = open(UINUPUT_PATH, O_WRONLY | O_NDELAY);
    __android_log_print(ANDROID_LOG_INFO, MOUSE_DRIVER_NAME, "open uinput result: %d", fd_mouse);
    if (fd_mouse <= 0) {
        __android_log_print(ANDROID_LOG_ERROR, MOUSE_DRIVER_NAME, "error open %s", UINUPUT_PATH);
        return -1;
    }
    memset(&uinp, 0x00, sizeof(uinp));
    strncpy(uinp.name, "myMouse", strlen("myMouse"));
    uinp.id.version = 1;
    uinp.id.bustype = BUS_USB;

    ioctl(fd_mouse, UI_SET_EVBIT, EV_KEY);
    ioctl(fd_mouse, UI_SET_EVBIT, EV_REL);
    ioctl(fd_mouse, UI_SET_KEYBIT, BTN_MIDDLE);
    ioctl(fd_mouse, UI_SET_KEYBIT, BTN_LEFT);
    ioctl(fd_mouse, UI_SET_KEYBIT, BTN_RIGHT);
    ioctl(fd_mouse, UI_SET_RELBIT, REL_X);
    ioctl(fd_mouse, UI_SET_RELBIT, REL_Y);

    if (write(fd_mouse, &uinp, sizeof(uinp)) != sizeof(uinp)) {
        close(fd_mouse);
        fd_mouse = -1;
        return fd_mouse;
    }

    if (ioctl(fd_mouse, UI_DEV_CREATE)) {
        close(fd_mouse);
        fd_mouse = -1;
        return fd_mouse;
    }
    return fd_mouse;
}

int UinputManager::handleMouseMove(int RelX, int RelY) {
    if (fd_mouse < 0) {
        __android_log_print(ANDROID_LOG_ERROR, MOUSE_DRIVER_NAME, "error open %s", UINUPUT_PATH);
        return -1;
    }
    __android_log_print(ANDROID_LOG_INFO, MOUSE_DRIVER_NAME, "received move action of (%d, %d)", RelX, RelY);

    int ret;
    static struct input_event ievent[3];
    static struct timespec now;
    float tv_nsec = 0;
    clock_gettime(CLOCK_MONOTONIC, &now);
    tv_nsec = now.tv_nsec / 1000;

    ievent[0].time.tv_sec = now.tv_sec;
    ievent[0].time.tv_usec = tv_nsec;
    ievent[0].type = EV_REL;
    ievent[0].code = REL_X;
    ievent[0].value = RelX;

    ievent[1].time.tv_sec = now.tv_sec;
    ievent[1].time.tv_usec = tv_nsec;
    ievent[1].type = EV_REL;
    ievent[1].code = REL_Y;
    ievent[1].value = RelY;

    ievent[2].time.tv_sec = now.tv_sec;
    ievent[2].time.tv_usec = tv_nsec;
    ievent[2].type = EV_SYN;
    ievent[2].code = 0;
    ievent[2].value = 0;

    write(fd_mouse, &ievent[0], sizeof(ievent[0]));
    write(fd_mouse, &ievent[1], sizeof(ievent[1]));
    ret = write(fd_mouse, &ievent[2], sizeof(ievent[2]));

    return ret;
}

int UinputManager::handleMouseKey(int keycode, int status) {

    if (fd_mouse < 0) {
        __android_log_print(ANDROID_LOG_ERROR, MOUSE_DRIVER_NAME, "not found mouse");
        return -1;
    }
    int ret = -1;
    struct input_event event;
    memset(&event, 0, sizeof(event));
    gettimeofday(&event.time, NULL);

    event.type = EV_KEY;
    event.code = keycode;
    event.value = status;
    write(fd_mouse, &event, sizeof(event));

    event.type = EV_SYN;
    event.code = 0;
    event.value = 0;
    ret = write(fd_mouse, &event, sizeof(event));

    return ret;
}

