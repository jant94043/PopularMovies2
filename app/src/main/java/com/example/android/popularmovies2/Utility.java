package com.example.android.popularmovies2;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 */
public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     *
     */
    public static ArrayList<HashMap<String, String>> getDataFromJson(String jsonStr,
                                                                     String listName, String[] keys) {

        ArrayList<HashMap<String, String>> itemMaps = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(jsonStr);
            JSONArray array = response.getJSONArray(listName);
            int count = array.length();

            for (int mI = 0; mI < count; mI++) {
                JSONObject itemObject = array.getJSONObject(mI);
                HashMap<String, String> itemMap = new HashMap<>();
                for (String key : keys) {
                    String value = itemObject.optString(key, "no_value");
                    itemMap.put(key, value);
                }
                itemMaps.add(itemMap);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }

        return itemMaps;
    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * This method I got off the web.
     * http://blog.lovelyhq.com/setting-listview-height-depending-on-the-items/
     *
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;
        } else {
            return false;
        }

    }
}
