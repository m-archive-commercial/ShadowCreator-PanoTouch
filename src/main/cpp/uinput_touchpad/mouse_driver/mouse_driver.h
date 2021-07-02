//
// Created by 南川 on 2021/6/1.
//

#ifndef TOUCHPAD_MOUSE_DRIVER_H
#define TOUCHPAD_MOUSE_DRIVER_H

class MouseDriver {

private:
    int x, y;
    int fd;
    bool isLeftDown = false;

public:
    MouseDriver();

    virtual ~MouseDriver();

    int init();

    int move(int x, int y);

    int leftClick();

    int rightClick();

    int leftDown();

    int leftUp();

    int scroll(int x, int y);
};

#endif //TOUCHPAD_MOUSE_DRIVER_H


