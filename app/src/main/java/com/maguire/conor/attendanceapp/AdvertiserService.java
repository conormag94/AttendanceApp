package com.maguire.conor.attendanceapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

public class AdvertiserService extends Service {

    private static final String LOG_TAG = AdvertiserService.class.getSimpleName();

    public static boolean running = false;

    private BluetoothManager bluetoothManager;
    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer gattServer;

    private MyAdvertiseCallback advertiserCallback;

    private String studentNumber;
    private int numberOfReads;


    @Override
    public void onCreate() {
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        running = true;
        Log.i("BLE_ADVERTISE", "Creating AdvertiserService");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentExtra = intent.getStringExtra(MainActivity.STUDENT_NUMBER);
        if (intentExtra != null)
            studentNumber = intentExtra;
        else
            studentNumber = "Data";

        Log.i("BLE_NUMBER", "Student Number: " + studentNumber);

        numberOfReads = 0;
        advertise();
        startGATTServer();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        stopAdvertising();
        stopGATTServer();

        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void advertise() {
        goForeground();

        advertiser = bluetoothManager.getAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( true )
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("EB342F19-99A4-4155-97CD-D3BFBF9E574B"));

        String advertiseData = studentNumber;
        Log.i("BLE_NUMBER", "Sending: " + advertiseData);

//        AdvertiseData data = new AdvertiseData.Builder()
//                .setIncludeDeviceName( true )
//                .addServiceData( pUuid, advertiseData.getBytes( Charset.forName( "UTF-8" ) ) )
//                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( false )
                .addServiceUuid( new ParcelUuid(StudentAttendanceProfile.STUDENT_NUMBER_SERVICE))
                .build();

        Log.i(LOG_TAG, data.toString());
        advertiserCallback = new MyAdvertiseCallback();
        advertiser.startAdvertising( settings, data, advertiserCallback );
    }

    private void stopAdvertising() {
        advertiser.stopAdvertising(advertiserCallback);
    }

    private class MyAdvertiseCallback  extends AdvertiseCallback {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i("BLE_ADVERTISE", "Advertising");
            super.onStartSuccess(settingsInEffect);
//            advertisingStatus = (TextView) findViewById(R.id.advertising_status);
//            advertisingStatus.setText("Currently Advertising");
        }

        @Override
        public void onStartFailure(int errorCode) {
            stopSelf();
            Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };

    private void startGATTServer() {
        Log.d(LOG_TAG, "Starting Gatt Server...");
        gattServer = bluetoothManager.openGattServer(this, gattServerCallback);
        gattServer.addService(StudentAttendanceProfile.createStudentNumberService());
        Log.d(LOG_TAG, "Gatt Server Started");
    }

    private void stopGATTServer() {
        Log.d(LOG_TAG, "Stopping Gatt Server...");
        gattServer.close();
        Log.d(LOG_TAG, "Gatt Server Stopped");
    }

    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(LOG_TAG, "BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(LOG_TAG, "BluetoothDevice DISCONNECTED: " + device);
            }
        }

                @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(StudentAttendanceProfile.STUDENT_NUMBER)) {
                Log.d(LOG_TAG, "Read request for " + characteristic.toString() + " from " + device.toString());
                boolean success = gattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        studentNumber.getBytes());

                // Student number has been successfully read by the scanner
                if (success && numberOfReads < 2) {
                    numberOfReads++;

                    if (numberOfReads == 1)
                        sleepService();
                    else if (numberOfReads == 2)
                        stopSelf();
                }


            } else {
                Log.d(LOG_TAG, "Invalid characteristic");
                gattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }
    };

    // Stops the advertising and gatt server for a period of time before restarting
    private void sleepService() {
        final Handler handler = new Handler(Looper.getMainLooper());
        Log.d(LOG_TAG, "Sleeping the Service******");
        stopAdvertising();
        stopGATTServer();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "*******Restarting the Service");
                advertise();
                startGATTServer();
            }
        }, 5000);
    }

    private void goForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification n = new Notification.Builder(this)
                .setContentTitle("Signing you in")
                .setContentText("Recording your attendance via Bluetooth")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, n);
    }

}
