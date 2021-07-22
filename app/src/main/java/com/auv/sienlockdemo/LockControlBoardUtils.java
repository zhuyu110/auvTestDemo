package com.auv.sienlockdemo;

import android.content.Context;
import android.util.Log;

import com.azhon.serialport.ReceiveDataListener;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class LockControlBoardUtils {

    private final static String TAG=LockControlBoardUtils.class.getSimpleName();
    private static LockControlBoardUtils lockControlBoardUtils;
    private final ReceiveDataListener mListener=new ReceiveDataListener() {
        @Override
        public void receiveData(ByteBuf byteBuf) {
            //将数据转成十六进制
            String hex = ByteBufUtil.hexDump(byteBuf).toUpperCase();
            Log.d(TAG, "new data."+hex);
            LiveEventBus.get("newData",byte[].class).post(byteBuf.array());
        }
    };

    /*

      获取门状态
       */
    public  void getDoorStatue(int cellNo){
        int[] ints = parseCellID(cellNo);
        getDoorStatue(ints[0],ints[1]);

    }
    private  void getDoorStatue(int boardId, int cellId){
        try {
            Log.i(TAG, "控制板ID:" + boardId + ", 柜位ID:" + cellId);
            byte[] v1_1=new byte[]{(byte) 0x80, 0, 0, 0X33, 0};
            v1_1[1]= DataUtils.HexToByte(DataUtils.IntToHex(boardId));
            v1_1[2]=DataUtils.HexToByte(DataUtils.IntToHex(cellId));
            v1_1[4]=DataUtils.HexToByte(DataUtils.getBCC(v1_1));
            Log.i(TAG, "crc16_data:" + DataUtils.ByteArrToHex(v1_1));
            SerialPortUtil.getInstances().sendSerialPort(v1_1);
            msleep(550);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public  void openDoor(int arg3) {

        if (!SerialPortUtil.getInstances().isSerialPortLive()) {
            Log.i(TAG, "aaaaaaaa");
            return;
        }
        Log.i(TAG, "开门柜位编号********------" + arg3);
        int[] v0_1=LockControlBoardUtils.parseCellID(arg3);
        LockControlBoardUtils.OpenDoor(v0_1[0], v0_1[1]);
    }

    private static void OpenDoor(int boardId, int cellId) {

        try {
            Log.i(TAG, "控制板ID:" + boardId + ", 柜位ID:" + cellId);
            byte[] v1_1=new byte[]{(byte) 0x8a, 0, 0, 0X11, 0};
            v1_1[1]= DataUtils.HexToByte(DataUtils.IntToHex(boardId));
            v1_1[2]=DataUtils.HexToByte(DataUtils.IntToHex(cellId));
            v1_1[4]=DataUtils.HexToByte(LockControlBoardUtils.getBCC(v1_1));
            Log.i(TAG, "crc16_data:" + DataUtils.ByteArrToHex(v1_1));
            new Thread(() -> OpenDoor(cellId, v1_1, boardId)).start();
            msleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

   public void closeSerialPort() {
        SerialPortUtil.getInstances().closeSerialPort();
    }

    private static String getBCC(byte[] arg7, int arg8, int arg9) {
        String v0="";
        byte[] v2=new byte[1];
        int v3=arg8;
        while (v3 < arg7.length) {
            if (v3 != arg9) {
                v2[0]=((byte) (v2[0] ^ arg7[v3]));
                ++v3;
                continue;
            }
            break;
        }

        String v3_1=Integer.toHexString(v2[0] & 255);
        if (v3_1.length() == 1) {
            v3_1='0' + v3_1;
        }
        v0=v0 + v3_1.toUpperCase();
        Log.i(TAG,"BCC验证" + v0);
        return v0;
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
        Log.i(TAG,"BCC验证:" + v0);
        return v0;
    }

   public static LockControlBoardUtils getInstances() {
       if (lockControlBoardUtils == null) {
           synchronized (AUVCellController.class) {
               if (lockControlBoardUtils == null) {
                   lockControlBoardUtils = new LockControlBoardUtils();
               }
           }
       }
       return lockControlBoardUtils;

    }

    private static void OpenDoor(int arg11, byte[] arg12, int arg13) {
        byte[] v0_1;
        String v0="开锁指令";
        String v3="开锁返回数据";
        int v4=550;
        if (arg11 == 0) {
            SerialPortUtil.getInstances().sendSerialPort(arg12);
            Log.e(TAG,v0 + DataUtils.ByteArrToHex(arg12));
            LockControlBoardUtils.msleep(v4);
            int v5_1=Integer.parseInt(BaseApplication.strMainCellTotal);
            int v6_1=Integer.parseInt(BaseApplication.strSecondaryCellTotal);
            LockControlBoardUtils.msleep(v5_1 * 550);
            for (int v7 =1; v7 <= Integer.parseInt(BaseApplication.getStrSecondaryCount); ++v7) {
                Log.i(TAG,"第 " + v7 + "块副板，全开");
                arg12[1]=DataUtils.HexToByte(DataUtils.IntToHex(arg13 + v7));
                arg12[4]=DataUtils.HexToByte(DataUtils.IntToHex(0));
                arg12[4]=DataUtils.HexToByte(LockControlBoardUtils.getBCC(arg12, 0, arg12.length));
                Log.i(TAG,"crc16_data:" + DataUtils.ByteArrToHex(arg12));
                SerialPortUtil.getInstances().sendSerialPort(arg12);
                LockControlBoardUtils.msleep(v4);
                LockControlBoardUtils.msleep(v6_1 * 500);
            }
        } else {
            SerialPortUtil.getInstances().sendSerialPort(arg12);
            Log.e(TAG,v0 + DataUtils.ByteArrToHex(arg12));
            LockControlBoardUtils.msleep(v4);
        }



    }


    private static void msleep(int arg2) {
        try {
            Thread.sleep(arg2);
        } catch (InterruptedException v0_1) {
            v0_1.printStackTrace();
        }
    }

    public void openSerialPort() {
        SerialPortUtil.getInstances().openSerialPort(BaseApplication.PORT_NAME);
        SerialPortUtil.getInstances().setReceiverListener(mListener);
    }

    public static int[] parseCellID(int arg9) {
        int v0=-1;
        int v1=-1;
        int v2=BaseApplication.strMainCellTotal.isEmpty() ? 18 : Integer.parseInt(BaseApplication.strMainCellTotal);
        int v3=BaseApplication.strSecondaryCellTotal.isEmpty() ? 12 : Integer.parseInt(BaseApplication.strSecondaryCellTotal);
        Log.i(TAG,"主板位数:" + v2 + "---副板位数:" + v3);
        if (arg9 <= v2) {
            v1=1;
            v0=arg9;
        }

        if (arg9 > v2) {
            v1=(((int) Math.ceil((((double) (arg9 - v2))) * 1 / (((double) v3))))) + 1;
            v0=(arg9 - v2) % v3;
            if (v0 == 0) {
                v0=v3;
            }
        }

        Log.i(TAG,"板ID:" + v1 + "锁ID:" + v0);
        return new int[]{v1, v0};
    }


  public void   onDeviceStateChange(){
        closeSerialPort();
        openSerialPort();
    }
}