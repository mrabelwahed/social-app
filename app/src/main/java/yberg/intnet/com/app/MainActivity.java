package yberg.intnet.com.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yberg.intnet.com.app.security.HttpsTrustManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ProfileFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        PostDialog.OnFragmentInteractionListener,
        AdapterView.OnItemClickListener {

    private static CoordinatorLayout coordinatorLayout;

    private SearchView searchView;
    private ListPopupWindow listPopupWindow;
    private MenuItem menuItem;
    private TextView headerUsername, headerName;

    private static FragmentManager fragmentManager;
    public static FeedFragment feedFragment;
    public static ProfileFragment profileFragment;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ArrayAdapter searchAdapter;

    private RequestQueue requestQueue;
    private SharedPreferences prefs;

    private static int uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        fragmentManager = getSupportFragmentManager();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.base);

        HttpsTrustManager.allowAllSSL();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        View header = navigationView.getHeaderView(0);

        prefs = getSharedPreferences("com.intnet.yberg", Context.MODE_PRIVATE);
        headerUsername = (TextView) header.findViewById(R.id.username);
        headerName = (TextView) header.findViewById(R.id.name);
        headerUsername.setText("@" + prefs.getString("username", ""));
        headerName.setText(prefs.getString("name", ""));

        uid = prefs.getInt("uid", -1);

        feedFragment = FeedFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.fragmentView, feedFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (profileFragment != null && profileFragment.isAdded()) {
                feedFragment = FeedFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.fragmentView, feedFragment).commit();
            }
            else {
                super.onBackPressed();
            }
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

                if (!searchItems.isEmpty()) {
                    profileFragment = ProfileFragment.newInstance(searchItems.get(0).getUid());
                    fragmentManager.beginTransaction().replace(R.id.fragmentView, profileFragment).commit();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if (!s.equals("")) {
                    StringRequest editProfileRequest = new StringRequest(Request.Method.POST, Database.SEARCH_PROFILE_URL, new Response.Listener<String>() {
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
                                    makeSnackbar(jsonResponse.getString("message"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Database.getErrorListener(coordinatorLayout, null)
                    ) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<>();
                            parameters.put("name", s);
                            return parameters;
                        }
                    };
                    editProfileRequest.setRetryPolicy(Database.getRetryPolicy());
                    requestQueue.add(editProfileRequest);

                } else {
                    listPopupWindow.dismiss();
                }
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                if (feedFragment.isAdded())
                    getNavigationView().setCheckedItem(R.id.nav_home);
                else if (profileFragment.isAdded())
                    getNavigationView().setCheckedItem(R.id.nav_profile);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        profileFragment = ProfileFragment.newInstance(
                ((SearchItem) searchAdapter.getItem(position)).getUid()
        );
        fragmentManager.beginTransaction().replace(R.id.fragmentView, profileFragment).commit();
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
        switch (item.getItemId()) {
            case R.id.nav_home:
                feedFragment = FeedFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.fragmentView, feedFragment).commit();
                break;
            case R.id.nav_profile:
                profileFragment = ProfileFragment.newInstance(getUid());
                fragmentManager.beginTransaction().replace(R.id.fragmentView, profileFragment).commit();
                break;
            case R.id.nav_search:
                menuItem.expandActionView();
                break;
            case R.id.nav_sign_out:
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
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
    public void onDialogSubmit(final PostDialog dialog, final String text, final String fileName) {
        StringRequest addPostRequest = new StringRequest(Request.Method.POST, Database.ADD_POST_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("addPost response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        makeSnackbar("Sent post");
                        dialog.dismiss();
                        feedFragment.updateFeed();
                    }
                    else {
                        dialog.setEnabled(true);
                        makeSnackbar(jsonResponse.getString("message"));
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
                if (fileName != null)
                    parameters.put("image", fileName);
                return parameters;
            }
        };
        requestQueue.add(addPostRequest);
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public static Activity getActivity() throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        HashMap activities = (HashMap) activitiesField.get(activityThread);
        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activitiesField.get(activityRecord);
                return activity;
            }
        }
        return null;
    }

    public static int getUid() {
        return uid;
    }

    public static float dpToPixels(int dp, View view) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }

    public static FragmentManager getMainFragmentManager() {
        return fragmentManager;
    }

    public static void makeSnackbar(String text) {
        if (feedFragment.isAdded())
            feedFragment.makeSnackbar(text);
        else
            Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_LONG).show();
    }

    public static void makeSnackbar(int resId) {
        makeSnackbar(coordinatorLayout.getResources().getText(resId).toString());
    }
}
