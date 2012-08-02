package annotool.select;


/* JNI interface: native methods to do mRMR selection */

class mRMRNative
{
     public static native int[] miq(float[] features, int[] target, int nfeature, int nsample, int nvar);
	 public static native int[] mid(float[] features, int[] target, int nfeature, int nsample, int nvar);
}