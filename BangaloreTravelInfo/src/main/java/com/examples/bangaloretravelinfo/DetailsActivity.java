package com.examples.bangaloretravelinfo;

import android.app.Activity;
import android.content.Intent;
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

import com.examples.bangaloretravelguide.R;

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
        Intent intent = getIntent();

        ToStop = intent.getStringExtra("ToStop");
        FromStop = intent.getStringExtra("FromStop");
        TotalDistance = intent.getFloatExtra("TotalDistance", 0);
        AutoFare = intent.getDoubleExtra("AutoFare", 0);
        BusNumbers = intent.getStringArrayListExtra("BusNumbers");
        Distance = intent.getStringArrayListExtra("Distance");
        JourneyTime = intent.getStringArrayListExtra("JourneyTime");
        Fare = intent.getStringArrayListExtra("Fare");
        ServiceType = intent.getStringArrayListExtra("ServiceType");

        setContentView(R.layout.activity_details);
        distText = (TextView) findViewById(R.id.distance_info);
        autoFare = (TextView) findViewById(R.id.auto_fare);
        fromText = (TextView) findViewById(R.id.from_detail_text);
        toText = (TextView) findViewById(R.id.to_detail_text);

        LoadDetails();
    }

    private void  LoadDetails()
    {
        fromText.setText(FromStop);
        toText.setText(ToStop);
        distText.setText(TotalDistance.toString() + " KM");
        autoFare.setText("Rs " + AutoFare.toString());

        for (int i = 0; i < BusNumbers.size(); i++) {
            AddBusDetails(BusNumbers.get(i), Distance.get(i), JourneyTime.get(i),
                    Fare.get(i), ServiceType.get(i));
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
