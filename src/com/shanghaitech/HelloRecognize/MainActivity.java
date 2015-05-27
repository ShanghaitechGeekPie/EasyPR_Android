package com.shanghaitech.HelloRecognize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
	private ImageView imageView = null;
	private Bitmap bmp = null;
	private TextView m_text = null;
	private String path = null;
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	    } else {
	        System.loadLibrary("imageproc");
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.image_view);
		m_text = (TextView) findViewById(R.id.myshow);
		path = Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public void Uartclick(View view) {
		startActivity(new Intent(MainActivity.this, DeviceListActivity.class));
//        final Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
//        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
//        MainActivity.this.startActivity(intent);
//		m_text.setText("a");
//		// Find all available drivers from attached devices.
//		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//		List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
//		if (availableDrivers.isEmpty()) {
//		  return;
//		}
//
//		// Open a connection to the first available driver.
//		UsbSerialDriver driver = availableDrivers.get(0);
//		UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
//		if (connection == null) {
//		  // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
//		  return;
//		}
//
//		// Read some data! Most have just one port (port 0).
//		UsbSerialPort port = driver.getPorts().get(0);
//		try {
//		  port.open(connection);
//		  port.setParameters(115200, 8, 1, 0);
//		  byte buffer[] = new byte[16];
//		  int numBytesRead = port.read(buffer, 1000);
//			m_text.setText(numBytesRead);
//		  port.close();
//		 // Log.d(TAG, "Read " + numBytesRead + " bytes.");
//		} catch (IOException e) {
//		  // Deal with error.
//		} finally {
//		}
	}


	public void Readclick(View view) throws UnsupportedEncodingException {
		System.out.println("entering the jni");
		String datapath = path+"/input.data";
		String imgpath = path+"/input.jpg";
		byte resultByte = CarPlateDetection.ImageProcRead(datapath,imgpath);
		bmp = BitmapFactory.decodeFile(imgpath);
		imageView.setImageBitmap(bmp);
	//	System.out.println(resultByte);
//		bmp.setPixel(x, y, color);
		//m_text.setText(resultByte);
	}


	public void click(View view) throws UnsupportedEncodingException {
		System.out.println("entering the jni");
		String svmpath = path+"/svm.xml";
		String annpath = path+"/ann.xml";
		String datapath = path+"/input.data";
		byte[] resultByte = CarPlateDetection.ImageProc(datapath,svmpath,annpath);
		String result = new String(resultByte,"utf-8");
		System.out.println(result);
		m_text.setText(result);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
	}
}
