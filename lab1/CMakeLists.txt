cmake_minimum_required(VERSION 3.20)
project(lab1)

add_subdirectory(lib/googletest-master)
include_directories(lib/googletest-master/googletest/include)
include_directories(lib/googletest-master/googlemock/include)
set(CMAKE_CXX_STANDARD 14)

add_executable(lab1 main.cpp flatmap.cpp flatmap.h)
target_link_libraries(lab1 gtest gtest_main)
target_link_libraries(lab1 gmock gmock_main)