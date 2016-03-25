package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RequestQueue requestQueue;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ScrollView scrollView;

    private TextView username, name, joined, posts, comments, followers, following, followLabel, nothingToShowTextView;
    private EditText firstName, lastName, email, password, newPassword, passwordConfirm;
    private LinearLayout followButton, latestPostSection, editProfileSection;
    private RelativeLayout submitButton;
    private ImageView followIcon;
    private ProgressBar spinner;
    private CardView latestPostCard;

    private Post latestPost;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param profile Profile uid
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(int profile) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt("profile", profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Profile");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        updateProfile();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        username = (TextView) view.findViewById(R.id.username);
        name = (TextView) view.findViewById(R.id.name);
        //joined TextView
        posts = (TextView) view.findViewById(R.id.posts);
        comments = (TextView) view.findViewById(R.id.comments);
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);

        followLabel = (TextView) view.findViewById(R.id.followLabel);
        followIcon = (ImageView) view.findViewById(R.id.followIcon);
        followButton = (LinearLayout) view.findViewById(R.id.followButton);
        followButton.setOnClickListener(
                new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        setFollowing(followLabel.getText().toString().equals("Follow"));

                        StringRequest followRequest = new StringRequest(Request.Method.POST, Database.FOLLOW_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("follow response: " + response);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    if (jsonResponse.getBoolean("success")) {
                                        setFollowing(jsonResponse.getBoolean("follows"));
                                        followers.setText("" + jsonResponse.getInt("followers"));
                                        following.setText("" + jsonResponse.getInt("following"));
                                    } else {
                                        Snackbar.make(getActivity().findViewById(R.id.base),
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
                                parameters.put("follow", "" + getArguments().getInt("profile"));
                                return parameters;
                            }
                        };
                        requestQueue.add(followRequest);
                    }
                }
        );

        latestPostSection = (LinearLayout) view.findViewById(R.id.latestPostSection);

        editProfileSection = (LinearLayout) view.findViewById(R.id.editProfileSection);
        editProfileSection.setVisibility(
                MainActivity.getUid() == getArguments().getInt("profile") ? View.VISIBLE : View.GONE);

        firstName = (EditText) view.findViewById(R.id.firstName);
        lastName = (EditText) view.findViewById(R.id.lastName);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        newPassword = (EditText) view.findViewById(R.id.newPassword);
        passwordConfirm = (EditText) view.findViewById(R.id.passwordConfirm);

        spinner = (ProgressBar) view.findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(
                new LightingColorFilter(0xFF000000, Color.WHITE));

        submitButton = (RelativeLayout) view.findViewById(R.id.submitButton);
        if (MainActivity.getUid() == getArguments().getInt("profile")) {
            submitButton.setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            boolean shouldReturn = setBorderIfEmpty(password);
                            if (!newPassword.getText().toString().equals("") &&
                                    !passwordConfirm.getText().toString().equals(newPassword.getText().toString())) {
                                ViewGroup parent = (ViewGroup) passwordConfirm.getParent();
                                passwordConfirm.setText("");
                                passwordConfirm.setBackgroundResource(R.drawable.edittext_red_border);
                                ((ImageView) parent.getChildAt(parent.indexOfChild(passwordConfirm) + 1))
                                        .setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                                shouldReturn = true;
                            }
                            // Return if some input is empty
                            if (shouldReturn)
                                return;

                            hideSoftKeyboard();
                            setEnabled(false);

                            StringRequest editProfile = new StringRequest(Request.Method.POST, Database.EDIT_PROFILE_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    System.out.println("editProfile response: " + response);
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.getBoolean("success")) {
                                            updateProfile();
                                            Snackbar.make(getActivity().findViewById(R.id.base),
                                                    jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Snackbar.make(getActivity().findViewById(R.id.base),
                                                    jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    setEnabled(true);
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    if (newPassword.getText().toString().equals(passwordConfirm.getText().toString())) {
                                        Map<String, String> parameters = new HashMap<>();
                                        parameters.put("uid", "" + MainActivity.getUid());
                                        parameters.put("firstName", firstName.getText().toString());
                                        parameters.put("lastName", lastName.getText().toString());
                                        parameters.put("email", email.getText().toString());
                                        parameters.put("password", password.getText().toString());
                                        parameters.put("newPassword", newPassword.getText().toString());
                                        return parameters;
                                    }
                                    return null;
                                }
                            };
                            requestQueue.add(editProfile);
                        }
                    }
            );
        }

        password.addTextChangedListener(new LoginActivity.GenericTextWatcher(password));
        passwordConfirm.addTextChangedListener(new LoginActivity.GenericTextWatcher(passwordConfirm));

        CardView followCard = (CardView) view.findViewById(R.id.followCard);
        followCard.setCardElevation(MainActivity.dpToPixels(1, followCard));

        LayoutInflater factory = LayoutInflater.from(getActivity());

        latestPostCard = (CardView) factory.inflate(R.layout.card, null);
        latestPostCard.setRadius(MainActivity.dpToPixels(2, latestPostCard));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                (int) MainActivity.dpToPixels(16, latestPostCard),
                (int) MainActivity.dpToPixels(4, latestPostCard),
                (int) MainActivity.dpToPixels(16, latestPostCard),
                (int) MainActivity.dpToPixels(4, latestPostCard)
        );
        latestPostCard.setLayoutParams(params);
        latestPostCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PostActivity.class);
                        intent.putExtra("post", latestPost);
                        getActivity().startActivity(intent);
                    }
                }
        );

        nothingToShowTextView = new TextView(getContext());
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                (int) MainActivity.dpToPixels(16, nothingToShowTextView),
                (int) MainActivity.dpToPixels(4, nothingToShowTextView),
                (int) MainActivity.dpToPixels(16, nothingToShowTextView),
                (int) MainActivity.dpToPixels(8, nothingToShowTextView)
        );
        nothingToShowTextView.setLayoutParams(params);
        nothingToShowTextView.setTypeface(null, Typeface.ITALIC);
        nothingToShowTextView.setText("Nothing to show");

        updateProfile();
        setEnabled(true);

        return view;
    }

    /**
     * Enables or disables click on the login button and shows or hides a loading spinner.
     * @param enabled Whether the button should be enabled or disabled
     */
    public void setEnabled(boolean enabled) {
        submitButton.setEnabled(enabled);
        spinner.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
    }

    public void setFollowing(boolean follows) {
        if (follows) {
            followButton.setBackgroundResource(R.drawable.button_red);
            followLabel.setText("Unfollow");
            followIcon.setImageResource(R.drawable.cancel);
        }
        else {
            followButton.setBackgroundResource(R.drawable.button);
            followLabel.setText("Follow");
            followIcon.setImageResource(R.drawable.person_add);
        }
    }

    public boolean setBorderIfEmpty(EditText editText) {
        if (editText.getText().toString().equals("")) {
            ViewGroup parent;
            parent = (ViewGroup) editText.getParent();
            editText.setBackgroundResource(R.drawable.edittext_red_border);
            ((ImageView) parent.getChildAt(parent.indexOfChild(editText) + 1)).setColorFilter(
                    ContextCompat.getColor(getActivity(), R.color.red));
            return true;
        }
        return false;
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(null, 0);
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

    public void updateProfile() {

        firstName.setText("");
        lastName.setText("");
        email.setText("");
        password.setText("");
        newPassword.setText("");
        passwordConfirm.setText("");

        firstName.clearFocus();
        lastName.clearFocus();
        email.clearFocus();
        password.clearFocus();
        newPassword.clearFocus();
        passwordConfirm.clearFocus();

        StringRequest profileRequest = new StringRequest(Request.Method.POST, Database.PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("profile response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        latestPostSection.removeAllViews();
                        JSONObject profile = jsonResponse.getJSONObject("profile");
                        User user = new User(
                                profile.getInt("uid"),
                                profile.getString("username"),
                                profile.getString("firstName"),
                                profile.getString("lastName"),
                                profile.getString("email"),
                                profile.getString("image")
                        );
                        if (!jsonResponse.isNull("post")) {
                            JSONObject post = jsonResponse.getJSONObject("post");
                            latestPost = new Post(
                                    post.getInt("pid"),
                                    user,
                                    post.getString("text"),
                                    post.getString("posted"),
                                    post.getInt("comments"),
                                    null,
                                    post.getInt("upvotes"),
                                    post.getInt("downvotes"),
                                    post.getInt("voted"),
                                    post.getString("image")
                            );
                            //Populate post card
                            ((TextView) latestPostCard.findViewById(R.id.username)).setText(user.getUsername());
                            ((TextView) latestPostCard.findViewById(R.id.name)).setText(user.getName());
                            ((TextView) latestPostCard.findViewById(R.id.time)).setText(latestPost.getPosted());
                            ((TextView) latestPostCard.findViewById(R.id.text)).setText(latestPost.getText());
                            ((TextView) latestPostCard.findViewById(R.id.comments)).setText("" + latestPost.getNumberOfComments());
                            ((TextView) latestPostCard.findViewById(R.id.upvotes)).setText("" + latestPost.getUpvotes());
                            ((TextView) latestPostCard.findViewById(R.id.downvotes)).setText("" + latestPost.getDownvotes());
                            ((ImageView) latestPostCard.findViewById(R.id.upvote)).setColorFilter(
                                    ContextCompat.getColor(getActivity(), R.color.gray)
                            );
                            ((ImageView) latestPostCard.findViewById(R.id.downvote)).setColorFilter(
                                    ContextCompat.getColor(getActivity(), R.color.gray)
                            );
                            if (post.getInt("voted") == 1) {
                                ((ImageView) latestPostCard.findViewById(R.id.upvote)).setColorFilter(
                                        ContextCompat.getColor(getActivity(), R.color.green)
                                );
                            }
                            else if (post.getInt("voted") == -1) {
                                ((ImageView) latestPostCard.findViewById(R.id.downvote)).setColorFilter(
                                        ContextCompat.getColor(getActivity(), R.color.red)
                                );
                            }
                            latestPostSection.addView(latestPostCard);
                        }
                        else {
                            latestPostSection.addView(nothingToShowTextView);
                        }
                        username.setText(user.getUsername());
                        name.setText(user.getName());
                        posts.setText("" + profile.getInt("posts"));
                        comments.setText("" + profile.getInt("comments"));
                        followers.setText("" + profile.getInt("followers"));
                        following.setText("" + profile.getInt("following"));

                        setFollowing(profile.getInt("follows") == 1);

                        try {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getUsername());
                        } catch (NullPointerException ignored) { }
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.base),
                                jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                    }
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
                parameters.put("uid", "" + MainActivity.getUid());
                parameters.put("profile", "" + getArguments().getInt("profile"));
                return parameters;
            }
        };
        requestQueue.add(profileRequest);
    }
}
