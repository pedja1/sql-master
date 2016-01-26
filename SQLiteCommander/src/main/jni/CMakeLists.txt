cmake_minimum_required(VERSION 3.2)
project(helloworld)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES sqlite_cmd.c sqlite3/sqlite3.c sqlite3/sqlite3.h sqlite_verify.c sqlite_verify.h sqlite_verify_main.c)
add_executable(helloworld ${SOURCE_FILES})

set(THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)
target_link_libraries(helloworld Threads::Threads)
target_link_libraries(helloworld ${CMAKE_DL_LIBS})
