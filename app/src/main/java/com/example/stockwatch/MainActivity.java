package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    //Stock objects Stored here
    private final ArrayList<Stock> StocksList = new ArrayList<>();

    //Temporary List for storing stock when no network connection & READJSONFILE
    private final ArrayList<Stock> tmpStocksList = new ArrayList<>();

    //RecyclerView & Adapter
    private RecyclerView recyclerView;
    private Stock_Adapter s_adapter;

    //Swiper Refresh
    private SwipeRefreshLayout swiper;

    //USER INPUT STOCK CHOICE
    private String UserChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the color of the Menu Bar/Action Bar (NOT Taught in class) so reference --> https://www.geeksforgeeks.org/how-to-change-the-color-of-action-bar-in-an-android-app/
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000"))); //set to BLACK

        //Set the Recycler View
        recyclerView = findViewById(R.id.RecyckerView);

        //Set the Adapter
        s_adapter = new Stock_Adapter(StocksList, this);

        //Set the Adapter onto RecyclerView
        recyclerView.setAdapter(s_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Set Swiper Refresh
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        //Load the initial data from internet
        StockNameDownloader stockNameRunnable = new StockNameDownloader();
        new Thread(stockNameRunnable).start();

        readJSONFile();

        //Set stocks data to 0 if no network connection
        if(CheckNetworkConnection() == false){ //No Network Connection
            for(Stock stock: tmpStocksList){
                stock.setPrice(0.0);
                stock.setPriceChange(0.0);
                stock.setPercentageChange(0.0);
                StocksList.add(stock);
            }
            Collections.sort(StocksList);
            s_adapter.notifyDataSetChanged(); //VERY IMPORTANT to update Recycler View
        }
        else{ //There is network connection

            //get the stocks data for update
            for(Stock stock: tmpStocksList){
                StockDataDownloader stockDataDownloader = new StockDataDownloader(this, stock.getSymbol());
                new Thread(stockDataDownloader).start();
            }
        }

        //Testing  (OR forloop)
//        String TestSymbol = "AAPL";
//        String TestCompany = "Apple, Inc.";
//        double TestPrice = 19.59;
//        double TestPriceChange = 2.39;
//        double TestPercentageChange = 9.59;
//
//        StocksList.add(new Stock(TestSymbol, TestCompany, TestPrice, TestPriceChange, TestPercentageChange));
//
//        String TestSymbol1 = "GOOG";
//        String TestCompany1 = "Google, Inc.";
//        double TestPrice1 = 35.40;
//        double TestPriceChange1 = -3.59;
//        double TestPercentageChange1 = -5.59;
//
//        StocksList.add(new Stock(TestSymbol1, TestCompany1, TestPrice1, TestPriceChange1, TestPercentageChange1));

    }

    //Inflate custom menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //For menu button "AddStock"
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.AddStock){
            CreateStockDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void CreateStockDialog(){

        if(CheckNetworkConnection() == false){ //Connection is false
            //Create Dialog for no connection
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        else{ //Connection is TRUE

            //Dialog for User stock input
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            final EditText UserEditText = new EditText(this); //Create EditText Dialog
            UserEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            UserEditText.setGravity(Gravity.CENTER_HORIZONTAL);
            UserEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS); //user inputs set to ALL CAPS

            builder1.setView(UserEditText);

            builder1.setTitle("Stock Selection");
            builder1.setMessage("Please enter a Stock Symbol:");
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserChoice = UserEditText.getText().toString().trim(); // trim to remove spaces

                    //Find the match from UserInput from StockNameDownloader
                    final ArrayList<String> results = StockNameDownloader.findMatches(UserChoice);

                    if(results.size() == 0){ //No match
                        //Dialog for Stock No Match
                        CreateDialog(UserChoice);
                    }
                    else if(results.size() == 1){ //Only one match
                        GetSelection(results.get(0));
                    }
                    else{ //if more than 1 match
                        String[] array = results.toArray(new String[0]);

                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                        builder2.setTitle("Make a selection");
                        builder2.setItems(array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String stockSymbol = results.get(which);
                                GetSelection(stockSymbol);
                            }
                        });
                        builder2.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing and return, user cancelled
                            }
                        });
                        AlertDialog dialog1 = builder2.create();
                        dialog1.show();
                    }
                }
            });

            builder1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //User cancelled dialog, do nothing
                }
            });

            AlertDialog dialog = builder1.create();
            dialog.show();
        }
    }

    private void CreateDialog(String StockSymbol){
        //Dialog for stock no match
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + StockSymbol);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void GetSelection(String StockSymbol){
        String[] data = StockSymbol.split("-"); //split data "AAPL - Apple"
        StockDataDownloader stockDataDownloader = new StockDataDownloader(this, data[0].trim());
        new Thread(stockDataDownloader).start();
    }

    //Click the Stock on the RecyclerView will take user to the website of that Stock
    @Override
    public void onClick(View v) {
        final int position = recyclerView.getChildLayoutPosition(v);
        Stock StockSelected = StocksList.get(position);
        String StockSymbol = StockSelected.getSymbol();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String url = "https://www.marketwatch.com/investing/stock/" + StockSymbol;

        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    //Long click for delete
    @Override
    public boolean onLongClick(View v) {
        final int position = recyclerView.getChildLayoutPosition(v);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.delete);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol " + StocksList.get(position).getSymbol() + "?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StocksList.remove(position);

                //Update JSONFile for the removed stock
                writeJSONFile();

                s_adapter.notifyDataSetChanged(); //VERY IMPORTANT for updating the display of RecyclerView
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing user cancelled dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    //for Swiper Refresh
    private void doRefresh(){
        if(CheckNetworkConnection() == true){
            StocksList.clear();
            tmpStocksList.clear(); //solved stocks being added twice
            readJSONFile();

            //Get stock data updates
            for(Stock stock: tmpStocksList) {
                StockDataDownloader stockDataDownloader = new StockDataDownloader(this, stock.getSymbol());
                new Thread(stockDataDownloader).start();
            }
        }
        else{ //Cannot refresh cause Network Connection
            //Create Dialog for no connection
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        s_adapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }

    //Checking Network status
    private boolean CheckNetworkConnection(){
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = CM.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnectedOrConnecting()){ //Network Connected --> TRUE
            return true;
        }
        else{ //No Network --> FALSE
            return false;
        }
    }


    public void addStock(Stock stock){
        //No stock found
        if(stock == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Symbol Not Found: " + UserChoice);
            builder.setMessage("No Data for selection");

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        //for Duplicate Stock
        if(StocksList.contains(stock)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.error);
            builder.setTitle("Duplicate Stock");
            builder.setMessage(stock.getSymbol() + " is already displayed");

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        StocksList.add(stock);
        Collections.sort(StocksList);

        //Update JSON File
        writeJSONFile();

        s_adapter.notifyDataSetChanged(); //VERY IMPORTANT to update RecyclerView Display
    }


    @Override
    protected void onPause() {
        super.onPause();
        writeJSONFile();
    }

    //Read or Load JSON File
    private void readJSONFile(){
        try{
            InputStream FIS = getApplicationContext().openFileInput("StocksFile.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(FIS, StandardCharsets.UTF_8));

            //Saving text read into Builder Object
            StringBuilder sb = new StringBuilder();
            String ReadLine;
            while((ReadLine = reader.readLine()) != null){
                sb.append(ReadLine);
            }
            FIS.close();

            JSONArray JsArray = new JSONArray(sb.toString());

            for(int i = 0; i < JsArray.length(); i++){
                JSONObject JsObject = JsArray.getJSONObject(i);
                String StockSymbol = JsObject.getString("Stock_Symbol");
                String StockCompany = JsObject.getString("Stock_Company");
                String TmpStockPriceStr = JsObject.getString("Stock_Price");
                double StockPrice = Double.parseDouble(TmpStockPriceStr); //convert to double
                String TmpStockPriceChangeStr = JsObject.getString("Stock_PriceChange");
                double StockPriceChange = Double.parseDouble(TmpStockPriceChangeStr); //convert to double
                String TmpStockPercentageChangeStr = JsObject.getString("Stock_PercentageChange");
                double StockPercentageChange = Double.parseDouble(TmpStockPercentageChangeStr); //convert to double

                Stock stock = new Stock(StockSymbol, StockCompany, StockPrice, StockPriceChange, StockPercentageChange);
                tmpStocksList.add(stock);
            }

            s_adapter.notifyDataSetChanged(); //VERY IMPORTANT updating data
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Write or Save JSON File
    private void writeJSONFile(){
        try{
            FileOutputStream FOS = getApplicationContext().openFileOutput("StocksFile.json", Context.MODE_PRIVATE);

            JSONArray JsArray = new JSONArray();

            for(Stock stock: StocksList){
                JSONObject JSNOteObject = new JSONObject();
                JSNOteObject.put("Stock_Symbol", stock.getSymbol());
                JSNOteObject.put("Stock_Company", stock.getCompany());
                JSNOteObject.put("Stock_Price", stock.getPrice());
                JSNOteObject.put("Stock_PriceChange", stock.getPriceChange());
                JSNOteObject.put("Stock_PercentageChange", stock.getPercentageChange());

                JsArray.put(JSNOteObject);
            }

            //Writing to the file when finished
            String JSONFileContent = JsArray.toString();
            FOS.write(JSONFileContent.getBytes()); //needs to be in bytes
            FOS.close();
        }
        catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}