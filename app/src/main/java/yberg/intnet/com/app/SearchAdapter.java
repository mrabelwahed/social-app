package yberg.intnet.com.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Viktor on 2016-03-24.
 */
public class SearchAdapter extends ArrayAdapter<SearchItem> {

    private Activity context;
    private ArrayList<SearchItem> items;

    public SearchAdapter(Activity context, int textViewResourceId, ArrayList<SearchItem> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        Holder holder;

        if (view == null) {

            System.out.println("EMPTY!!");
            // Get a new instance of the row layout view
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.search_item, null);

            holder = new Holder(view);
            view.setTag(holder);
        }
        else {
            holder = (Holder) view.getTag();
        }

        if (items.size() == 0) {

            parent.removeView(holder.image);
            parent.removeView(holder.username);
            holder.name.setText("No users");
        }

        SearchItem item = super.getItem(position);

        //holder.image.setImageResource(item.getImage());
        holder.username.setText(item.getUsername());
        holder.name.setText(item.getName());

        return view;
    }

    public class Holder {
        ImageView image;
        TextView username, name;
        public Holder(View v) {
            image = (ImageView) v.findViewById(R.id.image);
            username = (TextView) v.findViewById(R.id.username);
            name = (TextView) v.findViewById(R.id.name);
        }
    }

}
