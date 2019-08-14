package com.example.animation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;


import com.example.R;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends AppCompatActivity {

    Button login;
    Animation profilefrombottom;
    public static String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login=findViewById(R.id.loginBtn);
        profilefrombottom= AnimationUtils.loadAnimation(this,R.anim.profilefrombottom);
        login.setAnimation(profilefrombottom);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validation = true;
                EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
                EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
                String errStr = "";
                //Validate Username
                if (txtUsername.getText().toString().isEmpty()) {
                    validation = false;
                    errStr = errStr + "Username cannot be left blank. ";
                }

                //Validate Password
                if (txtPassword.getText().toString().isEmpty()) {
                    validation = false;
                    errStr = errStr + "Password cannot be left blank. ";
                }

                //Check Validation
                if (!validation) {
                    System.out.println(errStr);
                    Toast.makeText(LoginActivity.this, errStr, Toast.LENGTH_LONG).show();
                } else {
                    launchSession(txtUsername.getText().toString(), v);
                    username = txtUsername.getText().toString();
                }
            }
        });

    }

    public void launchSession (String usrName, View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userName", usrName);
        boolean permissions = CheckPermissions();
        if(!permissions) {
            RequestPermissions();
        } else if (permissions){
            startActivity(intent);
        } else {
            Snackbar.make(view, "Permissions not granted. Please enable permissions and try again.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public boolean CheckPermissions() {
        int resultRW = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int resultRec = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int resultOutputStream = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        boolean value = resultRW == PackageManager.PERMISSION_GRANTED && resultRec == PackageManager.PERMISSION_GRANTED && resultOutputStream == PackageManager.PERMISSION_GRANTED;
        return value;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, INTERNET}, 1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
