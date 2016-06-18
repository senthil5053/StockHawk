package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.realm.implementation.RealmLineData;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.network.VolleyManager;
import com.sam_chordas.android.stockhawk.network.VolleyResponseListener;
import com.sam_chordas.android.stockhawk.rest.HistoricalData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class StockDetailActivity extends AppCompatActivity {

    private static final String TAG = StockDetailActivity.class.getSimpleName();
    private Context mContext;
    private String currentDate;
    private String pastDate;
    private LineChart chart;
    private int lastId;
    private RealmLineData realmLineData;
    private Realm realm;
    private String symbol;
    private RealmChangeListener realmChangeListener;
    private RealmResults<HistoricalData> results;
    private RealmLineDataSet<HistoricalData> historicalDataRealmLineDataSet;
    private ProgressBar progressBar;
    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        chart = (LineChart) findViewById(R.id.chart);
        mContext = this;
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(mContext).build();
        realm = Realm.getInstance(realmConfig);
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                setData();
            }
        };

        symbol = getIntent().getStringExtra(getString(R.string.string_symbol));
        getSupportActionBar().setTitle(symbol);

        try {
            fetchData(urlBuild(symbol));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setData();

    }


    private String urlBuild(String stockInput) {
        getDates();
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol =  "
                    , "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\" and startDate = \"" + pastDate + "\" and endDate = \"" + currentDate + "\"", "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlStringBuilder.toString();
    }

    private void fetchData(String url) throws IOException {

        VolleyManager.makeJsonObjectRequest(url, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Log.d(TAG, "onError() called with: " + "message = [" + message + "]");
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), getString(R.string.network_toast), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    fetchData(urlBuild(symbol));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                snackbar.show();
            }

            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = response.getJSONObject("query");
                    if (jsonObject.getInt("count") > 1) {
                        jsonObject = jsonObject.getJSONObject("results");
                        if (jsonObject != null) {
                            JSONArray resultsArray = jsonObject.getJSONArray("quote");
                            saveData(resultsArray);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveData(final JSONArray jsonArray) {

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = jsonArray.length() - 1; i >= 0; i--) {
                    JSONObject object = null;
                    try {
                        object = jsonArray.getJSONObject(i);
                        realm.copyToRealm(new HistoricalData(++lastId, symbol, object.getString("Date"), Float.parseFloat(object.getString("High"))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void setData() {
        //realm.beginTransaction();


        results = realm.where(HistoricalData.class).equalTo("stock", symbol).findAll();
        results.addChangeListener(realmChangeListener);

        historicalDataRealmLineDataSet = new RealmLineDataSet(results, "value", "id");
        // create a dataset and give it a type

        historicalDataRealmLineDataSet.setDrawFilled(true);
        historicalDataRealmLineDataSet.setFillAlpha(120);
        historicalDataRealmLineDataSet.setFillColor(Color.BLUE);
        historicalDataRealmLineDataSet.setCircleColor(Color.BLACK);
        historicalDataRealmLineDataSet.setDrawCircleHole(false);
        historicalDataRealmLineDataSet.setCircleSize(2f);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(historicalDataRealmLineDataSet); // add the datasets

        realmLineData = new RealmLineData(results, "date", dataSets);


        chart.setDrawGridBackground(false);
        chart.setDescription("");
        chart.setNoDataText("Loading chart...");
        chart.setDrawGridBackground(true);

        CustomMarkerView customMarkerView = new CustomMarkerView(mContext, R.layout.custom_marker_view);
        chart.setMarkerView(customMarkerView);

        //Toast to display the value
       /* chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                //display msg when value selected
                if (entry == null)
                    return;
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(mContext, "" + entry.getVal(), Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onNothingSelected() {

            }
        });*/

        XAxis xAxis = chart.getXAxis();
        xAxis.enableGridDashedLine(8f, 5f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis yAxis = chart.getAxisRight();
        yAxis.setDrawLabels(false);
        yAxis.enableGridDashedLine(8f, 5f, 0f);


        chart.setAutoScaleMinMaxEnabled(true);
        chart.getLegend().setEnabled(false);
        chart.setData(realmLineData);
        chart.animateXY(3000, 1000, Easing.EasingOption.Linear, Easing.EasingOption.Linear);
    }

    private void getDates() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        currentDate = dateFormat.format(cal.getTime());
        RealmResults<HistoricalData> historicalData = realm.where(HistoricalData.class).equalTo("stock", symbol).findAll();
        if (historicalData.size() > 0) {
            pastDate = historicalData.last().getDate();
            lastId = historicalData.last().getId();
        } else {
            lastId = -1;
            cal.add(Calendar.YEAR, -1);
            pastDate = dateFormat.format(cal.getTime());
        }
    }


}
