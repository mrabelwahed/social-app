package yberg.intnet.com.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PeopleActivity extends AppCompatActivity {

    private ListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RequestQueue requestQueue;
    private ArrayList<SearchItem> people;
    private SearchAdapter adapter;

    private int uid;
    private boolean showFollowers = false, showFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uid = intent.getIntExtra("uid", -1);
        if (intent.getStringExtra("show").equals("followers")) {
            showFollowers = true;
            getSupportActionBar().setTitle(intent.getStringExtra("firstName") + "'s followers");
        }
        else {
            showFollowing = true;
            getSupportActionBar().setTitle(intent.getStringExtra("firstName") + " is following");
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        people = new ArrayList<>();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        updatePeople();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        listView = (ListView) findViewById(R.id.listView);

        adapter = new SearchAdapter(this, R.layout.person_item, people);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int uid = people.get(position).getUid();
                MainActivity.profileFragment = ProfileFragment.newInstance(uid);
                MainActivity.getMainFragmentManager().beginTransaction().replace(
                        R.id.fragment_view, MainActivity.profileFragment).commitAllowingStateLoss();
                finish();
            }
        });

        updatePeople();
    }

    public void updatePeople() {
        StringRequest getFollowersRequest = new StringRequest(Request.Method.POST,
                showFollowers ? Database.GET_FOLLOWERS_URL : Database.GET_FOLLOWING_URL,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("getFollowers response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        people.clear();
                        if (!jsonResponse.isNull(showFollowers ? "followers" : "following")) {
                            JSONArray followers = jsonResponse.getJSONArray(showFollowers ? "followers" : "following");
                            for (int i = 0; i < followers.length(); i++) {
                                JSONObject follower = followers.getJSONObject(i);
                                people.add(
                                        new SearchItem(
                                                follower.getInt("uid"),
                                                follower.getString("username"),
                                                follower.getString("name"),
                                                follower.getString("image")
                                        )
                                );
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Snackbar.make(findViewById(R.id.base),
                                jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        MainActivity.makeSnackbar(R.string.request_timeout);
                        finish();
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                System.out.println("uid: " + uid);
                parameters.put("uid", "" + uid);
                return parameters;
            }
        };
        getFollowersRequest.setRetryPolicy(Database.getRetryPolicy());
        requestQueue.add(getFollowersRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
