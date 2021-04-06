package com.auv.sienlockdemo;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataUtils {
  public static String Byte2Hex(Byte paramByte) {
    return String.format("%02x", new Object[] { paramByte }).toUpperCase();
  }
  
  public static String ByteArrToHex(byte[] paramArrayOfbyte) {
    StringBuilder stringBuilder = new StringBuilder();
    int j =0;
    if(paramArrayOfbyte!=null){
     j=paramArrayOfbyte.length;
    }
    for (int i = 0; i < j; i++)
      stringBuilder.append(Byte2Hex(Byte.valueOf(paramArrayOfbyte[i]))); 
    return stringBuilder.toString();
  }
  
  public static String ByteArrToHex(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
    StringBuilder stringBuilder = new StringBuilder();
    while (paramInt1 < paramInt2) {
      stringBuilder.append(Byte2Hex(Byte.valueOf(paramArrayOfbyte[paramInt1])));
      paramInt1++;
    } 
    return stringBuilder.toString();
  }
  
  public static byte HexToByte(String paramString) {
    return (byte)Integer.parseInt(paramString, 16);
  }

  public static int HexToInt(String paramString) {
    return Integer.parseInt(paramString, 16);
  }
  
  public static String IntToHex(int paramInt) {
    return Integer.toHexString(paramInt);
  }
  
  public static List<String> getDivLines(String paramString, int paramInt) {
    ArrayList<String> arrayList = new ArrayList();
    int j = paramString.length();
    int k = (int)Math.floor((paramString.length() / paramInt));
    for (int i = 0; i < k; i++)
      arrayList.add(paramString.substring(i * paramInt, (i + 1) * paramInt)); 
    if (j % paramInt > 0)
      arrayList.add(paramString.substring(k * paramInt, paramString.length())); 
    return arrayList;
  }
  
  public static int isOdd(int paramInt) {
    return paramInt & 0x1;
  }
  
  public static String sum(String paramString) {
    List<String> list = getDivLines(paramString, 2);
    int i = 0;
    Iterator<String> iterator = list.iterator();
    while (iterator.hasNext())
      i += HexToInt(iterator.next()); 
    String str = twoByte(IntToHex(i));
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(paramString);
    stringBuilder.append(str);
    return stringBuilder.toString().toUpperCase();
  }
  
  public static String twoByte(String paramString) {
    if (paramString.length() > 4)
      return paramString.substring(0, 4); 
    int j = paramString.length();
    for (int i = 0; i < 4 - j; i++) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("0");
      stringBuilder.append(paramString);
      paramString = stringBuilder.toString();
    } 
    return paramString;
  }

  public static String getBCC(byte[] arg7) {
    String v0="";
    byte[] v2=new byte[1];
    int v3;
    for (v3=0; v3 < arg7.length; ++v3) {
      v2[0]=((byte) (v2[0] ^ arg7[v3]));
    }

    String v3_1=Integer.toHexString(v2[0] & 255);
    if (v3_1.length() == 1) {
      v3_1='0' + v3_1;
    }

    v0=v0 + v3_1.toUpperCase();
    Log.i("","BCC验证:" + v0);
    return v0;
  }
}


/* Location:              D:\BaiduNetdiskDownload\Android反编译\dex2jar-2.0\classes2-dex2jar.jar!\com\example\sienlockdemo\DataUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */