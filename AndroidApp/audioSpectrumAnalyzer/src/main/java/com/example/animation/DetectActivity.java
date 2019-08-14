package com.example.animation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.audioanalyzer.AnalyzerActivity;

import static com.example.audioanalyzer.WavWriter.dispPrediction;
import static com.example.audioanalyzer.WavWriter.receivedPrediction;

public class DetectActivity extends AppCompatActivity {

    ImageView imgProfile, imgPercentage;
    TextView txtPercentage;

    Animation profilefrombottom;
    Button btnStart;
    String username;
    String prediction = String.valueOf(dispPrediction);
    ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

      /*  username = getIntent().getStringExtra("username");*/
        imgProfile= (ImageView) findViewById(R.id.imgProfile);
        btnStart= (Button) findViewById(R.id.btnStart);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        
        profilefrombottom= AnimationUtils.loadAnimation(this,R.anim.profilefrombottom);
        imgProfile.setAnimation(profilefrombottom);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getApplicationContext(), AnalyzerActivity.class);
                startActivity(intent2);
            }
        });
        progressBar.setProgress(Integer.parseInt(txtPercentage.getText().toString().substring(0, txtPercentage.getText().length() - 3)));

    }
    @Override
    public void onRestart() {
        super.onRestart();
        // add get percentage method call here
        if (receivedPrediction) {
            System.out.println("if receivedPrediction true");
            if (dispPrediction < 10.0) {
                txtPercentage.setText(String.format("%.2f", dispPrediction) + "%");
            }
            else {
                txtPercentage.setText(String.format("%.1f", dispPrediction) + "%");
            }
            progressBar.setProgress((int) dispPrediction);
        }
    }

}
