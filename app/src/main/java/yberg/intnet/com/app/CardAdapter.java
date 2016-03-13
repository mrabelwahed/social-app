package yberg.intnet.com.app;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Viktor on 2016-03-04.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private ArrayList<Post> mPosts;
    private ArrayList<Comment> mComments;
    private static Activity mActivity;
    private static boolean mFromMainActivity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(Activity activity, ArrayList<Post> posts, boolean fromMainActivity) {
        mActivity = activity;
        mPosts = posts;
        mFromMainActivity = fromMainActivity;
    }

    public CardAdapter(Activity activity, ArrayList<Post> posts, ArrayList<Comment> comments, boolean fromMainActivity) {
        this(activity, posts, fromMainActivity);
        mComments = comments;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView mUsername, mName, mPosted, mText, mNoComments, mUpvotes, mDownvotes;
        public ImageView mCloseButton;
        public ImageView mUpvote, mDownvote;

        public LinearLayout mCommentsSection;

        private View.OnClickListener cardListener;
        private View.OnClickListener closeListener;
        private View.OnClickListener voteListener;

        private ArrayList<Post> mPosts;
        private ArrayList<Comment> mComments;

        public ViewHolder(View view, ArrayList<Post> posts, ArrayList<Comment> comments) {
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

            this.mPosts = posts;
            this.mComments = comments;

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
                        current.setCardElevation(dpToPixels(1, current));

                        if (current != card) {
                            ViewGroup.LayoutParams size = current.getLayoutParams();
                            size.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            current.setLayoutParams(size);
                            current.findViewById(R.id.close_button).setVisibility(View.INVISIBLE);
                        }
                    }

                    CardView cardView = (CardView) card;
                    LinearLayout commentsSection = (LinearLayout) card.findViewById(R.id.comments_section);
                    ImageView closeButton = (ImageView) card.findViewById(R.id.close_button);


                    ViewGroup.LayoutParams cardSize = card.getLayoutParams();
                    ViewGroup.LayoutParams commentsSectionSize = commentsSection.getLayoutParams();

                    closeButton.setVisibility(View.INVISIBLE);
                    commentsSectionSize.height = 0;

                    if (cardSize.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        closeButton.setVisibility(View.VISIBLE);
                        commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        cardSize.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        cardView.setCardElevation(dpToPixels(5, card));
                    } else { // if size.height == ViewGroup.LayoutParams.MATCH_PARENT)
                        closeButton.setVisibility(View.INVISIBLE);
                        commentsSectionSize.height = 0;
                        cardSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
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

                    ViewGroup.LayoutParams cardSize = mCardView.getLayoutParams();
                    ViewGroup.LayoutParams commentsSectionSize = commentsSection.getLayoutParams();

                    if (cardSize.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                        closeButton.setVisibility(View.INVISIBLE);
                        commentsSectionSize.height = 0;
                        cardSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        commentsSection.setLayoutParams(commentsSectionSize);
                        mCardView.setLayoutParams(cardSize);
                        mCardView.setCardElevation(dpToPixels(1, mCardView));
                    }
                }
            };

            voteListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // TODO Send to php file and get amount of up & downvotes back
                    View parent = (View) v.getParent();

                    ImageView up = (ImageView) parent.findViewById(R.id.upvote);
                    TextView upvotes = (TextView) parent.findViewById(R.id.upvotes);
                    ImageView down = (ImageView) parent.findViewById(R.id.downvote);
                    TextView downvotes = (TextView) parent.findViewById(R.id.downvotes);

                    if (v.getId() == R.id.upvote || v.getId() == R.id.upvotes) {
                        down.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorDivider));
                        down.setTag(R.color.colorDivider);
                        if (up.getTag().equals(R.color.green)) {
                            up.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorDivider));
                            up.setTag(R.color.colorDivider);
                            upvotes.setText("" + (Integer.parseInt(upvotes.getText().toString()) - 1));
                        } else {
                            up.setColorFilter(ContextCompat.getColor(mActivity, R.color.green));
                            up.setTag(R.color.green);
                            upvotes.setText("" + (Integer.parseInt(upvotes.getText().toString()) + 1));
                        }
                    }
                    else if (v.getId() == R.id.downvote || v.getId() == R.id.downvotes) {
                        up.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorDivider));
                        up.setTag(R.color.colorDivider);
                        if (down.getTag().equals(R.color.red)) {
                            down.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorDivider));
                            down.setTag(R.color.colorDivider);
                            downvotes.setText("" + (Integer.parseInt(downvotes.getText().toString()) - 1));
                        }
                        else {
                            down.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
                            down.setTag(R.color.red);
                            downvotes.setText("" + (Integer.parseInt(downvotes.getText().toString()) + 1));
                        }
                    }
                }
            };
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // Create a new view

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        ViewHolder vh = new ViewHolder(view, mPosts, mComments);

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
        holder.mNoComments.setText(post.getComments() + " comments");
        holder.mUpvotes.setText("" + post.getUpvotes());
        holder.mDownvotes.setText("" + post.getDownvotes());

        if (mComments != null) {
            holder.mCommentsSection.removeAllViews();
            if (mComments.size() > 0) {
                ViewGroup.LayoutParams commentsSectionSize = holder.mCommentsSection.getLayoutParams();
                commentsSectionSize.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.mCommentsSection.setLayoutParams(commentsSectionSize);
            }
            for (Comment c : mComments) {
                ViewGroup container = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.comment, null);
                ((TextView) container.findViewById(R.id.comment)).setText(c.getText());
                ((TextView) container.findViewById(R.id.user)).setText(c.getUser().getName());
                ((TextView) container.findViewById(R.id.time)).setText(c.getCommented());
                holder.mCommentsSection.removeView(holder.mCommentsSection.findViewById(R.id.progress));
                holder.mCommentsSection.addView(container);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static float dpToPixels(int dp, View view) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }

}
