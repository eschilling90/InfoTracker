package erik.infotracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eriks_000 on 4/2/2015.
 */

public class LoginFragment extends Fragment {

    private AsyncHttpClient httpClient = new AsyncHttpClient();



    public LoginFragment() {
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.login_fragment,container, false);
        ((ActionBarActivity)getActivity()).getSupportActionBar().hide();

        Button login = (Button) rootView.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                EditText name = (EditText) rootView.findViewById(R.id.nameL);
                final String nameValue = name.getText().toString();

                EditText password = (EditText) rootView.findViewById(R.id.passwordL);
                final String passwordValue = password.getText().toString();

                //if Email is valid and Password is not empty
                if(!nameValue.equals("") && !passwordValue.equals("")) {
                    //Make connection to back end to check if user exists and if password is correct
                    httpClient.post(InfoTrackerActivity.REQUEST_URL + "login?username=" + nameValue.replace(" ", "") +"&password="+passwordValue, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] response) {
                            try{
                                JSONObject jsonResponse = new JSONObject(new String(response));
                                int code = jsonResponse.getInt("statusCode");
                                if(code == 202) {
                                    showToast("Name does not exist!",rootView);
                                } else if(code == 201) {
                                    showToast("Wrong Password!",rootView);
                                } else {
                                    //Save user's information to avoid logging in every time
                                    SharedPreferences.Editor editor = InfoTrackerActivity.pref.edit();
                                    editor.putString("username", nameValue);
                                    editor.commit();

                                    // Redirect to view alL Expenses page
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    WeightTrackerFragment frag  = new WeightTrackerFragment();
                                    ft.replace(R.id.container, frag);
                                    ft.commit();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int i, Header[] headers, byte[] response, Throwable throwable) {
                            showToast("Internet Connection not Available!",rootView);
                        }
                    });
                } else {
                    showToast("Required Fields Missing", rootView);
                }
            }
        });

        Button register = (Button) rootView.findViewById(R.id.registerL);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Redirect to Register page
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                RegisterFragment frag = new RegisterFragment();
                ft.replace(R.id.container, frag);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return rootView;
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
