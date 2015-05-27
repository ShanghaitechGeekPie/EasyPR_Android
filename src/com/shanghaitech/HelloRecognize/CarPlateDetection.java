package com.shanghaitech.HelloRecognize;


public class CarPlateDetection {
	public static native byte[] ImageProc(String imgpath, String svmpath, String annpath);
	public static native byte ImageProcRead(String datapath, String imgpath);
}
