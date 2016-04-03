package yberg.intnet.com.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import yberg.intnet.com.app.util.BitmapHandler;

/**
 * Created by Viktor on 2016-03-04.
 *
 * Custom adapter for the recycler view with posts.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private ArrayList<Post> mPosts;
    private static Activity mActivity;
    private boolean mFromMainActivity;
    private OnItemClickListener mListener;

    private String stringComment;

    private BitmapHandler bitmapHandler;

    public CardAdapter(Activity activity, ArrayList<Post> posts, OnItemClickListener listener, boolean fromMainActivity) {
        mActivity = activity;
        mPosts = posts;
        mListener = listener;
        mFromMainActivity = fromMainActivity;
        bitmapHandler = new BitmapHandler();

        stringComment = activity.getResources().getString(R.string.comment);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // Create a new view

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        ViewHolder vh = new ViewHolder(view, mPosts, new CardAdapter.ViewHolder.OnItemClickListener() {
            public void onClick(View caller) { mListener.onClick(caller); }
        }, mFromMainActivity);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Gets an object at the given position in the posts array and populates
        // text and image views.

        Post post = mPosts.get(position);

        holder.mUsername.setText(post.getUser().getUsername());
        holder.mName.setText(post.getUser().getName());
        if (post.getUser().getImage() != null) {
            byte[] imageAsBytes = Base64.decode(post.getUser().getImage().getBytes(), Base64.DEFAULT);
            Bitmap thumbnail = bitmapHandler.getThumbnail(
                    BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
            );
            holder.mPostProfilePicture.setImageBitmap(thumbnail);
        }
        else {
            holder.mPostProfilePicture.setImageBitmap(null);
            holder.mPostProfilePicture.setImageResource(R.drawable.person);
        }
        holder.mPosted.setText(post.getPosted());
        holder.mText.setText(post.getText());
        holder.mNoComments.setText("" + post.getNumberOfComments());
        holder.mUpvotes.setText("" + post.getUpvotes());
        holder.mDownvotes.setText("" + post.getDownvotes());
        holder.mCardView.setTag("closed");
        holder.mCardView.setCardElevation(MainActivity.dpToPixels(1, holder.mCardView));

        holder.mPostImageBorder.setVisibility(View.GONE);
        if (post.getImage() != null) {
            byte[] imageAsBytes = Base64.decode(post.getImage().getBytes(), Base64.DEFAULT);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            holder.mPostImage.setImageBitmap(bitmap);
            holder.mPostImageBorder.setVisibility(View.VISIBLE);
            if (!mFromMainActivity) {
                holder.mPostImage.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentManager fm = ((PostActivity) mActivity).getSupportFragmentManager();
                                ImageDialog imageDialog = ImageDialog.newInstance(bitmapHandler.getLarger(bitmap));
                                imageDialog.show(fm, "fragment_image_dialog");
                            }
                        }
                );
            }
            else {
                final CardView cardView = holder.mCardView;
                holder.mPostImage.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cardView.performClick();
                            }
                        }
                );
            }
        }
        else {
            holder.mPostImage.setImageBitmap(null);
        }

        if (mFromMainActivity)
            holder.mCloseButton.setVisibility(View.INVISIBLE);

        if (!mFromMainActivity && MainActivity.getUid() == post.getUser().getUid())
            holder.mDeletePostButton.setVisibility(View.VISIBLE);

        holder.mUpvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
        holder.mUpvote.setTag(R.color.gray);
        holder.mDownvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
        holder.mDownvote.setTag(R.color.gray);

        if (post.getVoted() == 1) {
            holder.mUpvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.green));
            holder.mUpvote.setTag(R.color.green);
        }
        else if (post.getVoted() == -1) {
            holder.mDownvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
            holder.mDownvote.setTag(R.color.red);
        }

        if (post.getComments() != null) {
            ArrayList<Comment> comments = post.getComments();
            holder.mCommentsSection.removeAllViews();

            ViewGroup.LayoutParams commentsSectionSize = holder.mCommentsSection.getLayoutParams();
            if (mFromMainActivity)
                commentsSectionSize.height = 0;
            else
                commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.mCommentsSection.setLayoutParams(commentsSectionSize);

            for (Comment c : comments) {
                ViewGroup container = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.comment, null);
                ((TextView) container.findViewById(R.id.username)).setText(c.getUser().getUsername());
                ((TextView) container.findViewById(R.id.user)).setText(c.getUser().getName());
                ((TextView) container.findViewById(R.id.comment)).setText(c.getText());
                ((TextView) container.findViewById(R.id.time)).setText(c.getCommented());
                LinearLayout commentImageBorder = (LinearLayout) container.findViewById(R.id.commentImageBorder);
                commentImageBorder.setVisibility(View.GONE);
                ImageView commentProfilePicture = (ImageView) container.findViewById(R.id.commentProfilePicture);
                if (c.getUser().getImage() != null) {
                    byte[] imageAsBytes = Base64.decode(c.getUser().getImage().getBytes(), Base64.DEFAULT);
                    Bitmap thumbnail = bitmapHandler.getThumbnail(
                            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                    );
                    commentProfilePicture.setImageBitmap(thumbnail);
                }
                else {
                    commentProfilePicture.setImageBitmap(null);
                    commentProfilePicture.setImageResource(R.drawable.person);
                }
                ImageView commentImage = (ImageView) container.findViewById(R.id.commentImage);
                if (c.getImage() != null) {
                    byte[] imageAsBytes = Base64.decode(c.getImage().getBytes(), Base64.DEFAULT);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                    commentImage.setImageBitmap(bitmap);
                    commentImageBorder.setVisibility(View.VISIBLE);
                    if (!mFromMainActivity) {
                        commentImage.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FragmentManager fm = ((PostActivity) mActivity).getSupportFragmentManager();
                                        ImageDialog imageDialog = ImageDialog.newInstance(bitmapHandler.getLarger(bitmap));
                                        imageDialog.show(fm, "fragment_image_dialog");
                                    }
                                }
                        );
                    }
                }
                else {
                    commentImage.setImageBitmap(null);
                    commentImageBorder.setVisibility(View.GONE);
                }
                holder.mCommentsSection.removeView(holder.mCommentsSection.findViewById(R.id.progress));
                holder.mCommentsSection.addView(container);
                if (!mFromMainActivity && MainActivity.getUid() == c.getUser().getUid()) {
                    holder.mDeleteCommentButton = (ImageView) container.findViewById(R.id.deleteCommentButton);
                    holder.mDeleteCommentButton.setOnClickListener(holder);
                    holder.mDeleteCommentButton.setVisibility(View.VISIBLE);
                }
            }
        }

        if (!mFromMainActivity) {
            ViewGroup container = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.addcomment, null);
            ((TextView) container.findViewById(R.id.comment)).setText(stringComment);
            holder.mCommentsSection.addView(container);
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView mCardView;
        public TextView mUsername, mName, mPosted, mText, mNoComments, mUpvotes, mDownvotes;
        public ImageView mCloseButton, mDeletePostButton, mDeleteCommentButton;
        public ImageView mPostProfilePicture, mUpvote, mDownvote, mPostImage;
        public LinearLayout mCommentsSection, mPostInfo, mPostImageBorder;

        private OnItemClickListener mListener;

        private View.OnClickListener cardListener;
        private View.OnClickListener closeListener;
        private View.OnClickListener voteListener;
        private RequestQueue requestQueue;

        private ArrayList<Post> mPosts;
        private boolean mFromMainActivity;

        public ViewHolder(View view, ArrayList<Post> posts, OnItemClickListener listener, boolean fromMainActivity) {
            super(view);

            mPosts = posts;
            mListener = listener;
            mFromMainActivity = fromMainActivity;

            mCardView = (CardView) view;
            mUsername = (TextView) view.findViewById(R.id.username);
            mName = (TextView) view.findViewById(R.id.name);
            mPostProfilePicture = (ImageView) view.findViewById(R.id.postProfilePicture);
            mPosted = (TextView) view.findViewById(R.id.time);
            mText = (TextView) view.findViewById(R.id.text);
            mPostImage = (ImageView) view.findViewById(R.id.postImage);
            mPostImageBorder = (LinearLayout) view.findViewById(R.id.postImageBorder);
            mNoComments = (TextView) view.findViewById(R.id.comments);
            mUpvote = (ImageView) view.findViewById(R.id.upvote);
            mUpvotes = (TextView) view.findViewById(R.id.upvotes);
            mDownvote = (ImageView) view.findViewById(R.id.downvote);
            mDownvotes = (TextView) view.findViewById(R.id.downvotes);
            mCommentsSection = (LinearLayout) view.findViewById(R.id.commentsSection);

            mPostImageBorder.setVisibility(View.GONE);

            requestQueue = Volley.newRequestQueue(mActivity.getApplicationContext());

            setUpListeners();

            mUpvote.setOnClickListener(voteListener);
            mUpvotes.setOnClickListener(voteListener);
            mDownvote.setOnClickListener(voteListener);
            mDownvotes.setOnClickListener(voteListener);

            mPostImage.setOnClickListener(this);

            if (mFromMainActivity) {
                mCloseButton = (ImageView) view.findViewById(R.id.closeButton);
                mCloseButton.setOnClickListener(closeListener);

                mCardView.setOnClickListener(cardListener);
            }
            else {
                mDeletePostButton = (ImageView) view.findViewById(R.id.deletePostButton);
                mDeletePostButton.setOnClickListener(this);

                mPostInfo = (LinearLayout) view.findViewById(R.id.postInfo);
                mPostInfo.setOnClickListener(this);
            }
        }

        /**
         * Sets up listeners for the card, the close button and the vote buttons.
         */
        public void setUpListeners() {

            cardListener = new View.OnClickListener() {

                @Override
                public void onClick(View card) {

                    RecyclerView recyclerView = (RecyclerView) card.getParent();

                    TransitionManager.endTransitions(recyclerView);
                    TransitionManager.beginDelayedTransition(recyclerView);

                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        CardView current = (CardView) recyclerView.getChildAt(i);
                        current.setCardElevation(MainActivity.dpToPixels(1, current));

                        if (current != card) {
                            LinearLayout comments = (LinearLayout) current.findViewById(R.id.commentsSection);
                            ViewGroup.LayoutParams size = comments.getLayoutParams();
                            size.height = 0;
                            comments.setLayoutParams(size);
                            current.findViewById(R.id.closeButton).setVisibility(View.INVISIBLE);
                            current.setTag("closed");
                        }
                    }

                    CardView cardView = (CardView) card;
                    LinearLayout commentsSection = (LinearLayout) card.findViewById(R.id.commentsSection);
                    ImageView closeButton = (ImageView) card.findViewById(R.id.closeButton);

                    ViewGroup.LayoutParams cardSize = card.getLayoutParams();
                    ViewGroup.LayoutParams commentsSectionSize = commentsSection.getLayoutParams();

                    closeButton.setVisibility(View.INVISIBLE);
                    commentsSectionSize.height = 0;

                    if (card.getTag().equals("closed")) {
                        closeButton.setVisibility(View.VISIBLE);
                        commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        card.setTag("open");
                        cardView.setCardElevation(MainActivity.dpToPixels(5, card));
                    } else { // if (card.getTag().equals("open"))
                        closeButton.setVisibility(View.INVISIBLE);
                        commentsSectionSize.height = 0;
                        card.setTag("closed");
                        commentsSection.setLayoutParams(commentsSectionSize);
                        Intent intent = new Intent(mActivity, PostActivity.class);
                        intent.putExtra("post", mPosts.get(getAdapterPosition()));
                        mActivity.startActivity(intent);
                    }

                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    llm.scrollToPositionWithOffset(getAdapterPosition(), 0);

                    commentsSection.setLayoutParams(commentsSectionSize);
                    cardView.setLayoutParams(cardSize);
                }
            };

            if (mFromMainActivity) {
                closeListener = new View.OnClickListener() {

                    @Override
                    public void onClick(View closeButton) {

                        RecyclerView recyclerView = (RecyclerView) mCardView.getParent();
                        LinearLayout commentsSection = (LinearLayout) mCardView.findViewById(R.id.commentsSection);

                        TransitionManager.endTransitions(recyclerView);
                        TransitionManager.beginDelayedTransition(recyclerView);

                        //ViewGroup.LayoutParams cardSize = mCardView.getLayoutParams();
                        ViewGroup.LayoutParams commentsSectionSize = commentsSection.getLayoutParams();

                        closeButton.setVisibility(View.INVISIBLE);
                        commentsSectionSize.height = 0;
                        //cardSize.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        commentsSection.setLayoutParams(commentsSectionSize);
                        //mCardView.setLayoutParams(cardSize);
                        mCardView.setCardElevation(MainActivity.dpToPixels(1, mCardView));
                        mCardView.setTag("closed");
                    }
                };
            }

            voteListener = new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    View parent = (View) v.getParent();

                    final ImageView up = (ImageView) parent.findViewById(R.id.upvote);
                    final TextView upvotes = (TextView) parent.findViewById(R.id.upvotes);
                    final ImageView down = (ImageView) parent.findViewById(R.id.downvote);
                    final TextView downvotes = (TextView) parent.findViewById(R.id.downvotes);

                    if (v.getId() == R.id.upvote || v.getId() == R.id.upvotes) {
                        if (down.getTag().equals(R.color.red))
                            downvotes.setText("" + (Integer.parseInt(downvotes.getText().toString()) - 1));
                        down.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                        down.setTag(R.color.gray);
                        if (up.getTag().equals(R.color.green)) {
                            up.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                            up.setTag(R.color.gray);
                            upvotes.setText("" + (Integer.parseInt(upvotes.getText().toString()) - 1));
                        } else {
                            up.setColorFilter(ContextCompat.getColor(mActivity, R.color.green));
                            up.setTag(R.color.green);
                            upvotes.setText("" + (Integer.parseInt(upvotes.getText().toString()) + 1));
                        }
                    }
                    else if (v.getId() == R.id.downvote || v.getId() == R.id.downvotes) {
                        if (up.getTag().equals(R.color.green))
                            upvotes.setText("" + (Integer.parseInt(upvotes.getText().toString()) - 1));
                        up.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                        up.setTag(R.color.gray);
                        if (down.getTag().equals(R.color.red)) {
                            down.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                            down.setTag(R.color.gray);
                            downvotes.setText("" + (Integer.parseInt(downvotes.getText().toString()) - 1));
                        }
                        else {
                            down.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
                            down.setTag(R.color.red);
                            downvotes.setText("" + (Integer.parseInt(downvotes.getText().toString()) + 1));
                        }
                    }

                    StringRequest voteRequest = new StringRequest(Request.Method.POST, Database.VOTE_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("vote response: " + response);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.getBoolean("success")) {
                                    upvotes.setText("" + jsonResponse.getInt("upvotes"));
                                    downvotes.setText("" + jsonResponse.getInt("downvotes"));
                                    if (jsonResponse.getInt("vote") == 1) {
                                        up.setColorFilter(ContextCompat.getColor(mActivity,
                                                jsonResponse.getBoolean("voted") ? R.color.green : R.color.gray));
                                        up.setTag(jsonResponse.getBoolean("voted") ? R.color.green : R.color.gray);
                                    }
                                    else if (jsonResponse.getInt("vote") == -1) {
                                        down.setColorFilter(ContextCompat.getColor(mActivity,
                                                jsonResponse.getBoolean("voted") ? R.color.red : R.color.gray));
                                        down.setTag(jsonResponse.getBoolean("voted") ? R.color.red : R.color.gray);
                                    }
                                }
                                else {
                                    Snackbar.make(mActivity.findViewById(R.id.base), jsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, Database.getErrorListener(mActivity.findViewById(R.id.base), null)
                    ) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<>();
                            parameters.put("uid", "" + MainActivity.getUid());
                            parameters.put("pid", "" + mPosts.get(getAdapterPosition()).getPid());
                            if (v.getId() == R.id.upvote || v.getId() == R.id.upvotes)
                                parameters.put("type", "" + 1);
                            else if (v.getId() == R.id.downvote || v.getId() == R.id.downvotes)
                                parameters.put("type", "" + -1);
                            return parameters;
                        }
                    };
                    voteRequest.setRetryPolicy(Database.getRetryPolicy());
                    requestQueue.add(voteRequest);
                }
            };
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }

        /**
         * Onclick interface for custom behavior.
         */
        public interface OnItemClickListener {
            void onClick(View caller);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    /**
     * Onclick interface for custom behavior.
     */
    public interface OnItemClickListener {
        void onClick(View caller);
    }

}
