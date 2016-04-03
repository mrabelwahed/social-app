package yberg.intnet.com.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import yberg.intnet.com.app.util.BitmapHandler;

/**
 * Created by Viktor on 2016-03-24.
 */
public class SearchAdapter extends ArrayAdapter<SearchItem> {

    private BitmapHandler bitmapHandler;

    private Activity context;
    private ArrayList<SearchItem> items;

    public SearchAdapter(Activity context, int textViewResourceId, ArrayList<SearchItem> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;

        bitmapHandler = new BitmapHandler(null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        Holder holder;

        if (view == null) {

            // Get a new instance of the row layout view
            LayoutInflater inflater = context.getLayoutInflater();
            if (context instanceof PeopleActivity)
                view = inflater.inflate(R.layout.person_item, null);
            else
                view = inflater.inflate(R.layout.search_item, null);

            holder = new Holder(view);
            view.setTag(holder);
        }
        else {
            holder = (Holder) view.getTag();
        }

        SearchItem item = super.getItem(position);

        // Set username, name and image of search result items
        holder.username.setText(item.getUsername());
        holder.name.setText(item.getName());
        if (item.getImage() != null) {
            byte[] imageAsBytes = Base64.decode(item.getImage().getBytes(), Base64.DEFAULT);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            final Bitmap thumbnail = bitmapHandler.getThumbnail(bitmap);
            holder.image.setImageBitmap(thumbnail);
        }
        else {
            holder.image.setImageBitmap(null);
            holder.image.setImageResource(R.drawable.person);
        }

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
