package com.auv.sienlockdemo;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.jeremyliao.liveeventbus.LiveEventBus;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AUVCellController {

    private static final String TAG = AUVCellController.class.getSimpleName();
    private static final String TEMPERATURE_USB_VENDOR_ID = "10c4";     //供应商id
    private static final String TEMPERATURE_USB_PRODUCT_ID = "ffffea60";    //产品id
    private Context mContext;
    private UsbManager mUsbManager; //USB管理器
    private UsbSerialPort sTemperatureUsbPort = null;  //接usb端口
    private SerialInputOutputManager mSerialIoManager;  //输入输出管理器（本质是一个Runnable）
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();  //用于不断从端口读取数据
    public static  AUVCellController instance;
    //数据输入输出监听器
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    String s = Bcc16Util.byteTo16String(data).toUpperCase();
                    Log.d(TAG, "new data."+s);
                    LiveEventBus.get("newData",byte[].class).post(data);
                }
            };

    public AUVCellController(Context context) {
        mContext = context;
    }

    public static AUVCellController getInstance(Context context) {
        if (instance == null) {
            synchronized (AUVCellController.class) {
                if (instance == null) {
                    instance = new AUVCellController(context);
                }
            }
        }
        return instance;
    }

    public void initUsbControl() {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        //全部设备
        List<UsbSerialDriver> usbSerialDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        //全部端口
        List<UsbSerialPort> usbSerialPorts = new ArrayList<UsbSerialPort>();
        for (UsbSerialDriver driver : usbSerialDrivers) {
            List<UsbSerialPort> ports = driver.getPorts();
            Log.d(TAG, String.format("+ %s: %s port%s",
                    driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
            usbSerialPorts.addAll(ports);
        }
        String vendorId;
        String productId;
        //校验设备，设备是 2303设备
        for (UsbSerialPort port : usbSerialPorts) {
            UsbSerialDriver driver = port.getDriver();
            UsbDevice device = driver.getDevice();
            vendorId = NumConvertUtil.IntToHexString((short) device.getVendorId());
            productId = NumConvertUtil.IntToHexString((short) device.getProductId());
            if (vendorId.equals(TEMPERATURE_USB_VENDOR_ID) && productId.equals(TEMPERATURE_USB_PRODUCT_ID)) {
                sTemperatureUsbPort = port;
            }
        }
        if (sTemperatureUsbPort != null) {
            //成功获取端口，打开连接
            UsbDeviceConnection connection = mUsbManager.openDevice(sTemperatureUsbPort.getDriver().getDevice());
            if (connection == null) {
                Log.e(TAG, "Opening device failed");
                return;
            }
            try {
                sTemperatureUsbPort.open(connection);
                //设置波特率
                sTemperatureUsbPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (IOException e) {
                //打开端口失败，关闭！
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                try {
                    sTemperatureUsbPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sTemperatureUsbPort = null;
                return;
            }
        } else {
            //提示未检测到设备
        }
    }


    public void onDeviceStateChange() {
        //重新开启USB管理器
        stopIoManager();
        startIoManager();
    }

    private void startIoManager() {
        if (sTemperatureUsbPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sTemperatureUsbPort, mListener);
            mExecutor.submit(mSerialIoManager);  //实质是用一个线程不断读取USB端口
        }
    }
    public void write(byte[] contentStr){
        if (sTemperatureUsbPort != null) {
            try {
                sTemperatureUsbPort.write(contentStr, 10000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    public void onPause() {
        stopIoManager();
        if (sTemperatureUsbPort != null) {
            try {
                sTemperatureUsbPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sTemperatureUsbPort = null;
        }
    }

    /*
    * 开门*/
    public  void openDoor(int cellNo){
        int[] ints = parseCellID(cellNo);
        OpenDoor(ints[0],ints[1]);
    }



    public static int[] parseCellID(int arg9) {
        int v0=-1;
        int v1=-1;
        int v2= BaseApplication.strMainCellTotal.isEmpty() ? 18 : Integer.parseInt(BaseApplication.strMainCellTotal);
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


    private  void OpenDoor(int boardId, int cellId) {

        try {
            Log.i(TAG, "控制板ID:" + boardId + ", 柜位ID:" + cellId);
            byte[] v1_1=new byte[]{(byte) 0x8a, 0, 0, 0X11, 0};
            v1_1[1]= DataUtils.HexToByte(DataUtils.IntToHex(boardId));
            v1_1[2]=DataUtils.HexToByte(DataUtils.IntToHex(cellId));
            v1_1[4]=DataUtils.HexToByte(DataUtils.getBCC(v1_1));
            Log.i(TAG, "crc16_data:" + DataUtils.ByteArrToHex(v1_1));
            write(v1_1);
            msleep(550);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    private static void msleep(int arg2) {
        try {
            Thread.sleep(arg2);
        } catch (InterruptedException v0_1) {
            v0_1.printStackTrace();
        }
    }
}