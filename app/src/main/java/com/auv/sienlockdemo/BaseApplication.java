package com.auv.sienlockdemo;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {
  public static final String CELL_OPEN_TIME = "2200-01-01 00:00:00";
  
  public static String PORT_NAME;
  
  public static boolean gbCancel = false;
  
  public static String getStrSecondaryCount;
  
  public static boolean isFinish = false;
  public static int countTotal;

  private static Context mContext;
  
  public static String strCellOpenOverTime;
  
  public static String[] strCellOpenTime;
  
  public static String strCellTotal;
  
  public static String strMainCellTotal;
  
  public static String strSecondaryCellTotal;
  
  public static String username = "4";
  

  static {
    strMainCellTotal = "12";
    strSecondaryCellTotal = "12";
    getStrSecondaryCount = "1";
    strCellTotal = "400";
    strCellOpenOverTime = "2";
    PORT_NAME = "ttyS4";
  }
  
  public static Context getContext() {
    return mContext;
  }
  

  public void onCreate() {
    super.onCreate();
    mContext = getApplicationContext();
    strCellOpenTime = new String[Integer.parseInt(strCellTotal)];
    for (int i = 0; i < Integer.parseInt(strCellTotal); i++)
      strCellOpenTime[i] = "2200-01-01 00:00:00"; 
  }
}