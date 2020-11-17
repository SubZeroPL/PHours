#!/bin/sh
release_id=$(curl -s -N -H "Accept: application/vnd.github.v3.text" https://api.github.com/repos/SubZeroPL/PHours/releases/tags/v0.4-alpha | grep -Po --max-count=1 '"id": .*,' | sed  's/\,//g' | sed 's/"id": //')
curl -X DELETE -H "Accept: application/vnd.github.v3+json" https://api.github.com/repos/SubZeroPL/PHours/releases/$release_id
