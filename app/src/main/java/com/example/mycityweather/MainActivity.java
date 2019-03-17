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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

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
    private Button fetch_data;  // Button using which user will fetch data
    String fetch_city_name;  // City name entered by user
    private ArrayList<WeatherData_model> weatherItems;
    private Runnable runnable;
    String city_name, main_weather, weather_description;   // Will be used while parsing json
    private double current_temperature, min_temperature, max_temperature, humidity, cloud_coverage;
    private LinearLayout show_data_layout;   // Layout where fetched data is shown
    private TextView show_city_name, show_main_temp, show_min_temp, show_max_temp,
            show_main_weather, show_description, show_humidity, show_cloud;   // Views where data will be shown
    private AutoCompleteTextView get_city_name;   // Here, city names fetched from json will be stored
    ImageView show_icon;   // To show icon provided by the json
    List<String> city_data;   // To store names of city parsed from json
    ArrayAdapter<String> city_adapter;   // city adapter for edittext suggestions
    LinearLayout layout_for_error;   // If city is not found, it will be shown
    String icon;  // for weather icon
    String iconUrl;   // URL from where icon will be fetched

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbl_enter_city = findViewById(R.id.lblEnterCity);
        fetch_data = findViewById(R.id.fetch_button);
        weatherItems = new ArrayList<>();

        // Assigning ids from XML file
        show_city_name = findViewById(R.id.show_city_name);
        show_main_temp = findViewById(R.id.show_main_temp);
        show_min_temp = findViewById(R.id.show_min_temp);
        show_max_temp = findViewById(R.id.show_max_temp);
        show_main_weather = findViewById(R.id.show_main_weather);
        show_description = findViewById(R.id.show_description);
        show_humidity = findViewById(R.id.show_humidity);
        show_cloud = findViewById(R.id.show_cloud);
        show_icon = findViewById(R.id.show_icon);

        // In this layout, data of city will be shown. Initially, this layout is hidden.
        // If the city entered by user is matched, this layout will be visible to show to user
        show_data_layout = findViewById(R.id.show_data_layout);
        show_data_layout.setVisibility(View.INVISIBLE);

        // In this layout, error will be shown. Initially, this layout is hidden.
        // If the city entered by user is not matched, this layout will be visible to show to user
        layout_for_error = findViewById(R.id.layout_show_error);
        layout_for_error.setVisibility(View.INVISIBLE);

        city_data = new ArrayList<String>();
        try{
            // This part is for city suggestions in the edittext
            // City names are provided by openweathermap and parsed from json downloaded
            // from http://bulk.openweathermap.org/sample/city.list.json.gz
            // There are many city names and countries which are exactly same with name.
            // But co-ordinates are different.
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset());
            for (int i=0; i<jsonArray.length(); i++){
                String get_city_name = jsonArray.getJSONObject(i).getString("name");  // City name
                String get_country_name = jsonArray.getJSONObject(i).getString("country");   // Country code
                city_data.add(get_city_name + "," + get_country_name);   // Append city name and country code
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        // assign city names data to city_adapter
        city_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, city_data);
        get_city_name = findViewById(R.id.get_city_name);   // Here, city name will be got from user
        get_city_name.setAdapter(city_adapter);

        // If user presses the button
        fetch_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch_city_name = get_city_name.getText().toString();   // Get city name entered by user

                // If it is empty, raise toast to user and return
                if (fetch_city_name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter city name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call function to show data
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

    // This function is to fetch city and country data from json provided by openweathermap
    public String loadJSONFromAsset(){
        String json = null;
        try{
            InputStream inputStream = getAssets().open("city_list.json");   // json in which data city, country data is stored
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);   // Read the file
            inputStream.close();
            json = new String(buffer, "UTF-8");   // store fetched data in json variable
        } catch (IOException e){
            e.printStackTrace();
        }
        return json;
    }

    public void get_weather_data(){

        // prepare URL
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
                        // Now store parsed data
                        try {
                             city_name = response.getString("name");  // assign city name
                             JSONObject main = response.getJSONObject("main");   // // assign current temperature
                             current_temperature = main.getDouble("temp");
                             min_temperature = main.getDouble("temp_min");   // assign minimum temperature
                             max_temperature = main.getDouble("temp_max");  // assign maximum temperature
                             JSONArray weather = response.getJSONArray("weather");
                             JSONObject main_object = weather.getJSONObject(0);
                             JSONObject description_object = weather.getJSONObject(0);
                             JSONObject icon_object = weather.getJSONObject(0);
                             main_weather = main_object.getString("main");   // assign weather
                             weather_description = description_object.getString("description");   // assign weather description
                             icon = icon_object.getString("icon");   // assign icon
                             humidity = main.getDouble("humidity");   // assign humidity
                             JSONObject clouds = response.getJSONObject("clouds");
                             cloud_coverage = clouds.getDouble("all");   // assign cloud coverage
                             iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";   // URL which contains image

                             // Now, I am storing the data in model.
                             // However, for this app, this model is not required because we are working in same file.
                             // But, in future if modifications are needed for the app, the model can be very useful
                             WeatherData_model model = new WeatherData_model(city_name, current_temperature, min_temperature,
                                     max_temperature, main_weather, weather_description, humidity, cloud_coverage);
                             weatherItems.add(model);

                             // Now, show data to user. Current, minimum, maximum temperature needs to modify
                             // because it is returned in Kelvin and we have to show in celsius
                             String show_temp = String.format("%.2f", model.getCurrent_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String min_temp = "Min. " + String.format("%.2f", model.getMin_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String max_temp = "Max. " + String.format("%.2f", model.getMax_temperature() - 273.15) + (char) 0x00B0 + "C";
                             String humidity_string = String.valueOf(model.getHumidity()) + "%";
                             String cloud_string = String.valueOf(model.getCloud_coverage()) + "%";

                             // assign data to textviews. Here, data is fetched from model
                             show_city_name.setText(model.getCity_name());
                             show_main_temp.setText(show_temp);
                             show_min_temp.setText(min_temp);
                             show_max_temp.setText(max_temp);
                             show_main_weather.setText(model.getMain_weather());
                             show_description.setText(model.getWeather_description());
                             show_humidity.setText(humidity_string);
                             show_cloud.setText(cloud_string);

                             // To show icon. It is fetched from URL made
                             Picasso.get().load(iconUrl).into(show_icon);   // http://square.github.io/picasso/

                             // Make error layout invisible
                             layout_for_error.setVisibility(View.INVISIBLE);

                             // Make layout visible in which data will be shown
                             show_data_layout.setVisibility(View.VISIBLE);
                        } catch (JSONException e){
                            e.printStackTrace();
                        } catch (Exception e){
                            // If exception occures, set error layout visible
                            layout_for_error.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // If city is not matched, API returns error 404.
                // If it happens, make error layout visible and layout which contains data invisible
                layout_for_error.setVisibility(View.VISIBLE);
                show_data_layout.setVisibility(View.INVISIBLE);
            }
        });

        // To handle multiple requests
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}
