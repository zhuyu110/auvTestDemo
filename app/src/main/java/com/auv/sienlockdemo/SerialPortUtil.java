package com.auv.sienlockdemo;

import android.util.Log;

import com.azhon.serialport.ReceiveDataListener;
import com.azhon.serialport.SerialPortPlus;

import java.io.IOException;

public class SerialPortUtil {

  private static   final String  TAG  =SerialPortUtil.class.getSimpleName();
  
  private static SerialPortUtil serialPortUtil;


    SerialPortPlus serialPortPlus;

 public static SerialPortUtil getInstances() {
      if(serialPortUtil==null){
          serialPortUtil =new SerialPortUtil();
      }

      return serialPortUtil;
  }




  
  public void closeSerialPort() {
    Log.i("test", "关闭串口");
    if( serialPortPlus!=null){
        serialPortPlus.close();
    }

  }
  
  public boolean isSerialPortLive() {
    boolean bool = false;
    if (this.serialPortPlus != null)
      bool = true; 
    return bool;
  }
  

  
  public void openSerialPort(String paramString) {

    Log.d("serial", paramString);
    try {
        serialPortPlus = new SerialPortPlus(paramString, 9600, 0, 0, 8, 1);
      if (serialPortPlus == null) {
        Log.i(TAG,"打开失败");
      } else {
        Log.i(TAG,"打开成功");
      }
      return;
    } catch (IOException iOException) {
      iOException.printStackTrace();
      return;
    } catch (Exception e) {
        e.printStackTrace();
    }
  }





  public void sendSerialPort(byte[] paramArrayOfbyte) {
      serialPortPlus.writeAndFlush(paramArrayOfbyte);
  }

  public  void setReceiverListener(ReceiveDataListener  receiverListener){
     if(serialPortPlus!=null){
         serialPortPlus.setReceiveDataListener(receiverListener);
     }

  }
  

}
