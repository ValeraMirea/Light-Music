package com.example.lightmusic;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        // Bluetooth настройки
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Получение списка сопряженных устройств
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            // Здесь будут отображаться все сопряженные устройства. Получаем имя и MAC адресс каждого.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC адрес
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName, deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }
            // Отображение сопряженного устройства с помощью RecyclerView
            RecyclerView recyclerView = findViewById(R.id.textViewDeviceName);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this, deviceList);
            recyclerView.setAdapter(deviceListAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            View view = findViewById(R.id.textViewDeviceName);
            Snackbar snackbar = Snackbar.make(view, "Активируйте Bluetooth или добавьте устройство в список сопряженных", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    //Включаем Bluetooth. Если он уже активен, то игнорируется этот шаг
                    if (ActivityCompat.checkSelfPermission(SelectDeviceActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    if (bluetoothAdapter.disable()) {
                        Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
                                Toast.LENGTH_LONG).show();
                    }
                    if (!bluetoothAdapter.isEnabled()) {
                        String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
                        if (ActivityCompat.checkSelfPermission(SelectDeviceActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        startActivityForResult(new Intent(enableBT), 1);
                    }
                    else if(bluetoothAdapter == null){
                        // Устройство не поддерживает Bluetooth
                        Toast.makeText(getApplicationContext(), "Это устройство не поддерживает Bluetooth",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            snackbar.show();
            handler.postDelayed(() -> {
                finish();
                overridePendingTransition(R.anim.slidein, R.anim.slideout); //Анимация обновления (вертикальная)
                onBackPressed();
                startActivity(getIntent());
                overridePendingTransition(R.anim.slidein, R.anim.slideout);
            }, 5000);
        }
    }
    @Override
    public void onBackPressed() {
        handler.removeCallbacksAndMessages(null); // остановить таймер
        super.onBackPressed();
    }
}
