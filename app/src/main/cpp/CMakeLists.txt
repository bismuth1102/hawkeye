# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

#设置OpenCv sdk的路径变量
set(pathToOpenCv D:/OpenCV-android-sdk/)

#cmake version 根据自己的情况设置
cmake_minimum_required(VERSION 3.4.1)

#支持-std=gnu++11
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

#配置加载native依赖
include_directories(${pathToOpenCv}/sdk/native/jni/include)

#动态方式加载
add_library(lib_opencv SHARED IMPORTED)

#引入libopencv_java4.so文件
set_target_properties(lib_opencv
        PROPERTIES
        IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so )


# 自己的源文件
add_library(
        # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        Hungarian.cpp
        KalmanTracker.cpp
        SORT.cpp
)

#查找库
find_library(
        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

#链接库
target_link_libraries(
        # Specifies the target library.
        native-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} android -ljnigraphics lib_opencv

)
