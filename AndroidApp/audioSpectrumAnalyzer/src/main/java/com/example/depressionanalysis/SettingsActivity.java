package com.example.depressionanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner storageSpinner = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> storageArray = ArrayAdapter.createFromResource(this, R.array.storage, android.R.layout.simple_spinner_item);
        storageArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storageSpinner.setAdapter(storageArray);

        Spinner ledSpinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> ledArray = ArrayAdapter.createFromResource(this, R.array.leds, android.R.layout.simple_spinner_item);
        ledArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ledSpinner.setAdapter(ledArray);


    }
}
