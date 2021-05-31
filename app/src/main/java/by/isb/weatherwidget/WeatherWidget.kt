package by.isb.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import by.isb.weatherwidget.data.entities.forecast.Forecast
import by.isb.weatherwidget.data.repository.forecast.ForecastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WeatherWidgetConfigureActivity]
 */
class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        //   loadForecast()
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deletePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

private val forecastRepository = ForecastRepository()
private val ioScope = CoroutineScope(Dispatchers.IO)

var lat = 0.0
var lon = 0.0
var units = "metric"

var forecasts = listOf<Forecast>()

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    lat = loadPref(context, appWidgetId, "lat")?.toDoubleOrNull() ?: 0.0
    lon = loadPref(context, appWidgetId, "lon")?.toDoubleOrNull() ?: 0.0
    units = loadPref(context, appWidgetId, "units") ?: "metric"
    // Construct the RemoteViews object

    ioScope.launch {
        loadForecast(context)
    }

    val views = RemoteViews(context.packageName, R.layout.weather_widget)

    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))

    val intent = Intent(context, WeatherWidget::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }

    val updateIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    views.setOnClickPendingIntent(R.id.item_refresh, updateIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

suspend fun loadForecast(context : Context) {
    withContext(ioScope.coroutineContext) {
        forecasts = forecastRepository.loadForecast(
            lat,
            lon,
            units
        )
    }

    if (forecasts.isNotEmpty()) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        val dateMillis = (forecasts.get(0)?.date?.times(1000))?.toLong()
        var dateTime = SimpleDateFormat("dd.mm").format(dateMillis).toString()
        views.setTextViewText(R.id.day_1_number, dateTime)
    }
}