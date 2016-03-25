package yberg.intnet.com.app;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.TypedValue;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viktor on 2016-03-04.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private ArrayList<Post> mPosts;
    private static Activity mActivity;
    private boolean mFromMainActivity;

    public CardAdapter(Activity activity, ArrayList<Post> posts, boolean fromMainActivity) {
        mActivity = activity;
        mPosts = posts;
        mFromMainActivity = fromMainActivity;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mUsername, mName, mPosted, mText, mNoComments, mUpvotes, mDownvotes;
        public ImageView mCloseButton;
        public ImageView mUpvote, mDownvote;

        public LinearLayout mCommentsSection;

        private View.OnClickListener cardListener;
        private View.OnClickListener closeListener;
        private View.OnClickListener voteListener;
        private RequestQueue requestQueue;

        private ArrayList<Post> mPosts;

        public ViewHolder(View view, ArrayList<Post> posts, boolean mFromMainActivity) {
            super(view);

            mCardView = (CardView) view;
            mUsername = (TextView) view.findViewById(R.id.username);
            mName = (TextView) view.findViewById(R.id.name);
            mPosted = (TextView) view.findViewById(R.id.time);
            mText = (TextView) view.findViewById(R.id.text);
            mNoComments = (TextView) view.findViewById(R.id.comments);
            mUpvote = (ImageView) view.findViewById(R.id.upvote);
            mUpvotes = (TextView) view.findViewById(R.id.upvotes);
            mDownvote = (ImageView) view.findViewById(R.id.downvote);
            mDownvotes = (TextView) view.findViewById(R.id.downvotes);

            mCommentsSection = (LinearLayout) view.findViewById(R.id.comments_section);

            mPosts = posts;

            requestQueue = Volley.newRequestQueue(mActivity.getApplicationContext());

            setUpListeners();

            mUpvote.setOnClickListener(voteListener);
            mUpvotes.setOnClickListener(voteListener);
            mDownvote.setOnClickListener(voteListener);
            mDownvotes.setOnClickListener(voteListener);

            if (mFromMainActivity) {
                mCloseButton = (ImageView) view.findViewById(R.id.close_button);
                mCloseButton.setOnClickListener(closeListener);

                mCardView.setOnClickListener(cardListener);
            }
        }

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
                            /*ViewGroup.LayoutParams size = current.getLayoutParams();
                            size.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            current.setLayoutParams(size);*/
                            LinearLayout comments = (LinearLayout) current.findViewById(R.id.comments_section);
                            ViewGroup.LayoutParams size = comments.getLayoutParams();
                            size.height = 0;
                            comments.setLayoutParams(size);
                            current.findViewById(R.id.close_button).setVisibility(View.INVISIBLE);
                            current.setTag("closed");
                        }
                    }

                    CardView cardView = (CardView) card;
                    LinearLayout commentsSection = (LinearLayout) card.findViewById(R.id.comments_section);
                    ImageView closeButton = (ImageView) card.findViewById(R.id.close_button);

                    ViewGroup.LayoutParams cardSize = card.getLayoutParams();
                    ViewGroup.LayoutParams commentsSectionSize = commentsSection.getLayoutParams();

                    closeButton.setVisibility(View.INVISIBLE);
                    commentsSectionSize.height = 0;

                    if (card.getTag().equals("closed")) {
                        closeButton.setVisibility(View.VISIBLE);
                        commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        //cardSize.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        card.setTag("open");
                        cardView.setCardElevation(MainActivity.dpToPixels(5, card));
                    } else { // if size.height == ViewGroup.LayoutParams.MATCH_PARENT)
                        closeButton.setVisibility(View.INVISIBLE);
                        commentsSectionSize.height = 0;
                        //cardSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        card.setTag("closed");
                        commentsSection.setLayoutParams(commentsSectionSize);
                        Intent intent = new Intent(mActivity, PostActivity.class);
                        intent.putExtra("post", mPosts.get(getAdapterPosition()));
                        System.out.println("post: " + mPosts.get(getAdapterPosition()).getText());
                        mActivity.startActivity(intent);
                    }

                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    llm.scrollToPositionWithOffset(getAdapterPosition(), 0);

                    commentsSection.setLayoutParams(commentsSectionSize);
                    cardView.setLayoutParams(cardSize);
                }
            };

            closeListener = new View.OnClickListener() {

                @Override
                public void onClick(View closeButton) {

                    RecyclerView recyclerView = (RecyclerView) mCardView.getParent();
                    LinearLayout commentsSection = (LinearLayout) mCardView.findViewById(R.id.comments_section);

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

            voteListener = new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    // TODO Send to php file and get amount of up & downvotes back
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

                    StringRequest getFeedRequest = new StringRequest(Request.Method.POST, Database.VOTE_URL, new Response.Listener<String>() {
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
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) { }
                    }) {
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
                    requestQueue.add(getFeedRequest);
                }
            };
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // Create a new view

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        ViewHolder vh = new ViewHolder(view, mPosts, mFromMainActivity);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Post post = mPosts.get(position);

        holder.mUsername.setText("@" + post.getUser().getUsername());
        holder.mName.setText(post.getUser().getName());
        holder.mPosted.setText(post.getPosted());
        holder.mText.setText(post.getText());
        holder.mNoComments.setText("" + post.getNumberOfComments());
        holder.mUpvotes.setText("" + post.getUpvotes());
        holder.mDownvotes.setText("" + post.getDownvotes());
        holder.mCardView.setTag("closed");
        holder.mCardView.setCardElevation(MainActivity.dpToPixels(1, holder.mCardView));
        if (mFromMainActivity)
            holder.mCloseButton.setVisibility(View.INVISIBLE);

        if (post.getVoted() == 1) {
            holder.mUpvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.green));
            holder.mUpvote.setTag(R.color.green);
        }
        else if (post.getVoted() == -1) {
            holder.mDownvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
            holder.mDownvote.setTag(R.color.red);
        }
        else {
            holder.mUpvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
            holder.mUpvote.setTag(R.color.gray);
            holder.mDownvote.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
            holder.mDownvote.setTag(R.color.gray);
        }

        if (post.getComments() != null) {
            ArrayList<Comment> comments = post.getComments();
            holder.mCommentsSection.removeAllViews();

            ViewGroup.LayoutParams commentsSectionSize = holder.mCommentsSection.getLayoutParams();
            System.out.println("mFromMainActivity: " + mFromMainActivity);
            if (mFromMainActivity)
                commentsSectionSize.height = 0;
            else
                commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.mCommentsSection.setLayoutParams(commentsSectionSize);

            for (Comment c : comments) {
                ViewGroup container = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.comment, null);
                ((TextView) container.findViewById(R.id.comment)).setText(c.getText());
                ((TextView) container.findViewById(R.id.user)).setText(c.getUser().getName());
                ((TextView) container.findViewById(R.id.time)).setText(c.getCommented());
                holder.mCommentsSection.removeView(holder.mCommentsSection.findViewById(R.id.progress));
                holder.mCommentsSection.addView(container);
            }
        }

        if (!mFromMainActivity) {
            ViewGroup container = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.addcomment, null);
            ((TextView) container.findViewById(R.id.comment)).setText("Comment");
            holder.mCommentsSection.addView(container);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

}
