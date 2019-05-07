package com.barak.tabs.network;

import com.barak.tabs.app.App;
import com.barak.tabs.model.ChangeLogDialog;
import com.barak.tabs.model.MyTab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DownloadExecelDataSource  {


    private static DownloadExecelDataSource instance;

    public static DownloadExecelDataSource getInstance() {
        return instance;
    }


    public static ChangeLogDialog downloadUrl(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();
            is = conn.getInputStream();

            String result = convertStreamToString(is);
            int start = result.indexOf("{", result.indexOf("{") + 1);
            int end = result.lastIndexOf("}");
            if (start<0 ||end<0){
                return null;
            }
            String jsonResponse = result.substring(start, end);
            try {
                JSONObject table = new JSONObject(jsonResponse);
                return (processJson(table));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    private static ChangeLogDialog processJson(JSONObject object) {
        ChangeLogDialog newDialog = null;
        LinkedHashMap<String, MyTab> tMap = new LinkedHashMap<>();
        for (MyTab t : App.getTabs()) {
            tMap.put(t.getUrl(), t);
        }
        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");
                String title = columns.getJSONObject(0).getString("v");
                String url = columns.getJSONObject(1).getString("v");
                int action = columns.getJSONObject(2).getInt("v");
                int tabType = columns.getJSONObject(3).getInt("v");
                boolean visibility = false;
                switch (action) {
                    case 9:
                        tMap.remove(url);
                        break;
                    case 1:
                        visibility = true;
                    case 0:
                        tMap.put(url, new MyTab(title, url, MyTab.TabType.fromInt(tabType), visibility));
                        break;
                    case 10:
                    case 11:
                        newDialog = new ChangeLogDialog(title,url,action);

                }
            }
            App.setStringArrayPref_(new ArrayList<>(tMap.values()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newDialog;
    }
}
