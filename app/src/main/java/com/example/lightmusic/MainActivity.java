package com.example.lightmusic;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private SharedPreferences preferences;

    final Handler handler = new Handler();
    private static final int PERMISSION_REQUEST_CODE = 2;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization);
        autorization();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        }

    }

    public void autorization(){
        username = findViewById(R.id.edit_user);
        password = findViewById(R.id.edit_password);
        Button login = findViewById(R.id.button_login);
        TextView loginLocked = findViewById(R.id.login_locked);
        preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // Получение данных пользователя из SharedPreferences

        String savedUsername = preferences.getString("username", "");
        String savedPassword = preferences.getString("password", "");

        // Проверка и автоматическая авторизация, если данные пользователя сохранены

        if (!savedUsername.equals("") && !savedPassword.equals("")) {
            login(savedUsername, savedPassword);
        }

        // Если введенные логин ("user") и пароль ("pass") будут введены правильно,
        // показываем Toast сообщение об успешном входе:

        login.setOnClickListener(v -> {
            String enteredUsername = username.getText().toString();
            String enteredPassword = password.getText().toString();
            if (enteredUsername.equals("user") && enteredPassword.equals("pass")) {
                preferences.edit().putString("username", enteredUsername).apply();
                preferences.edit().putString("password", enteredPassword).apply();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    handler.postDelayed(() -> {
                        Toast.makeText(this, "Успешная авторизация!", Toast.LENGTH_SHORT).show();
                        overridePendingTransition(R.anim.loginin, R.anim.loginout);
                        Intent intent = new Intent(this, MainScreen.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.loginin, R.anim.loginout);
                        finish();
                    }, 500);
                }

            } else {

                //Если данные неверные, показываем пользователю ошибку

                Toast.makeText(this, "Неверные логин или пароль", Toast.LENGTH_SHORT).show();
                loginLocked.setVisibility(View.VISIBLE);
                loginLocked.setBackgroundColor(Color.RED);
                loginLocked.setText("Вход заблокирован! Попробуйте еще раз!");
            }
        });
    }


    public void login(String savedUsername, String savedPassword) {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, продолжаем работу приложения
                autorization();
            } else {
                // Разрешение не получено, запрашиваем его у пользователя снова
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)) {
                    // Показываем пользователю объяснение, зачем нужно разрешение
                    Toast.makeText(this, "Для работы приложения необходимо разрешение на использование Bluetooth", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                } else {
                    // Пользователь уже отказал в разрешении, показываем ему диалоговое окно с просьбой вручную включить разрешение
                    Toast.makeText(this, "Для работы приложения необходимо разрешение на использование Bluetooth. Пожалуйста, включите его в настройках приложения.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

}