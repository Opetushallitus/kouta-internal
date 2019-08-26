#!/bin/bash

#set -x

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

for i in {1..200}
do
    printf -v j "%05d" ${i}
    "${scriptdir}/compareWithBackend.sh" "toteutus/1.2.246.562.17.000000000000000${j}"
    res=$?

    if ((res > 0)); then
        exit ${#res}
    fi
done
