# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("PanoTouch")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native_pano_touch_mouse_driver

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        uinput_touchpad/mouse_driver/native_uinput_mouse_driver.cpp
        #        uinput_touchpad/touchpad_driver.cpp
        uinput_touchpad/mouse_driver/mouse_driver.cpp

        )

add_library(
        native_pano_touch_honghu_gamepad

        SHARED

        honghu_gamepad/native_honghu_gamepad.cpp
        honghu_gamepad/HandShankHelper.h
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native_pano_touch_mouse_driver

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries( # Specifies the target library.
        native_pano_touch_honghu_gamepad

        #        native_honghu_gamepad

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})