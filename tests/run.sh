#!/bin/bash

function info {
	echo "[INFO]" $*
}

function error {
	echo "[ERROR]" $*
}

BASEDIR=$(pwd)

failures=0
errors=0
oks=0

function lsTestCases {
	find . -maxdepth 1 -regex "\./t[0-9]*" -type d
}

function runTestCase {
	local tc=$1
	cd $tc
	count=${#tc}
	local i=0
	while [ $i -lt $count ]; do
		padding="-$padding"
		let "i ++"
	done
	echo "/-- $tc --------------------------------------------------------------------------\\"
	ts1=$(date +%s)
	ns1=$(date +%N)
	PATH=${BASEDIR}/${install_dir}/scripts:$PATH bash run.sh
	ec=$?
	ts2=$(date +%s)
	ns2=$(date +%N)
	echo "\\--$padding----------------------------------------------------------------------------/"
	msec=$(( ($ts2 - $ts1) * 1000 + $ns2 / 1000000 - $ns1 / 1000000 ))
	info "took ${msec} msec"
	if [ $ec -gt 0 ]; then
		let "failures ++"
	elif [ $ec -lt 0 ]; then
		let "errors ++"
	else
		let "oks ++"
	fi
	cd $BASEDIR
	info "-----------------------------------"
}

info "-----------------------------------"
info "Running integration tests"
info

installer=$1
install_dir='target/local'
rm -rf ${install_dir}
mkdir -p ${install_dir}

info "Installing..."
java -jar ${installer} --install ${install_dir} > /dev/null
bash ${install_dir}/setup.sh
info "Starting..."
bash ${install_dir}/start.sh > target/output.log &
PID=$(wait-tcp 2022 5) || exit 2
info "Server started"
info "-----------------------------------"
info "TESTS"
for tc in $(lsTestCases); do
	runTestCase $tc
done
info "OK:       $oks"
info "Failures: $failures"
info "Errors:   $errors"
info "-----------------------------------"
info "Killing server"
kill $PID

info "Done"

info "-----------------------------------"
info "TEST SUCCESSED"
exit 0
#error "TEST FAILED"
#exit 1

