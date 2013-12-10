package com.examples.bangaloretravelinfo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.examples.bangaloretravelguide.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by sudheendra.sn on 11/25/13.
 */
public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable{
    private LayoutInflater mInflater;
    ArrayList<String> Stops;
    ArrayList<String> StopList;

    public AutoCompleteAdapter(final Context context, int TextViewResourceId) {
        super(context, TextViewResourceId);
        mInflater = LayoutInflater.from(context);
        StopList = new ArrayList<String>();
        LoadStopsFromRawFile(context);
    }

    private void LoadStopsFromRawFile(Context ctx)
    {
        InputStream is = ctx.getResources().openRawResource(R.raw.bus_stops);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;

        try {
            while ((line = br.readLine()) != null) {
                StopList.add(line);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView tv;
        if (convertView != null) {
            tv = (TextView) convertView;
            tv.setBackgroundColor(Color.BLACK);
            tv.setTextColor(Color.WHITE);
        } else {
            // Log.i("BangTravel", "Else Block");
            tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            tv.setBackgroundColor(Color.BLACK);
            tv.setTextColor(Color.WHITE);
        }

        tv.setText((getItem(position)));
        return tv;
    }

    public int getCount() {
        return Stops.size();

    }

    public String getItem(int index) {
        return Stops.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                if (constraint != null) {
                    try {
                        Stops = GetStopNames(constraint.toString());
                        /*Log.i("Bang Travel", "Stops Size: " + Stops.size());
                        for (int i = 0; i < Stops.size(); i++) {
                            Log.i("Bang Travel", "Stop is: " + Stops.get(i));
                        }*/
                    } catch (Exception e) {
                    }
                }
                if (Stops == null) {
                    Stops = new ArrayList<String>();
                }

                final FilterResults filterResults = new FilterResults();
                filterResults.values = Stops;
                filterResults.count = Stops.size();

                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(final CharSequence contraint, final FilterResults results) {
                clear();
                for (String stopName : (ArrayList<String>) results.values) {
                    add(stopName);
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(final Object resultValue) {
                return resultValue == null ? "" : ((String) resultValue);
            }
        };
        return myFilter;
    }

    private ArrayList<String> GetStopNames(String s)
    {
        /*ArrayList<String> stoplist = new ArrayList<String>();
        JSONParser jParser = new JSONParser();
        // getting JSON string from URL
        String url = "http://mybmtc.com" + "/busstopname/autocomplete/" + s;
        String json = jParser.getStringFromUrl(url);
        try {

            if (json.length() != 0) {
                // Start parsing json string.
                String [] stops = json.split(",");
                for (int i = 0; i < stops.length; i++) {
                    String stopName = stops[i].split(":")[1].replaceAll("[\"\\}\\]]", "").trim();
                    stoplist.add(stopName);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.i("Bang Travel", ex.getMessage());
        }*/

        ArrayList<String> stoplist = new ArrayList<String>();
        String stopNameLower = s.toLowerCase();
        String firstChar = s.substring(0,1);
        String stopName = firstChar.toUpperCase() + stopNameLower.substring(1);

        for (int i = 0; i < StopList.size(); i++) {
            String stop = StopList.get(i);
            if (stop.contains(stopName))
                stoplist.add(stop);
        }

        return stoplist;
    }

    /*private class GetStops extends AsyncTask<String, Integer, Integer> {

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
                        StopList.add(stopName);
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
    }*/

}

