//
// Created by 南川 on 2021/6/13.
//

#ifndef SHADOWCREATOR_TOUCHPAD_DRIVER_H
#define SHADOWCREATOR_TOUCHPAD_DRIVER_H

#include "../../../../../../../../Library/Android/sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/linux/input.h"
#include "../../../../../../../../Library/Android/sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/linux/uinput.h"

class TouchpadDriver {

private:
    struct input_event event;
    struct uinput_user_dev uinp;
    int uinp_fd;

public:
    void send_event(int type, int code, int value);

    int init();

    void destroy();

    void testSingleTouch();

    void testFingerDrag();

    TouchpadDriver() : uinp_fd(-1) {};

    ~ TouchpadDriver() {
        destroy();
    }
};



#endif //SHADOWCREATOR_TOUCHPAD_DRIVER_H
