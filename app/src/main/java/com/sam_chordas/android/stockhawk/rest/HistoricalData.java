package com.sam_chordas.android.stockhawk.rest;

import io.realm.RealmObject;

public class HistoricalData extends RealmObject {

    private int id;
    private String stock;
    private String date;
    private float value;

    public HistoricalData() {

    }

    public HistoricalData(int id, String stock, String date, float value) {
        this.id = id;
        this.stock = stock;
        this.date = date;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getStock() {
        return stock;
    }

    public float getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }
}
