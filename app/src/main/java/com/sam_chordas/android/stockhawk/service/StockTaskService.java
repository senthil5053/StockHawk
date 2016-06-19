package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.network.VolleyManager;
import com.sam_chordas.android.stockhawk.network.VolleyResponseListener;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String TAG = StockTaskService.class.getSimpleName();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;
    private static final String SYNC_CLICKED = "com.sam_chordas.android.stockhawk.SYNC_CLICKED";

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    /*String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }*/

    private void fetchData(final String url) {
        Log.d(TAG, "fetchData() called with: " + "url = [" + url + "]");
        VolleyManager.makeJsonObjectRequest(url, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Log.d(TAG, "onError() called with: " + "message = [" + message + "]");
                Toast.makeText(mContext, mContext.getString(R.string.network_toast), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "fetch data response:"+response.toString());
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);
                    }
                    ArrayList arrayList = Utils.quoteJsonToContentVals(response.toString());
                    Log.d(TAG, "onResponse: arrayList: " + arrayList);
                    if (arrayList == null) {
                        Handler h = new Handler(mContext.getMainLooper());

                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, mContext.getString(R.string.error_no_symbol), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                Utils.quoteJsonToContentVals(response.toString()));
                        syncWidget();
                    }
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(TAG, "Error applying batch insert", e);
                }
            }
        });
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (params.getTag().equals(mContext.getString(R.string.string_init)) || params.getTag().equals(mContext.getString(R.string.string_periodic))) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (initQueryCursor != null) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"").append(initQueryCursor.getString(initQueryCursor.getColumnIndex(mContext.getString(R.string.string_symbol)))).append("\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (params.getTag().equals(mContext.getString(R.string.string_add))) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString(mContext.getString(R.string.string_symbol));
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            String urlString = urlStringBuilder.toString();
            fetchData(urlString);
        }

        return result;
    }

    private void syncWidget() {
        Log.d(TAG, "Sending broadcast to widget");
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(SYNC_CLICKED)
                .setPackage(mContext.getPackageName());
        mContext.sendBroadcast(dataUpdatedIntent);
    }

}