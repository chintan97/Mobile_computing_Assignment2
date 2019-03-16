package com.example.mycityweather;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    TextView lbl_enter_city;
    EditText get_city;
    Button fetch_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbl_enter_city = findViewById(R.id.lblEnterCity);
        get_city = findViewById(R.id.get_city_name);
        fetch_data = findViewById(R.id.fetch_button);

        fetch_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("City", get_city.getText().toString());
            }
        });
    }
}
