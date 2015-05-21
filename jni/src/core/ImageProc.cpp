#include<com_example_carplate_CarPlateDetection.h>
#include "plate_locate.h"
#include "plate_judge.h"
#include "chars_segment.h"
#include "chars_identify.h"
#include "plate_detect.h"
#include "chars_recognise.h"
#include "plate_recognize.h"
using namespace easypr;
#include <android/log.h>
#include <string>
#define LOG_TAG "System.out"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

char* jstring2str(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("GB2312");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return rtn;
}

JNIEXPORT jbyteArray JNICALL Java_com_example_carplate_CarPlateDetection_ImageProc(
		JNIEnv *env, jclass obj, jstring imgpath, jstring svmpath,
		jstring annpath) {
	CPlateRecognize pr;
//	const string *img = (*env)->GetStringUTFChars(env, imgpath, 0);
//	const string *svm = (*env)->GetStringUTFChars(env, svmpath, 0);
//	const string *ann = (*env)->GetStringUTFChars(env, annpath, 0);
	char* img = jstring2str(env,imgpath);
	char* svm = jstring2str(env,svmpath);
	char* ann = jstring2str(env,annpath);

	// #############################################################
	// Mat src = imread(img);
	Mat src;
	src.create(480,640, CV_8UC3);

	FILE *in;
	in=fopen(img,"r");

	int i,j,y=0,u=0,v=0;
	char t,handle=1;
	double R,G,B;
	for (j=0;j<480;j++){
		for (i=0;i<640;i++){
			fscanf(in,"%X",&y);
			if (handle){
				fscanf(in,"%X",&u);
				handle=0;
			}else{
				fscanf(in,"%X",&v);
				handle=1;
			}

			R=(y+1.4075*(v-128));
			G=(y-0.455*(u-128)-0.7169*(v-128));
			B=(y+1.779*(u-128));

			src.at<Vec3b>(j,i)[0] = (unsigned char)(B<0)?0:(B>255)?255:(unsigned char)B;
			src.at<Vec3b>(j,i)[1] = (unsigned char)(G<0)?0:(G>255)?255:(unsigned char)G;
			src.at<Vec3b>(j,i)[2] = (unsigned char)(R<0)?0:(R>255)?255:(unsigned char)R;
		}
	}
	fclose(in);

	// #############################################################
	pr.LoadSVM(svm);
	pr.LoadANN(ann);

	pr.setGaussianBlurSize(5);
	pr.setMorphSizeWidth(17);

	pr.setVerifyMin(3);
	pr.setVerifyMax(20);

	pr.setLiuDingSize(7);
	pr.setColorThreshold(150);

	vector < string > plateVec;

	int count = pr.plateRecognize(src, plateVec);
	string str = "0";

	if (count == 0) {
		//str="NONE";
		str = plateVec[0];
	}

	char *result = new char[str.length() + 1];
	strcpy(result, str.c_str());
	jbyte *by = (jbyte*) result;
	jbyteArray jarray = env->NewByteArray(strlen(result));
	env->SetByteArrayRegion(jarray, 0, strlen(result), by);
	return jarray;
}
