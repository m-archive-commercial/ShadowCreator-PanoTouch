//
// Created by 南川 on 2021/6/22.
//

#include "touchpad_driver.h"
//#include <linux/input.h>
//#include <unistd.h>

void TouchpadDriver::testSingleTouch() {
//    暂停两秒
    usleep(2000000);
    send_event(0x0003, 0x39, 0x000006c4);
    send_event(0x0003, 0x35, 0x000003ac);
    send_event(0x0003, 0x36, 0x00000042);
    send_event(0x0003, 0x30, 0x0000002b);
    send_event(0x0003, 0x3a, 0x0000002b);
    send_event(0x0001, 0x14a, 0x00000001);

    send_event(0x0000, 0x0, 0x00000000);
    send_event(0x0003, 0x39, 0xffffffff);
    send_event(0x0001, 0x14a, 0x00000000);
    send_event(0x0000, 0x0, 0x00000000);

//
//    int tracking_id = 18;
//    send_event(EV_ABS, ABS_MT_TRACKING_ID, tracking_id);
//    send_event(EV_ABS, ABS_MT_POSITION_X, i);
//    send_event(EV_ABS, ABS_MT_POSITION_Y, 600);
//    send_event(EV_ABS, ABS_PRESSURE, 25);
//    send_event(EV_ABS, ABS_MT_TOUCH_MAJOR, 5);
//    send_event(0, 0, 0);

}

void TouchpadDriver::testFingerDrag() {
    int tracking_id = 19;
    send_event(EV_ABS, ABS_MT_TOUCH_MAJOR, tracking_id);
    send_event(EV_ABS, ABS_MT_POSITION_X, 100);
    send_event(EV_ABS, ABS_MT_POSITION_Y, 100);
    send_event(EV_ABS, ABS_PRESSURE, 25);
    send_event(EV_ABS, ABS_MT_TOUCH_MAJOR, 5);
    send_event(EV_ABS, ABS_MT_SLOT, 1);
    send_event(EV_ABS, ABS_MT_POSITION_X, 300);
    send_event(EV_ABS, ABS_MT_POSITION_Y, 300);
    send_event(0, 0, 0);
    send_event(EV_ABS, ABS_MT_SLOT, 0);
    send_event(EV_ABS, ABS_MT_TRACKING_ID, -1);
    send_event(EV_ABS, ABS_MT_SLOT, 1);
    send_event(EV_ABS, ABS_MT_TRACKING_ID, -1);
    send_event(0, 0, 0);

}