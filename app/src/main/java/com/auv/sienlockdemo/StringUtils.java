package com.auv.sienlockdemo;

import android.text.TextUtils;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;

public class StringUtils {
    @Contract("null -> true")
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    @NotNull
    public static List<String> getAllDevices() {

        SerialPortFinder serialPortFinder = new SerialPortFinder();

        String[] allSerialPorts= serialPortFinder.getAllDevices();

        List<String> serialPortList = new ArrayList<>();

        for (String serialPort : allSerialPorts) {

            String DEV = "/dev/";

            String serial_pix_1 ="/dev/ttyS";

            String serial_pix_2 ="/dev/ttymxc";

            String serial_pix=null;

            if(!serialPort.contains(DEV)){

                serialPort = DEV + serialPort;

            }

            if (serialPort.startsWith(serial_pix_1) ) {
                 serial_pix = serial_pix_1;
            }else if(serialPort.startsWith(serial_pix_2) ) {
                serial_pix = serial_pix_2;
            }

            if (!TextUtils.isEmpty(serial_pix)) {

                serialPort=  serialPort.substring(0, serial_pix.length()+1);

                serialPortList.add(serialPort);

            }


        }
        return serialPortList;
    }
}
