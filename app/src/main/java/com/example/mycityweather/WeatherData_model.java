package com.example.mycityweather;

public class WeatherData_model {
    private String city_name, main_weather, weather_description;
    private double current_temperature, min_temperature, max_temperature, humidity, cloud_coverage;

    public WeatherData_model(String city_name, double current_temperature, double min_temperature, double max_temperature, String main_weather, String weather_description, double humidity, double cloud_coverage){
        this.city_name = city_name;
        this.current_temperature = current_temperature;
        this.min_temperature = min_temperature;
        this.max_temperature = max_temperature;
        this.main_weather = main_weather;
        this.weather_description = weather_description;
        this.humidity = humidity;
        this.cloud_coverage = cloud_coverage;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getMain_weather() {
        return main_weather;
    }

    public void setMain_weather(String main_weather) {
        this.main_weather = main_weather;
    }

    public String getWeather_description() {
        return weather_description;
    }

    public void setWeather_description(String weather_description) {
        this.weather_description = weather_description;
    }

    public double getCurrent_temperature() {
        return current_temperature;
    }

    public void setCurrent_temperature(double current_temperature) {
        this.current_temperature = current_temperature;
    }

    public double getMin_temperature() {
        return min_temperature;
    }

    public void setMin_temperature(double min_temperature) {
        this.min_temperature = min_temperature;
    }

    public double getMax_temperature() {
        return max_temperature;
    }

    public void setMax_temperature(double max_temperature) {
        this.max_temperature = max_temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getCloud_coverage() {
        return cloud_coverage;
    }

    public void setCloud_coverage(double cloud_coverage) {
        this.cloud_coverage = cloud_coverage;
    }
}
