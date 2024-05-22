cmake_minimum_required(VERSION 3.6.0)
project(ifma_fireyer_jni)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
# self include
include_directories("inc")

file(GLOB foo_sources . *.cpp *.h
        inc/*.h
        src/*.cpp
)

set(SOURCE_FILES ${foo_sources})
add_executable(${PROJECT_NAME} ${SOURCE_FILES})
