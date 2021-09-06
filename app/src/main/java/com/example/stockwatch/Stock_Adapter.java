package com.example.stockwatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Stock_Adapter extends RecyclerView.Adapter<Stock_ViewHolder> {

    private ArrayList<Stock> StocksList;
    private MainActivity mainAct;

    public Stock_Adapter(ArrayList<Stock> StocksList, MainActivity mainAct){
        this.StocksList = StocksList;
        this.mainAct = mainAct;
    }

    //Create each Stock's ViewHolder depending on how many Stocks object
    @NonNull
    @Override
    public Stock_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate layout to Recycler View
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row_template, parent, false);

        //Setting the Long press and tap for items in the Recycler View
        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new Stock_ViewHolder(itemView);
    }


    //To bind the stock objects to the ViewHolder to set the Text Views
    @Override
    public void onBindViewHolder(@NonNull Stock_ViewHolder holder, int position) {

        Stock stock = StocksList.get(position);

        holder.StockSymbolTextView.setText(stock.getSymbol());
        holder.StockCompanyTextView.setText(stock.getCompany());
        holder.StockPriceTextView.setText(String.format("%.2f", stock.getPrice()));
        holder.StockPercentageChangeTextView.setText(String.format("(%.2f%%)", stock.getPercentageChange())); //%% --> percentage symbol

        int Color_Code = 0;

        if(stock.getPriceChange() < 0){ //if PriceChange is Negative
            Color_Code = 0xFFFF0000; //RED
            holder.StockPriceChangeTextView.setText(String.format("▼ %.2f", stock.getPriceChange())); //For the Arrow down/up symbol --> https://www.alt-codes.net/arrow_alt_codes.php#:~:text=type%20the%20Alt%20Code%20value,got%20a%20%E2%86%93%20downwards%20arrow.
        }
        else{ //if PriceChange is Positive or no change

            if(stock.getPriceChange() == 0.0){
                Color_Code = 0xFFFFFFFF; //WHITE
                holder.StockPriceChangeTextView.setText(String.format(" %.2f", stock.getPriceChange()));
            }
            else {
                Color_Code = 0xFF80FF00; //GREEN
                holder.StockPriceChangeTextView.setText(String.format("▲ %.2f", stock.getPriceChange()));
            }
        }

        holder.StockSymbolTextView.setTextColor(Color_Code);
        holder.StockCompanyTextView.setTextColor(Color_Code);
        holder.StockPriceTextView.setTextColor(Color_Code);
        holder.StockPriceChangeTextView.setTextColor(Color_Code);
        holder.StockPercentageChangeTextView.setTextColor(Color_Code);
    }


    @Override
    public int getItemCount() {
        return StocksList.size();
    }
}
