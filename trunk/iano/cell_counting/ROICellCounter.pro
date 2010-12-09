
TEMPLATE      = lib
CONFIG       += plugin
INCLUDEPATH  += ../../basic_c_fun
HEADERS       = ROICellCounter.h CellClassifier.h CellFeatureExtractor.h FeatureExtractorInterface.h DataClassifierInterface.h SVMClassifier.h svm.h HaarFeatureExtractor.h basicutil.h
SOURCES       = ROICellCounter.cpp CellClassifier.cpp CellFeatureExtractor.cpp SVMClassifier.cpp svm.cpp HaarFeatureExtractor.cpp basicutil.cpp
TARGET        = $$qtLibraryTarget(ROICellCounter)
DESTDIR       = D:\research\V3D\v3d_win32_2.466_v3dneuron_2.0\v3d_win32_2.466_v3dneuron_2.0\plugins\ROICellCounter

