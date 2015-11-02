package com.example.android.popularmovies2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 */
public class TrailerListAdapter extends ArrayAdapter<TrailerListAdapter.Item> {


    public TrailerListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent,false);
        }
        final Item item = getItem(position);
        TextView nameView = (TextView)convertView.findViewById(R.id.trailer_name_view);
        nameView.setText(item.getByKey(Item.MDB_KEY_TRAILER_NAME));
        ImageView runView = (ImageView)convertView.findViewById(R.id.trailer_run_icon);
        runView.setOnClickListener(new View.OnClickListener() {
                                       String trailerUrl = item.getTrailerUrl();

                                       @Override
                                       public void onClick(View v) {
                                           showTrailer(trailerUrl);
                                       }
                                   }
        );
        return convertView;
    }

    private void showTrailer( String trailerUrl) {
        getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(trailerUrl)));
    }

    public void addFromJson( String jsonStr) {
        clear();
        addAll(Item.getItemsFromJson(jsonStr));
    }

    static public class Item {

        final static public String MDB_KEY_TRAILER_NAME   = "name";
        final static public String MDB_KEY_SOURCE         = "source";

        final static private String MDB_YOUTUBE_LIST      = "youtube";

        final static public String[] MDB_KEY_LIST = {
            MDB_KEY_TRAILER_NAME,
            MDB_KEY_SOURCE,
        };

        private HashMap<String,String> mInfo = new HashMap<>(MDB_KEY_LIST.length);

        public Item(HashMap<String, String> info) {
            mInfo = info;
        }

        public String getByKey( String key) {
          return mInfo.get( key);
        }

        public HashMap<String,String> getInfoMap() {
            return mInfo;
        }

        public String getTrailerUrl() {
            String trailerSource = mInfo.get(MDB_KEY_SOURCE);
            return "http://www.youtube.com/watch?v="+trailerSource;
        }

        static public Item[] getItemsFromJson( String jsonStr) {
            ArrayList<HashMap<String,String>> infoList =
                    Utility.getDataFromJson( jsonStr,MDB_YOUTUBE_LIST, MDB_KEY_LIST);
            Item[] items = new Item[infoList.size()];
            for ( int ii = 0; ii < items.length; ii++) {
                items[ii] = new Item(infoList.get(ii));
            }
            return items;
        }
    }

}
