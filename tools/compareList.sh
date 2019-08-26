#!/bin/bash

#set -x

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

for i in 4aeaefeb-3518-435a-9688-510fa60184e0 f3845a5f-3076-40ae-8fa7-d173624e3c4d
do
    "${scriptdir}/compareWithBackend.sh" "valintaperuste/${i}"
    res=$?

    if ((res > 0)); then
        exit ${#res}
    fi
done
