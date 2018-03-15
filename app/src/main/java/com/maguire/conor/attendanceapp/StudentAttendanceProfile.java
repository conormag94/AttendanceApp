package com.maguire.conor.attendanceapp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by Conor on 14/03/2018.
 */

public class StudentAttendanceProfile {

    private static final String LOG_TAG = StudentAttendanceProfile.class.getSimpleName();

    public static UUID STUDENT_NUMBER_SERVICE = UUID.fromString("4CC28E11-4465-4136-B84C-AB34109B3D87");
    public static UUID STUDENT_NUMBER = UUID.fromString("8FB63C83-2AAC-44DC-8FB6-6BE9257CBDD1");

    public static BluetoothGattService createStudentNumberService() {
        BluetoothGattService gattService = new BluetoothGattService(STUDENT_NUMBER_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic studentNumber = new BluetoothGattCharacteristic(STUDENT_NUMBER,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        gattService.addCharacteristic(studentNumber);
        return  gattService;
    }
}
