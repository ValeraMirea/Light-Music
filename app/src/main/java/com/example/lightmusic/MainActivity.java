package com.example.lightmusic;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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
    private Button Scan_btn;

    // Bluetooth

    private BluetoothAdapter myBluetoothAdapter;
    BluetoothSocket clientSocket;
    private static final int DISCOVERABLE_BT_TIME = 300;
    private ListView listDevice;
    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<String>();

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
        final Button scan_btn = findViewById(R.id.ScanBt);
        final ListView listDevise = findViewById(R.id.listOfDevice);
        //ListView listDevice = (ListView) findViewById(R.id.listOfDevice);

        // Инициализация Bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // еще вариант инициализации переменых для работы программы
        /*
        modeField = (EditText) findViewById(R.id.TextModeWorking);
        power_btn = (Button) findViewById(R.id.Power);
        change_btn = (Button) findViewById(R.id.ChooseButton);
        */
        Bluetooth();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);
        // Отключаем кнопку переключения режима и скана по умолчанию
        change_btn.setEnabled(false);
        scan_btn.setEnabled(false);
        if (myBluetoothAdapter.isEnabled()) {
            bl_Image.setImageResource(R.drawable.ic_action_on);
        } else {
            Bluetooth();
            bl_Image.setImageResource(R.drawable.ic_action_off);
        }
        // Обработка нажатия кнопки питания для адресной ленты
        power_btn.setOnClickListener(view -> {

            if (myBluetoothAdapter.isEnabled()) {
                // Включаем кнопку переключения режима
                if (counter == 1) {
                    power_btn.setText("Power ON");
                    scan_btn.setEnabled(true);
                    //bl_Image.setImageResource(R.drawable.ic_action_on);
                    counter += 1;
                } else {
                    power_btn.setText("Power OFF");
                    scan_btn.setEnabled(false);
                    counter -= 1;
                }
            } else {
                Bluetooth();
            }
        });
        //Поиск и подключение к модулю БТ, убийственная штука, но тормозит все

        scan_btn.setOnClickListener(view -> {
            try {
                Check_Bluetooth();
                Bluetooth();
                 ConnectToBluetooth();
                change_btn.setEnabled(true);
            } catch (Exception e) {
                //Если есть ошибки, выводим их в лог
                Log.d("BLUETOOTH", e.getMessage());
            }
        });
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

    //Функция проверки Блютуз есть или нет
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

    // Функция работы с Bluetooth

    public void Bluetooth() {

        //Включаем Bluetooth. Если он уже активен, то игнорируется этот шаг
        if (!myBluetoothAdapter.isEnabled()) {
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
            startActivityForResult(new Intent(enableBT), 1);
            bl_Image.setImageResource(R.drawable.ic_action_on);
            //метод делающий устройство видимым для других
            Intent getVisible = new Intent(BluetoothAdapter.
                    ACTION_REQUEST_DISCOVERABLE);
            getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    DISCOVERABLE_BT_TIME);
            startActivity(getVisible);
            startActivityForResult(new Intent(enableBT), 1);
            Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
                    Toast.LENGTH_LONG).show();
        } else {
            if (myBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
                        Toast.LENGTH_LONG).show();
            } else {
                // bl_Image.setImageResource(R.drawable.ic_action_off);
                Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //Перенести в другую Активность, иначе RIP :)
    public void ConnectToBluetooth() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_BT_TIME);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }
    public void GetPairDevise(ArrayList<String> list, BluetoothAdapter bluetoothAdapter) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Выдать сообщение о ошибке
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceList.add(String.valueOf(device));
                list.add(device.getName() + "  -  " + device.getAddress());
            }
        }
    }

    public void GetScanDevise(BluetoothAdapter mYBluetoothAdapter) {
        if (listView.getCount() != 0)
            mDeviceList.clear();
        mDeviceList.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mYBluetoothAdapter.startDiscovery();
        IntentFilter myFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mResiver, myFilter);
        myFilter = new IntentFilter((BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(mResiver, myFilter);
    }
    private final BroadcastReceiver mResiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice myDevice;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                myDevice =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (myDevice.getName() != null) {
                    mDeviceList.add(String.valueOf(myDevice));
                    mDeviceList.add(myDevice.getName() + "  -  " + myDevice.getAddress());
                    listDevice.setAdapter((ListAdapter) mDeviceList);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                myBluetoothAdapter.cancelDiscovery();
            }
        }
    };





//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent
//                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
//                    return;
//                }
//                Set<BluetoothDevice> listOfDevices = myBluetoothAdapter.getBondedDevices();
//
//                if (listOfDevices.size() > 0) {
//                    Log.d("BT", "List of devices: ");
//                    for (BluetoothDevice bt : listOfDevices) {
//                        Log.d("BT", bt.getName() + " " + bt.getAddress());
//                mDeviceList.add(device.getName() + "\n" + device.getAddress());
//                Log.i("BT", device.getName() + "\n" + device.getAddress());
//                listView.setAdapter(new ArrayAdapter<String>(context,
//                        android.R.layout.simple_list_item_1, mDeviceList));
//                         //Connect to device
//                        myBluetoothAdapter.getProfileProxy(MainActivity.this, new BluetoothProfile.ServiceListener() {
//                            @RequiresApi(api = Build.VERSION_CODES.S)
//                            @Override
//                            public void onServiceConnected(int profile, BluetoothProfile proxy) {
//                                if (profile == BluetoothProfile.HEADSET) {
//                                    BluetoothHeadset headset = (BluetoothHeadset) proxy;
//                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                                        ActivityCompat.requestPermissions((Activity) context,
//                                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
//                                                1000);
//                                        return;
//                                    }
//                                    headset.getConnectionState(bt);
//                                }
//                                myBluetoothAdapter.closeProfileProxy(profile, proxy);
//                            }
//
//                            @Override
//                            public void onServiceDisconnected(int profile) {
//                            }
//                        }, BluetoothProfile.HEADSET);
//                    }
//                }
//            }
//        }
//    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
    }
}