package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.content.Context;

import com.sam_chordas.android.stockhawk.network.VolleyManager;

/**
 * Created by Senthil on 25/03/16.
 */
public class StockHawkApplication extends Application {

    public static Context applicationContext;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        //Initializing VolleyManager
        VolleyManager.init(getApplicationContext());
        //Stetho.initializeWithDefaults(this);
    }
}
