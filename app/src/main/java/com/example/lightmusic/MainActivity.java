package com.example.lightmusic;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    //Переменные

    private int ChooseMode;
    private int counter;
    private final static int REQUEST_ENABLE_BT = 0;

    //Управляющие элементы

    private EditText modeField;
    private Button power_btn;
    private Button change_btn;
    private ImageView bl_Image;

    // Bluetooth

    private BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket clientSocket;

    public MainActivity() {
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Создание потоков для работы Bluetooth
        ChooseMode = 1;
        counter = 1;
        //Создание и инициализация компонентов и блоков программы

        final Button power_btn = findViewById(R.id.Power);
        final Button change_btn = findViewById(R.id.ChooseButton);
        final TextView modeField = findViewById(R.id.TextModeWorking);
        final ImageView bl_Image = findViewById(R.id.Bluetooth);

        // Инициализация Bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // еще вариант инициализации переменых для работы программы
        /*
        modeField = (EditText) findViewById(R.id.TextModeWorking);
        power_btn = (Button) findViewById(R.id.Power);
        change_btn = (Button) findViewById(R.id.ChooseButton);
        */
        Bluetooth();
        // Отключаем кнопку переключения режима по умолчанию
        change_btn.setEnabled(false);
        if (myBluetoothAdapter.isEnabled()){
            bl_Image.setImageResource(R.drawable.ic_action_on);
        }
        else{
            Bluetooth();
            bl_Image.setImageResource(R.drawable.ic_action_off);
        }
        // Обработка нажатия кнопки питания для адресной ленты
        power_btn.setOnClickListener(view -> {

            if (myBluetoothAdapter.isEnabled()){
                // Включаем кнопку переключения режима
                if (counter == 1) {
                    power_btn.setText("Power ON");
                    change_btn.setEnabled(true);
                    //bl_Image.setImageResource(R.drawable.ic_action_on);
                    counter += 1;
                } else {
                    power_btn.setText("Power OFF");
                    change_btn.setEnabled(false);
                    counter -= 1;
                }
            }
            else{
                Bluetooth();
            }
        });
        //Поиск и подключение к модулю БТ, убийственная штука, но тормозит все
        ConnectToBluetooth();
        //обработчик событий кнопки переключения режимов
        change_btn.setOnClickListener(view -> {
            try {
                //Получаем выходной поток для передачи данных
                OutputStream outStream = clientSocket.getOutputStream();

                //Значение переменнгой, которую будет отправлять по БТ
                int value = 0;

                //В зависимости от того, какая кнопка была нажата,
                //изменяем данные для посылки

            switch (ChooseMode++) {
                case 1:
                    value = (change_btn.isPressed() ? 1 : 0) + 60;
                    modeField.setText("Выбран режим столбик громкости от зеленого к красному" + " " + value);
                    break;
                case 2:
                    value = (change_btn.isPressed() ? 1 : 0) + 70;
                    modeField.setText("Выбран режим столбик громкости бегущая радуга" + " " + value);
                    break;
                case 3:
                    value = (change_btn.isPressed() ? 1 : 0) + 80;
                    modeField.setText("Выбран режим светомузыки по частотам (5 полос симетрично)" + " " + value);
                    break;
                case 4:
                    value = (change_btn.isPressed() ? 1 : 0) + 90;
                    modeField.setText("Выбран режим светомузыки по частотам (3 полосы)" + " " + value);
                    ChooseMode = 1;
                    break;
            }

                //Пишем данные в выходной поток
                outStream.write(value);
            } catch (IOException e) {
                //Если есть ошибки, выводим их в лог
                Log.d("BLUETOOTH", e.getMessage());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    private void Check_Bluetooth() {
        if (myBluetoothAdapter == null) {
                    // Устройство не поддерживает Bluetooth
                    Toast.makeText(getApplicationContext(), "Это устройство не поддерживает Bluetooth",
                            Toast.LENGTH_LONG).show();
//                    bluetooth_flag = false;
                } else if (!myBluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
                            Toast.LENGTH_LONG).show();
//                    bluetooth_flag = false;
//                } else {
//                    Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
//                            Toast.LENGTH_LONG).show();
//                    bluetooth_flag = true;
                }
    }
    public void Bluetooth(){

        //Включаем Bluetooth. Если он уже активен, то игнорируется этот шаг
        String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else{
            Check_Bluetooth();
        }
        startActivityForResult(new Intent(enableBT), 1);
        Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
                Toast.LENGTH_LONG).show();
    }
    //Перенести в другую Активность, иначе RIP :)
    public void ConnectToBluetooth(){
        try{
            //Устройство с данным адресом - наш Bluetooth Bee
            //Адрес опредеяется следующим образом: установите соединение
            //между ПК и модулем (пин: 1234), а затем посмотрите в настройках
            //соединения адрес модуля. Скорее всего он будет аналогичным.
            BluetoothDevice device = myBluetoothAdapter.getRemoteDevice("00:13:02:01:00:09");

            //Инициируем соединение с устройством
            Method m = device.getClass().getMethod(
                    "createRfcommSocket", new Class[] {int.class});

            clientSocket = (BluetoothSocket) m.invoke(device, 1);
            clientSocket.connect();

            //В случае появления любых ошибок, выводим в лог сообщение
        } catch (IOException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (SecurityException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (InvocationTargetException e) {
            Log.d("BLUETOOTH", e.getMessage());
        }

        //Выводим сообщение об успешном подключении
        Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();
    }

//    public void Bluetooth(){
//        if (!myBluetoothAdapter.isEnabled()) {
//            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
//            {
//                Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
//                        Toast.LENGTH_LONG).show();
//                bl_Image.setImageResource(R.drawable.ic_action_off);
//            }
//            startActivity(turnOn);
//        } else {
//            Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
//                        Toast.LENGTH_LONG).show();
//                bl_Image.setImageResource(R.drawable.ic_action_on);
//        }
//        Toast.makeText(getApplicationContext(), "Это устройство не поддерживает Bluetooth",
//                        Toast.LENGTH_LONG).show();
//                bl_Image.setImageResource(R.drawable.ic_action_off);
//                myBluetoothAdapter.disable();
//    }
//    public boolean BluetoothON() {
//        if (!myBluetoothAdapter.isEnabled()) {
//            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
//            {
//                return false;
//            }
//            startActivity(turnOn);
//        } else {
//            return true;
//        }
//        return false;
//    }
//    public void GetPairDevise(ArrayList<String> list, BluetoothAdapter bluetoothAdapter) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // Выдать сообщение о ошибке
//            return;
//        }
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                arrayNewDevice.add(device);
//                list.add(device.getName() + "  -  " + device.getAddress());
//            }
//        }
//    }
//    public void GetScanDevise(BluetoothAdapter mYBluetoothAdapter) {
//        if (listNewDeviceAdapter.getCount() != 0)
//            listNewDeviceAdapter.clear();
//        arrayNewDevice.clear();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mYBluetoothAdapter.startDiscovery();
//        IntentFilter myFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mResiver, myFilter);
//        myFilter = new IntentFilter((BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
//        registerReceiver(mResiver, myFilter);
//    }
//    private final BroadcastReceiver mResiver = new BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                myDevice =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (myDevice.getName() != null) {
//                    arrayNewDevice.add(myDevice);
//                    deviseScanList.add(myDevice.getName() + "  -  " + myDevice.getAddress());
//                    listDevice.setAdapter(listNewDeviceAdapter);
//                }
//            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                myBluetoothAdapter.cancelDiscovery();
//            }
//        }
//    };
}