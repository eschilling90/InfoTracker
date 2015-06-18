package erik.infotracker;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eriks_000 on 3/30/2015.
 */
public class WeightTrackerFragment extends Fragment {

    private AsyncHttpClient httpClient = new AsyncHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.weight_tracker_fragment, container, false);

        final String username = InfoTrackerActivity.pref.getString("username","");

        httpClient.get(InfoTrackerActivity.REQUEST_URL + "weight?username=" + username, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                Log.d("debug", "response is: " + new String(response));
                try {
                    JSONArray jsonObject = new JSONArray(new String(response));
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
                        addWeightToList(newWeight, formatDate.format(newDate), rootView);
                    }
                } catch (JSONException e) {
                    setWeightListErrorMessage("Error loading weights", rootView);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                setWeightListErrorMessage("Error loading weights", rootView);
            }
        });

        Button submitButton = (Button) rootView.findViewById(R.id.submitWeightButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TextView weightBox = (TextView) rootView.findViewById(R.id.submitWeightBox);
                weightBox.clearFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(weightBox.getWindowToken(), 0);
                String newWeight = weightBox.getText().toString();
                if (!newWeight.isEmpty()) {
                    httpClient.post(InfoTrackerActivity.REQUEST_URL + "weight?username=" + username + "&weight=" + newWeight, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(new String(response));
                                int code = jsonResponse.getInt("statusCode");
                                if (code == 200) {
                                    showToast("Weight added", rootView);
                                    DateFormat df = new SimpleDateFormat("MM/dd/yy");
                                    addWeightToList(weightBox.getText().toString(), df.format(new Date()), rootView);
                                } else {
                                    showToast("Error adding weight", rootView);
                                }
                            } catch (JSONException e) {
                                Log.d("response", "response is: " + new String(response));
                                setWeightListErrorMessage("Error loading weights", rootView);
                            }
                            weightBox.setText("");
                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                            showToast("Error adding weight", rootView);
                            weightBox.setText("");
                        }
                    });
                }
            }
        });

        Button goToGraphButton = (Button) rootView.findViewById(R.id.weight_tracker_to_graph_fragment_button);
        goToGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView weightBox = (TextView) rootView.findViewById(R.id.submitWeightBox);
                weightBox.clearFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(weightBox.getWindowToken(), 0);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                WeightGraphFragment frag = new WeightGraphFragment();
                ft.replace(R.id.container, frag);
                ft.commit();
            }
        });

        return rootView;
    }

    private void addWeightToList(String weight, String date, View rootView) {

        LinearLayout weightTable = (LinearLayout) rootView.findViewById(R.id.weightTable);
        RelativeLayout linear = new RelativeLayout(getActivity());
        linear.setBackgroundResource(R.drawable.single_expense);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT + 70);
        linear.setClickable(true);
        lp.setMargins(0,0,0,0);
        linear.setLayoutParams(lp);

        TextView dateText = new TextView(getActivity());
        dateText.setText(date);
        dateText.setId(View.generateViewId());
        dateText.setTextAppearance(getActivity(), R.style.weight_text);
        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        //dateParams.setMargins(10, 15, 150, 0);
        dateParams.setMarginStart(20);
        dateParams.setMarginEnd(150);
        linear.addView(dateText,dateParams);

        TextView weightText = new TextView(getActivity());
        weightText.setText(weight);
        weightText.setTextAppearance(getActivity(), R.style.weight_text);
        RelativeLayout.LayoutParams weightParams = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        //weightParams.setMargins(5, 15, 0, 0);
        weightParams.addRule(RelativeLayout.RIGHT_OF, dateText.getId());
        linear.addView(weightText,weightParams);

        ImageView arrow = new ImageView(getActivity());
        if (weightTable.getChildCount() > 0) {
            RelativeLayout line = (RelativeLayout) weightTable.getChildAt(0);
            TextView lineWeight = (TextView) line.getChildAt(1);
            if (Double.parseDouble(lineWeight.getText().toString()) < Double.parseDouble(weight)) {
                arrow.setImageResource(R.drawable.up_arrow);
            } else if (Double.parseDouble(lineWeight.getText().toString()) > Double.parseDouble(weight)) {
                arrow.setImageResource(R.drawable.down_arrow);
            }
        }
        RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        linear.addView(arrow, arrowParams);

        weightTable.addView(linear, 0);
    }

    private void setWeightListErrorMessage(String message, View rootView) {
        LinearLayout weightTable = (LinearLayout) rootView.findViewById(R.id.weightTable);

        TextView errorMessage = new TextView(getActivity());
        errorMessage.setText(message);

        weightTable.addView(errorMessage, 0);
    }

    public void showToast(String message, View rootView)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) rootView.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 350);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
