package com.example.lightmusic;

import static android.content.ContentValues.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    private EditText modeField;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // используется в обработчике Bluetooth для определения статуса сообщения
    private final static int MESSAGE_READ = 2; // используется в обработчике Bluetooth для идентификации обновления сообщения

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI переменные
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setEnabled(false);
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setBackgroundColor(getResources().getColor(R.color.black));
        final TextView textViewModeWorking = findViewById(R.id.ModeWorking);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Включаем Bluetooth. Если он уже активен, то игнорируется этот шаг
        if (!bluetoothAdapter.isEnabled()) {
            imageView.setImageResource(R.drawable.ic_action_off);
            Toast.makeText(getApplicationContext(), "Модуль Bluetooth отключен",
                    Toast.LENGTH_LONG).show();
        }
        else if(bluetoothAdapter == null){
            // Устройство не поддерживает Bluetooth
            imageView.setImageResource(R.drawable.ic_action_err);
            Toast.makeText(getApplicationContext(), "Это устройство не поддерживает Bluetooth",
                    Toast.LENGTH_LONG).show();
        }
        else if (bluetoothAdapter.isEnabled()){
            imageView.setImageResource(R.drawable.ic_action_on);
            Toast.makeText(getApplicationContext(), "Модуль Bluetooth включен",
                    Toast.LENGTH_LONG).show();
        }

        // Ссылаемся на выбор устройства
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Получение MAC адреса устройства BT
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Получение прогреса о соединении
            toolbar.setSubtitle("Подключение к: " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            Когда будет найдено "имя устройства" строки ниже вызовает новый поток для создания соединения Bluetooth с выбранным устройством
             */
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, getApplicationContext());
            createConnectThread.start();
        }

        /*
        Немного магии Hendler, не спрашивайте как это работает
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Подключено к: " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonToggle.setEnabled(true);
                                imageView.setImageResource(R.drawable.ic_action_con);
                                break;
                            case -1:
                                toolbar.setSubtitle("Сбой подключения");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                imageView.setImageResource(R.drawable.ic_action_err);
                                break;
                        }
                        break;
                }
            }
        };

        // Выбор устройства
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Помешаем Адаптер в список
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        // Управление Лентой
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmdText = null;
                String btnState = buttonToggle.getText().toString().toLowerCase(); //получение текста с кнопки маленькими буквами
                switch (btnState) {
                    case "включить": //можно добавлять комаед сколько угодно, помнить про задержку при передаче сигнала по Bluetooth
                        buttonToggle.setText("Выключить");
                        // Команда для включения светодиода
                        textViewModeWorking.setText("Лента активна");
                        cmdText = "<turn on>";
                        break;
                    case "выключить":
                        buttonToggle.setText("Включить");
                        textViewModeWorking.setText("Лента отключена");
                        // Команда для выключения светодиода
                        cmdText = "<turn off>";
                        break;
                }
                // Отправка команды Ардуине
                connectedThread.write(cmdText);
            }
        });
    }

    /* ============================ Поток для создания Bluetooth соединения =================================== */
    public static class CreateConnectThread extends Thread {
        private Context context;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, Context context) {
            /*
            Создание временного объекта сокета
             */
            this.context = context;
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                 simplify int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            UUID uuid = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                uuid = bluetoothDevice.getUuids()[0].getUuid();
            }

            try {
                /*
                Получите BluetoothSocket для подключения к данному устройству BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                 simplify int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Отмена обнаружения.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.cancelDiscovery();
            try {
                // Подключение к девайсу. Дальнейшая блокировка Сокета
                // Иначе выкинуть ошибку
                mmSocket.connect();
                Log.e("Status", "Устройство подключено");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Неудачное подключение, закрывает сокет и возвращается
                try {
                    mmSocket.close();
                    Log.e("Status", "Не удается подключиться к устройству");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Не возможно закрыть сокет", closeException);
                }
                return;
            }

                // Попытка подключения удалась. Выполнять работу, связанную с
                // соединение в отдельном потоке.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        // Окончательное закрытие сокета и дальнейшая работа с подключенным девайсом
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Не возможно закрыть сокет", e);
            }
        }
    }

    /* =============================== Поток обработки данных =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Получить входные и выходные потоки, используя временные объекты, потому что
            // потоки участников являются окончательными

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // Буфер потока
            int bytes = 0; // Вернутые байты для прочтения
            // Продолжайте прослушивать входной поток до тех пор, пока не возникнет исключение
            while (true) {
                try {
                    /*
                    Считывайте из входного потока из Arduino до тех пор, пока не будет достигнут символ завершения.
                    Затем отправьте целое строковое сообщение обработчику.
                     */
                    bytes = mmInStream.read(buffer);
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Отправка данных для работы с подключенным устройством*/
        public void write(String input) {
            if (input != null) { //условие на пустоту конвертации байтов
                byte[] bytes = input.getBytes(); //конвертация массива
                try {
                    mmOutStream.write(bytes);
                } catch (IOException e) {
                    Log.e("Send Error","Unable to send message",e);
                }
            }

        }

        /* Завершение соединения */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Прерывание соединения ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}