package com.example.animation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.depressionanalysis.SettingsActivity;
import com.example.depressionanalysis.PredictiveIndex;

public class MainActivity extends AppCompatActivity {

    ImageView bgapp, clover;
    LinearLayout txtSplash, txtHome, menus;
    Animation frombottom;
    Button btnDetect,btnHistory, btnPref;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ani);
        username = getIntent().getStringExtra("username");

        frombottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);

        bgapp = (ImageView) findViewById(R.id.bgapp);
        clover = (ImageView) findViewById(R.id.clover);
        txtSplash = (LinearLayout) findViewById(R.id.txtSplash);
        txtHome = (LinearLayout) findViewById(R.id.txtHome);
        menus = (LinearLayout) findViewById(R.id.menus);
        btnDetect = (Button) findViewById(R.id.btnDetect);
        btnHistory = (Button) findViewById(R.id.btnHistory);
        btnPref = (Button) findViewById(R.id.btnPref);

        bgapp.animate().translationY(-2000).setDuration(800).setStartDelay(1000);
        clover.animate().alpha(0).setDuration(800).setStartDelay(600);
        txtSplash.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(1000);

        txtHome.startAnimation(frombottom);
        menus.startAnimation(frombottom);
        PredictiveIndex.setUSER(username);

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), DetectActivity.class);
               /* intent2.putExtra("userName", username);*/
                startActivity(intent2);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), HistoryActivity.class);
                /* intent2.putExtra("userName", username);*/
                startActivity(intent2);

            }
        });

        btnPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(getApplicationContext(), SettingsActivity.class);
                /* intent2.putExtra("userName", username);*/
                startActivity(intent3);

            }
        });
    }
}
