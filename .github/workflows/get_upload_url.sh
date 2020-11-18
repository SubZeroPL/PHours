#!/bin/sh
upload_url=$(curl -s -H "Accept: application/vnd.github.v3.text" https://api.github.com/repos/SubZeroPL/PHours/releases/tags/$tag_name | grep -Po '"upload_url": ".*"' | sed 's/\"//g' | sed 's/upload_url: //')
echo $upload_url
