#ifndef UINPUT_MANAGER_H
#define UINPUT_MANAGER_H

#include <fcntl.h>
#include <errno.h>
#include <dirent.h>
#include <math.h>
#include <time.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
//#include <utils/RefBase.h>
//#include <cutils/log.h>
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/input.h>
#include <linux/uinput.h>
#include <malloc.h>
#include <stdint.h>

#define KEYDOWN 1
#define KEYUP 0

class UinputManager {

private:
    int fd_keyboard;
    int fd_mouse;
    int width;
    int height;

public:
    UinputManager();

    virtual ~UinputManager();

    int createKeyboardDevice();

    int handleKey(int keycode, int status);

    int createMouseDevice();

    int handleMouseMove(int RelX, int RelY);

    int handleMouseKey(int keycode, int status);

    int leftClick() {
        handleMouseKey(BTN_LEFT, 1);
        handleMouseKey(BTN_LEFT, 0);
        return 0;
    };

    int rightClick() {
        handleMouseKey(BTN_RIGHT, 1);
        handleMouseKey(BTN_RIGHT, 0);
        return 0;
    };
};

#endif