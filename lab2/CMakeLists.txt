cmake_minimum_required(VERSION 3.20)
project(lab2)

add_subdirectory(lib/googletest-master)
include_directories(lib/googletest-master/googletest/include)
include_directories(lib/googletest-master/googlemock/include)
set(CMAKE_CXX_STANDARD 14)

add_executable(lab2 main.cpp ../../.config/JetBrains/CLion2021.2/scratches/prisoner_logic.h score_table.cpp score_table.h factory.h strategy.h cooperate.cpp cooperate.h controller.cpp controller.h fast.cpp fast.h impostor.cpp impostor.h tit4tat.cpp tit4tat.h detailed.cpp detailed.h tournament.cpp tournament.h random.cpp random.h tests.cpp arg_parser.cpp arg_parser.h)
target_link_libraries(lab2 gtest gtest_main)
target_link_libraries(lab2 gmock gmock_main)