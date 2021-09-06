package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

//This class will download a stock data
public class StockDataDownloader implements Runnable{

    private static final String TAG = "StockDataDownloader";

    private static final String Stock_Data_URL1 = "https://cloud.iexapis.com/stable/stock/";
    private static final String Stock_Data_URL2 = "/quote?token=";
    private static final String API_Token = "pk_dbf683cbed9e44be8e0ae2f7456c2702";

    private MainActivity mainAct;
    private String searchTarget;

    public StockDataDownloader(MainActivity mainAct, String searchTarget){
        this.mainAct = mainAct;
        this.searchTarget = searchTarget;
    }

    @Override
    public void run() {
        //Building the URL
        Uri.Builder uriBuilder = Uri.parse(Stock_Data_URL1 + searchTarget + Stock_Data_URL2 + API_Token).buildUpon();

        String urlToUse = uriBuilder.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try{

            //connect
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check connection
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return; //exit from this run() thread runnable
            }

            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));

            String ReadLine;
            while((ReadLine = reader.readLine()) != null){
                sb.append(ReadLine).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        }
        catch (Exception e){
            Log.e(TAG, "run: ", e);
            return;
        }

        ParsingData(sb.toString());

        Log.d(TAG, "run: StockDataDownloader --> run()");
    }

    //Parsing Data
    private void ParsingData(String s){
        try{
            JSONObject JsObject = new JSONObject(s);

            String StockSymbol = JsObject.getString("symbol");
            String StockCompany = JsObject.getString("companyName");

            String StockPriceStr = JsObject.getString("latestPrice");
            double StockPrice = 0.0;
            if(!StockPriceStr.trim().isEmpty() && !StockPriceStr.trim().equals("null")){
                StockPrice = Double.parseDouble(StockPriceStr);
            }

            String StockPriceChangeStr = JsObject.getString("change");
            double StockPriceChange = 0.0;
            if(!StockPriceChangeStr.trim().isEmpty() && !StockPriceChangeStr.trim().equals("null")){
                StockPriceChange = Double.parseDouble(StockPriceChangeStr);
            }

            String StockPercentageChangeStr = JsObject.getString("changePercent");
            double StockPercentageChange = 0.0;
            if(!StockPercentageChangeStr.trim().isEmpty() && !StockPercentageChangeStr.trim().equals("null")){
                    StockPercentageChange = Double.parseDouble(StockPercentageChangeStr);
            }

            final Stock stock = new Stock(StockSymbol, StockCompany, StockPrice, StockPriceChange, StockPercentageChange);

            mainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mainAct.addStock(stock);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
