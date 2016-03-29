package yberg.intnet.com.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.transition.TransitionManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostDialog extends DialogFragment {

    public static int RESULT_LOAD_IMAGE = 1;
    public static int RESULT_CAMERA     = 2;
    public static int THUMB_HEIGHT      = 128;
    public static int IMAGE_QUALITY     = 70;  // 0 - 100
    public static int MAX_WIDTH         = 960;
    public static int MAX_HEIGHT        = 540;

    private OnFragmentInteractionListener mListener;

    private EditText postText;
    private TextView header, postingAs, username, name, attachedImageName;
    private ImageView closeButton, sendButton, imageButton, cameraButton, attachedImage, attachmentCloseButton;
    private View view;
    private RelativeLayout attachment;
    private ProgressBar progressBar;

    private RequestQueue requestQueue;

    private String imgDecodableString, encodedString;
    private Uri fileUri;
    private Bitmap imageToUpload;

    public PostDialog() {
        // Empty constructor required for DialogFragment
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Title.
     * @param postingAs Label.
     * @param username The posting user's username.
     * @param name The posting user's name.
     * @return A new instance of fragment PostDialog.
     */
    public static PostDialog newInstance(String title, String postingAs, String username, String name) {
        PostDialog fragment = new PostDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("postingAs", postingAs);
        args.putString("username", username);
        args.putString("name", name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_post_dialog, container, false);

        header = (TextView) view.findViewById(R.id.header);
        postText = (EditText) view.findViewById(R.id.post);
        postingAs = (TextView) view.findViewById(R.id.postingAs);
        username = (TextView) view.findViewById(R.id.username);
        name = (TextView) view.findViewById(R.id.name);

        header.setText(getArguments().getString("title"));
        postingAs.setText(getArguments().getString("postingAs"));
        username.setText(getArguments().getString("username"));
        name.setText(getArguments().getString("name"));

        closeButton = (ImageView) view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        imageButton = (ImageView) view.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                    }
                }
        );

        cameraButton = (ImageView) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        fileUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                                File.separator + "_tmp.jpg"));
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(cameraIntent, RESULT_CAMERA);
                    }
                }
        );

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        sendButton = (ImageView) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEnabled(false);
                        System.out.println("imageToUpload: " + imageToUpload);
                        if (imageToUpload != null) {
                            setEnabled(false);
                            imageToUpload = getCompressedBitmap(imageToUpload);
                            new Base64Encoder(imageToUpload).execute();
                        }
                        else {
                            mListener.onDialogSubmit(PostDialog.this, postText.getText().toString(), null);
                        }
                    }
                }
        );

        attachment = (RelativeLayout) view.findViewById(R.id.attachment);
        attachment.setVisibility(View.INVISIBLE);
        attachedImage = (ImageView) view.findViewById(R.id.attachedImage);
        attachedImageName = (TextView) view.findViewById(R.id.attachedImageName);

        attachmentCloseButton = (ImageView) view.findViewById(R.id.attachmentCloseButton);
        attachmentCloseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TransitionManager.endTransitions(attachment);
                        TransitionManager.beginDelayedTransition(attachment);

                        attachedImage.setImageBitmap(null);
                        attachedImageName.setText("");
                        attachment.setVisibility(View.INVISIBLE);
                    }
                }
        );

        postText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        setEnabled(true);

        return view;
    }

    public void setEnabled(boolean enabled) {
        progressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
        sendButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        imageButton.setEnabled(enabled);
        cameraButton.setEnabled(enabled);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            System.out.println("resultCode: " + resultCode);
            if (resultCode == getActivity().RESULT_OK && data != null) {

                TransitionManager.endTransitions(attachment);
                TransitionManager.beginDelayedTransition(attachment);

                attachment.setVisibility(View.VISIBLE);
                if (requestCode == RESULT_LOAD_IMAGE) {
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
                    Bitmap thumbnail = getThumbnail(imageToUpload);
                    attachedImage.setImageBitmap(thumbnail);

                    String fileName;
                    if (imgDecodableString.contains("/")) {
                        String[] split = imgDecodableString.split("/");
                        fileName = split[split.length - 1];
                    } else {
                        fileName = imgDecodableString;
                    }
                    attachedImageName.setText(fileName);
                } else if (requestCode == RESULT_CAMERA) {
                    imageToUpload = BitmapFactory.decodeFile(fileUri.getPath());
                    Bitmap thumbnail = getThumbnail(imageToUpload);
                    attachedImage.setImageBitmap(thumbnail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG).show();
            attachment.setVisibility(View.INVISIBLE);
        }
    }

    public Bitmap getThumbnail(Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap,
                (int) ((bitmap.getWidth() / (double) bitmap.getHeight()) * THUMB_HEIGHT), THUMB_HEIGHT);
    }

    public Bitmap getCompressedBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() > MAX_WIDTH || bitmap.getHeight() > MAX_HEIGHT) {
            float scale = Math.min(((float) MAX_WIDTH / bitmap.getWidth()),
                    ((float) MAX_HEIGHT / bitmap.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizedImage = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return resizedImage;
        }
        return bitmap;
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
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
        void onDialogSubmit(final PostDialog dialog, final String text, final String fileName);
    }

    private class Base64Encoder extends AsyncTask<Void, Void, Void> {

        Bitmap bitmap;

        public Base64Encoder(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream);

            byte[] array = stream.toByteArray();
            encodedString = Base64.encodeToString(array, 0);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            uploadImageToServer();
        }

    }

    public void uploadImageToServer() {
        StringRequest uploadRequest = new StringRequest(Request.Method.POST, Database.UPLOAD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("uploadRequest response: " + response);
                setEnabled(true);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        String fileName = jsonResponse.getString("fileName");
                        mListener.onDialogSubmit(PostDialog.this, postText.getText().toString(), fileName);
                    } else {
                        Snackbar.make(view, jsonResponse.getString("message"), Snackbar.LENGTH_LONG);
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
                        Snackbar.make(view, R.string.request_timeout,
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("uid", "" + MainActivity.getUid());
                parameters.put("image", encodedString);
                return parameters;
            }
        };
        uploadRequest.setRetryPolicy(Database.getRetryPolicy());
        requestQueue.add(uploadRequest);
    }
}
