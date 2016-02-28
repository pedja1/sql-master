#!/usr/bin/env bash

ndk-build
if [ $? -eq 0 ]; then
    cd ../libs
    for f in $(ls .);
    do
        mv $f/ls $f/libls.so
        mv $f/sqlite_verify $f/libsqlite_verify.so
        mv $f/sqlite_cmd $f/libsqlite_cmd.so
    done
fi