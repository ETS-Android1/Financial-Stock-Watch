package com.example.stockwatch;

import java.io.Serializable;
import java.util.Objects;


public class Stock implements Comparable<Stock>{

    private String Symbol;
    private String Company;
    private double Price;
    private double PriceChange;
    private double PercentageChange;

    public Stock(String Symbol, String Company, Double Price, Double PriceChange, Double PercentageChange){
        this.Symbol = Symbol;
        this.Company = Company;
        this.Price = Price;
        this.PriceChange = PriceChange;
        this.PercentageChange = PercentageChange;
    }

    public String getSymbol(){
        return this.Symbol;
    }

    public String getCompany(){
        return this.Company;
    }

    public double getPrice(){
        return this.Price;
    }

    public double getPriceChange(){
        return this.PriceChange;
    }

    public double getPercentageChange(){
        return this.PercentageChange;
    }

    public void setPrice(double d){
        this.Price = d;
    }

    public void setPriceChange(double d){
        this.PriceChange = d;
    }

    public void setPercentageChange(double d){
        this.PercentageChange = d;
    }

    //IMPORTANT Need to Override equals() & hasCode() in order for detecting .contains --> duplicates in the method "addStock()" to work
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return Symbol.equals(stock.Symbol) &&
                Company.equals(stock.Company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Symbol, Company);
    }

    @Override
    public int compareTo(Stock stock) {
        return this.Symbol.compareTo(stock.Symbol);
    }
}
