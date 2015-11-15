#!/bin/sh
# V7Extractor disk targetPath
echo $1
echo $2
java -cp ./classes/ unixv7.V7Extractor $1 $2
