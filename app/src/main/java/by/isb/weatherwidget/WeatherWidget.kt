package by.isb.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import by.isb.weatherwidget.data.entities.forecast.Forecast
import by.isb.weatherwidget.data.repository.forecast.ForecastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

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
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
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

var lat = 53.893009
var lon = 53.893009
var units = "metric"

var forecasts = listOf<Forecast>()

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
//    lat = loadTitlePref(context, appWidgetId,"lat").toDouble()
//    lon = loadTitlePref(context, appWidgetId,"lon").toDouble()
//    units = loadTitlePref(context, appWidgetId,"units")
    // Construct the RemoteViews object

    loadForecast()
    
    val views = RemoteViews(context.packageName, R.layout.weather_widget)

    val dateMillis = (forecasts.get(0).date * 1000).toLong()

    var dateTime = SimpleDateFormat("dd.mm").format(dateMillis).toString()

    views.setTextViewText(R.id.day_1_number, dateTime)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun loadForecast() {
    ioScope.launch {
        forecasts = forecastRepository.loadForecast(
            lat,
            lon,
            units
        )
    }
}