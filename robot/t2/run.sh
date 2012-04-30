#!/bin/bash

source robot

workdir=$(unpack repo.bundle)

cd $workdir

magrit config add localhost:$MAGRIT_TEST_PORT testing
git config --local magrit.log.maxwidth 40

runOrDie -t 3 -l output.log \
	magrit build-log -3
check $?

