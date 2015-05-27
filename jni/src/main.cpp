#include "include/plate_recognize.h"
#include "include/util.h"
#include "include/feature.h"
#include "include/CParser.h"

using namespace easypr;

//标准读入地址
extern const string NATIVE_RECG_PATH = "RECG";

//标准读入地址
extern const bool	OPTION_HEX = false;
extern const bool	OPTION_DEBUG = true;
extern const int	OPTION_LIFEMODE = false;

int show_dialog(Mat src);
int plate_locatte_main(vector<Mat>& resultDT);
int plate_judge_main(vector<Mat>& src, vector<Mat>& resultDT);
int plate_detect_main(vector<Mat>& resultDT);
int plate_recognize_main();

// "--test_plate_locate    [ -tpl ]    车牌定位                ",
// "--test_plate_judge     [ -tpj ]    车牌判断                ",
// "--test_plate_detect    [ -tpd ]    车牌检测                ",
// "--test_chars_segment   [ -tcs ]    字符分隔                ",
// "--test_chars_identify  [ -tci ]    字符鉴别                ",
// "--test_chars_recognize [ -tcr ]    字符识别                ",
// "--test_plate_recognize [ -tpr ]    车牌识别                ",
// "--test_all             [ -ta  ]    测试全部                ",
// "--general_test         [ -gt  ]    批量测试-general_test   ",
// "--native_test   

int main(){
	vector<Mat> DT;
	int result;
	result = plate_recognize_main();
	//result = plate_judge_main(DT, DT);
}

int show_dialog(Mat src){
	imshow("RECG_show", src);
	waitKey(0);
    destroyWindow("plate_locate");
}

int plate_locatte_main(vector<Mat>& resultDT){
	cout << "process plat_locatte_main...";

	Mat src = imread(NATIVE_RECG_PATH+"/test.jpg");

	CPlateLocate plate;
	plate.setDebug(OPTION_DEBUG);
	plate.setLifemode(OPTION_LIFEMODE);

	int result = plate.plateLocate(src, resultDT);
	if (result == 0)
	{
		int num = resultDT.size();
		for (int j = 0; j < num; j++)
		{
			Mat resultMat = resultDT[j];
			show_dialog(resultMat);
		}
		cout << "OK";
	}

	cout << endl;
	return result;
}

int plate_judge_main(vector<Mat>& src, vector<Mat>& resultDT){
	cout << "process plate_judge_main...";

	vector<Mat> resultVec;

	CPlateJudge plate;
	int resultJu = plate.plateJudge(src, resultVec);

	if (resultJu == 0)
	{
		int num = resultVec.size();
		for (int j = 0; j < num; j++)
		{
			Mat resultMat = resultVec[j];
			show_dialog(resultMat);
		}
		cout << "OK";
	}

	cout << endl;
	return resultJu;
}

int plate_detect_main(vector<Mat>& resultDT){
	cout << "process plate_detect_main...";

	Mat src = imread(NATIVE_RECG_PATH+"/test.jpg");

	CPlateDetect plate;
	plate.setPDDebug(OPTION_DEBUG);
	plate.setPDLifemode(OPTION_LIFEMODE);

	int result = plate.plateDetectDeep(src, resultDT);
	if (result == 0)
	{
		int num = resultDT.size();
		for (int j = 0; j < num; j++)
		{
			Mat resultMat = resultDT[j];
			show_dialog(resultMat);
		}
		cout << "OK";
	}

	cout << endl;
	return result;
}

int plate_recognize_main(){
	cout << "process plate_detect_main..." << endl;

	
	// Mat src = imread(NATIVE_RECG_PATH+"/test.yuv");
	Mat src; //= imread(NATIVE_RECG_PATH+"/test.yuv");
	src.create(480,640, CV_8UC3);

	FILE *in;
	in=fopen("RECG/data.input","r");

	int i,j,y=0,u=0,v=0;
	char t,handle=1;
	double R,G,B;
	char temp;
	for (j=0;j<480;j++){
		for (i=0;i<640;i++){
			if (OPTION_HEX)
				fscanf(in,"%c%c%X",&temp,&temp,&y);
			else
				fscanf(in,"%X",&y);
			if (handle){
				if (OPTION_HEX)
					fscanf(in,"%c%c%X",&temp,&temp,&u);
				else
					fscanf(in,"%X",&u);
				handle=0;
			}else{
				if (OPTION_HEX)
					fscanf(in,"%c%c%X",&temp,&temp,&v);
				else
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

	// show_dialog(src);
	// exit;

	CPlateRecognize plate;
	plate.LoadANN("model/ann.xml");
	plate.LoadSVM("model/svm.xml");
	plate.setDebug(OPTION_DEBUG);
	plate.setLifemode(OPTION_LIFEMODE);

	vector<string> plateVec;

	int result = plate.plateRecognize(src, plateVec);
	if (result == 0)
	{
		int num = plateVec.size();
		for (int j = 0; j < num; j++)
		{
			cout << "plateRecognize[" << j << "]： "<< plateVec[j] << endl;			
		}
	}

	if (result != 0)
		cout << "result:" << result << endl;

	return result;
}
