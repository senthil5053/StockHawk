package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    private static final String SYNC_CLICKED = "com.sam_chordas.android.stockhawk.SYNC_CLICKED";
    private static final String TAG = StockWidgetProvider.class.getSimpleName();

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_provider);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        final Intent refreshIntent = new Intent(context, StockWidgetProvider.class);
        refreshIntent.setAction(StockWidgetProvider.SYNC_CLICKED);
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0,
                refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent);

        views.setEmptyView(R.id.widget_list, R.id.empty_view);


        Intent appIntent = new Intent(context, MyStocksActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_text, appPendingIntent);

        // Set up collection items
        Intent clickIntentTemplate = new Intent(context, MyStocksActivity.class);
        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);

        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, StockWidgetRemoteViewsService.class));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Implements {@link BroadcastReceiver#onReceive} to dispatch calls to the various
     * other methods on AppWidgetProvider.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);


        if (intent.getAction().equals(SYNC_CLICKED)) {

            Log.v(TAG, "Received");
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final ComponentName componentName = new ComponentName(context, StockWidgetProvider.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.widget_list);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

