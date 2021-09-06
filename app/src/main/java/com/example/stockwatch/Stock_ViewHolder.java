package com.example.stockwatch;


import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class Stock_ViewHolder extends RecyclerView.ViewHolder {

    TextView StockSymbolTextView;
    TextView StockCompanyTextView;
    TextView StockPriceTextView;
    TextView StockPriceChangeTextView;
    TextView StockPercentageChangeTextView;

    //Will be created for each Stock in the StocksList for Display in the MainActivity using the stock_list_row_template.xml
    public Stock_ViewHolder(View itemView){
        super(itemView);

        this.StockSymbolTextView = itemView.findViewById(R.id.StockSymbolTextView);
        this.StockCompanyTextView = itemView.findViewById(R.id.CompanyNameTextView);
        this.StockPriceTextView = itemView.findViewById(R.id.StockPriceTextView);
        this.StockPriceChangeTextView = itemView.findViewById(R.id.PriceChangeTextView);
        this.StockPercentageChangeTextView = itemView.findViewById(R.id.PercentChangeTextView);
    }


}
