package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import yberg.intnet.com.app.util.BitmapHandler;
import yberg.intnet.com.app.util.PrettyTime;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * The profile fragment. Gets profile information and sends update requests when users
 * change their information.
 */
public class ProfileFragment extends Fragment {

    public static final int RESULT_LOAD_IMAGE   = 1;

    private OnFragmentInteractionListener mListener;
    private RequestQueue requestQueue;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView username, name, posts, comments, followers, following,
            followButtonLabel, nothingToShowTextView;
    private EditText firstName, lastName, email, password, newPassword, passwordConfirm;
    private LinearLayout editImageButton, followButton, followersButton, followingButton,
            latestPostSection, editProfileSection;
    private RelativeLayout submitButton;
    private ImageView profilePicture, followButtonIcon;
    private ProgressBar spinner;
    private CardView latestPostCard;

    private Post latestPost;
    private User user;
    private PrettyTime prettyTime;
    private String imgDecodableString;
    private Bitmap imageToUpload;

    private BitmapHandler bitmapHandler;

    private boolean myProfile = false;
    private String stringProfile, stringFollow, stringPasswordsDoNotMatch, stringNothingToShow,
            stringUnfollow, stringSomethingWentWrong;

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

        stringProfile = getResources().getString(R.string.profile);
        stringFollow = getResources().getString(R.string.follow);
        stringPasswordsDoNotMatch = getResources().getString(R.string.passwords_do_not_match);
        stringNothingToShow = getResources().getString(R.string.nothing_to_show);
        stringUnfollow = getResources().getString(R.string.unfollow);
        stringSomethingWentWrong = getResources().getString(R.string.something_went_wrong);

        prettyTime = new PrettyTime(getContext());
        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());

        bitmapHandler = new BitmapHandler();

        if (MainActivity.getUid() == getArguments().getInt("profile"))
            myProfile = true;


        //try {
            if (myProfile)
                ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_profile);
            else
                ((MainActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_search);
        //} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
        //        IllegalAccessException | NoSuchFieldException e) {
        //    e.printStackTrace();
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stringProfile);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateProfile();
                    }
                }
        );

        editImageButton = (LinearLayout) view.findViewById(R.id.editImageButton);
        if (myProfile) {
            editImageButton.setVisibility(View.VISIBLE);
            editImageButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                        }
                    }
            );
        }
        else {
            editImageButton.setVisibility(View.GONE);
        }

        username = (TextView) view.findViewById(R.id.username);
        name = (TextView) view.findViewById(R.id.name);
        profilePicture = (ImageView) view.findViewById(R.id.profilePicture);
        //joined TextView
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);
        posts = (TextView) view.findViewById(R.id.posts);
        comments = (TextView) view.findViewById(R.id.comments);

        // Set default
        username.setText("");
        name.setText("");
        followers.setText("0");
        following.setText("0");
        posts.setText("0");
        comments.setText("0");

        followButtonLabel = (TextView) view.findViewById(R.id.followButtonLabel);
        followButtonIcon = (ImageView) view.findViewById(R.id.followButtonIcon);
        followButton = (LinearLayout) view.findViewById(R.id.followButton);
        followButton.setOnClickListener(
                new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        setFollowing(followButtonLabel.getText().toString().equals(stringFollow));

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
                                        MainActivity.makeSnackbar(jsonResponse.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, Database.getErrorListener(mSwipeRefreshLayout, mSwipeRefreshLayout)
                        ) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> parameters = new HashMap<>();
                                parameters.put("uid", "" + MainActivity.getUid());
                                parameters.put("follow", "" + getArguments().getInt("profile"));
                                return parameters;
                            }
                        };
                        followRequest.setRetryPolicy(Database.getRetryPolicy());
                        requestQueue.add(followRequest);
                    }
                }
        );

        followersButton = (LinearLayout) view.findViewById(R.id.followersButton);
        followersButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PeopleActivity.class);
                        intent.putExtra("uid", getArguments().getInt("profile"));
                        intent.putExtra("firstName", user != null ? user.getFirstName() : "");
                        intent.putExtra("show", "followers");
                        startActivity(intent);
                    }
                }
        );

        followingButton = (LinearLayout) view.findViewById(R.id.followingButton);
        followingButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PeopleActivity.class);
                        intent.putExtra("uid", getArguments().getInt("profile"));
                        intent.putExtra("firstName", user != null ? user.getFirstName() : "");
                        intent.putExtra("show", "Following");
                        startActivity(intent);
                    }
                }
        );

        latestPostSection = (LinearLayout) view.findViewById(R.id.latestPostSection);

        editProfileSection = (LinearLayout) view.findViewById(R.id.editProfileSection);
        editProfileSection.setVisibility(myProfile ? View.VISIBLE : View.GONE);

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
        if (myProfile) {
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
                                passwordConfirm.requestFocus();
                                ((ImageView) parent.getChildAt(parent.indexOfChild(passwordConfirm) + 1))
                                        .setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                                MainActivity.makeSnackbar(stringPasswordsDoNotMatch);
                                shouldReturn = true;
                            }
                            // Return if some input is empty
                            if (shouldReturn)
                                return;

                            hideSoftKeyboard();
                            setEnabled(false);

                            editProfile();
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
                (int) MainActivity.dpToPixels(8, latestPostCard),
                (int) MainActivity.dpToPixels(4, latestPostCard),
                (int) MainActivity.dpToPixels(8, latestPostCard),
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
                (int) MainActivity.dpToPixels(8, nothingToShowTextView),
                (int) MainActivity.dpToPixels(16, nothingToShowTextView),
                (int) MainActivity.dpToPixels(8, nothingToShowTextView)
        );
        nothingToShowTextView.setLayoutParams(params);
        nothingToShowTextView.setTypeface(null, Typeface.ITALIC);
        nothingToShowTextView.setText(stringNothingToShow);

        updateProfile();
        setEnabled(true);

        return view;
    }

    /**
     * Enables or disables click on the login button and shows or hides a loading spinner.
     *
     * @param enabled Whether the button should be enabled or disabled
     */
    public void setEnabled(boolean enabled) {
        submitButton.setEnabled(enabled);
        spinner.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
    }

    public void setFollowing(boolean follows) {
        if (follows) {
            followButton.setBackgroundResource(R.drawable.button_red);
            followButtonLabel.setText(stringUnfollow);
            followButtonIcon.setImageResource(R.drawable.cancel);
        }
        else {
            followButton.setBackgroundResource(R.drawable.button);
            followButtonLabel.setText(stringFollow);
            followButtonIcon.setImageResource(R.drawable.person_add);
        }
    }

    /**
     * Sets the border of an edittext to red if it is empty.
     *
     * @param editText The edittext to set the border on
     * @return Whether the edittext was empty or not
     */
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            System.out.println("resultCode: " + resultCode);
            if (resultCode == getActivity().RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView after decoding the String
                imageToUpload = BitmapFactory.decodeFile(imgDecodableString);

                // Compress and encode the image, then upload it to the server
                BitmapHandler bitmapHandler = new BitmapHandler(new BitmapHandler.OnPostExecuteListener() {
                    @Override
                    public void onPostExecute(String encodedImage) {
                        uploadImageToServer(encodedImage);
                    }
                });
                bitmapHandler.process(imageToUpload);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.makeSnackbar(stringSomethingWentWrong);
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
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    /**
     * Updates the profile.
     * Requests profile information from the server and updates all fields.
     */
    public void updateProfile() {
        setEnabled(true);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        // Clear all fields and focuses
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

        // Send a profile request to the server
        StringRequest getProfileRequest = new StringRequest(Request.Method.POST, Database.GET_PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mSwipeRefreshLayout.setRefreshing(false);
                System.out.println("getProfile response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        latestPostSection.removeAllViews();

                        JSONObject profile = jsonResponse.getJSONObject("profile");
                        user = new User(
                                profile.getInt("uid"),
                                profile.getString("username"),
                                profile.getString("firstName"),
                                profile.getString("lastName"),
                                profile.getString("email"),
                                profile.isNull("image") ? null : profile.getString("image")
                        );

                        username.setText(user.getUsername());
                        name.setText(user.getName());
                        if (user.getImage() != null) {
                            byte[] imageAsBytes = Base64.decode(user.getImage().getBytes(), Base64.DEFAULT);
                            profilePicture.setImageBitmap(
                                    BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                            );
                        }
                        posts.setText("" + profile.getInt("posts"));
                        comments.setText("" + profile.getInt("comments"));
                        followers.setText("" + profile.getInt("followers"));
                        following.setText("" + profile.getInt("following"));

                        setFollowing(profile.getInt("follows") == 1);

                        if (!jsonResponse.isNull("post")) {
                            JSONObject post = jsonResponse.getJSONObject("post");
                            latestPost = new Post(
                                    post.getInt("pid"),
                                    user,
                                    post.getString("text"),
                                    prettyTime.getPrettyTime(post.getString("posted")),
                                    post.getInt("comments"),
                                    null,
                                    post.getInt("upvotes"),
                                    post.getInt("downvotes"),
                                    post.getInt("voted"),
                                    post.isNull("image") ? null : post.getString("image")
                            );


                            //Populate post card
                            if (user.getImage() != null) {
                                byte[] imageAsBytes = Base64.decode(user.getImage().getBytes(), Base64.DEFAULT);
                                Bitmap thumbnail = bitmapHandler.getThumbnail(
                                        BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                                );
                                ((ImageView) latestPostCard.findViewById(R.id.postProfilePicture)).setImageBitmap(thumbnail);
                            }
                            ((TextView) latestPostCard.findViewById(R.id.username)).setText(user.getUsername());
                            ((TextView) latestPostCard.findViewById(R.id.name)).setText(user.getName());
                            ((TextView) latestPostCard.findViewById(R.id.time)).setText(latestPost.getPosted());
                            ((TextView) latestPostCard.findViewById(R.id.text)).setText(latestPost.getText());
                            LinearLayout postImageBorder = (LinearLayout) latestPostCard.findViewById(R.id.postImageBorder);
                            ImageView postImage = (ImageView) latestPostCard.findViewById(R.id.postImage);
                            if (latestPost.getImage() != null) {
                                byte[] imageAsBytes = Base64.decode(latestPost.getImage().getBytes(), Base64.DEFAULT);
                                postImage.setImageBitmap(
                                        BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                                );
                                postImageBorder.setVisibility(View.VISIBLE);
                            }
                            else {
                                postImage.setImageBitmap(null);
                                postImageBorder.setVisibility(View.GONE);
                            }
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

                        try {
                            if (!((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().equals(user.getName()))
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getName());
                        } catch (NullPointerException ignored) { }
                    } else {
                        MainActivity.makeSnackbar(jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, Database.getErrorListener(getActivity().findViewById(R.id.base), mSwipeRefreshLayout)
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("uid", "" + MainActivity.getUid());
                parameters.put("profile", "" + getArguments().getInt("profile"));
                return parameters;
            }
        };
        getProfileRequest.setRetryPolicy(Database.getRetryPolicy());
        requestQueue.add(getProfileRequest);
    }

    /**
     * Sends an edit request to the server without an image.
     */
    public void editProfile() {
        editProfile(null);
    }

    /**
     * Sends an edit request to the server.
     *
     * @param fileName The file name of the image that has just been uploaded
     */
    public void editProfile(final String fileName) {
        StringRequest editProfileRequest = new StringRequest(Request.Method.POST, Database.EDIT_PROFILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("editProfile response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        updateProfile();
                        MainActivity.makeSnackbar(jsonResponse.getString("message"));
                    } else {
                        MainActivity.makeSnackbar(jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setEnabled(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        setEnabled(true);
                        MainActivity.makeSnackbar(R.string.request_timeout);
                    }
                }
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
                    if (fileName != null)
                        parameters.put("image", fileName);
                    return parameters;
                }
                return null;
            }
        };
        editProfileRequest.setRetryPolicy(Database.getRetryPolicy());
        requestQueue.add(editProfileRequest);
    }


    /**
     * Uploads a base64 encoded image to the server.
     *
     * @param encodedImage The base64 encoded image
     */
    public void uploadImageToServer(final String encodedImage) {
        StringRequest uploadRequest = new StringRequest(Request.Method.POST, Database.UPLOAD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("uploadRequest response: " + response);
                setEnabled(true);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        String fileName = jsonResponse.getString("fileName");
                        editProfile(fileName);
                    } else {
                        MainActivity.makeSnackbar(jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setEnabled(true);
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        MainActivity.makeSnackbar(R.string.request_timeout);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("uid", "" + MainActivity.getUid());
                parameters.put("image", encodedImage);
                return parameters;
            }
        };
        uploadRequest.setRetryPolicy(Database.getRetryPolicy());
        requestQueue.add(uploadRequest);
    }
}
