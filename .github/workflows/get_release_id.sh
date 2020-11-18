#!/bin/sh
release_id=$(curl -s -N -H "Accept: application/vnd.github.v3.text" https://api.github.com/repos/SubZeroPL/PHours/releases/tags/$tag_name | grep -Po --max-count=1 '"id": .*,' | sed  's/\,//g' | sed 's/"id": //')
echo $release_id
