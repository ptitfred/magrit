#!/bin/bash

function check {
	if [ $1 -gt 0 ]; then
		exit $1
	fi
}

(
	magrit build-log -3
	check $?
) | tee output.log

lines=$(wc -l <output.log)

function assert {
	test $1 -eq $2
	local ec=$?
	if [ $ec -gt 0 ]; then
		echo "{assert} Expected $1 but was $2"
		exit $ec
	fi
}

assert 4 $lines

