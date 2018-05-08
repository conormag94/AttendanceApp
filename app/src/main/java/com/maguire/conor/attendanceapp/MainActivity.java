package com.maguire.conor.attendanceapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

        BluetoothAdapter.getDefaultAdapter().enable();
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

}
