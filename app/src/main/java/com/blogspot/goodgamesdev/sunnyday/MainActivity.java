package com.blogspot.goodgamesdev.sunnyday;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    // inner class for web connection, to download json from openweathermap website
    public class DownloadTask extends AsyncTask<String, Void, String> {

        // this code will never touch our UI
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // this code can touch UI, but just a bit :)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.i("JSON: returned val", s);

            try {

                listView = findViewById(R.id.listViewId);
                ArrayList<String> chosenData = new ArrayList<>();

                //       TESTING JSONOBJECT AND HOW TO GET TO THIS DATA
                // *********************************************************************

                JSONObject jsonObject = new JSONObject(s);
                Log.i("jsonObject:", jsonObject.toString());
                String weatherInfoEverything = jsonObject.getString("weather");
                Log.i("Weather content: ", weatherInfoEverything);
                JSONArray arr = new JSONArray(weatherInfoEverything);

                // here I choose the most interesting data to show
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    Log.i("main", jsonPart.getString("main"));
                    Log.i("description", jsonPart.getString("description"));
                    chosenData.add(jsonPart.getString("main"));
                    chosenData.add(jsonPart.getString("description"));
                }

                String mainData = jsonObject.getString("main");


                // chosenData.add(retrieveCountry);

                // *********************************************************************

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (MainActivity.this, android.R.layout.simple_list_item_1, chosenData);
                listView.setAdapter(arrayAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // API key from openweathermap
    // api.openweathermap.org/data/2.5/weather?lat=57&lon=21&appid=e146665a4b33f325f8356036d11f0baf
    private static final String API_KEY = "e146665a4b33f325f8356036d11f0baf";

    Button button;
    String webAddress;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        client = LocationServices.getFusedLocationProviderClient(this);

        button = findViewById(R.id.getLocationButtonId);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("Test Message:", "The latitude is " + String.valueOf(latitude));
                            Log.d("Test Message:", "The longitude is " + String.valueOf(longitude));

                            // build string with latitude and longitude and save it to webAddress variable
                            webAddress = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                            Log.d("webAddress: ", webAddress);

                            // instantiate inner class and run it's method
                            DownloadTask task = new DownloadTask();
                            task.execute(webAddress);

                        }
                    }
                });
            }
        });
    }

    // permission from user to use device location
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }


}
