package com.sam_chordas.android.stockhawk.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Senthil on 1/20/16.
 * <p/>
 * All the network related methods are included in here.
 */
public class VolleyManager {

    /**
     * Internal instance variable.
     */
    private static VolleyManager sInstance;

    /**
     * The request queue.
     */
    private RequestQueue mRequestQueue;

    /**
     * Volley image loader
     */
    private ImageLoader mImageLoader;

    /**
     * Image cache implementation
     */
    private ImageLoader.ImageCache mImageCache;

    private VolleyManager() {
        // no instances
    }

    /**
     * This is the initializer.
     *
     * @param context Your application context.
     */
    //public static void init(Context context, int cacheSize) {
    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new VolleyManager();
            sInstance.mRequestQueue = Volley.newRequestQueue(context);
            //sInstance.mImageCache = new BitmapLruImageCache(cacheSize);
            sInstance.mImageLoader = new ImageLoader(VolleyManager.getRequestQueue(), sInstance.mImageCache);
        }
    }

    /**
     * Gets the image loader from the singleton.
     *
     * @return The RequestQueue.
     * @throws java.lang.IllegalStateException This is thrown if init has not been called.
     */
    public static RequestQueue getRequestQueue() {
        if (sInstance == null) {
            throw new IllegalStateException("The VolleyManager must be initialized.");
        }
        return sInstance.mRequestQueue;
    }

    /**
     * Gets the image loader from the singleton.
     *
     * @return The ImageLoader.
     * @throws java.lang.IllegalStateException This is thrown if init has not been called.
     */
    public static ImageLoader getImageLoader() {
        if (sInstance == null) {
            throw new IllegalStateException("The VolleyManager must be initialized.");
        }
        return sInstance.mImageLoader;
    }


    /**
     * Network request
     *
     * @param url      - url of the request
     * @param listener - listener
     */
    public static void makeJsonObjectRequest(String url, final VolleyResponseListener listener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.toString());
                    }
                });
        VolleyManager.getRequestQueue().add(jsonObjectRequest);
    }
}