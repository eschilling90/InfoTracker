package erik.infotracker;

/**
 * Created by eriks_000 on 4/2/2015.
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterFragment extends Fragment {

    private AsyncHttpClient httpClient = new AsyncHttpClient();

    public RegisterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.registration_fragment,container, false);

        Button registerButton = (Button) rootView.findViewById(R.id.registerButtonR);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Connection to back end to add user
                EditText name = (EditText) rootView.findViewById(R.id.nameR);
                EditText password = (EditText) rootView.findViewById(R.id.passwordR);
                EditText passwordRetype = (EditText) rootView.findViewById(R.id.repasswordR);

                final String nameValue = name.getText().toString();
                String passwordValue = password.getText().toString();
                String passwordRetypeValue = passwordRetype.getText().toString();

                Log.d("debug","password is"+passwordValue);
                Log.d("debug","password retype is"+ passwordRetypeValue);

                //Check if passwords are equal and email is a valid email address
                if (!nameValue.isEmpty() && !passwordValue.isEmpty() && !passwordRetypeValue.isEmpty() && (passwordValue.equals(passwordRetypeValue)))
                {
                    //Connect to webservice to add user and check result
                    httpClient.post(InfoTrackerActivity.REQUEST_URL + "register?name=" + nameValue + "&username=" + nameValue + "&password=" + passwordValue, new AsyncHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                            try{
                                JSONObject jsonResponse = new JSONObject(new String(response));
                                int code = jsonResponse.getInt("statusCode");
                                if(code == 200)  {
                                    SharedPreferences.Editor editor = InfoTrackerActivity.pref.edit();
                                    editor.putString("username", nameValue);
                                    editor.commit();                                    //if Successful then take to weight tracker fragment
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.container, new WeightTrackerFragment());
                                    ft.addToBackStack(null);
                                    ft.commit();
                                } else {
                                    showToast("Username already exists",rootView);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                            showToast("Request Failed",rootView);
                        }
                    });
                } else if (nameValue.isEmpty() || passwordValue.isEmpty() || passwordRetypeValue.isEmpty()) {
                    showToast("Required Fields Missing",rootView);
                } else if (!passwordValue.equals(passwordRetypeValue)) {
                    showToast("Passwords don't Match",rootView);
                } else {
                    showToast("Unknown Error",rootView);
                }
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
