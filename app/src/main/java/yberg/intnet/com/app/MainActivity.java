package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ProfileFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        PostDialog.OnFragmentInteractionListener,
        AdapterView.OnItemClickListener {

    private SearchView searchView;
    private ListPopupWindow listPopupWindow;
    private MenuItem menuItem;
    private TextView headerUsername, headerName;
    private FeedFragment feedFragment;
    private ProfileFragment profileFragment;

    private ArrayAdapter searchAdapter;

    private RequestQueue requestQueue;

    private static int uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        View header = navigationView.getHeaderView(0);

        SharedPreferences prefs = getSharedPreferences("com.intnet.yberg", Context.MODE_PRIVATE);
        headerUsername = (TextView) header.findViewById(R.id.username);
        headerName = (TextView) header.findViewById(R.id.name);
        headerUsername.setText("@" + prefs.getString("username", ""));
        headerName.setText(prefs.getString("name", ""));

        uid = prefs.getInt("uid", -1);

        FragmentManager fragmentManager = getSupportFragmentManager();
        feedFragment = FeedFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.fragment_view, feedFragment).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed_bar_menu, menu);

        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();

        final ArrayList<SearchItem> searchItems = new ArrayList<>();
        listPopupWindow = new ListPopupWindow(this);
        searchAdapter = new SearchAdapter(this, R.layout.search_item, searchItems);
        listPopupWindow.setAdapter(searchAdapter);
        listPopupWindow.setAnchorView(searchView);
        listPopupWindow.setWidth((int) dpToPixels(250, searchView));
        listPopupWindow.setModal(false);
        listPopupWindow.setOnItemClickListener(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                menuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if (!s.equals("")) {

                    StringRequest editProfile = new StringRequest(Request.Method.POST, Database.SEARCH_PROFILE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("searchProfile response: " + response);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.getBoolean("success")) {
                                    searchItems.clear();
                                    if (!jsonResponse.isNull("profiles")) {
                                        JSONArray profiles = jsonResponse.getJSONArray("profiles");
                                        for (int i = 0; i < profiles.length(); i++) {
                                            JSONObject profile = profiles.getJSONObject(i);
                                            searchItems.add(
                                                    new SearchItem(
                                                            profile.getInt("uid"),
                                                            profile.getString("username"),
                                                            profile.getString("name"),
                                                            profile.getString("image")
                                                    )
                                            );
                                        }
                                        searchAdapter.notifyDataSetChanged();
                                        if (!listPopupWindow.isShowing()) {
                                            listPopupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
                                            listPopupWindow.show();
                                        }
                                    }
                                    else {
                                        searchAdapter.clear();
                                        listPopupWindow.dismiss();
                                    }
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
                        public void onErrorResponse(VolleyError error) { }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<>();
                            parameters.put("name", s);
                            return parameters;
                        }
                    };
                    requestQueue.add(editProfile);

                } else {
                    listPopupWindow.dismiss();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        profileFragment = ProfileFragment.newInstance(
                ((SearchItem) searchAdapter.getItem(position)).getUid()
        );
        fragmentManager.beginTransaction().replace(R.id.fragment_view, profileFragment).commit();
        MenuItemCompat.collapseActionView(menuItem);
        listPopupWindow.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_home:
                feedFragment = FeedFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.fragment_view, feedFragment).commit();
                break;
            case R.id.nav_profile:
                profileFragment = ProfileFragment.newInstance(getUid());
                fragmentManager.beginTransaction().replace(R.id.fragment_view, profileFragment).commit();
                break;
            case R.id.nav_sign_out:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDialogSubmit(final PostDialog dialog, final String text) {
        StringRequest addPostRequest = new StringRequest(Request.Method.POST, Database.ADD_POST_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("addPost response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        Snackbar.make(findViewById(R.id.base),
                                "Sent post", Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                        feedFragment.updateFeed();
                    }
                    else {
                        dialog.setEnabled(true);
                        Snackbar.make(findViewById(R.id.base),
                                jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) { }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("uid", "" + MainActivity.getUid());
                parameters.put("text", text);
                return parameters;
            }
        };
        requestQueue.add(addPostRequest);
    }

    public static int getUid() {
        return uid;
    }

    public static float dpToPixels(int dp, View view) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }
}
