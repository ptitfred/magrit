Compiling for Linux target 
==========================

 ```shell
 > mkdir build
 > cd build
 > cmake -DCMAKE_TOOLCHAIN_FILE=../toolchain/Linux-gcc-i686.cmake ../
 > make
 ```

Cross-compiling for Windows target
==================================

Check first if toolchain/Windows-mingw-i686.cmake supports your distribution
If it's supported, congratulations! You can then do:

 ```shell
 > mkdir build
 > cd build
 > cmake -DCMAKE_TOOLCHAIN_FILE=../toolchain/Windows-mingw-i686.cmake ../
 > make
 ```

If it's not, enabling support for your distro shouldn't be very difficult
(have a look at the first lines of the toolchain file).

[![Build Status](https://secure.travis-ci.org/daniperez/magrit.png?branch=master)](http://travis-ci.org/daniperez/magrit)
