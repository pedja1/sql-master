#!/usr/bin/env bash

ndk-build
cd ../libs
for f in $(ls .);
do
    mv $f/ls $f/libls.so
    mv $f/sqlite_verify $f/libsqlite_verify.so
done