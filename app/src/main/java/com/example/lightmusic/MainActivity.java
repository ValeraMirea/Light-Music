package com.example.lightmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {

    //Переменные
    private int ChooseMode;
    private int counter;
    private EditText modeField;
    private Button power_btn;
    private Button change_btn;
    private ImageView bl_Image;
    private boolean Bluetooth_flag;

    // Bluetooth
    private BluetoothAdapter myBluetoothAdapter;
    // private BluetoothClient mBluetoothClient;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ChooseMode = 1;
        counter = 1;
        Bluetooth_flag = false;

        //Создание и инициализация компонентов и блоков программы

        final Button power_btn = findViewById(R.id.Power);
        final Button change_btn = findViewById(R.id.ChooseButton);
        final TextView modeField = findViewById(R.id.TextModeWorking);
        final ImageView bl_Image = findViewById(R.id.Bluetooth);


        // еще вариант инициализации переменых для работы программы
        /*
        modeField = (EditText) findViewById(R.id.TextModeWorking);
        power_btn = (Button) findViewById(R.id.Power);
        change_btn = (Button) findViewById(R.id.ChooseButton);
        */

        // Отключаем кнопку переключения режима по умолчанию
        change_btn.setEnabled(false);
        bl_Image.setImageResource(R.drawable.ic_action_off);

        // Инициализация Bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // mBluetoothClient= new BluetoothClient(myBluetoothAdapter);
        // mMode = "Mode1";


//        // Обработчик событий Bluetooth
//        mBluetoothClient.setHandler(new Handler(){
//            // Обработчик сообщений
//            @Override
//            public void handleMessage(Message msg) {
//
//                if (msg.what == BluetoothClient.MESSAGE_READ) {
//                    String readMessage = null;
//                    try {
//                        readMessage = new String((byte[]) msg.obj, "UTF-8");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    // Обновить UI поле с текущим режимом
//                    modeField.setText(readMessage);
//
//                    // Вывести сообщение Toast о успешной настройке режима
//                    Toast.makeText(getApplicationContext(), "Mode configured successfully.",
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        // Обработка нажатия кнопки питания
        power_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View view) {
                if (myBluetoothAdapter == null) {
                    // Устройство не поддерживает Bluetooth
                    Toast.makeText(getApplicationContext(), "Это устройство не поддерживает Bluetooth",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (!myBluetoothAdapter.isEnabled()) {
                        // Включаем Bluetooth
                        //myBluetoothAdapter.enable();
                        Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
                                Toast.LENGTH_LONG).show();
                        bl_Image.setImageResource(R.drawable.ic_action_off);
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        Bluetooth_flag = false;

                    }
                    else {
                        // Включаем кнопку переключения режима
                        Bluetooth_flag = true;
                        if (counter==1){
                            power_btn.setText("Power ON");
                            change_btn.setEnabled(true);
                            bl_Image.setImageResource(R.drawable.ic_action_on);
                            counter+=1;
                        }
                        else if (counter!=1){
                            power_btn.setText("Power OFF");
                            change_btn.setEnabled(false);
                            bl_Image.setImageResource(R.drawable.ic_action_off);
                            counter -= 1;
                        }

                    }
                }
            }
        });

        //обработчик событий кнопки переключения режимов

        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (ChooseMode++){
                    case 1:
                        modeField.setText("Выбран режим столбик громкости от зеленого к красному");
                        break;
                    case 2:
                        modeField.setText("Выбран режим столбик громкости бегущая радуга");
                        break;
                    case 3:
                        modeField.setText("Выбран режим светомузыки по частотам (5 полос симетрично)");
                        break;
                    case 4:
                        modeField.setText("Выбран режим светомузыки по частотам (3 полосы) ");
                        ChooseMode = 0;
                        break;
                }
            }
        });
    }
}