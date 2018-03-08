package com.maguire.conor.attendanceapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // This is an extra: Key-Value data that can be transferred from one intent to another
    public static final String EXTRA_MESSAGE = "com.maguire.conor.attendanceapp.MESSAGE";
    public static final String STUDENT_NUMBER = "com.maguire.conor.attendanceapp.STUDENT_NUMBER";

    public TextView advertisingStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        advertisingStatus = (TextView) findViewById(R.id.advertising_status);
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AdvertiserService.running) {
            advertisingStatus.setText("Currently Advertising");
        } else {
            advertisingStatus.setText("Not Currently Advertising");
        }
    }

    public void startAdvertising(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String studentNumber = editText.getText().toString();

        Intent serviceIntent = new Intent(this, AdvertiserService.class);
        serviceIntent.putExtra(STUDENT_NUMBER, studentNumber);
        startService(serviceIntent);

        advertisingStatus.setText("Currently Advertising");
    }

    public void stopAdvertising(View view) {
        Intent serviceIntent = new Intent(this, AdvertiserService.class);
        stopService(serviceIntent);
        advertisingStatus.setText("Not Currently Advertising");
    }

    public void advertise(View view) {
        Log.i("BLE_ADVERTISE", "Advertising");

        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

//        BluetoothAdapter.getDefaultAdapter().setName("Mag");

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("EB342F19-99A4-4155-97CD-D3BFBF9E574B"));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceData( pUuid, "Data".getBytes( Charset.forName( "UTF-8" ) ) )
                .build();

//        AdvertiseData data = new AdvertiseData.Builder()
//                .setIncludeDeviceName( true )
//                .addServiceUuid( pUuid )
//                .addServiceData( pUuid, "Data".getBytes( Charset.forName( "UTF-8" ) ) )
//                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
    }
}
