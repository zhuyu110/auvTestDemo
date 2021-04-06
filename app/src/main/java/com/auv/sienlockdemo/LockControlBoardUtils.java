package com.auv.sienlockdemo;

import android.util.Log;

public class LockControlBoardUtils {

    private final static String TAG=LockControlBoardUtils.class.getSimpleName();

    public interface IsDoorOpenCallBack {
        void fail(String arg1);

        void success(boolean arg1, String arg2);
    }

    public interface OpenDoorCallBack {
        void fail(String arg1);

        void success(boolean arg1);
    }

    public interface ReadAllDoorStatusCallBack {
        void fail(String arg1);

        void success(String arg1);
    }

    private static LockControlBoardUtils lockControlBoardUtils;
    private static OpenDoorCallBack mopenDoorCallBack;


    public LockControlBoardUtils() {
        super();
    }

    public static void OpenCell(int arg3, OpenDoorCallBack arg4) {
        if (!SerialPortUtil.getInstances().isSerialPortLive()) {
            Log.i(TAG, "aaaaaaaa");
            return;
        }
        Log.i(TAG, "开门柜位编号********------" + arg3);
        LockControlBoardUtils.mopenDoorCallBack=arg4;
        int[] v0_1=LockControlBoardUtils.parseCellID(arg3);
        LockControlBoardUtils.OpenDoor(v0_1[0], v0_1[1]);
    }

    private static void OpenDoor(int boardId, int cellId) {

        try {
            Log.i(TAG, "控制板ID:" + boardId + ", 柜位ID:" + cellId);
            byte[] v1_1=new byte[]{(byte) 0x8a, 0, 0, 0X11, 0};
            v1_1[1]=DataUtils.HexToByte(DataUtils.IntToHex(boardId));
            v1_1[2]=DataUtils.HexToByte(DataUtils.IntToHex(cellId));
            v1_1[4]=DataUtils.HexToByte(LockControlBoardUtils.getBCC(v1_1));
            Log.i(TAG, "crc16_data:" + DataUtils.ByteArrToHex(v1_1));
            new Thread(() -> OpenDoor(cellId, v1_1, boardId)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void closeSerialPort() {
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

    private static String getBCC(byte[] arg7) {
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

    static LockControlBoardUtils getInstances() {

        if (LockControlBoardUtils.lockControlBoardUtils == null) {

            LockControlBoardUtils.lockControlBoardUtils=new LockControlBoardUtils();
        }

        return LockControlBoardUtils.lockControlBoardUtils;
    }


//    public static void isDoorOpen(int arg8, IsDoorOpenCallBack arg9) {
//        Class v0=LockControlBoardUtils.class;
//        __monitor_enter(v0);
//        try {
//            LockControlBoardUtils.getSerialPortUtil();
//            int[] v1=LockControlBoardUtils.parseCellID(arg8);
//            byte[] v2=new byte[]{-128, 0, 0, 51, 0};
//            v2[1]=DataUtils.HexToByte(DataUtils.IntToHex(v1[0]));
//            v2[2]=DataUtils.HexToByte(DataUtils.IntToHex(v1[1]));
//            v2[4]=DataUtils.HexToByte(LockControlBoardUtils.getBCC(v2, 0, v2.length));
//            Logger v3=LockControlBoardUtils.logger;
//            v3.error("查询单个锁状态" + DataUtils.ByteArrToHex(v2));
//            new Thread(new - $$Lambda$LockControlBoardUtils$m1gvYa3JpjK7obRkkOzgEvNh0DA(v2, arg9)).start();
//        } catch (Throwable v8) {
//            __monitor_exit(v0);
//            throw v8;
//        }
//
//        __monitor_exit(v0);
//    }

    private static void OpenDoor(int arg11, byte[] arg12, int arg13) {
        byte[] v0_1;
        String v0="开锁指令";
        String v3="开锁返回数据";
        int v4=550;
        if (arg11 == 0) {
            SerialPortUtil.getInstances().sendSerialPort(arg12);
            Log.e(TAG,v0 + DataUtils.ByteArrToHex(arg12));
            LockControlBoardUtils.msleep(v4);
            v0_1= SerialPortUtil.getInstances().receiveData();
            Log.i(TAG,v3 + DataUtils.ByteArrToHex(v0_1));
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
                Log.i(TAG,v3 + DataUtils.ByteArrToHex(SerialPortUtil.getInstances().receiveData()));
                LockControlBoardUtils.msleep(v6_1 * 500);
            }
        } else {
            SerialPortUtil.getInstances().sendSerialPort(arg12);
            Log.e(TAG,v0 + DataUtils.ByteArrToHex(arg12));
            LockControlBoardUtils.msleep(v4);
        }

        v0_1 = SerialPortUtil.getInstances().receiveData();

        Log.i(TAG,v3 + DataUtils.ByteArrToHex(v0_1));
        if (v0_1.length != 5) {
            LockControlBoardUtils.mopenDoorCallBack.fail("读取锁的返回值失败");
        } else if (v0_1[3] == 0) {
            LockControlBoardUtils.mopenDoorCallBack.success(false);
        } else {
            LockControlBoardUtils.mopenDoorCallBack.success(true);
        }
    }
//
//    static void lambda$isDoorOpen$1(byte[] arg6, IsDoorOpenCallBack arg7) {
//        LockControlBoardUtils.serialPortUtil.sendSerialPort(arg6);
//        LockControlBoardUtils.msleep(200);
//        byte[] v0=LockControlBoardUtils.serialPortUtil.receiveData();
//        if (v0.length != 5) {
//            arg7.fail("读取返回值信息失败");
//        } else if (DataUtils.HexToByte(LockControlBoardUtils.getBCC(v0, 0, v0.length - 1)) == v0[v0.length - 1]) {
//            Logger v1=LockControlBoardUtils.logger;
//            v1.error("查询返回" + DataUtils.ByteArrToHex(v0));
//            if (v0[3] == 0) {
//                arg7.success(true, DataUtils.ByteArrToHex(v0));
//            } else {
//                arg7.success(false, DataUtils.ByteArrToHex(v0));
//            }
//        }
//    }
//
//    static void lambda$readAllDoorStatus$2(byte[] arg21, int arg22, int arg23, int arg24, ReadAllDoorStatusCallBack arg25) {
//        Logger v3;
//        long v0_5;
//        String v5_2;
//        SimpleDateFormat v0_4;
//        String v16;
//        Logger v0;
//        int v1=arg24;
//        ReadAllDoorStatusCallBack v2=arg25;
//        LockControlBoardUtils.serialPortUtil.sendSerialPort(arg21);
//        LockControlBoardUtils.msleep(200);
//        byte[] v4=LockControlBoardUtils.serialPortUtil.receiveData();
//        int v5=1;
//        if (DataUtils.HexToByte(LockControlBoardUtils.getBCC(v4, 0, v4.length - 1)) == v4[v4.length - 1]) {
//            int v6=0;
//            while (v6 < arg22) {
//                String v8=DataUtils.Byte2Hex(Byte.valueOf(v4[v4.length - 3 - v6]));
//                int v9=Integer.parseInt(v8, 16);
//                v0=LockControlBoardUtils.logger;
//                v0.info("数据标志位:" + v8 + "----" + v9 + "----Offset:" + v6);
//                int v10_1=0;
//                while (v10_1 < 8) {
//                    if (v6 * 8 + v10_1 >= arg23) {
//                    } else {
//                        int v12=v1 == v5 ? v6 * 8 + v10_1 : Integer.parseInt(BaseApplication.strMainCellTotal) + (v1 - 2) * Integer.parseInt(BaseApplication.strSecondaryCellTotal) + v6 * 8 + v10_1;
//                        String v0_1="2200-01-01 00:00:00";
//                        String v14="门:";
//                        if ((v9 & v5 << v10_1) == 0) {
//                            Logger v15=LockControlBoardUtils.logger;
//                            v15.info(v14 + (v12 + 1) + "  状态开，时间:" + BaseApplication.strCellOpenTime[v12]);
//                            String v1_1="yyyy-MM-dd HH:mm:ss";
//                            if (BaseApplication.strCellOpenTime[v12].equals(v0_1)) {
//                                BaseApplication.strCellOpenTime[v12]=new SimpleDateFormat(v1_1).format(new Date(System.currentTimeMillis()));
//                                v16=v8;
//                                goto label_205;
//                            }
//
//                            v0=LockControlBoardUtils.logger;
//                            v0.info(v14 + (v12 + 1) + "  状态开 ****************************************");
//                            try {
//                                v0_4=new SimpleDateFormat(v1_1);
//                                v5_2=v8;
//                            } catch (Exception v0_2) {
//                                v16=v8;
//                                goto label_171;
//                            } catch (ParseException v0_3) {
//                                v16=v8;
//                                goto label_175;
//                            }
//
//                            try {
//                                v0_5=new Date(System.currentTimeMillis()).getTime() - v0_4.parse(BaseApplication.strCellOpenTime[v12]).getTime();
//                                v3=LockControlBoardUtils.logger;
//                                v16=v5_2;
//                            } catch (Exception v0_2) {
//                                v16=v5_2;
//                                goto label_171;
//                            } catch (ParseException v0_3) {
//                                v16=v5_2;
//                                goto label_175;
//                            }
//
//                            try {
//                                v3.info(v14 + (v12 + 1) + "开门时长: ***************" + v0_5);
//                                if (v0_5 <= (((long) (Integer.parseInt(BaseApplication.strCellOpenOverTime) * 1000)))) {
//                                    goto label_205;
//                                }
//
//                                v3=LockControlBoardUtils.logger;
//                                v3.info("柜门超时报警时间: ***************" + BaseApplication.strCellOpenOverTime);
//                                LockControlBoardUtils.logger.info("播报");
//                                goto label_205;
//                            } catch (Exception v0_2) {
//                            } catch (ParseException v0_3) {
//                                label_175:
//                                v0_3.printStackTrace();
//                                goto label_205;
//                            }
//
//                            label_171:
//                            v0_2.printStackTrace();
//                            goto label_205;
//                        }
//
//                        v16=v8;
//                        if (BaseApplication.strCellOpenTime[v12].equals(v0_1)) {
//                            v0=LockControlBoardUtils.logger;
//                            v0.info(v14 + (v12 + 1) + "一直关闭");
//                        } else {
//                            Logger v1_3=LockControlBoardUtils.logger;
//                            v1_3.info(v14 + (v12 + 1) + "刚关闭--------------------------");
//                            BaseApplication.strCellOpenTime[v12]=v0_1;
//                        }
//
//                        label_205:
//                        ++v10_1;
//                        v1=arg24;
//                        v8=v16;
//                        v5=1;
//                        continue;
//                    }
//
//                    break;
//                }
//
//                ++v6;
//                v5=1;
//                v1=arg24;
//            }
//
//            v0=LockControlBoardUtils.logger;
//            v0.error("查询返回" + DataUtils.ByteArrToHex(v4));
//            v2.success(DataUtils.ByteArrToHex(v4));
//        } else {
//            v2.fail("读取返回值信息失败");
//        }
//    }

    private static void msleep(int arg2) {
        try {
            Thread.sleep(arg2);
        } catch (InterruptedException v0_1) {
            v0_1.printStackTrace();
        }
    }

    public void openSerialPort() {
        SerialPortUtil.getInstances().openSerialPort(BaseApplication.PORT_NAME);

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

//    public static void readAllDoorStatus(int arg12, ReadAllDoorStatusCallBack arg13) {
//        int v2;
//        int v1;
//        Class v0=LockControlBoardUtils.class;
//        __monitor_enter(v0);
//        try {
//            LockControlBoardUtils.getSerialPortUtil();
//            double v3=8;
//            if (arg12 == 1) {
//                v1=((int) Math.ceil(Double.parseDouble(BaseApplication.strMainCellTotal) / v3));
//                v2=Integer.parseInt(BaseApplication.strMainCellTotal);
//            } else {
//                v1=((int) Math.ceil(Double.parseDouble(BaseApplication.strSecondaryCellTotal) / v3));
//                v2=Integer.parseInt(BaseApplication.strSecondaryCellTotal);
//            }
//
//            byte[] v3_1=new byte[]{-128, 0, 0, 51, 0};
//            v3_1[1]=DataUtils.HexToByte(DataUtils.IntToHex(arg12));
//            ((byte[]) v3)[4]=DataUtils.HexToByte(LockControlBoardUtils.getBCC(((byte[]) v3), 0, ((byte[]) v3).length));
//            new Thread(new - $$Lambda$LockControlBoardUtils$yUptHUkJPm5IyssCO6NyelG1ahI(v3_1, v1, v2, arg12, arg13)).start();
//        } catch (Throwable v12) {
//            __monitor_exit(v0);
//            throw v12;
//        }
//
//        __monitor_exit(v0);
//    }
}