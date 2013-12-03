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

        LoadDetails();
    }

    private void  LoadDetails()
    {
        distText.setText(TotalDistance.toString());
        autoFare.setText(new Double(AutoFare).toString());

        for (int i = 0; i < BusNumbers.size(); i++) {
            AddBusDetails(BusNumbers.get(i), Distance.get(i), JourneyTime.get(i),
                    Fare.get(i), ServiceType.get(i));
        }
    }

    private void AddBusDetails(String busnum, String distance, String journeyTime, String fare, String serviceType)
    {
        LinearLayout ll = new LinearLayout(this);
        ll.setBackgroundResource(R.drawable.linear_lyt_background);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView BusNumber = new TextView(this);
        BusNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        BusNumber.setPadding(10, 0, 0, 0);
        BusNumber.setText("Bus # : " + busnum);

        TextView Fare = new TextView(this);
        Fare.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        Fare.setPadding(10, 0, 0, 0);
        Fare.setText("Fare : " + fare);

        TextView JourneyTime = new TextView(this);
        JourneyTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        JourneyTime.setPadding(10, 0, 0, 0);
        JourneyTime.setText("Journey Time : " + journeyTime);

        TextView Distance = new TextView(this);
        Distance.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        Distance.setPadding(10, 0, 0, 0);
        Distance.setText("Distance : " + distance);

        TextView ServiceType = new TextView(this);
        ServiceType.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        ServiceType.setPadding(10, 0, 0, 0);
        ServiceType.setText("Service : " + ServiceType);

        ll.addView(BusNumber, 0);
        ll.addView(Fare, 1);
        ll.addView(JourneyTime, 2);
        ll.addView(Distance, 3);
        ll.addView(ServiceType, 4);

        LinearLayout parent = (LinearLayout) (findViewById(R.layout.activity_details));
        parent.addView(ll);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_details, container, false);
            return rootView;
        }
    }

}
