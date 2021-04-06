package com.auv.sienlockdemo;

import android.util.Log;
import android_serialport_api.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortUtil {

  private static   final String  TAG  =SerialPortUtil.class.getSimpleName();
  
  private static SerialPortUtil serialPortUtil;
  
  private InputStream inputStream = null;
  
  private boolean isStart = false;
  
  private ReceiveThread mReceiveThread = null;
  
  private OutputStream outputStream = null;
  
  private SerialPort serialPort = null;
  
  static SerialPortUtil getInstances() {
      if(serialPortUtil==null){
          serialPortUtil =new SerialPortUtil();
      }

      return serialPortUtil;
  }
  
  private void getSerialPort() {
    if (this.mReceiveThread == null)
      this.mReceiveThread = new ReceiveThread(); 
    this.mReceiveThread.start();
  }
  
  public void closeSerialPort() {
    Log.i("test", "关闭串口");
    try {
      if (this.inputStream != null)
        this.inputStream.close(); 
      if (this.outputStream != null)
        this.outputStream.close(); 
      this.isStart = false;
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } 
  }
  
  public boolean isSerialPortLive() {
    boolean bool = false;
    if (this.inputStream != null)
      bool = true; 
    return bool;
  }
  
  public void openSerialPort() {
    try {
      SerialPort serialPort = new SerialPort(new File("/dev/ttyS1"), 9600, 0);
      if (serialPort == null) {

          Log.i(TAG,"打开失败");

      } else {

          Log.i(TAG,"打开成功");
      }
      this.serialPort = serialPort;
      this.inputStream = this.serialPort.getInputStream();
      this.outputStream = this.serialPort.getOutputStream();
      this.isStart = true;
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } 
  }
  
  public void openSerialPort(String paramString) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("/dev/");
    stringBuilder.append(paramString);
    Log.d("serial", stringBuilder.toString());
    try {
      stringBuilder = new StringBuilder();
      stringBuilder.append("/dev/");
      stringBuilder.append(paramString);
      SerialPort serialPort = new SerialPort(new File(stringBuilder.toString()), 9600, 0);
      this.serialPort = serialPort;
      if (serialPort == null) {
        Log.i(TAG,"打开失败");
      } else {
        Log.i(TAG,"打开成功");
      }

      this.inputStream = this.serialPort.getInputStream();
      this.outputStream = this.serialPort.getOutputStream();
      //getSerialPort();
      this.isStart = true;
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } 
  }

    public byte[] receiveData() {
        byte[] v0_1;
        if(this.inputStream == null) {
            return null;
        }
        try {
            int v1 = this.inputStream.available();
            v0_1 = new byte[v1];
            this.inputStream.read(v0_1);
            Log.i(TAG,"读取到数据长度 " + v1 + " 读取到数据" + DataUtils.ByteArrToHex(v0_1));
            return v0_1;
        } catch(IOException v2) {
            v2.printStackTrace();
        }
        return null;
    }


  
  public void sendSerialPort(byte[] paramArrayOfbyte) {
    try {
      this.outputStream.write(paramArrayOfbyte);
      this.outputStream.flush();
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } 
  }
  
  private class ReceiveThread extends Thread {
    private ReceiveThread() {}
    
    public void run() {
      super.run();
      while (SerialPortUtil.this.isStart) {
        if (SerialPortUtil.this.inputStream == null)
          return; 
        byte[] arrayOfByte = new byte[1024];
        try {
          int i = SerialPortUtil.this.inputStream.read(arrayOfByte);
          if (i > 0) {
            String str = DataUtils.ByteArrToHex(arrayOfByte, 0, i);
              String stringBuilder="接收到串口的数据：" +
                      str;
              Log.i(TAG, stringBuilder);
          } 
        } catch (IOException iOException) {
          iOException.printStackTrace();
        } 
      } 
    }
  }
}
