package yberg.intnet.com.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Post> mPosts;
    private RequestQueue requestQueue;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosts = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());

        ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_home);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Start");
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.base);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateFeed();
                    }
                }
        );

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showPostDialog();
                    }
                }
        );

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CardAdapter(getActivity(), mPosts, new CardAdapter.OnItemClickListener() {
            @Override
            public void onClick(View caller) {

            }
        }, true);
        mRecyclerView.setAdapter(mAdapter);

        updateFeed();

        return view;
    }

    public void showPostDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        System.out.println("fm: " + fm);
        SharedPreferences prefs = getActivity().getSharedPreferences("com.intnet.yberg", Context.MODE_PRIVATE);
        PostDialog postDialog = PostDialog.newInstance("New post", "Posting as",
                prefs.getString("username", ""), prefs.getString("name", ""));
        postDialog.show(fm, "fragment_post_dialog");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void updateFeed() {
        mSwipeRefreshLayout.setRefreshing(true);
        try {
            StringRequest getFeedRequest = new StringRequest(Request.Method.POST, Database.FEED_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("getFeedRequest response: " + response);
                    mSwipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            mPosts.clear();
                            if (!jsonResponse.isNull("feed")) {
                                JSONArray feed = jsonResponse.getJSONArray("feed");
                                for (int i = 0; i < feed.length(); i++) {
                                    // Add the contents of each json object to the posts array list
                                    JSONObject post = feed.getJSONObject(i);
                                    JSONObject user = post.getJSONObject("user");
                                    JSONArray comments = post.getJSONArray("comments");
                                    ArrayList<Comment> mComments = new ArrayList<>();
                                    for (int j = comments.length() - 1; j >= 0; j--) {
                                        JSONObject comment = comments.getJSONObject(j);
                                        JSONObject usr = comment.getJSONObject("user");
                                        mComments.add(new Comment(comment.getInt("cid"),
                                                new User(
                                                        usr.getInt("uid"),
                                                        usr.getString("username"),
                                                        usr.getString("name"),
                                                        usr.isNull("image") ? null : usr.getString("image")
                                                ),
                                                comment.getString("text"),
                                                comment.getString("commented"),
                                                comment.isNull("image") ? null : comment.getString("image")
                                        ));
                                    }
                                    mPosts.add(new Post(
                                            post.getInt("pid"),
                                            new User(
                                                    user.getInt("uid"),
                                                    user.getString("username"),
                                                    user.getString("name"),
                                                    user.isNull("image") ? null : user.getString("image")
                                            ),
                                            post.getString("text"),
                                            post.getString("posted"),
                                            post.getInt("numberOfComments"),
                                            mComments,
                                            post.getInt("upvotes"),
                                            post.getInt("downvotes"),
                                            post.getInt("voted"),
                                            post.isNull("image") ? null : post.getString("image")
                                    ));
                                }
                            } else {
                                mRecyclerView.setBackgroundResource(R.drawable.account);
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Snackbar.make(coordinatorLayout, jsonResponse.getString("message"), Snackbar.LENGTH_LONG);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, Database.getErrorListener(coordinatorLayout, mSwipeRefreshLayout)
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    System.out.println("Sending uid: " + MainActivity.getUid() + " to " + Database.FEED_URL);
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("uid", "" + MainActivity.getUid());
                    return parameters;
                }
            };
            getFeedRequest.setRetryPolicy(Database.getRetryPolicy());
            requestQueue.add(getFeedRequest);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeSnackbar(String text) {
        Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_LONG).show();
    }
}
