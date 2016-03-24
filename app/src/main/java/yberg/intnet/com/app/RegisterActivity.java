package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, username, password, passwordConfirm;
    private RelativeLayout registerButton;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar spinner;
    private RequestQueue requestQueue;
    private ImageView check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(
                new LightingColorFilter(0xFF000000, Color.WHITE));

        check = (ImageView) findViewById(R.id.login_ok);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        registerButton = (RelativeLayout) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // Check if any input is empty
                        boolean shouldReturn = setBorderIfEmpty(firstName);
                        shouldReturn = setBorderIfEmpty(lastName) || shouldReturn;
                        shouldReturn = setBorderIfEmpty(email) || shouldReturn;
                        shouldReturn = setBorderIfEmpty(username) || shouldReturn;
                        shouldReturn = setBorderIfEmpty(password) || shouldReturn;
                        shouldReturn = setBorderIfEmpty(passwordConfirm) || shouldReturn;
                        if (!passwordConfirm.getText().toString().equals(password.getText().toString())) {
                            ViewGroup parent = (ViewGroup) passwordConfirm.getParent();
                            passwordConfirm.setText("");
                            passwordConfirm.setBackgroundResource(R.drawable.edittext_red_border);
                            ((ImageView) parent.getChildAt(parent.indexOfChild(passwordConfirm) + 1))
                                    .setColorFilter(ContextCompat.getColor(RegisterActivity.this, R.color.green));
                            shouldReturn = true;
                        }
                        // Return if some input is empty
                        if (shouldReturn)
                            return;

                        setEnabled(false);

                        StringRequest registerRequest = new StringRequest(Request.Method.POST, Database.REGISTER_URL, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                System.out.println("response: " + response);
                                if (response != null) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.getBoolean("success")) {

                                            check.setVisibility(View.VISIBLE);

                                            SharedPreferences.Editor editor = RegisterActivity.this.getSharedPreferences(
                                                    "com.intnet.yberg", Context.MODE_PRIVATE
                                            ).edit();
                                            editor.clear();
                                            editor.putString("username", username.getText().toString());
                                            editor.apply();

                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Snackbar.make(coordinatorLayout, jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                setEnabled(true);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {}
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                if (password.getText().toString().equals(passwordConfirm.getText().toString())) {
                                    Map<String, String> parameters = new HashMap<>();
                                    parameters.put("username", username.getText().toString());
                                    parameters.put("firstName", firstName.getText().toString());
                                    parameters.put("lastName", lastName.getText().toString());
                                    parameters.put("email", email.getText().toString());
                                    parameters.put("password", password.getText().toString());
                                    return parameters;
                                }
                                return null;
                            }
                        };
                        requestQueue.add(registerRequest);
                    }
                }
        );
        firstName.addTextChangedListener(new LoginActivity.GenericTextWatcher(firstName));
        lastName.addTextChangedListener(new LoginActivity.GenericTextWatcher(lastName));
        username.addTextChangedListener(new LoginActivity.GenericTextWatcher(username));
        email.addTextChangedListener(new LoginActivity.GenericTextWatcher(email));
        password.addTextChangedListener(new LoginActivity.GenericTextWatcher(password));
        passwordConfirm.addTextChangedListener(new LoginActivity.GenericTextWatcher(passwordConfirm));

        EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getId() == R.id.passwordConfirm)
                    registerButton.performClick();
                return false;
            }
        };
        passwordConfirm.setOnEditorActionListener(editorActionListener);

        findViewById(R.id.loginTextView).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                }
        );

        setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Enables or disables click on the login button and shows or hides a loading spinner.
     * @param enabled Whether the button should be enabled or disabled
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            registerButton.setEnabled(true);
            registerButton.setBackgroundResource(R.drawable.button);
            spinner.setVisibility(View.INVISIBLE);
        }
        else {
            registerButton.setEnabled(false);
            registerButton.setBackgroundResource(R.drawable.button_pressed);
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
            ((ImageView) parent.getChildAt(parent.indexOfChild(editText) + 1)).setColorFilter(
                    ContextCompat.getColor(RegisterActivity.this, R.color.red));
            return true;
        }
        return false;
    }
}
