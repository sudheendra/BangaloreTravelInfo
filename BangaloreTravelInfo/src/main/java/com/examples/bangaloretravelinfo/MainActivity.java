package com.examples.bangaloretravelinfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.examples.bangaloretravelguide.R;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.String;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private AutoCompleteTextView toText;
    private AutoCompleteTextView fromText;
    private Button getdetailsBtn;
    private HttpClient client;

    private static final String TAG_RESULTS = "results";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_VIEWPORT = "viewport";
    private static final String TAG_NORTHEAST = "northeast";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    // contacts JSONArray
    JSONArray results = null;

    private double AutoFare;
    private Float TotalDistance;
    private ArrayList<String> BusNumbers;
    private ArrayList<String> Distance;
    private ArrayList<String> JourneyTime;
    private ArrayList<String> Fare;
    private ArrayList<String> ServiceType;
    private String ToStop;
    private String FromStop;

    DetailsActivity detailsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getActionBar().setBackgroundDrawable(new ColorDrawable(0xff247aff));

        detailsActivity = new DetailsActivity();

        BusNumbers = new ArrayList<String>();
        Distance = new ArrayList<String>();
        JourneyTime = new ArrayList<String>();
        Fare = new ArrayList<String>();
        ServiceType = new ArrayList<String>();

        toText = (AutoCompleteTextView) findViewById(R.id.to_text);
        fromText = (AutoCompleteTextView) findViewById(R.id.from_text);
        toText.setThreshold(3);
        fromText.setThreshold(3);
        toText.setAdapter(new AutoCompleteAdapter(getApplicationContext(), R.id.list_item));
        fromText.setAdapter(new AutoCompleteAdapter(getApplicationContext(), R.id.list_item));

        client = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(client.getParams(), "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        getdetailsBtn = (Button) findViewById(R.id.search_details);
        getdetailsBtn.setOnClickListener(OnDetailsClicked);

        toText.setOnItemSelectedListener(OnToItemSelected);
        fromText.setOnItemSelectedListener(OnFromItemSelected);

        toText.setOnFocusChangeListener(OnFocusChanged);
        fromText.setOnFocusChangeListener(OnFocusChanged);
    }

    private View.OnClickListener OnDetailsClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try
            {
                ToStop = toText.getText().toString();
                FromStop = fromText.getText().toString();

                String toaddress = ToStop + ", Bangalore";
                String fromaddress = FromStop + ", Bangalore";

                String url = "http://mybmtc.com/trip-planner/" + EncodeUrl(FromStop.trim()) + "%280%29/" + EncodeUrl(ToStop.trim()) + "%280%29/0/0/0/0/D/0/0";
                new JsoupParseHtml().execute(url);

                TotalDistance =  truncate(GetDistanceFromUrl(toaddress, fromaddress), 2);
                Math.round(TotalDistance);
                Log.i("Bang Travel", "Total distance: " + TotalDistance);

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };

    private AdapterView.OnItemSelectedListener OnToItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            toText.setText(adapterView.getItemAtPosition(i).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private AdapterView.OnItemSelectedListener OnFromItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            fromText.setText(adapterView.getItemAtPosition(i).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private View.OnFocusChangeListener OnFocusChanged = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            // StopNames.clear();
        }
    };

    private Float GetDistanceFromUrl(String toaddress, String fromaddress)
    {
        Float[] to_lat_long = GetLatLong(toaddress).clone();
        Float[] from_lat_long = GetLatLong(fromaddress).clone();

        Log.i("Bang Travel", "Length of TO: " + to_lat_long.length);
        Log.i("Bang Travel", "Length of FROM: " + from_lat_long.length);

        float[] distanceResults = new float[5];

        if (to_lat_long.length > 1 && from_lat_long.length > 1)
        {
            //Calculate distance
            try {
            Location.distanceBetween(from_lat_long[0], from_lat_long[1], to_lat_long[0], to_lat_long[1], distanceResults);
            if (distanceResults.length > 0)
                return (distanceResults[0]/1000);
            }
            catch (NullPointerException ex) {
                Toast.makeText(getApplicationContext(), "Unable to get the Co-Ordiantes of Location, Please Retry", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Failed to find the specified location", Toast.LENGTH_LONG).show();
        }
        return new Float(0);
    }

    private Float[] GetLatLong(String address)
    {
        Float[] lat_long = new Float[2];
        String googleMapURI = "http://maps.googleapis.com/maps/api/geocode/json?address=";
        String addr = address.replaceAll(" ", "%20");
        googleMapURI += addr + "&sensor=false";
        String latitude;
        String longitude;

        JSONParser jParser = new JSONParser();
        // getting JSON string from URL
        JSONObject json = jParser.getJSONFromUrl(googleMapURI);

        try {
            // Getting Array of results
            results = json.getJSONArray(TAG_RESULTS);

            if (results.length() > 0)
            {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject r = results.getJSONObject(i);

                    // geometry and location is again JSON Object
                    JSONObject geometry = r.getJSONObject(TAG_GEOMETRY);

                    JSONObject viewport = geometry.getJSONObject(TAG_VIEWPORT);

                    JSONObject northest = viewport.getJSONObject(TAG_NORTHEAST);

                    latitude = northest.getString(TAG_LAT);
                    longitude = northest.getString(TAG_LNG);

                    lat_long[0] = Float.parseFloat(latitude);
                    lat_long[1] = Float.parseFloat(longitude);
                }
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.i("Bang Travel", ex.getMessage());
        }

        return lat_long;
    }

    private double GetAutoFare(float distance)
    {
        return 20 * distance;
    }

    private class JsoupParseHtml extends AsyncTask<String, Integer, Integer> {

        Document doc;
        protected Integer doInBackground(String... urls)
        {
            BusNumbers.clear();
            Distance.clear();
            JourneyTime.clear();
            Fare.clear();
            ServiceType.clear();
            try {
                doc = Jsoup.connect(urls[0]).get();
                String title = doc.title();
                System.out.println("title : " + title);

                // get all links
                /*Elements links = doc.select("a[href]");
                for (Element link : links) {

                    // get the value from href attribute
                    System.out.println("\nlink : " + link.attr("href"));
                    System.out.println("text : " + link.text());
                }*/

                Elements routeDetails = doc.select("td");
                String NumberOfRoutesStr = routeDetails.get(0).text();
                Integer NumberOfRoutesInt = Integer.parseInt(NumberOfRoutesStr.split(" ")[0]);
                Log.i("Bang Travel", "No of routes: " + NumberOfRoutesInt);

                for (int i = 2; i < routeDetails.size() - 2; i = i + 4) {
                    String totalDetails = routeDetails.get(i).text();
                    if (totalDetails.startsWith("Route"))
                    {
                        String routeNum = routeDetails.get(i).text();
                        String[] busdetails = routeNum.split(":");
                        if (busdetails.length > 1)
                        {
                            /*for (int k = 0; k < busdetails.length; k++) {
                                Log.i("Bang Travel", "Bus Details: " + k + ": " + busdetails[k]);
                            }*/

                            if (busdetails.length >= 6)
                            {
                                BusNumbers.add(busdetails[1].split(" ")[1]);
                                Distance.add(busdetails[2].split(" ")[1]);
                                JourneyTime.add(busdetails[3].split(" ")[1] + " " + busdetails[3].split(" ")[2]);
                                String fare = busdetails[4].split(" ")[1].split("[^0-9]")[0];
                                Fare.add(fare);
                                ServiceType.add(busdetails[5].split(" ")[1] + " " + busdetails[5].split(" ")[2]);
                                Log.i("Bang Travel", "Service Type: " + ServiceType);
                            }
                            else if (busdetails.length == 5)
                            {
                                BusNumbers.add(busdetails[1].split(" ")[1]);
                                Distance.add(busdetails[2].split(" ")[1]);
                                JourneyTime.add(busdetails[3].split(" ")[1] + " " + busdetails[3].split(" ")[2]);
                                Fare.add("100");
                                ServiceType.add(busdetails[4].split(" ")[0] + " " + busdetails[4].split(" ")[1]);
                                Log.i("Bang Travel", "Service Type: " + ServiceType);
                            }
                        }
                    }
                }

                Log.i("Bang Travel", "Vector Size: " + BusNumbers.size() + " " + Distance.size() + " " + JourneyTime.size() + " " + Fare.size() + " " + ServiceType.size());
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            return new Integer(1);
        }

        protected void onProgressUpdate(Integer... progress)
        {

        }

        protected void onPostExecute(Integer result) {
            Log.i("Bang Travel", "Post Execute");
            LoadDetailsActivity();
        }
    }


    private void LoadDetailsActivity()
    {
        if (TotalDistance > 0)
        {
            AutoFare = truncate(GetAutoFare(TotalDistance), 2);
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("ToStop", ToStop);
            intent.putExtra("FromStop", FromStop);
            intent.putExtra("TotalDistance", TotalDistance);
            intent.putExtra("AutoFare", AutoFare);
            intent.putStringArrayListExtra("BusNumbers", BusNumbers);
            intent.putStringArrayListExtra("Distance", Distance);
            intent.putStringArrayListExtra("JourneyTime", JourneyTime);
            intent.putStringArrayListExtra("Fare", Fare);
            intent.putStringArrayListExtra("ServiceType", ServiceType);

            startActivity(intent);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Unable to calculate distance", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private String EncodeUrl(String s) {
        String url = "";
        try {
            url = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        }
        catch (UnsupportedEncodingException ex) {

        }
        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static double truncate(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = (long) value;
        return (double) tmp / factor;
    }

    public static float truncate(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = (long) value;
        return (float) tmp / factor;
    }
}