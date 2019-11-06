#!/bin/bash

if [[ -z $KOUTA_INTERNAL_TEST_SESSION || -z $KOUTA_BACKEND_TEST_SESSION ]]; then
    echo "$KOUTA_INTERNAL_TEST_SESSION and KOUTA_BACKEND_TEST_SESSION variables need to set to current kouta internal and kouta backend session ids, respectively."
    exit 40
fi

#set -x

curl_json () {
    body=$(curl -sSf "$2/$1" -H 'accept: application/json' -H "Cookie: session=$3")
    res=$?

    if (( res > 0 )); then
        set -x
        curl "$2/$1" -H 'accept: application/json' -H "Cookie: session=$3"
        { set +x; } 2>/dev/null
        return 22
    else
        jq '. | walk( if type == "array" then sort else . end )' <<< "${body}" > "$4"
       return $?
    fi
}

scriptdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ext_file="${scriptdir}/internal.json"
back_file="${scriptdir}/backend.json"

curl_json "$1" http://localhost:8098/kouta-internal ${KOUTA_INTERNAL_TEST_SESSION} "${ext_file}"
if (( $? > 0 )); then exit 22; fi
curl_json "$1" http://localhost:8099/kouta-backend ${KOUTA_BACKEND_TEST_SESSION} "${back_file}"
if (( $? > 0 )); then exit 22; fi

diff -q "${ext_file}" "${back_file}"
res=$?

if (( res > 0 )); then
    #vimdiff internal.json backend.json
    diff "${ext_file}" "${back_file}"
    exit 1
else
    echo "The outputs match for $1 "
    exit 0
fi
