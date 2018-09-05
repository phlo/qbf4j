#!/bin/bash

ZIPED="qcir-non-prenex.tar"

cd /tmp
wget https://phlo.github.io/qbf4j/files/${ZIPED}.xz
tar xvfJ ${ZIPED}.xz
tar xvf ${ZIPED}
mv non-prenex qcir-non-prenex
cd qcir-non-prenex
gunzip *.gz
for f in pg-hkb-[0-9].qcir
do
  mv $f ${f/hkb-/hkb-0}
done
cd ..
rm ${ZIPED}*
