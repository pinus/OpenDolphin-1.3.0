#!/bin/bash

echo -n "pngのファイル名(拡張子なし):"
read filename

array=(16 32 128 256 512)

mkdir "${filename}.iconset"
for i in "${array[@]}"
do
    sips -Z $i ${filename}.png --out "${filename}.iconset/icon_${i}x${i}.png"
    sips -Z $((i*2)) ${filename}.png --out "${filename}.iconset/icon_${i}x${i}@2x.png"
done

iconutil -c icns "${filename}.iconset"
#rm -rf "${filename}.iconset"

