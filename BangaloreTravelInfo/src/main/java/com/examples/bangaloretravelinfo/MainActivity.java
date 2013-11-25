package com.examples.bangaloretravelinfo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.examples.bangaloretravelguide.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MainActivity extends ActionBarActivity {

    private AutoCompleteTextView toText;
    private AutoCompleteTextView fromText;
    private Button getdetailsBtn;
    private HttpClient client;
    private HttpPost post;
    private HttpGet get;
    private HttpResponse response;
    private static String serverName = "http://mybmtc.com/trip-planner/";

    private static final String TAG_RESULTS = "results";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_VIEWPORT = "viewport";
    private static final String TAG_NORTHEAST = "northeast";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    // contacts JSONArray
    JSONArray results = null;

    private double AutoFare;
    private Vector<String> BusNumbers;
    private Vector<String> Distance;
    private Vector<String> JourneyTime;
    private Vector<String> Fare;
    private Vector<String> ServiceType;
    private ArrayAdapter<String> StopNamesAdapter;
    private ArrayList<String> StopNames;

    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toText = (AutoCompleteTextView) findViewById(R.id.to_text);
        fromText = (AutoCompleteTextView) findViewById(R.id.from_text);

        BusNumbers = new Vector<String>();
        Distance = new Vector<String>();
        JourneyTime = new Vector<String>();
        Fare = new Vector<String>();
        ServiceType = new Vector<String>();
        StopNames = new ArrayList<String>();
        StopNamesAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        toText.setAdapter(StopNamesAdapter);
        fromText.setAdapter(StopNamesAdapter);

        client = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(client.getParams(), "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        get = new HttpGet("http://mybmtc.com/trip-planner/Central%20Silk%20Board%280%29/Marathahalli%20Bridge%280%29/0/0/0/0/D/0/0");

        getdetailsBtn = (Button) findViewById(R.id.search_details);
        getdetailsBtn.setOnClickListener(OnDetailsClicked);

        toText.addTextChangedListener(OnTextChanged);
        fromText.addTextChangedListener(OnTextChanged);

        toText.setOnItemSelectedListener(OnToItemSelected);
        StopNamesAdapter.notifyDataSetChanged();
        fromText.setOnItemSelectedListener(OnFromItemSelected);

        toText.setOnFocusChangeListener(OnFocusChanged);
    }

    private View.OnClickListener OnDetailsClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try
            {
                String url = "http://mybmtc.com/trip-planner/Central%20Silk%20Board%280%29/Marathahalli%20Bridge%280%29/0/0/0/0/D/0/0";
                new JsoupParseHtml().execute(url);

                String toaddress = toText.getText().toString() + ", Bangalore";
                String fromaddress = fromText.getText().toString() + ", Bangalore";

                Float distance = GetDistanceFromUrl(toaddress, fromaddress);
                if (distance > 0)
                {
                    AutoFare = GetAutoFare(distance);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Unable to calculate distance", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };

    private TextWatcher OnTextChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 3)
            {
                new GetStops().execute(editable.toString());
                Log.i("Bang Travel", "Stopnames Len: " + StopNames.size());
            }
            else if (editable.length() < 3)
            {
                if (StopNames.size() > 0)
                    StopNames.clear();
            }
        }
    };

    private AdapterView.OnItemSelectedListener OnToItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            toText.setText(adapterView.getItemAtPosition(i).toString());
            StopNames.clear();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private AdapterView.OnItemSelectedListener OnFromItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            fromText.setText(adapterView.getItemAtPosition(i).toString());
            StopNames.clear();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private View.OnFocusChangeListener OnFocusChanged = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            StopNames.clear();
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
                return distanceResults[0]/100;
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

            Toast.makeText(getApplication(),
                    "Number of results : " + results.length(),
                    Toast.LENGTH_LONG).show();
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
            try {
                doc = Jsoup.connect("http://mybmtc.com/trip-planner/Central%20Silk%20Board%280%29/Marathahalli%20Bridge%280%29/0/0/0/0/D/0/0").get();
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
                        Log.i("Bang Travel", totalDetails);
                        String routeNum = routeDetails.get(i).text();
                        String[] busdetails = routeNum.split(":");
                        if (busdetails.length > 1)
                        {
                            BusNumbers.add(busdetails[1].split(" ")[1]);
                            Distance.add(busdetails[2].split(" ")[1]);
                            JourneyTime.add(busdetails[3].split(" ")[1] + " " + busdetails[3].split(" ")[2]);
                            Fare.add(busdetails[4].split(" ")[1]);
                            ServiceType.add(busdetails[5].split(" ")[1] + " " + busdetails[5].split(" ")[2]);

                            /*for (int k = 0; k < busdetails.length; k++) {
                                Log.i("Bang Travel", "Bus Details: " + k + ": " + busdetails[k]);
                            }*/
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

        }
    }

    private class GetStops extends AsyncTask<String, Integer, Integer> {

        protected Integer doInBackground(String... urls) {

            JSONParser jParser = new JSONParser();
            // getting JSON string from URL
            String url = "http://mybmtc.com" + "/busstopname/autocomplete/" + urls[0];
            Log.i("Bang Travel", "URL: " + url);
            String json = jParser.getStringFromUrl(url);
            try {

                if (json.length() != 0) {
                    // Start parsing json string.
                    String [] stops = json.split(",");
                    for (int i = 0; i < stops.length; i++) {
                        String stopName = stops[i].split(":")[1].replaceAll("[\"\\}\\]]", "").trim();
                        StopNames.add(stopName);
                        Log.i("bang Travel", "Stop Name: " + stopName);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Log.i("Bang Travel", ex.getMessage());
            }
            return 0;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result) {

        }
    }

    private String CreatePostString()
    {
        try {
            String ToValueModified = URLEncoder.encode(toText.getText().toString(),"UTF-8");
            String FromValueModified = URLEncoder.encode(fromText.getText().toString(), "UTF-8");

            String urlString = serverName + FromValueModified + "/" + ToValueModified + "/" + "/0/0/0/0/D/0/0";
            return urlString;
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
        // String postInfo = "origin=" + FromValueModified + "&destination=" + ToValueModified + "+" +
        //     "&origin-hidden-id=0&destination-hidden-id=0&from_time=0&to_time=0&form_id=bmtc_public_home_trip_planner_form";

    }

    private class GetDetails extends AsyncTask<String, Integer, Long> {
        InputStream is = null;
        protected Long doInBackground(String... urls) {
            try
            {
                Log.i("Bang Travel", "*****Do in BackGround*****");
                is  = client.execute(get).getEntity().getContent();
            }

            catch (UnsupportedEncodingException ex)
            {

            }

            catch (IOException ex)
            {

            }
            return new Long(1);
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
            Log.i("Bang Travel", "*****On Post Execute*****");
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int next = is.read();
                while (next > -1) {
                    bos.write(next);
                    next = is.read();
                }
                bos.flush();
                byte[] data = bos.toByteArray();
                Log.i("Bang Travel", "BOS Size: " + bos.size());
                Log.i("Bang Travel", "Data Len: " + data.length);
                is.close();
                bos.close();
            }
            catch (IOException ex) {
                Log.i("Bang Travel", ex.getMessage());
            }
        }
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
}