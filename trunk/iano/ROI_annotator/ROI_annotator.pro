
TEMPLATE      = lib
CONFIG       += plugin
INCLUDEPATH  += ../../basic_c_fun
HEADERS       = ROI_annotator.h ROIClassifier.h StackSimpleHaarFeatureExtractor.h FeatureExtractorInterface.h DataClassifierInterface.h SVMClassifier.h svm.h HaarFeatureExtractor.h
SOURCES       = ROI_annotator.cpp ROIClassifier.cpp StackSimpleHaarFeatureExtractor.cpp SVMClassifier.cpp svm.cpp HaarFeatureExtractor.cpp
TARGET        = $$qtLibraryTarget(ROI_annotator)
DESTDIR       = D:\research\V3D\v3d_win32_2.452_v3dneuron_1.0\v3d_win32_2.452_v3dneuron_1.0\plugins\ROI_annotator

