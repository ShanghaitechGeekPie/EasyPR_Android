# Hallo Recognize
Hello Recognize本是EasyPR的android移植版本，本项目旨在提升原版效率，修正bug，同时加入了UART通讯。

#注意MainActivity下
		String svmpath = path+"/svm.xml";
		String annpath = path+"/ann.xml";
		String datapath = path+"/input.data";
将项目jni/model 下的svm.xml和ann.xml和测试图片的RGB矩阵导入你手机的SD卡根目录。

#使用步骤
* 下载并导入OpenCV-2.4.10-android-sdk项目到eclipse，设置为is Library
* 下载并导入usb-serial-for-android并添加到eclipse，设为is Library
* 导入本项目，添加引用
右键properties-> Android-> library-> add OpenCV-2.4.10-android-sdk
usb-serial-for-android引用参见其官方说明
* 修改jni/android.mk
把里面的路径替换成你本机的路径。

* 重新编译
cmd 到jni目录，执行ndk-build
如果无法编译，删除obj目录

