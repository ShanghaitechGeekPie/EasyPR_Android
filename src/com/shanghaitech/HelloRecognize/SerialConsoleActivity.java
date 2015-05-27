/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.shanghaitech.HelloRecognize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monitors a single {@link UsbSerialPort} instance, showing all data
 * received.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialConsoleActivity extends Activity {

    private final String TAG = SerialConsoleActivity.class.getSimpleName();

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     *
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialPort sPort = null;

    private TextView mTitleTextView;
    private TextView mTitleTextStatusView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private String Dumptext="";
    private FileWriter fw;
    private int num=0;
    private int datacount=0;
    private boolean first_loop=true;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SerialConsoleActivity.this.updateReceivedData(data);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mTitleTextStatusView = (TextView) findViewById(R.id.textView_status);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mProgressBar.setProgress(0);
        mTitleTextStatusView.setText("Ready....");
        num=0;
        datacount=0;
        first_loop=true;
        try {
            fw = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/input.data");
            fw.write("");  
            fw.close();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
            mTitleTextStatusView.setText("Waiting for data....");
        }
        onDeviceStateChange();
    }

    private void savetofile() {
        mTitleTextStatusView.setText("Saving data....");
    //    mDumpTextView.getText();
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTitleTextStatusView.setText("Saving Finished");
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
    	//Dumptext
        try {
            fw = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/input.data",true);
	        for (int i = 0; i < data.length; i++) {
	        	num++;
	        	int bordervalue=first_loop?7874:7860;
	        	if (num<=bordervalue){
		        	int v = data[i] & 0xFF;  
		        	Dumptext=Integer.toHexString(v);
		        	Dumptext+=" ";
			        fw.write(Integer.toHexString(v));
			        fw.write(" ");
		        	datacount++;
		            mProgressBar.setProgress((int)datacount*100/614400);
	        	}else if (num==7874+62){
	        		num=0;
	        		first_loop=false;
	        	}
	        }
	        //fw.write("FINISH\n ");
	        
	        fw.close();  
	    } catch (IOException e) {
        	num--;
	        e.printStackTrace();
	    }

        final String message = "Read " + data.length + " bytes.\n"
        		+ "All Receive Data: " + datacount + " out of 614400. \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.setText(message);
      //  mDumpTextView.append(Dumptext);
       // mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     * @param driver
     */
    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

}
