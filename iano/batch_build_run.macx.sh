#!/bin/bash
# 080830: by Hanchuan Peng
# Oct 14 2008, use the makefile for Mac, by Jie Zhou
# Jan 13, 2009: edited by Hanchuan Peng

export CLASSPATH=/Users/pengh/work/iano/annotool/ij.jar:/Users/pengh/work/iano/annotool/libsvm.jar:/Users/pengh/work/iano/Jama-1.0.2.jar:./
javac annotool/*.java
javah annotool.select.mRMRNative

mv annotool_select_mRMRNative.h mRMR/.

cd mRMR
make -f javamrmr.makefile.macx
cd ..
mv mRMR/libmRMRNative.dylib .

javac IANO_.java
javac IANOTagger_.java

java -Xms500M -Xmx1200M annotool.AnnotatorGUI

