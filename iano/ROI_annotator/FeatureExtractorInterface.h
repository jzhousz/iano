/* Jie Zhou March 2010
 *
 */

#ifndef __FEATUREEXINTERFACE_H__
#define __FEATUREEXINTERFACE_H__



class FeatureExtractorInterface
{
public:
   	virtual ~FeatureExtractorInterface() {}
    virtual float * calcFeatures() = 0;  
};

#endif
