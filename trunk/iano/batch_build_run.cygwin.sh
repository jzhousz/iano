#!/bin/bash
# Feb 2, 2009: By Jie Zhou

export CLASSPATH=annotool/ij.jar:annotool/libsvm.jar:annotool/Jama-1.0.2.jar:./
/cygdrive/c/"Program Files"/Java/jdk1.6.0_01/bin/javac annotool/*.java
/cygdrive/c/"Program Files"/Java/jdk1.6.0_01/bin/javah annotool.select.mRMRNative

mv annotool_select_mRMRNative.h mRMR/.

cd mRMR
make -f javamrmr.makefile.win
cd ..
mv mRMR/mRMRNative.dll .

/cygdrive/c/"Program Files"/Java/jdk1.6.0_01/bin/javac IANO_.java

java -Xms500M -Xmx1200M annotool.AnnotatorGUI

