package com.example.mycityweather;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView lbl_enter_city;
    private Button fetch_data;
    String fetch_city_name;
    private ArrayList<WeatherData_model> weatherItems;
    private Runnable runnable;
    String city_name, main_weather, weather_description;
    private double current_temperature, min_temperature, max_temperature, humidity, cloud_coverage;
    private LinearLayout show_data_layout;
    private TextView show_city_name, show_main_temp, show_min_temp, show_max_temp,
            show_main_weather, show_description, show_humidity, show_cloud;
    private AutoCompleteTextView get_city_name;
    List<String> city_data;
    ArrayAdapter<String> city_adapter;
    LinearLayout layout_for_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbl_enter_city = findViewById(R.id.lblEnterCity);
        fetch_data = findViewById(R.id.fetch_button);
        weatherItems = new ArrayList<>();
        show_city_name = findViewById(R.id.show_city_name);
        show_main_temp = findViewById(R.id.show_main_temp);
        show_min_temp = findViewById(R.id.show_min_temp);
        show_max_temp = findViewById(R.id.show_max_temp);
        show_main_weather = findViewById(R.id.show_main_weather);
        show_description = findViewById(R.id.show_description);
        show_humidity = findViewById(R.id.show_humidity);
        show_cloud = findViewById(R.id.show_cloud);
        show_data_layout = findViewById(R.id.show_data_layout);
        show_data_layout.setVisibility(View.INVISIBLE);
        layout_for_error = findViewById(R.id.layout_show_error);
        layout_for_error.setVisibility(View.INVISIBLE);

        city_data = new ArrayList<String>();
        try{
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset());
            for (int i=0; i<jsonArray.length(); i++){
                String get_city_name = jsonArray.getJSONObject(i).getString("name");
                String get_country_name = jsonArray.getJSONObject(i).getString("country");
                city_data.add(get_city_name + "," + get_country_name);
            }
            Log.i("total cities: ", String.valueOf(city_data.size()));
        } catch (JSONException e){
            e.printStackTrace();
        }
        city_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, city_data);
        get_city_name = findViewById(R.id.get_city_name);
        get_city_name.setAdapter(city_adapter);

        fetch_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch_city_name = get_city_name.getText().toString();
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

    public String loadJSONFromAsset(){
        String json = null;
        try{
            InputStream inputStream = getAssets().open("city_list.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e){
            e.printStackTrace();
        }
        return json;
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

                             String show_temp = String.format("%.2f", model.getCurrent_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String min_temp = String.format("%.2f", model.getMin_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String max_temp = String.format("%.2f", model.getMax_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String humidity_string = String.valueOf(model.getHumidity()) + "%";
                             String cloud_string = String.valueOf(model.getCloud_coverage()) + "%";
                             show_city_name.setText(model.getCity_name());
                             show_main_temp.setText(show_temp);
                             show_min_temp.setText(min_temp);
                             show_max_temp.setText(max_temp);
                             show_main_weather.setText(model.getMain_weather());
                             show_description.setText(model.getWeather_description());
                             show_humidity.setText(humidity_string);
                             show_cloud.setText(cloud_string);

                             layout_for_error.setVisibility(View.INVISIBLE);
                             show_data_layout.setVisibility(View.VISIBLE);
                             Log.i("city name", model.getWeather_description());
                        } catch (JSONException e){
                            e.printStackTrace();
                        } catch (Exception e){
                            layout_for_error.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_for_error.setVisibility(View.VISIBLE);
                show_data_layout.setVisibility(View.INVISIBLE);
                //Toast.makeText(getApplicationContext(), "Could not fetch data!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
