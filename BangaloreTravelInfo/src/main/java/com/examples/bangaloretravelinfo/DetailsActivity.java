package com.examples.bangaloretravelinfo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examples.bangaloretravelguide.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DetailsActivity extends Activity {

    private TextView distText;
    private TextView autoFare;
    private TextView fromText;
    private TextView toText;

    private String ToStop;
    private String FromStop;

    private Float TotalDistance;
    private Double AutoFare;

    private ArrayList<String> BusNumbers;
    private ArrayList<String> Distance;
    private ArrayList<String> JourneyTime;
    private ArrayList<String> Fare;
    private ArrayList<String> ServiceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Bangalore Travel Guide");

        Intent intent = getIntent();

        ToStop = intent.getStringExtra("ToStop");
        FromStop = intent.getStringExtra("FromStop");
        TotalDistance = intent.getFloatExtra("TotalDistance", 0);
        AutoFare = intent.getDoubleExtra("AutoFare", 0);

        BusNumbers = new ArrayList<String>();
        Distance = new ArrayList<String>();
        JourneyTime = new ArrayList<String>();
        Fare = new ArrayList<String>();
        ServiceType = new ArrayList<String>();

        setContentView(R.layout.activity_details);
        distText = (TextView) findViewById(R.id.distance_info);
        autoFare = (TextView) findViewById(R.id.auto_fare);
        fromText = (TextView) findViewById(R.id.from_detail_text);
        toText = (TextView) findViewById(R.id.to_detail_text);

        fromText.setText(FromStop);
        toText.setText(ToStop);
        distText.setText(TotalDistance.toString() + " KM");
        autoFare.setText("Rs " + AutoFare.toString());

        String url = "http://mybmtc.com/trip-planner/" + EncodeUrl(FromStop.trim()) + "%280%29/" + EncodeUrl(ToStop.trim()) + "%280%29/0/0/0/0/D/0/0";
        new JsoupParseHtml().execute(url);
    }

    private void  LoadDetails()
    {
        for (int i = 0; i < BusNumbers.size(); i++) {
            AddBusDetails(BusNumbers.get(i), Distance.get(i), JourneyTime.get(i),
                    Fare.get(i), ServiceType.get(i));
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

    private class JsoupParseHtml extends AsyncTask<String, Integer, Integer> {

        Document doc;
        int NumberOfRoutesInt;
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
                NumberOfRoutesInt = Integer.parseInt(NumberOfRoutesStr.split(" ")[0]);
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
            catch (SocketTimeoutException ex) {
                Toast.makeText(getApplicationContext(), "Connection Timed Out", Toast.LENGTH_LONG).show();
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
            // Log.i("Bang Travel", "Post Execute")
            LoadDetails();
            if (NumberOfRoutesInt == 0)
            {
                Toast.makeText(getApplicationContext(), "0 Direct routes found", Toast.LENGTH_LONG);
            }
        }
    }

    private void AddBusDetails(String busnum, String distance, String journeyTime, String fare, String serviceType)
    {
        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout parent = (LinearLayout) (findViewById(R.id.bmtc_details));

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundResource(R.drawable.line_layout);

        TextView BusNumber = new TextView(this);
        BusNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        BusNumber.setPadding(10, 0, 0, 0);
        BusNumber.setText("Bus # : " + busnum);

        TextView Fare = new TextView(this);
        Fare.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        Fare.setPadding(10, 0, 0, 0);
        Fare.setText("Fare : " + "Rs " + fare);

        TextView JourneyTime = new TextView(this);
        JourneyTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        JourneyTime.setPadding(10, 0, 0, 0);
        JourneyTime.setText("Journey Time : " + journeyTime);

        TextView Distance = new TextView(this);
        Distance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        Distance.setPadding(10, 0, 0, 0);
        Distance.setText("Distance : " + distance + " KM");

        View line = new View(this);
        line.setBackgroundResource(R.drawable.line_layout);

        /*TextView ServiceType = new TextView(this);
        ServiceType.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        ServiceType.setPadding(10, 0, 0, 0);
        ServiceType.setText("Service : " + ServiceType);*/

        ll.addView(BusNumber, 0);
        ll.addView(Fare, 1);
        ll.addView(JourneyTime, 2);
        ll.addView(Distance, 3);
        // ll.addView(ServiceType, 4);

        parent.addView(ll, linLayoutParam);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
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
