package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

//This class will download the Stock Name and Stock Company name data from the internet using Thread Runnable
public class StockNameDownloader implements Runnable{

    private static final String TAG = "StockNameDownloader";

    private static final String StockName_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> StockNameHashMap = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(StockName_URL);
        String UrlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(UrlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Checking URL, if not ok exit out of Thread Runnable
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            //Start reading the URL data
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String readLine;
            while((readLine = reader.readLine()) != null){
                sb.append(readLine).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        }
        catch (Exception e){
            Log.e(TAG, "run: ", e);
            return;
        }

        ParsingData(sb.toString());

        Log.d(TAG, "run: StockNameDownloader --> run() ");
    }


    //Parsing Data saving to Hashmap
    private void ParsingData(String s){
        try{
            JSONArray JsArray = new JSONArray(s);

            for(int i = 0; i < JsArray.length(); i++){
                JSONObject StockObject = (JSONObject) JsArray.get(i);

                String StockSymbol = StockObject.getString("symbol");
                String StockCompany = StockObject.getString("name");

                StockNameHashMap.put(StockSymbol, StockCompany); //(key, value)
            }
            Log.d(TAG, "ParsingData: ");
        }
        catch (JSONException e) {
            Log.d(TAG, "ParsingData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Getting the user input to match the data base stored in the Hashmap earlier using ParsingData(String s)
    public static ArrayList<String> findMatches(String str){
        String strToMatch = str.toLowerCase().trim();
        HashSet<String> matchSet = new HashSet<>();

        for(String StockSymbol: StockNameHashMap.keySet()){
            if(StockSymbol.toLowerCase().trim().contains(strToMatch)){
                matchSet.add(StockSymbol + " - " + StockNameHashMap.get(StockSymbol));  //get --> value
            }
            String StockCompany = StockNameHashMap.get(StockSymbol);
            if(StockCompany != null && StockCompany.toLowerCase().trim().contains(strToMatch)){
                matchSet.add(StockSymbol + " - " + StockCompany);
            }
        }

        //Sorting by putting the hashmap into ArrayList and sort
        ArrayList<String> results = new ArrayList<>(matchSet);
        Collections.sort(results);

        return results;
    }
}
