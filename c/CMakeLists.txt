cmake_minimum_required(VERSION 3.27)
project(Canterbury C)

set(CMAKE_C_STANDARD 99)

add_executable(canterbury
    src/main.c
    src/common.h
    src/chunk.h src/chunk.c
    src/memory.h src/memory.c
    src/debug.h src/debug.c
    src/value.h src/value.c
)
