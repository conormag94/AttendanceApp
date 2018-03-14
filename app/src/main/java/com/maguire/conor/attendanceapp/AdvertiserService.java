package com.maguire.conor.attendanceapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;

public class AdvertiserService extends Service {

    public static boolean running = false;

    private BluetoothLeAdvertiser advertiser;

    private MyAdvertiseCallback advertiserCallback;

    private String studentNumber;

    @Override
    public void onCreate() {
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
        advertise();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        stopAdvertising();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void advertise() {
        goForeground();

        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( true )
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("EB342F19-99A4-4155-97CD-D3BFBF9E574B"));

        String advertiseData = studentNumber;
        Log.i("BLE_NUMBER", "Sending: " + advertiseData);

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceData( pUuid, advertiseData.getBytes( Charset.forName( "UTF-8" ) ) )
                .build();

        advertiserCallback = new MyAdvertiseCallback();
        advertiser.startAdvertising( settings, data, advertiserCallback );
    }

    private void stopAdvertising() {
        advertiser.stopAdvertising(advertiserCallback);
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

}
