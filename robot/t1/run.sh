#!/bin/bash

source robot

runOrDie -t 3 -l output.log \
	magrit build-log -3
check $?

lines=$(wc -l <output.log)

assert 3 $lines

