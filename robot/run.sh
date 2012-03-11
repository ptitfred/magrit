#!/bin/bash

source utils/robot

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
	for dir in $(lsTestDirectories)
	do
		# Retains directories containing an executable run.sh file
		[ -x $dir/run.sh ] && echo $dir;
	done
}

function lsTestDirectories {
	find . -maxdepth 1 -regex "\./t[0-9]*" -type d
}

function runTestCase {
	local tc=$1
	cd $tc
	local padding=$(strCpy "-" ${#tc})
	info "--- $tc ---------------------------------------------------------------"
	ts1=$(date +%s)
	ns1=$(date +%N)
	PATH=${BASEDIR}/${install_dir}/scripts:${BASEDIR}/utils:$PATH bash run.sh &
	testPid=$!
	wait $testPid
	ec=$?
	ts2=$(date +%s)
	ns2=$(date +%N)
	killChildren $testPid
	info "---$padding-----------------------------------------------------------------"
	sec=$(echo "scale=3; ($ts2 - $ts1) * 1 + $ns2 / 1000000000 - $ns1 / 1000000000" | bc -l)
	info "Took ${sec} sec"
	if [ $ec -gt 0 ]; then
		let "failures ++"
	elif [ $ec -lt 0 ]; then
		let "errors ++"
	else
		let "oks ++"
	fi
	cd $BASEDIR
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
mistakes=$(( $failures + $errors ))
total=$(( $oks + $mistakes ))
info "OK:       $oks"
info "Failures: $failures"
info "Errors:   $errors"
info "Total:    $total"
info "-----------------------------------"
info "Killing server"
kill $PID

info "Done"

info "-----------------------------------"
if [ $mistakes -eq 0 ]; then
	info "TEST SUCCESSED"
else
	error "TEST FAILED"
fi
exit $mistakes

