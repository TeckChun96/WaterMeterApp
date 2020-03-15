
cmake_minimum_required(VERSION 3.6)

#make can include
include_directories(${CMAKE_SOURCE_DIR}/../../../native/jni/include)
#add libs file
add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../native/libs/${ANDROID_ABI}/libopencv_java4.so)

file(GLOB native_srcs *.cpp)
add_library(native-libs SHARED ${native_srcs})


find_library(
        log-lib
        log)


target_link_libraries(
        native-libs
        ${log-lib}
        lib_opencv
        )