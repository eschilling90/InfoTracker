package erik.infotracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by eriks_000 on 5/11/2015.
 */
public class WeightGraphFragment extends Fragment {

    private AsyncHttpClient httpClient = new AsyncHttpClient();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.weight_graph_fragment, container, false);
        final String username = InfoTrackerActivity.pref.getString("username", "");

        Button goToTrackerButton = (Button) rootView.findViewById(R.id.weight_graph_to_weight_tracker_button);
        goToTrackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                WeightTrackerFragment frag = new WeightTrackerFragment();
                ft.replace(R.id.container, frag);
                ft.commit();
            }
        });

        httpClient.get(InfoTrackerActivity.REQUEST_URL + "weight?username=" + username, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                Log.d("debug", "response is: " + new String(response));
                ArrayList<Entry> entries = new ArrayList<Entry>();
                try {
                    JSONArray jsonObject = new JSONArray(new String(response));
                    float maxWeight = 0;
                    float minWeight = 300;
                    for (int j=0; j<jsonObject.length(); j++) {
                        String newWeight = jsonObject.getJSONObject(j).getString("weight");
                        String isoDate = jsonObject.getJSONObject(j).getString("date");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date newDate = null;
                        try {
                            newDate = df.parse(isoDate);
                        } catch (ParseException e) {
                            newDate = new Date();
                        }
                        DateFormat formatDate = new SimpleDateFormat("MM/dd/yy");
                        float weightF = Float.parseFloat(newWeight);
                        if (weightF > maxWeight) maxWeight = weightF;
                        if (weightF < minWeight) minWeight = weightF;
                        Entry entry = new Entry(weightF, j);
                        entries.add(entry);
                    }
                    LineDataSet weightDataSet = new LineDataSet (entries, "weight");
                    weightDataSet.setColor(getResources().getColor(R.color.trackerblue));
                    ArrayList<LineDataSet> weightDataSets = new ArrayList<LineDataSet>();
                    weightDataSets.add(weightDataSet);
                    ArrayList<String> labels = new ArrayList<String>();
                    for (int j=0; j<entries.size(); j++) {
                        labels.add(Integer.toString(j));
                    }
                    LineData weightData = new LineData(labels, weightDataSets);
                    LineChart chart = (LineChart) rootView.findViewById(R.id.weight_graph_chart);
                    chart.setData(weightData);
                    chart.setVisibleYRange(maxWeight - minWeight + 2, YAxis.AxisDependency.LEFT);
                    chart.centerViewTo(entries.size() / 2, (maxWeight - minWeight) / 2 + minWeight, YAxis.AxisDependency.LEFT);
                    chart.setDescription("");
                    chart.invalidate();
                } catch (JSONException e) {
                    //setGraphErrorMessage("Error loading weights", rootView);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                //setGraphErrorMessage("Error loading weights", rootView);
            }
        });

        return rootView;
    }
}
