package com.example.mycityweather;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView lbl_enter_city;
    private EditText get_city;
    private Button fetch_data;
    String fetch_city_name;
    private ArrayList<WeatherData_model> weatherItems;
    private Runnable runnable;
    String city_name, main_weather, weather_description;
    private double current_temperature, min_temperature, max_temperature, humidity, cloud_coverage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbl_enter_city = findViewById(R.id.lblEnterCity);
        get_city = findViewById(R.id.get_city_name);
        fetch_data = findViewById(R.id.fetch_button);
        weatherItems = new ArrayList<>();

        fetch_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch_city_name = get_city.getText().toString();
                Log.i("City", fetch_city_name);

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        get_weather_data();
                    }
                };

                Thread thread = new Thread(null, runnable, "background");
                thread.start();

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    public void get_weather_data(){
        final String baseUrl = "http://api.openweathermap.org/data/2.5/weather?q=";

        // String with city name appended
        String urlWithCityName = baseUrl.concat(TextUtils.isEmpty(fetch_city_name) ? "halifax" : fetch_city_name);

        // String with API ID appended
        String urlWithCityAPPID = urlWithCityName.concat("&APPID=6331b86d4fc107bdec0985f62573548b");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                urlWithCityAPPID,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Response", "Request successful");

                        // Now store parsed data
                        try {
                            city_name = response.getString("name");
                            JSONObject main = response.getJSONObject("main");
                            current_temperature = main.getDouble("temp");
                            min_temperature = main.getDouble("temp_min");
                            max_temperature = main.getDouble("temp_max");
                            JSONArray weather = response.getJSONArray("weather");
                            //JSONArray weather_list = weather.getJSONArray("weather");
                            JSONObject main_object = weather.getJSONObject(0);
                            JSONObject description_object = weather.getJSONObject(0);
                            main_weather = main_object.getString("main");
                            weather_description = description_object.getString("description");
                            humidity = main.getDouble("humidity");
                            JSONObject clouds = response.getJSONObject("clouds");
                            cloud_coverage = clouds.getDouble("all");

                            WeatherData_model model = new WeatherData_model(city_name, current_temperature, min_temperature,
                                    max_temperature, main_weather, weather_description, humidity, cloud_coverage);

                            weatherItems.add(model);

                            Log.i("city name", model.getWeather_description());

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Could not fetch data!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
