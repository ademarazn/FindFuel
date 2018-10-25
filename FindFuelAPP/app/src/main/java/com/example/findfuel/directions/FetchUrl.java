package com.example.findfuel.directions;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Ademar
 * @since Classe criada em 14/10/2017
 */

// Fetches data from url passed
public class FetchUrl extends AsyncTask<Object, Void, ArrayList<String>> {

    private GoogleMap mMap;

    @Override
    protected ArrayList<String> doInBackground(Object... objects) {

        // For storing data from web service
        ArrayList<String> data = new ArrayList<>();

        try {
            mMap = (GoogleMap) objects[0];
            // Fetching the data from web service
            data = downloadUrl(objects[1].toString());
            Log.d("Background Task data", data.get(0));
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        super.onPostExecute(result);

        GetDirectionsData getDirectionsData = new GetDirectionsData();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = result.get(0);

        // Invokes the thread for parsing the JSON data
        getDirectionsData.execute(dataTransfer);

    }

    private ArrayList<String> downloadUrl(String strUrl) throws IOException {
        ArrayList<String> data = new ArrayList<>();
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("text") && data.size() < 2) {
                    data.add(line.trim().replace("\"text\" : \"", "").replace("\",", ""));
                }
                sb.append(line);
            }

            data.add(0, sb.toString());
            Log.d("downloadUrl", data.get(0));
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }
}
