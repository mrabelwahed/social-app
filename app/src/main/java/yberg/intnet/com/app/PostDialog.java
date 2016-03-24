package yberg.intnet.com.app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostDialog extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    private EditText postText;
    private TextView header, postingAs, username, name;
    ImageView closeButton, sendButton;

    public PostDialog() {
        // Empty constructor required for DialogFragment
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Title.
     * @return A new instance of fragment PostDialog.
     */
    // TODO: Rename and change types and number of parameters
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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_dialog, container, false);

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

        sendButton = (ImageView) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setEnabled(false);
                        mListener.onDialogSubmit(PostDialog.this, postText.getText().toString());
                    }
                }
        );

        postText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    public void setEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onDialogSubmit(final PostDialog dialog, final String text);
    }
}
