#!/bin/sh
# V7Extractor disk targetPath
# e.g., V7Extractor rp06.disk /unix
# if the second argumentis omitted, all files are extracted to output directory
echo $1
echo $2
java -cp ./classes/ unixv7.V7Extractor $1 $2
