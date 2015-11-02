package com.example.android.popularmovies2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 */
public class ReviewListAdapter extends ArrayAdapter<ReviewListAdapter.Item> {

    // The List view can only be sized after we know the width.
    // Before that the text view does not know what fits on one line.
    private boolean mListHeightSizingRequested = false;
    private ListView mListView;

    public ReviewListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public void setListView( ListView listView) {
        mListView = listView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent,false);
        }
        final Item item = getItem(position);
        TextView textView = (TextView)convertView.findViewById(R.id.review_item_view);
        int parentWidth = parent.getWidth();
        if( parentWidth != 0) {
            textView.setWidth( parentWidth);
            if(!mListHeightSizingRequested) {
                mListHeightSizingRequested = true;
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        Utility.setListViewHeightBasedOnItems(mListView);
                    }
                });
            }
        }
        textView.setText(item.getReviewStr());

        return convertView;
    }

    public void addFromJson( String jsonStr) {
        clear();
        addAll(Item.getItemsFromJson(jsonStr));
    }

    static public class Item {

        final static public String MDB_KEY_REVIEW_AUTHOR        = "author";
        final static public String MDB_KEY_REVIEW_CONTENT       = "content";

        final static private String MDB_REVIEW_LIST             = "results";

        final static public String[] MDB_KEY_LIST = {
            MDB_KEY_REVIEW_AUTHOR,
            MDB_KEY_REVIEW_CONTENT,
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

        static public Item[] getItemsFromJson( String jsonStr) {
            ArrayList<HashMap<String,String>> infoList =
                    Utility.getDataFromJson( jsonStr, MDB_REVIEW_LIST, MDB_KEY_LIST);
            Item[] items = new Item[infoList.size()];
            for ( int ii = 0; ii < items.length; ii++) {
                items[ii] = new Item(infoList.get(ii));
            }
            return items;
        }

        public String getReviewStr() {
            return mInfo.get( MDB_KEY_REVIEW_CONTENT) + "\n - " + mInfo.get(MDB_KEY_REVIEW_AUTHOR);
        }

    }

}
