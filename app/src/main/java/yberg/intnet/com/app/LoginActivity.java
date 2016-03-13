package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private RelativeLayout loginButton;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar spinner;
    private RequestQueue requestQueue;
    private ImageView check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(
                new LightingColorFilter(0xFF000000, Color.WHITE));

        check = (ImageView) findViewById(R.id.login_ok);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        String storedUsername;
        if (!(storedUsername = getSharedPreferences("com.intnet.yberg", Context.MODE_PRIVATE).getString("username", "")).equals("")) {
            username.setText(storedUsername);
            password.requestFocus();
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        loginButton = (RelativeLayout) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // Check if any input is empty
                        boolean shouldReturn = setBorderIfEmpty(username);
                        shouldReturn = setBorderIfEmpty(password) || shouldReturn;
                        // Return if some input is empty
                        if (shouldReturn)
                            return;

                        setEnabled(false);

                        StringRequest loginRequest = new StringRequest(Request.Method.POST, Database.LOGIN_URL, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                System.out.println("response: " + response);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    if (jsonResponse.getBoolean("success")) {

                                        check.setVisibility(View.VISIBLE);

                                        JSONObject user = jsonResponse.getJSONObject("user");

                                        // Store user information in Shared Preferences
                                        SharedPreferences.Editor editor = LoginActivity.this.getSharedPreferences(
                                                "com.intnet.yberg", Context.MODE_PRIVATE
                                        ).edit();
                                        editor.putInt("uid", user.getInt("uid"));
                                        editor.putString("username", user.getString("username"));
                                        editor.putString("name", user.getString("name"));
                                        editor.apply();

                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        password.setText("");
                                        Snackbar.make(coordinatorLayout, jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                                    }
                                    setEnabled(true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> parameters = new HashMap<>();
                                parameters.put("username", username.getText().toString());
                                parameters.put("password", password.getText().toString());

                                System.out.println("Sending " + username.getText().toString() + ", " +
                                        password.getText().toString() + " to " + Database.LOGIN_URL);

                                return parameters;
                            }
                        };
                        requestQueue.add(loginRequest);
                    }
                }
        );
        username.addTextChangedListener(new GenericTextWatcher(username));
        password.addTextChangedListener(new GenericTextWatcher(password));

        EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getId() == R.id.password)
                    loginButton.performClick();
                return false;
            }
        };
        password.setOnEditorActionListener(editorActionListener);

        findViewById(R.id.registerTextView).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                        finish();
                    }
                }
        );

        setEnabled(true);

        password.setText("123");
        loginButton.performClick();
    }

    /**
     * Enables or disables click on the login button and shows or hides a loading spinner.
     * @param enabled enable or disable
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            loginButton.setEnabled(true);
            loginButton.setBackgroundResource(R.drawable.button);
            spinner.setVisibility(View.INVISIBLE);
        }
        else {
            loginButton.setEnabled(false);
            loginButton.setBackgroundResource(R.drawable.button_pressed);
            spinner.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the border color of an edittext to red if it is empty.
     * @param editText the edittext to be checked
     * @return true if the edittext is empty
     */
    public boolean setBorderIfEmpty(EditText editText) {
        if (editText.getText().toString().equals("")) {
            ViewGroup parent;
            parent = (ViewGroup) editText.getParent();
            editText.setBackgroundResource(R.drawable.edittext_red_border);
            ((ImageView) parent.getChildAt(parent.indexOfChild(editText) + 1)).setColorFilter(Color.parseColor("#F44336"));
            return true;
        }
        return false;
    }

    public static class GenericTextWatcher implements TextWatcher {

        public EditText editText;
        public GenericTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable editable) {
            editText.setBackgroundResource(R.drawable.rounded_corners);
            ViewGroup parent = (ViewGroup) editText.getParent();
            ((ImageView) parent.getChildAt(parent.indexOfChild(editText) + 1)).setColorFilter(null);
        }
    }
}
