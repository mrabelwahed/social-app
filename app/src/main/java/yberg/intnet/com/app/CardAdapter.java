package yberg.intnet.com.app;

import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Viktor on 2016-03-04.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private ArrayList<Post> mPosts;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(ArrayList<Post> posts) {
        mPosts = posts;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public TextView mUsername, mName, mPosted, mText;
        public ViewHolder(View view) {
            super(view);
            mCardView = (CardView) view;
            mUsername = (TextView) view.findViewById(R.id.username);
            mName = (TextView) view.findViewById(R.id.name);
            mPosted = (TextView) view.findViewById(R.id.time);
            mText = (TextView) view.findViewById(R.id.text);

            mCardView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            RecyclerView recyclerView = (RecyclerView) view.getParent();

                            TransitionManager.endTransitions(recyclerView);
                            TransitionManager.beginDelayedTransition(recyclerView);

                            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                                CardView current = (CardView) recyclerView.getChildAt(i);
                                current.setCardElevation(dpToPixels(1, current));

                                if (current != view) {
                                    ViewGroup.LayoutParams size = current.getLayoutParams();
                                    size.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                    current.setLayoutParams(size);
                                }
                            }

                            CardView cardView = (CardView) view;

                            // TODO
                            // Instead of maximizing card view height, keep it wrapped and add
                            // comments in new text views at the bottom.
                            ViewGroup.LayoutParams size = view.getLayoutParams();
                            if (size.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                                size.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            }
                            else {
                                size.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                cardView.setCardElevation(dpToPixels(5, view));
                            }

                            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                            llm.scrollToPositionWithOffset(getAdapterPosition(), 0);

                            cardView.setLayoutParams(size);
                        }
                    }
            );
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // Create a new view

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Post post = mPosts.get(position);

        holder.mUsername.setText(post.getUser().getUsername());
        holder.mName.setText(post.getUser().getName());
        holder.mPosted.setText(post.getPosted());
        holder.mText.setText(post.getText());
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
