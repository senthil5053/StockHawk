package com.sam_chordas.android.stockhawk.network;

import org.json.JSONObject;

/**
 * Created by Senthil on 1/20/16.
 */
public interface VolleyResponseListener {
    void onError(String message);

    void onResponse(JSONObject response);
}
