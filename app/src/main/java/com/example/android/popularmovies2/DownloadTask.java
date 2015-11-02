package com.example.android.popularmovies2;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *  First arg:  URL as a string
 *
 *  To get JSON string overload this method:
 *         protected void onPostExecute(String json)
 */
abstract class DownloadTask extends
        AsyncTask<String, Void, String> {

    private final String LOG_TAG = DownloadTask.class.getSimpleName();

    @Override
    abstract public void onPostExecute(String json);

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] args) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String res = "";

        if (args.length < 1) {
            return "";
        }

        try {

            // Create the request to movie database, and open the connection
            URL url = new URL(args[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append('\n');
            }

            res = buffer.toString();

            Log.v(LOG_TAG, res);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the  data.
            return "";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return res;
    }
}
