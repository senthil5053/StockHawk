package com.sam_chordas.android.stockhawk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.stetho.Stetho;

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
        Log.d("Application", "onCreate: ");
        applicationContext = getApplicationContext();
        Stetho.initializeWithDefaults(this);
        Log.d("StockHawkApplication", "StockHawkApplication: oncreate: "+applicationContext);
    }
}
